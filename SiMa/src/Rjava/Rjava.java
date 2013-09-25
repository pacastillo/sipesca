/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Rjava;

import Entorno.Conectar.Conectar;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Antonio Fernández Ares (antares.es@gmail.com)
 */
public class Rjava extends Thread {

    String localPath = "/var/www/Rjava/";
    Conectar _c;

    /**
     * Este método calcula la predición de la serie temporal para el nodo
     * indicado
     *
     * @param idNodo Identificador del nodo que se va a predecir
     */
    public void calculaNodo(String idNodo) {

        File f = new File("/tmp/datosR.csv");
        f.delete();

        //Llamada a Mysql para que calcule según el intervalo de tiempo las próximas predicciones
        try {
            Statement st = _c.crearSt();
            //st.execute("CALL agrupaPasosPorIntervalosNodotoR('2013-08-01','2013-08-31'," + 60 * 24 + ",'" + idNodo + "')");

            //ResultSet rs = st.executeQuery("CALL agrupaPasosPorIntervalosNodo('2013-08-01','2013-08-31'," + 60 * 24 + ",'" + idNodo + "')");
            ResultSet rs = st.executeQuery("CALL agrupaPasosPorIntervalosNodo('2013-01-07 00:00:00','2013-06-02 00:00:00'," + 60 + ",'" + idNodo + "')");

            FileWriter fstream = new FileWriter("/tmp/datosR.csv");
            BufferedWriter out = new BufferedWriter(fstream);

            while (rs.next()) {
                //out.write('"' + rs.getString(1) + '"' + "," +  rs.getString("total") + "\n");
                out.write(rs.getString("total") + "\n");
            }

            out.close();
            _c.cerrar();
        } catch (SQLException ex) {
            Logger.getLogger(Rjava.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Rjava.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Rjava.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Ejecutamos el R
        BufferedReader br3 = null;

        String rScriptFileName = localPath + "script.r";
        String line;


        try {
            Process _p = Runtime.getRuntime().exec("/usr/bin/Rscript " + rScriptFileName);
            System.out.println(_p.getOutputStream().toString());

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(_p.getInputStream()));
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();


            _p.waitFor();

        } catch (IOException ex) {
            Logger.getLogger(Rjava.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Rjava.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Step Four: Import data from R and put it into myDataArray's empty last column

    }

    public Rjava() {
        this._c = new Conectar();
    }

    @Override
    public void run() {
        //Qué tiene que hacer este método
        //calculaNodo("1351591695721");
        calculaNodo("1351591800440");

    }
}
