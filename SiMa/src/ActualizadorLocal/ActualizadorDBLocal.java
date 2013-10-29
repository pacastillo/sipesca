/* 
 * Copyright (C) 2013 Antonio Fernández Ares (antares.es@gmail.com)
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
package ActualizadorLocal;

import ActualizadorLocal.Clientes.ClienteNodos;
import ActualizadorLocal.Clientes.ClienteDispositivos;
import ActualizadorLocal.Clientes.ClientePasos;
import Entorno.Conectar.Conectar;
import Entorno.Configuracion.Config;
import Entorno.Depuracion.Debug;
import java.util.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase encargada de la actualización de los datos de la base de datos Local
 * con los datos del servidor de Córdoba
 *
 * @author mgarenas, Antonio Fernández Ares (antares.es@gmail.com)
 */
public class ActualizadorDBLocal extends Thread {

    //Variables de Fechas
    static Calendar calendarStart;
    static Calendar calendarEnd;
    static Calendar limite;
    static Calendar origen;
    static java.util.Date startDate;
    static java.util.Date endDate;
    static java.util.Date ultimaActualizacion;
    /**
     * Variables de configuración de la actualización
     */
    static int NUMERO_DIAS_PETICION = 2;
    static int NUMERO_MINUTOS_PETICION = 60 * 24;
    static double FACTOR_TIEMPO_ESPERA = 1.25;
    long TIEMPO_ACTUALIZACIONES_MS = 1000 * 60 * 30;
    //Variable de conexión (para procesador síncrono)
    static Conectar conexion;
    //Variables para medida de tiempo
    static long t_start, time;
    //Clientes actualizadores de nodos, dispositivos y pasos
    static Object response;
    static ClienteNodos clientNo;
    static ClientePasos clientPa;
    //Definimos "el grupo de hebras" para poder gestionar la concurrencia.
    static ThreadGroup tg;
    static int MAX_HEBRAS_EN_COLA_PARA_DESCARGAR = 10;
    static long TIME_BASE_ESPERA = 1000;
    static long INCREMENTO_TIME_POR_HEBRA = 750;
    static int contadorRepeticion = 0;
    static boolean sigo = true;
    //Variables de depuración
    static Debug _d = new Debug();
    
    //Variables de configuración
    static Config _c = new Config();
    
    //static String label;
    //Variable de gestión de tiempo y actualización

    /**
     * Función de concurrencia: Comprueba el número de hebras hijas que tiene el
     * proceso PADRE, y en caso de ser un número aceptable, incrementa el
     * contador.
     *
     * @param i Parámetro que del contador actual
     * @return i si el número de hebras vivas está por encima de lo aceptable,
     * i++ si el número de hebras vivas no supera el máximo permitido.
     */
    private static int checkChildrens(int i) {
        if (tg.activeCount() > MAX_HEBRAS_EN_COLA_PARA_DESCARGAR) {
            sigo = false;
        } else {
            i++;
            sigo = true;
        }
        return i;
    }

    /**
     * Función de concurrencia: Duerme el proceso padre un cierto tiempo,
     * mientras espera que se procesen los hijos.
     *
     * @throws InterruptedException Si no se puede dormir el proceso padre
     */
    private static void waitForIt() throws InterruptedException {
        long t = TIME_BASE_ESPERA * (contadorRepeticion + 1) + (tg.activeCount() * INCREMENTO_TIME_POR_HEBRA);
        contadorRepeticion++;
        System.out.println("-- Demasiadas peticiones a la DB pendientes (" + tg.activeCount() + "). Esperando " + _d.df.format(t / 1000.00) + "s --");
        Thread.sleep(t);
    }

    /**
     * Actualiza los nodos del sistema.
     */
    public static void actualizarNodos() {
        try {
            _d.primeOUT("Nodos", "Actualizando");
            clientNo = new ClienteNodos(conexion);
            response = clientNo.get_Nodos(String.class);
            clientNo.procesarDatos(response.toString());
            _d.primeOUT("Nodos", "Número de nodos: " + clientNo.getHowManyNodos());
        } catch (SQLException ex) {
            _d.primeERR("Error durmiendo hebra principal");
        }
    }

    /**
     * Actualiza los dispositivos vinculados al nodo i.
     *
     * @param i número de nodo según el array clientNo
     * @throws SQLException
     */
    public static void actualizarDispositivos(int i) throws SQLException {
        String label = "[" + startDate.toString() + "][" + endDate.toString() + "][" + clientNo.getNodo(i) + "][Dispositivos]";
        try {
            contadorRepeticion = 0;
            _d.primeOUT(label, "Descargando");
            _d.timeCheck();
            ClienteDispositivos clientDi;
            clientDi = new ClienteDispositivos(String.valueOf(startDate.getTime()), String.valueOf(endDate.getTime()));
            clientDi.setLabel(label);
            clientDi.createWebResource(clientNo.getNodo(i));
            response = clientDi.get_Dispositivos(String.class);
            _d.primeOUT(label, "Descarga OK" + _d.timeDisplay(true));
            _d.primeOUT(label, "Procesando ");
            clientDi.setConexion(conexion);
            if (response != null) {
                clientDi.procesarDatos(response.toString());
            }
            _d.primeOUT(label, "Procesado OK " + clientDi.getProcesados() + _d.timeDisplay(true));
            clientDi.close();
        } catch (com.sun.jersey.api.client.UniformInterfaceException e) {
            if (e.getResponse().toString().endsWith("returned a response status of 204 No Content")) {
                _d.primeERR(label, "Error 204 descargando. Se omite.");
                _d.primeVerbose(label, clientPa.getQuery());
                if ((clientPa) != null) {
                    clientPa.close();
                }
            }
        } catch (Exception e) {
            _d.primeERR(label, "Algo ha ido mal: " + e.getMessage());
        }
    }

