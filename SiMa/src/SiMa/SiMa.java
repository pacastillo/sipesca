/*
 * SiMa : Sipesca Manager
 */
package SiMa;

import SincronizarFusionTables.conectarFusionTables;
import Entorno.Configuracion.Config;
import SincronizarFusionTables.PasosPorDia;
import com.google.api.services.fusiontables.Fusiontables;
import java.io.IOException;
import com.google.api.services.fusiontables.model.Sqlresponse;
import java.util.List;

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
        
        //_actualizarDB = new ActualizadorDBLocal("01-09-2013 00:00:00");
        //_actualizarDB.run();
        
        //conectarFusionTables _cF = new conectarFusionTables();
        
        //_cF.listaTablas();
        //Sqlresponse res =_cF.sql("SELECT ROWID FROM "+ _c.get("ft.PASOSPORDIA.ID") + " WHERE idNodo = \"01\"");
        //Sqlresponse res2 =_cF.sql("INSERT INTO "+ _c.get("ft.PASOSPORDIA.ID") + "(idNodo, Total) VALUES (\"01,100\")");
        //Sqlresponse res3 =_cF.update(_c.get("ft.PASOSPORDIA.ID"), "Total", "500", (String) res.getRows().get(0).get(0)  );
        
        //Sqlresponse res4 = _cF.delete( _c.get("ft.PASOSPORDIA.ID"), res.getRows());
        
        
        //System.out.println(res.toString());
        //res.getRows().get(0).get(0).
        
        PasosPorDia p = new PasosPorDia("2013-08-07 05:00:00");
        p.calcular();
        
        
        //conectarFusionTables t = new conectarFusionTables();
        //System.out.println(_c.getBool("debug"));

        
        _c.set("data.ultimo", Long.toString(System.currentTimeMillis()));

        
    }
}
