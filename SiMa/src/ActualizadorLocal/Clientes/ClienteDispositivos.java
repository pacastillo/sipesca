/* 
 * Copyright (C) 2013 antares
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ActualizadorLocal.Clientes;

import Entorno.Conectar.Conectar;
import Entorno.Configuracion.Config;
import Entorno.Depuracion.Debug;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.sql.SQLException;
import java.sql.Statement;
import javax.net.ssl.*;
import javax.ws.rs.core.MultivaluedMap;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author mgarenas, Antonio Fernández Ares (antares.es@gmail.com)
 *
 */
public class ClienteDispositivos {

    /**
     * Variable de depuración.
     */
    private Debug _d = new Debug();
    /**
     * Variable de configuración.
     */
    private static Config _c = new Config();
    /**
     * Variable de conexión HTTP.
     */
    private WebResource webResource;
    /**
     * Variable de conexión HTTP.
     */
    private Client client;
    /**
     * Variable de conexión MYSQL Local.
     */
    Conectar conexion;
    /**
     * Nombres de los parámetros GET de la petición HTTP.
     */
    String[] queryParamNames;
    /**
     * Valores de los parámetros GET de la petición HTTP.
     */
    String[] queryParamValues;
    /**
     * Variable de auditoría - Número de elementos procesados.
     */
    int procesados = 0;
    /**
     * Variable de auditoría - Número de elementos insertados.
     */
    int insertados = 0;
    /**
     * Variable de gestión peticiones en caché a la BD - Caché.
     */
    String cache = "";
    /**
     * Variable de gestión peticiones en caché a la BD - Tamaño actual.
     */
    int cache_size = 0;
    /**
     * Variable de gestión peticiones en caché a la BD - Tamáño máximo.
     */
    int MAX_CACHE_SIZE = _c.getInt("db.DISPOSITIVO.MAX_CACHE_SIZE 5000");
    /**
     * Variable de gestión multihebrado - Listado de hebras hijas.
     */
    List<threadSyncDB> l_th = new ArrayList<>();
    /**
     * Variable de gestión multiebrado - Tiempo de espera en caso de error en la
     * hebra.
     */
    public long TIME_SLEEP_IN_ERROR = _c.getInt("db.DISPOSITIVO.TIME_SLEEP_IN_ERROR");
    /**
     * Variable de gestión multiebrado - Número de herbas activas simultáneas.
     */
    public int MAX_HEBRAS_ACTIVAS_SIMULTANEAS = _c.getInt("db.DISPOSITIVO.MAX_HEBRAS_ACTIVAS_SIMULTANEAS");
    /**
     * Variable de gestión multihebrado - Número de intentos de conexión antes
     * de mostar error.
     */
    public int MAX_ERRORES_PARA_NOTIFICACION = _c.getInt("db.DISPOSITIVO.MAX_ERRORES_PARA_NOTIFICACION");
    /**
     * Etiqueta identificadora del proceso.
     */
    public String label;

    /**
     * Constructor por defecto.
     *
     * @param start Fecha de comienzo de los datos
     * @param end Fecha de fin de los datos
     */
    public ClienteDispositivos(String start, String end) {
        queryParamNames = new String[]{"user", "pass", "start", "end", "inc"};
        queryParamValues = new String[]{_c.get("sc.USER"), _c.get("sc.PASS"), start, end, "true"};
        com.sun.jersey.api.client.config.ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig(); // SSL configuration
        // SSL configuration
        config.getProperties().put(com.sun.jersey.client.urlconnection.HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new com.sun.jersey.client.urlconnection.HTTPSProperties(getHostnameVerifier(), getSSLContext()));
        client = Client.create(config);

    }

    /**
     * Función que estabece la etiqueta asociada al proceso
     *
     * @param label Etiqueta asociada al proceso
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Función que devuelve el número de elementos que han sido procesados. Se
     * considera elemento procesado a aquellos elementos que han sido
     * descargados, independientemente de si han sido o no insertados en la base
     * de datos.
     *
     * @return El número de elementos que han sido procesados en la petición
     */
    public int getProcesados() {
        return procesados;
    }

