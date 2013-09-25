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

import Entorno.Depuracion.Debug;
import Entorno.Configuracion.Config;

import Entorno.Conectar.Conectar;
import org.json.simple.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.sql.SQLException;
import java.sql.Statement;
import javax.net.ssl.*;
import javax.ws.rs.core.MultivaluedMap;
import org.json.simple.parser.JSONParser;

/**
 * Clase encargada de la gestión de la sincronización de los nodos y esas cosas
 * @author mgarenas, Antonio Fernández Ares (antares.es@gmail.com)
 */
public class ClienteNodos {
    
    /**
     * Variable de configuración del entorno
     */
    private static Config _c = new Config();
    /**
     * Variable de depuración
     */
    private static Debug _d = new Debug();

    
    java.util.Vector nodos = new java.util.Vector();
    private WebResource webResource;
    private Client client;
    private static String BASE_URI = _c.get("sc.NODO.BASE_URI");
    Conectar conexion;
    
    //Variables de cache de base de datos
    String cache ="";
    int cache_size = 0;
    int MAX_CACHE_SIZE= _c.getInt("db.NODO.MAX_CACHE_SIZE");

    public ClienteNodos(Conectar con) {
        this.conexion = con;
        com.sun.jersey.api.client.config.ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig(); // SSL configuration
        // SSL configuration
        config.getProperties().put(com.sun.jersey.client.urlconnection.HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new com.sun.jersey.client.urlconnection.HTTPSProperties(getHostnameVerifier(), getSSLContext()));
        client = Client.create(config);
        webResource = client.resource(BASE_URI);//.path("manufacturers");
    }

    public ClienteNodos() {
        com.sun.jersey.api.client.config.ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig(); // SSL configuration
        // SSL configuration
        config.getProperties().put(com.sun.jersey.client.urlconnection.HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new com.sun.jersey.client.urlconnection.HTTPSProperties(getHostnameVerifier(), getSSLContext()));
        client = Client.create(config);
        webResource = client.resource(BASE_URI);//.path("manufacturers");
    }

    public <T> T get_XML(Class<T> responseType) throws UniformInterfaceException {
        return webResource.accept(javax.ws.rs.core.MediaType.APPLICATION_XML).get(responseType);
    }

    public <T> T get_JSON(Class<T> responseType) throws UniformInterfaceException {
        return webResource.accept(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(responseType);
    }

    public <T> T get_Nodos(Class<T> responseType) throws UniformInterfaceException {
        String[] queryParamNames = new String[]{"user", "pass"};
        String[] queryParamValues = new String[]{_c.get("sc.USER"), _c.get("sc.PASS")};
        
        return webResource.queryParams(getQueryOrFormParams(queryParamNames, queryParamValues)).get(responseType);
    }

    public String getNodo(int i) {
        return Long.toString((Long) nodos.get(i)) ;
    }

    public int getHowManyNodos() {
        return nodos.size();
    }

    public void procesarDatos(String datos) throws SQLException {
        //Preprocesamos los datos, para darle un nombre al array:
        
        datos = "{\"nodos\":" + datos + "}";
        
        JSONParser parser = new JSONParser();

        try {
            JSONObject obj = (JSONObject) parser.parse(datos);
            JSONArray lista = (JSONArray) obj.get("nodos");
            
            for(int i = 0; i<lista.size() ; i++){
                long a0 = (long) ((JSONObject) lista.get(i)).get("idNodo");
                String a1 = (String) ((JSONObject) lista.get(i)).get("nombre");
                double a2 = (double) ((JSONObject) lista.get(i)).get("latitud");
                double a3 = (double) ((JSONObject) lista.get(i)).get("longitud");
                
                nodos.add(a0);
                
                //Tenemos que calcular el polígono para la visualización de los datos mediante GOOGLE FUSION TABLES:
                
                double lat = Math.toRadians(new Double(a2));
                double lon = Math.toRadians(new Double(a3));
                
                double radio = 50.0/6378137.0;
                
                String cadena_poligono = "<Polygon><outerBoundaryIs><LinearRing><coordinates>";
                
                for(int j = 0 ; j <= 360 ; j=j+15 ){
                    double r = Math.toRadians(j);
                    double lat_rad = Math.asin(Math.sin(lat)*Math.cos(radio) + Math.cos(lat)*Math.sin(radio)*Math.cos(r));
                    double lon_rad = Math.atan2(Math.sin(r)*Math.sin(radio)*Math.cos(lat),
                            Math.cos(radio)-Math.sin(lat)*Math.sin(lat_rad));
                    double lon_rad_f = ((lon+lon_rad+Math.PI) % (2*Math.PI)) - Math.PI;
                    
                    cadena_poligono = cadena_poligono + Math.toDegrees(lon_rad_f)+ "," + Math.toDegrees(lat_rad)+",0.0 ";
                }
                  
                cadena_poligono = cadena_poligono + "</coordinates></LinearRing></outerBoundaryIs></Polygon>";
                
                this.InsertarDatos("\"" + (String) Long.toString(a0) + "\",\"" + a1+ "\",\""+ Double.toString(a2)+ "\",\""+ Double.toString(a3) +"\",\""+ cadena_poligono + "\"");
                
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        syncDB();

    }

    public void procesarDatosSplit(String datos) throws SQLException {
        String datosAInsertar = "";
        datos = "," + datos;
        String[] result = datos.toString().split("}");
        for (int x = 0; x < result.length - 1; x++) {
            String[] result2 = result[x].split(",");
            for (int y = 1; y < result2.length; y++) {
                String[] result3 = result2[y].split(":");
                //for (int w=1; w<result3.length; w++){
                if (y == 1) {
                    nodos.add(result3[1]);
                }
                datosAInsertar += result3[1] + ", ";

            }
            this.InsertarDatosSync(datosAInsertar.substring(0, datosAInsertar.lastIndexOf(",")));
            datosAInsertar = "";
        }

    }

    public void setConexion(Conectar connect) {
        this.conexion = connect;
    }

    public void borrarDatosTablaNodos() throws SQLException {
        Statement st = conexion.crearSt();
        st.execute("Alter table nodo disable keys;");
        st = conexion.crearSt();
        //st.executeUpdate("Delete from nodo;");
    }

    public void InsertarDatosSync(String datos) throws SQLException {
        try {
            Statement st = conexion.crearSt();
            st.executeUpdate("INSERT INTO nodo (idNodo, nombre, latitud, longitud,poligono) VALUES (" + datos + " )  ON DUPLICATE KEY UPDATE nombre=nombre, latitud=latitud, longitud=longitud, poligono=poligono");
        } catch (com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException e) {
            return;
        }

    }
       
    public void InsertarDatos(String datos) {
        cache = cache + (cache_size != 0 ? "," : "") + " (" + datos + " ) ";
        cache_size++;

        if (cache_size >= MAX_CACHE_SIZE) {
            syncDB();
        }
    }
    
        public void syncDB() {
        try {
            Statement st = conexion.crearSt();
            st.executeUpdate("INSERT INTO nodo (idNodo, nombre, latitud, longitud, poligono) VALUES" + cache + "ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), latitud=VALUES(latitud), longitud=VALUES(longitud), poligono=VALUES(poligono); ");
            
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());

        }
        cache_size = 0;
        cache = "";
    }

    public void close() {
        client.destroy();
    }

    public void setUsernamePassword(String username, String password) {
        client.addFilter(new com.sun.jersey.api.client.filter.HTTPBasicAuthFilter(username, password));
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
