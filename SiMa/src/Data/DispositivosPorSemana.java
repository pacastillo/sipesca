
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package Data;

//~--- JDK imports ------------------------------------------------------------

import Entorno.Conectar.Conectar;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.sql.*;
import java.util.Date;
import java.text.ParseException;

/**
 *
 * @author mgarenas
 */
public final class DispositivosPorSemana {
    String         fileName;
    File           f;
    FileWriter     fw;
    BufferedWriter bw;

    public DispositivosPorSemana(String lFileName) throws IOException {
        fileName = lFileName;
        createFile();
        openFile();
        writeHead();
    }
    public DispositivosPorSemana() throws IOException {
        fileName = "DispositivosPorDiaDeLaSemana.txt";
        createFile();
        openFile();
        writeHead();
    }

    private void createFile() {
        fileName = System.getProperty("user.dir") + System.getProperty("file.separator") + fileName;
        f        = new File(fileName);
    }

    public void writeHead() throws IOException {
        bw.write("Nodo\tNombre\tDispositivos\tAño\tMes\tDia\thora\tDia\tFechaCompleta\n");
    }


    public void calcular(Conectar cn, String fechaInicio, String fechaFin) throws SQLException, IOException, ParseException {

        // Cuenta el número de dispositivos que han pasado por cada nodo, osea cuántas veces un dispositivo ha pasado por un nodo determinado.
        Statement st1 = cn.crearSt();
        String  query="select paso.idNodo, nodo.nombre, count(idDispositivo), YEAR(FROM_UNIXTIME(tInicio/1000)) as year, "
                + "MONTH(FROM_UNIXTIME(tInicio/1000)) as month, DAYOFMONTH(FROM_UNIXTIME(tInicio/1000)) as day, "
                + "HOUR(FROM_UNIXTIME(tInicio/1000)) as hour, DAYNAME(FROM_UNIXTIME(tInicio/1000)) as nameDay, FROM_UNIXTIME(tInicio/1000) as fecha from paso, nodo where paso.idNodo=nodo.idNodo "
                + "and FROM_UNIXTIME(tInicio/1000) < '"+fechaFin+"' and FROM_UNIXTIME(tInicio/1000) > '"+fechaInicio+"' group by nodo.IdNodo, year, month, day, hour;";
        ResultSet rs = st1.executeQuery(query);
                
        String s;
        //DateFormat format = new SimpleDateFormat("MMddyyHHmmss");
        while (rs.next()) {
                s= rs.getObject(1)+"\t"+rs.getObject(2)+"\t"+rs.getObject(3)+"\t"+rs.getObject(4)+"\t"+rs.getObject(5)+"\t"+rs.getObject(6)+"\t"+rs.getObject(7)+"\t"+rs.getObject(8)+"\t"+rs.getObject(9)+"\n";
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