    /**
     * Función que devuelve el número de elementos que han sido correctamente
     * insertados en la Base de Datos local. Se considera elemento insertado a
     * aquellos elementos que han sido descargados e insertados correctamente en
     * la base de datos local, es decir, aquellos que elementos que no se
     * encontraban anteriormente en la base de datos
     *
     * @return El número de elementos que han sido correctamente insertados en
     * la petición.
     */
    public int getInsertados() {
        return insertados;
    }

    /**
     * Función que realiza la petición de los disposistivo.
     *
     * @param <T>
     * @param responseType
     * @return La respuesta de la petición en el caso de que haya sido correcta.
     * Null en el caso de que se haya producido algún error.
     */
    public <T> T get_Dispositivos(Class<T> responseType) {

        try {
            return webResource.get(responseType);
        } catch (UniformInterfaceException e) {

            if (e.getResponse().toString().endsWith("returned a response status of 204 No Content")) {
                _d.primeERR(label, "Error 204 descargando. Se omite.");
            }

            return null;
        }
    }

    /**
     * Función que establece los parámetros para la conexión HTTP
     *
     * @param node Identificador del nodo para el que se va a realiza la
     * petición HTTP
     */
    public void createWebResource(String node) {
        webResource = client.resource("https://cityanalytics.net/restapi/rawdataservice/"
                + node + "/dispositivos?user=" + queryParamValues[0]
                + "&pass=" + queryParamValues[1]
                + "&start=" + queryParamValues[2]
                + "&end=" + queryParamValues[3]
                + "&inc=true");

        insertados = 0;
    }

    /**
     * Función que procesa mediante JSON el resultado de la petición HTTP.
     *
     * @param datos La respuesta de la petición HTTP
     */
    public void procesarDatos(String datos) {
        //Preprocesamos los datos, para darle un nombre al array, cosa que está muy mal que no venga hecha:
        datos = "{\"dispositivos\":" + datos + "}";

        JSONParser parser = new JSONParser();

        try {
            JSONObject obj = (JSONObject) parser.parse(datos);
            JSONArray lista = (JSONArray) obj.get("dispositivos");
            procesados = lista.size();

            int conta = 1;
            int lotes = procesados / MAX_CACHE_SIZE;

            for (int i = 0; i < lista.size(); i++) {

                if (i % MAX_CACHE_SIZE == 0) {
                    conta++;
                }

                String a0 = (String) ((JSONObject) lista.get(i)).get("idDispositivo");
                String a1 = (String) ((JSONObject) lista.get(i)).get("majorDeviceClass");
                String a2 = (String) ((JSONObject) lista.get(i)).get("minorDeviceClass");
                String a3 = (String) ((JSONObject) lista.get(i)).get("serviceClass");
                String a4 = (String) ((JSONObject) lista.get(i)).get("fabricante");

                this.InsertarDatos("\"" + (String) a0 + "\",\"" + a1 + "\",\"" + a2 + "\",\"" + a3 + "\",\"" + a4 + "\"");

            }
        } catch (Exception e) {
            System.err.println("E>" + e.getMessage());
        }

        syncDB();

    }

    /**
     * Función que procesa mediante SPLIT el resultado de la petición HTTP.
     *
     * @param datos
     * @throws SQLException
     * @deprecated
     * @see ClienteDispositivos.procesarDatos(String datos)
     */
    public void procesarDatosSlit(String datos) throws SQLException {

        String datosAInsertar = "";
        datos = "," + datos;
        String[] result = datos.toString().split("}");
        int conta = 0;
        for (int x = 0; x < result.length - 1; x++) {
            conta++;
            String[] result2 = result[x].split(",");
            for (int y = 1; y < result2.length; y++) {
                String[] result3 = result2[y].split(":");
                //for (int w=1; w<result3.length; w++){
                datosAInsertar += result3[1] + ", ";
                //System.err.println("x,y"+x+", "+y+" "+result3[1] + "|\t|" + datosAInsertar);
                //}
            }

            //this.InsertarDatosSync(datosAInsertar.substring(0, datosAInsertar.lastIndexOf(",")));
            datosAInsertar = "";
        }
        System.out.println("Dispositivos procesados: " + conta);

    }

    /**
     * Función que establece la conexión con la base de datos local
     *
     * @param connect Variable de conexión con la base de datos local
     * @deprecated
     */
    public void setConexion(Conectar connect) {
        this.conexion = connect;
    }