    /**
     * Actualiza los pasos vinculados con el nodo i.
     *
     * @param i número de nodo según el array clientNo
     * @throws SQLException
     */
    public static void actualizarPasos(int i) throws SQLException {
        String label = "[" + startDate.toString() + "][" + endDate.toString() + "][" + clientNo.getNodo(i) + "][Pasos]";

        //Actualizamos los pasos
        try {
            _d.primeOUT(label, "Descargando");
            _d.timeCheck();
            clientPa = new ClientePasos(String.valueOf(endDate.getTime()), String.valueOf(startDate.getTime()));
            clientPa.setLabel(label);
            clientPa.createWebResource(clientNo.getNodo(i));
            response = clientPa.get_Pasos(String.class);
            _d.primeOUT(label, "Descarga OK" + _d.timeDisplay(true));
            _d.primeOUT(label, "Procesando ");
            clientPa.procesarDatos(response.toString());
            clientPa.close();
            _d.primeOUT(label, "Procesado OK " + clientPa.getProcesados() + _d.timeDisplay(true));


        } catch (com.sun.jersey.api.client.UniformInterfaceException e) {
            if (e.getResponse().toString().endsWith("returned a response status of 204 No Content")) {
                _d.primeERR(label, "Error 204 descargando. Se omite.");
                System.err.println(clientPa.getQuery());
                if ((clientPa) != null) {
                    clientPa.close();
                }
            }
        }
    }

    /**
     * Constructor de la clase, indicando la fecha de la última actualización.
     * Constructor que indica la fecha límite de la última actualización.
     * @param fecha Fecha en formato texto en el formato YYYY-MM-DD HH:mm:SS Por ejemplo: "2013-05-01 00:00:01"
     */
    public ActualizadorDBLocal(String fecha) {
            //ultimaActualizacion = _d.sdf.parse(fecha);
          ultimaActualizacion = new Date();
          ultimaActualizacion.setTime(Long.parseLong(fecha));
          System.out.println(ultimaActualizacion.toString());  
    }

    /**
     * Constructor por defecto de la clase.
     * Establece la fecha de la última actualización a la fecha actual.
     */
    public ActualizadorDBLocal() {
        ultimaActualizacion = Calendar.getInstance().getTime();

    }

