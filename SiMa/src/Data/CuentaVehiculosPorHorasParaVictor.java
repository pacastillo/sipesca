
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
import java.util.Calendar;
import java.util.Locale;

/**
 *
 * @author mgarenas
 */
public class CuentaVehiculosPorHorasParaVictor {

    String fileName;
    File f;
    FileWriter fw;
    BufferedWriter bw;
    String fechas[];

    public CuentaVehiculosPorHorasParaVictor(String[] fechas, String lFileName) throws IOException {
        fileName = lFileName;
        createFile();
        openFile();
        writeHead();
        this.fechas = fechas;
    }

    public CuentaVehiculosPorHorasParaVictor() throws IOException {
        fileName = "CuentaVehiculosPorHoras.txt";
        createFile();
        openFile();
        writeHead();
    }

    private void createFile() {
        fileName = System.getProperty("user.dir") + System.getProperty("file.separator") + fileName;
        f = new File(fileName);
    }

    public void writeHead() throws IOException {
        bw.write("Nodo\tnombre\tfecha\tRangoHorario\tDispositivos\n");
    }

    public void calcular(Conectar cn, int[] inter) throws SQLException, IOException {

        // Cuenta el número de dispositivos que han pasado por cada nodo, osea cuántas veces un dispositivo ha pasado por un nodo determinado.
        Statement st2;
        ResultSet rs2 = null;
        int[] intervalos= inter;

        
        int numero = 0;
        String nodo, linea = "", s;
        Statement st = cn.crearSt();
        ResultSet rs = st.executeQuery("select idNodo, nombre from nodo order by nombre;");
        while (rs.next()) {
            nodo = rs.getObject(1).toString() + "";
            for (int j = 0; j < fechas.length; j++) {
                String anio = fechas[j].substring(6, 10);
                String mes = fechas[j].substring(3, 5);
                String dia = fechas[j].substring(0, 2);
                System.err.println("j:" + j);

                for (int i = 0; i < intervalos.length-1; i++) {
                    System.err.println("i:" + i);
                    st2 = cn.crearSt();
                    java.util.Calendar calendar = Calendar.getInstance();

                    java.util.Calendar calendar2 = Calendar.getInstance();
                    calendar.set(Integer.valueOf(anio), Integer.valueOf(mes)-1, Integer.valueOf(dia), intervalos[i],00);
                    calendar2.set(Integer.valueOf(anio), Integer.valueOf(mes)-1, Integer.valueOf(dia), intervalos[i], 59);
                         s = "select paso.IdNodo as nodo, nodo.nombre as nombre, count(idDispositivo) as numero, "
                            + " from_unixtime(tInicio/1000,'%Y-%m-%d %H:%i:%S') as fecha "
                            + " from paso, nodo where paso.IdNodo=nodo.IdNodo and "
                            + " paso.tInicio > " + (calendar.getTimeInMillis())
                            + " and paso.tInicio <=" + (calendar2.getTimeInMillis())
                            + " and paso.IdNodo ='" + nodo + "';";

                    System.err.println(s);
                    st2 = cn.crearSt();
                    rs2 = st2.executeQuery(s);
                    numero = 0;
                    linea = "";
                    String fecha="                   ";
                    
                    while (rs2.next()) {
                        numero=rs2.getInt("numero");
                        fecha = rs2.getString(4);
                        }
                        //linea = nodo + "\t" + rs.getObject(2) + "\t " + fechas[j] + "\t" + intervalos[i] + ":00-" + intervalos[i + 1] + ":00" + "\t" + numero + "\n";
                        linea = nodo + "\t" + rs.getObject(2) + "\t " + fechas[j] + "\t" + intervalos[i] + ":00-" + intervalos[i+1] + ":00\t" + String.valueOf(numero) + "\n";
                    
                    bw.write(linea);
                    System.err.print(linea);
                    rs2.close();
                }   
            }

        }
        rs.close();
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