    /**
     * Función que borra la tabla en base de datos Local
     * @throws SQLException 
     * @deprecated 
     */
    public void borrarDatosTablaDispositivo() throws SQLException {
        //Statement st = conexion.crearSt();
        //st.execute("Alter table dispositivo disable keys;");
        //st = conexion.crearSt();
        //st.executeUpdate("Delete from dispositivo;");
    }

    public void InsertarDatos(String datos) {
        cache = cache + (cache_size != 0 ? "," : "") + " (" + datos + " ) ";
        cache_size++;

        if (cache_size >= MAX_CACHE_SIZE) {
            syncDB();
        }
    }

    /**
     * Clase encargada de realizar las peticiones SQL a la Base de Datos local de forma paralela.
     */
    public class threadSyncDB extends Thread {

        /**
         * Consulta(s) a realizar en la Base de Datos.
         */
        private String query;
        
        /**
         * Identificador de la hebra.
         */
        private int id;
        
        /**
         * Variable de conexión con la Base de Datos Local. 
         */
        private Conectar c;
        
        /**
         * Variable de control - Indica el número de intentos de procesamiento de la petición.
         */
        int intentos = 0;
        
        /**
         * Variable de control - Indica si la petición ha sido procesada ya en la Base de Datos Local.
         */
        boolean procesada = false;
        
        /**
         * Variable de auditoría - Indica el número de elementos que han sido insertados en la Base de Datos Local.
         */
        int insertados = 0;

        /**
         * Constructor por defecto de la clase.
         * @param cache Caché de peticiones a realizar en la Base de Datos Local
         * @param i  Identificador de la hebra.
         */
        public threadSyncDB(String cache, int i) {
            query = cache;
            id = i;
        }

        /**
         * Método de ejecución de la hebra.
         * Procesa las peticiones almacenadas en la caché en la Base de Datos Local.
         */
        @Override
        public void run() {
            do {
                try {
                    this.c = new Conectar();
                    Statement st = c.crearSt();
                    insertados = st.executeUpdate("INSERT IGNORE INTO dispositivo (idDispositivo, majordeviceclass, minordeviceclass, serviceclass, fabricante) VALUES" + query + ";");
                    procesada = true;
                    c.cerrar();
                } catch (SQLException ex) {
                    procesada = false;
                    intentos++;
                    if (intentos > MAX_ERRORES_PARA_NOTIFICACION) {
                        _d.primeERR(label, "Error hebra " + this.getId() + " sincronización con DB Error " + ex.getErrorCode() + " Se intentará nuevamente (" + intentos + ")");
                    }
                    try {
                        sleep(TIME_SLEEP_IN_ERROR);
                    } catch (InterruptedException ex1) {
                        System.err.println("E>Error durmiendo hebra " + this.getId());
                    }
                } catch (NullPointerException e) {
                    procesada = false;
                    intentos++;
                    if (intentos > 0) {
                        _d.primeERR(label, "Error hebra " + this.getId() + " no se ha podido conectar a la DB. Se intentará nuevamente (" + intentos + ")");
                    }
                    try {
                        sleep(TIME_SLEEP_IN_ERROR);
                    } catch (InterruptedException ex1) {
                        System.err.println("E>Error durmiendo hebra " + this.getId());
                    }
                } catch (Exception ex) {
                    procesada = false;
                    intentos++;
                    if (intentos > 0) {
                        _d.primeERR(label, "Error hebra " + this.getId() + " no se ha podido conectar a la DB. Se intentará nuevamente (" + intentos + ")");
                    }
                    try {
                        sleep(TIME_SLEEP_IN_ERROR);
                    } catch (InterruptedException ex1) {
                        System.err.println("E>Error durmiendo hebra " + this.getId());
                    }

                }
            } while (!procesada);
        }

        /**
         * Finaliza la hebra.
         * @throws Throwable 
         */
        @Override
        protected void finalize() throws Throwable {
            conexion.cerrar();
            super.finalize();
        }
    }

    /**
     * Función que sincroniza con la Base de Datos.
     */
    public void syncDB() {
        try {
            l_th.add(new threadSyncDB(cache, l_th.size()));
            l_th.get(l_th.size() - 1).start();

        } catch (Exception ex) {
            System.err.println("E>" + ex.getMessage());

        }
        cache_size = 0;
        cache = "";
    }