    /**
     * Método principal de la clase.
     * Ejecuta la actualización de los Nodos, Dispositivos y Pasos en tiempo real.
     */
    @Override
    public void run() {
        try {
            //Aquí introducimos el código básico
            tg = Thread.currentThread().getThreadGroup();

            //Actualizamos los nodos:
            conexion = new Conectar();
            actualizarNodos();

            //Actualizamos desde la última fecha
            actualizaDesdeFecha();

            //Y una vez actualizado, entramos en el modo automático
            do {
              
              _c.set("data.ultimo", Long.toString(ultimaActualizacion.getTime()));
              
              
                if (tg.activeCount() > 2) {
                    waitForIt();
                } else if ((System.currentTimeMillis() - ultimaActualizacion.getTime()) < TIEMPO_ACTUALIZACIONES_MS) {
                    long tiempo_espera = (long) ((System.currentTimeMillis() - ultimaActualizacion.getTime()) * FACTOR_TIEMPO_ESPERA);
                    _d.primeOUT("Última actualización hace " + _d.df.format((float) (System.currentTimeMillis() - ultimaActualizacion.getTime()) / 1000 / 60) + " minutos");
                    _d.primeOUT("Me tengo que actualizar cada " + _d.df.format((float) TIEMPO_ACTUALIZACIONES_MS / 1000 / 60) + " minutos");
                    _d.primeOUT("Podría actualizar cada " + _d.df.format((float) tiempo_espera / 1000 / 60) + " minutos");

                    _d.primeOUT("Me voy a dormir " + _d.df.format((float) (tiempo_espera - (System.currentTimeMillis() - ultimaActualizacion.getTime())) / 1000 / 60) + " minutos");

                    Thread.sleep(tiempo_espera - (System.currentTimeMillis() - ultimaActualizacion.getTime()));

                    //TIEMPO_ACTUALIZACIONES_MS = tiempo_espera;
                    //NUMERO_MINUTOS_PETICION = (int) Math.round(tiempo_espera / 1000 / 60);


                    _d.primeOUT("TIEMPO_ACTUALIZACIONES_MS: " + tiempo_espera + " ms");
                    _d.primeOUT("NUMERO_MINUTOS_PETICION: " + NUMERO_MINUTOS_PETICION + " minutos");
                    actualizarNodos();
                    actualizaDesdeFecha();
                } else if ((System.currentTimeMillis() - ultimaActualizacion.getTime()) > TIEMPO_ACTUALIZACIONES_MS) {
                    long tiempo_espera = (long) (-(System.currentTimeMillis() - ultimaActualizacion.getTime()) * -FACTOR_TIEMPO_ESPERA);
                    _d.primeOUT("Última actualización hace " + _d.df.format((float) (System.currentTimeMillis() - ultimaActualizacion.getTime()) / 1000 / 60) + " minutos");
                    _d.primeOUT("Me tengo que actualizar cada " + _d.df.format((float) TIEMPO_ACTUALIZACIONES_MS / 1000 / 60) + " minutos");
                    _d.primeOUT("Me actualizaré ahora cada " + _d.df.format((float) tiempo_espera / 1000 / 60) + " minutos");

                    //TIEMPO_ACTUALIZACIONES_MS = tiempo_espera;
                    //NUMERO_MINUTOS_PETICION = (int) Math.round(tiempo_espera / 1000 / 60);

                    _d.primeOUT("TIEMPO_ACTUALIZACIONES_MS: " + tiempo_espera + " ms");
                    _d.primeOUT("NUMERO_MINUTOS_PETICION: " + NUMERO_MINUTOS_PETICION + " minutos");
                    actualizarNodos();
                    actualizaDesdeFecha();

                }
                
                _c.set("data.ultimo", Long.toString(ultimaActualizacion.getTime()));
                
            } while (true);

        } catch (ParseException ex) {
            Logger.getLogger(ActualizadorDBLocal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ActualizadorDBLocal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ActualizadorDBLocal.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    /**
     * Método que actualiza desde la última fecha de actualización
     * @return 0 - En un futuro se utilizará esté parámetro para información de la gestión del flujo de trabajo
     * @throws ParseException
     * @throws InterruptedException
     * @throws SQLException 
     * 
     */
    public int actualizaDesdeFecha() throws ParseException, InterruptedException, SQLException {

        Date ahora = Calendar.getInstance().getTime();

        origen = Calendar.getInstance();
        origen.setTime(ultimaActualizacion);

        limite = Calendar.getInstance();
        limite.setTime(ahora);
        limite.add(Calendar.MINUTE, NUMERO_MINUTOS_PETICION);

        synchronized (ActualizadorDBLocal.class) {
            calendarStart = origen;
            startDate = calendarStart.getTime();

            calendarEnd = origen;
            calendarEnd.add(Calendar.MINUTE, NUMERO_MINUTOS_PETICION);
            endDate = calendarEnd.getTime();
        }

        do {
            //System.err.println("[DISPOSITIVOS] De "+ startDate.toString() + " a " + endDate.toString());
            for (int i = 0; i < clientNo.getHowManyNodos(); i = checkChildrens(i)) {
                System.gc();
                if (!sigo) {
                    waitForIt();
                } else {
                   actualizarDispositivos(i);
                }
            }
            synchronized (ActualizadorDBLocal.class) {
                //calendarStart.setTime(calendarEnd.getTime());
                //startDate = calendarStart.getTime();

                startDate = endDate;
                calendarEnd.add(Calendar.MINUTE, NUMERO_MINUTOS_PETICION);
                endDate = calendarEnd.getTime();
            }

        } while (calendarEnd.before(limite));

        synchronized (ActualizadorDBLocal.class) {
            origen = Calendar.getInstance();
            origen.setTime(ultimaActualizacion);

            limite = Calendar.getInstance();
            limite.setTime(ahora);
            limite.add(Calendar.MINUTE, NUMERO_MINUTOS_PETICION);
            calendarStart = origen;
            startDate = calendarStart.getTime();

            calendarEnd = origen;
            calendarEnd.add(Calendar.MINUTE, NUMERO_MINUTOS_PETICION);
            endDate = calendarEnd.getTime();
        }


        do {
            //System.err.println("[PASOS] De "+ startDate.toString() + " a " + endDate.toString());

            for (int i = 0; i < clientNo.getHowManyNodos(); i = checkChildrens(i)) {
                System.gc();
                if (!sigo) {
                    waitForIt();
                } else {
                    actualizarPasos(i);

                }
            }

            synchronized (ActualizadorDBLocal.class) {
                //calendarStart.setTime(calendarEnd.getTime());
                //startDate = calendarStart.getTime();
                startDate = endDate;

                calendarEnd.add(Calendar.MINUTE, NUMERO_MINUTOS_PETICION);
                endDate = calendarEnd.getTime();
            }


        } while (calendarEnd.before(limite));

        //Actualizamos la fechad de la última actualización
        ultimaActualizacion = ahora;

        return 0;
    }
}
