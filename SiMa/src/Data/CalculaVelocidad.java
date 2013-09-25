
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Data;

//~--- JDK imports ------------------------------------------------------------
import Entorno.Conectar.Conectar;
import ModeloBD.Paso;
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
public class CalculaVelocidad {

    String fileName;
    File f;
    FileWriter fw;
    BufferedWriter bw;
    double espacio = 3700; //metros

    public CalculaVelocidad(String lFileName) throws IOException {
        fileName = lFileName;
        createFile();
        openFile();
        writeHead();
    }

    public CalculaVelocidad() throws IOException {
        fileName = "CalculaVelocidadMayor50.txt";
        createFile();
        openFile();
        writeHead();
    }

    private void createFile() {
        fileName = System.getProperty("user.dir") + System.getProperty("file.separator") + fileName;
        f = new File(fileName);
    }

    public void writeHead() throws IOException {
        bw.write("Dispostivos\tNodoInicio\tNodoFin\ttiempoInicio\ttiempoFin\tVelocidad(m/s)\tVelocidad(Km/h)\n");
    }

    public void calcular(Conectar cn) throws SQLException, IOException {

        // Cuenta el número de dispositivos que han pasado por cada nodo, osea cuántas veces un dispositivo ha pasado por un nodo determinado.
        Statement st2;
        ResultSet rs2;
        int numero = 0, index = 0, indice=0;
        double velocidad = 0, velociKh;
        Paso[] pasos;
        double[] intervalos= {50.0, 60.0, 70.0, 80.0, 90.0, 100.0, 120.0,140.0};
        int[] cuenta= {0,0,0,0,0,0,0,0};
        String nodo = "", dispositivo = "", inicio = "", fin = "", linea = "", s;
        java.util.Date first, second;
        Statement st1 = cn.crearSt();
        ResultSet rs = st1.executeQuery("select distinct(idDispositivo) as dispositivo from paso2;");

        while (rs.next()) {
            System.err.print("+");
            st2 = cn.crearSt();
            rs2 = st2.executeQuery("select idNodo as nodo, idDispositivo as disp, tInicio as inicio,  tFin as fin from paso2 "
                    + " where idDispositivo='" + rs.getString("dispositivo") + "' order by tInicio;");
            //System.err.println("Consulta:\n" + "select idNodo as nodo, idDispositivo as disp, tInicio as inicio,  tFin as fin from paso2 "
            //        + " where idDispositivo='" + rs.getString("dispositivo") + "' order by tInicio;");
            rs2.last();
            pasos = new Paso[rs2.getRow()];
            if (rs2.getRow() > 1) {
                pasos = new Paso[rs2.getRow()];
                index = 0;
                rs2.beforeFirst();
                while (rs2.next()) {
                    pasos[index] = new Paso(rs2.getString("nodo"), rs2.getString("disp"), rs2.getLong("inicio"), rs2.getLong("fin"));
                    index++;
                    //System.err.println(pasos[index - 1]);
                }
                rs2.close();
                for (int i = 0; i < pasos.length - 1; i++) {
                    //Ver si hay alguna opción de calcular velocidad 
                    if (!pasos[i].getIdNodo().equals(pasos[i + 1].getIdNodo())) { // si los nodos consecutivos son diferentes, miramos las fechas
                        first = pasos[i].getInicio();
                        second = pasos[i + 1].getInicio();
                        if (first.getMonth() == second.getMonth()) {
                            if (first.getDay() == second.getDay()) {
                                if (first.getHours() == second.getHours()) {
                                    velocidad = this.espacio / (Math.abs(first.getTime() - second.getTime()) / 1000);
                                    velociKh = (velocidad / 1000) * 3600;
                                    if (velociKh > 50 && velociKh<140) {
                                        linea = rs.getString("dispositivo") + "\t" + pasos[i].getIdNodo() + "\t" + pasos[i + 1].getIdNodo() + "\t"
                                                + first.getTime() + "\t" + second.getTime() + "\t" + velocidad + "\t" + (velocidad / 1000) * 3600;
                                        //System.err.println(linea);
                                        indice = (int)((velociKh-50)/10);
                                        if(indice> 7){
                                            indice=7;
                                        }
                                        cuenta[indice]++;
                                        bw.write(linea + "\n");
                                    }

                                }
                            }
                        }

                    }
                }
            }

        }
        rs.close();
        
        for(int i =0;i<intervalos.length ;i++){
            linea = intervalos[i]+ "\t"+cuenta[i];
            bw.write(linea+"\n");
        }
        bw.close();
    }

    public void writeLine(String line) {
    }

    void openFile() {
        try {
            fw = new FileWriter(f, true);
            bw = new BufferedWriter(fw);
        } catch (java.io.IOException e) {
            System.err.println("Error al abrir el fichero de Cuenta Registros por Intervalos");
        }
    }

    void closeFile() {
        try {
            bw.close();
        } catch (java.io.IOException e) {
            System.err.println("Error al cerrar el fichero de Cuenta Registros por Intervalos");
        }
    }
}
