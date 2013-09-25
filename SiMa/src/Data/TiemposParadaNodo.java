
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package Data;

//~--- JDK imports ------------------------------------------------------------

import Entorno.Conectar.Conectar;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

/**
 *
 * @author mgarenas
 */
public class TiemposParadaNodo {
    String         fileName;
    File           f;
    FileWriter     fw;
    BufferedWriter bw;

    public TiemposParadaNodo(String lFileName) throws IOException {
        fileName = lFileName;
        createFile();
        openFile();
        writeHead();
    }

    public TiemposParadaNodo() throws IOException {
        fileName = "TiemposParadaNodo.txt";
        createFile();
        openFile();
        writeHead();
    }
    private void createFile() {
        fileName = System.getProperty("user.dir") + System.getProperty("file.separator") + fileName;
        f        = new File(fileName);
    }

    public void writeHead() throws IOException {
        bw.write("Nodo\tTiempo(ms)\tNombreNodo\n");
    }

    public void calcular(Conectar cn) throws SQLException, IOException {

        // leer de la tabla nodos, los nodos e ir buscando en la tabla paso los pasos de cada dispositivo e ir sumando los tiempos.
        Statement st1 = cn.crearSt();
        ResultSet rs = st1.executeQuery("select nodo.idNodo, sum(tfin-tinicio), nodo.nombre from paso, nodo where nodo.idNodo=paso.idNodo group by nodo.idNodo;");
        String s;
        while (rs.next()) {
                s= rs.getObject(1)+"\t"+rs.getObject(2)+"\t"+rs.getObject(3)+"\n";
                bw.write(s);  
                System.err.print(s);
                //sumaTiempo=sumaTiempo+Long.parseLong(rs.getObject(2).toString())-Long.parseLong(rs.getObject(1).toString());
            
        }

        rs.close();
        bw.close();
        
    }

    public void writeLine(String line) {}

    void openFile() {
        try {
            fw = new FileWriter(f, true);
            bw = new BufferedWriter(fw);
        } catch (java.io.IOException e) {
            System.err.println("Error al abrir el fichero de Tiempos de Parada Por Nodo del fichero");
        }
    }

    void closeFile() {
        try {
            bw.close();
        } catch (java.io.IOException e) {
            System.err.println("Error al cerrar el fichero de Tiempos de Parada Por Nodo del fichero");
        }
    }

    /*public static void escribirResultados(Param param, boolean cabecera) {
        try {
            String sFicheroRecopilacion = "";

            if (Param.getAlgoritmo() == Param.SA) {
                sFicheroRecopilacion = System.getProperty("user.dir") + System.getProperty("file.separator")
                                       + Param.getFuncion() + param.getTipoEnfriamiento() + "IT"
                                       + ((int) param.getInitTemperature()) + "NC" + param.getNCool() + "NI"
                                       + param.getNIterations() + "PS" + param.getPopulationSize() + ".out";
            } else {
                sFicheroRecopilacion = System.getProperty("user.dir") + System.getProperty("file.separator")
                                       + Param.getFuncion() + "Gen" + param.getNGeneraciones() + "PS"
                                       + param.getPopulationSize() + "oP" + param.getCombinacionOperadores() + "Sel"
                                       + param.getSelectorType() + ".out";
            }

            System.err.println("Fichero " + sFicheroRecopilacion);

            File f = new File(sFicheroRecopilacion);

            if (f.exists() && cabecera) {    // si ya existe voy a ver si ya tienen los 30 resultados, sino, pues que siga por la ejecucion que toque.

                // cabecera=false;
                FileReader fR     = new FileReader(f);
                long       lineas = contarLineas(fR, sFicheroRecopilacion);

                if (lineas >= Param.EJECUCIONES) {
                    Launch.pasar = true;
                }

                if (lineas < Param.EJECUCIONES) {
                    Launch.pasar     = false;
                    Launch.ejecucion = (int) lineas;
                }

                fR.close();
            }

            FileWriter     fw = new FileWriter(sFicheroRecopilacion, true);
            BufferedWriter bw = new BufferedWriter(fw);

            if (cabecera) {
                System.err.println(" El fichero " + sFicheroRecopilacion);
                bw.write(param.Cabecera());
            } else {
                bw.write(param.toString());
            }

            bw.close();
        } catch (java.io.IOException e) {
            System.err.println("Error en la lectura del fichero");
        }
    
    }*/
}


//~ Formatted by Jindent --- http://www.jindent.com