    /**
     * Función que sincroniza de forma síncrona con la Base de Datos Local.
     * @param datos Petición a ejecutar en la Base de Datos Local.
     * @throws SQLException Si existe algún error al procesar la petición en la Base de Datos Local.
     * @deprecated 
     */
    public void InsertarDatosSync(String datos) throws SQLException {
        try {
            String[] comas = datos.split(",");
            if (comas.length < 5) {
                datos = datos + ", \"null\" ";
            }

            Statement st = conexion.crearSt();

            st.executeUpdate("INSERT INTO dispositivo (idDispositivo, majordeviceclass, minordeviceclass, serviceclass, fabricante) VALUES (" + datos + " )");

            if ((++insertados) % 100 == 0) {
                System.err.print("\n+");
            } else {
                System.err.print("+");
            }
            /*
             idDispositivo: Cadena tras el hash de la MAC Bluetooth de un dispositivo.
             majordeviceclass: Cadena con la característica obtenida de la MAC Bluetooth acerca de la procedencia y capacidades del dispositivo.
             minordeviceclass: Cadena con la característica obtenida de la MAC Bluetooth acerca de la procedencia y capacidades del dispositivo.
             serviceclass: Cadena con la característica obtenida de la MAC Bluetooth acerca de la procedencia y capacidades del dispositivo.
             fabricante: Cadena con el nombre del fabricante del dispositivo Bluetooth detectado.
             */
        } catch (com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException e) {
            return;
        }

    }

    /**
     * Clase encargada de la gestión de las hebras de ejecución paralela.
     * Se encarga de gestionar que todas las hebras han sido completadas, es decir, que se han procesado todas las peticiones
     * pendiente en la Base de Datos Local.
     */
    public class threadCierre extends Thread {

        /**
         * Lista de hebras de peticiones a la base de datos local.
         */
        private List<threadSyncDB> l = null;

        /**
         * Constructor principal.
         * @param _l Listado de hebras de peticiones a la Base de Datos Local
         */
        public threadCierre(List<ClienteDispositivos.threadSyncDB> _l) {
            this.l = _l;
        }

        /**
         * Metodo principal de la hebra.
         * Espera a la ejecución y procesado de todas las peticiones pendientes a la Base de Datos Local.
         */
        @Override
        public void run() {
            int insertados = 0;
            _d.primeOUT(label, "Escritura en BD: " + l.size() + " peticiones");
            while (!l.isEmpty()) {
                try {
                    l.get(0).join();
                    insertados = insertados + l.get(0).insertados;
                    l.remove(0);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClienteDispositivos.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            _d.primeOUT(label, "Escritura en DB OK.");
            _d.primeOUT(label, "Dispositivos insertados " + insertados + " de " + procesados + " procesados (" + (procesados - insertados) + ")");


        }

        /**
         * Función que se encarga de matar a la hebra.
         * @throws Throwable 
         */
        @Override
        protected void finalize() throws Throwable {
            conexion.cerrar();
            l.clear();
            super.finalize();
        }
    }

    /**
     * Función que indica que el cliente de sincronicación tiene que ser cerrado.
     * Gracias a la clase threadCierre se espera que se procesen todas las peticiones pendientes
     * en la Base de Datos Local
     */
    public void close() {
        threadCierre t = new threadCierre(l_th);
        t.start();
        client.destroy();
    }

    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                return true;
            }
        };
    }

    private MultivaluedMap getQueryOrFormParams(String[] paramNames, String[] paramValues) {
        MultivaluedMap<String, String> qParams = new com.sun.jersey.api.representation.Form();
        for (int i = 0; i < paramNames.length; i++) {
            if (paramValues[i] != null) {
                qParams.add(paramNames[i], paramValues[i]);
            }
        }
        //System.err.println(qParams.toString());
        return qParams;
    }

    private SSLContext getSSLContext() {
        javax.net.ssl.TrustManager x509;
        x509 = new javax.net.ssl.X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("SSL");
            ctx.init(null, new javax.net.ssl.TrustManager[]{x509}, null);
        } catch (java.security.GeneralSecurityException ex) {
        }
        return ctx;
    }
}
