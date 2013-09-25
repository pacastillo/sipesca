/*
 * SiMa : Sipesca Manager
 */
package SiMa;

import Entorno.Conectar.Conectar;
import SincronizarFusionTables.conectarFusionTables;
import Entorno.Configuracion.Config;
import java.io.IOException;
import ActualizadorLocal.ActualizadorDBLocal;

/**
 * Una clase para gobernarlas a todas, una clase para encontrarlas, una clase para atraerlas a todas y atarlas en las tinieblas
 * @author Antonio Fernández Ares (antares.es@gmail.com)
 */
public class SiMa {
    
    /**
     * Cargamos la configuración
     */
    static Config _c = new Config();
    
    /** 
     * Manejador de la actualización en Córdoba
     */
    static ActualizadorLocal.ActualizadorDBLocal _actualizarDB;


    /**
     * Método principal de la clase
     * @param args Los argumentos de ejecución (Actualmente no utilizados)
     */
    public static void main(String[] args) throws IOException {
        
        //System.out.println(_c.getInt("db.MAX_CACHE_SIZE"));
        System.out.println(_c.getBool("debug"));
        
        _actualizarDB = new ActualizadorDBLocal("01-09-2013 00:00:00");
        _actualizarDB.run();

        //ejemplo p = new ejemplo();
        //p.run();
        //System.out.println(_c.get("debug"));
        conectarFusionTables t = new conectarFusionTables();
        System.out.println(_c.getBool("debug"));
             
             //_r = new Rjava();
             //_r.run();
        
        Conectar c = new Conectar();
        _c.set("data.ultimo", Long.toString(System.currentTimeMillis()));

        
    }
}
