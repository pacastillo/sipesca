
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

/**
 *
 * @author mgarenas
 */
public class CuentaPasosPorDia {

    String fileName;
    File f;
    FileWriter fw;
    BufferedWriter bw;
    String[] intervalos;
    boolean agrupar;

    public CuentaPasosPorDia(String lFileName, String[] fechas, boolean agrupados) throws IOException {
        fileName = lFileName;
        agrupar = agrupados;
        createFile();
        openFile();
        writeHead();
        intervalos = fechas;
    }

    public CuentaPasosPorDia() throws IOException {
        fileName = "CuentaPasosPorDiaPorNodo.txt";
        createFile();
        openFile();
        writeHead();
    }

    private void createFile() {
        fileName = System.getProperty("user.dir") + System.getProperty("file.separator") + fileName;
        f = new File(fileName);
    }

    public void writeHead() throws IOException {
        bw.write("Fecha\tNodo\tNombre\tPasos\n");
    }

    public void calcular(Conectar cn) throws SQLException, IOException {

        // Cuenta el número de dispositivos que han pasado por cada nodo, osea cuántas veces un dispositivo ha pasado por un nodo determinado.
        Statement st2;
        ResultSet rs2;
        int numero = 0;
        double media = 0, desviacion = 0, suma = 0;
        String anio, mes, dia, nodo, linea = "", s;//, datosTotales="";
        Calendar cal = Calendar.getInstance();



        for (int i = 0; i < intervalos.length; i++) {
            anio = intervalos[i].substring(6, 10);
            mes = intervalos[i].substring(3, 5);
            dia = intervalos[i].substring(0, 2);
            System.err.println("i:" + i);
            st2 = cn.crearSt();
            if (agrupar) {
                s = "select paso.idNodo as nodo, nodo.nombre as nombre, count(idDispositivo) as numero, YEAR(FROM_UNIXTIME(tInicio/1000)) as year, "
                        + "MONTH(FROM_UNIXTIME(tInicio/1000)) as mes, DAYOFMONTH(FROM_UNIXTIME(tInicio/1000)) as dia"
                        + " from paso, nodo where paso.idNodo=nodo.idNodo and YEAR(FROM_UNIXTIME(tInicio/1000))='"
                        + anio + "' and MONTH(FROM_UNIXTIME(tInicio/1000))='" + mes
                        + "' and DAYOFMONTH(FROM_UNIXTIME(tInicio/1000))='" + dia + "' group by nodo.idNodo;";
                System.err.println(s);
                rs2 = st2.executeQuery(s);
                while (rs2.next()) {
                    linea = intervalos[i] + "\t" + rs2.getString("nodo") + "\t " + rs2.getString("nombre") + "\t" + rs2.getInt("numero") + "\n";
                    bw.write(linea);
                }
            } else {
                s = "select paso.idNodo as nodo, nodo.nombre as nombre, count(idDispositivo) as numero, YEAR(FROM_UNIXTIME(tInicio/1000)) as year, "
                        + "MONTH(FROM_UNIXTIME(tInicio/1000)) as mes, DAYOFMONTH(FROM_UNIXTIME(tInicio/1000)) as dia"
                        + " from paso, nodo where paso.idNodo=nodo.idNodo and YEAR(FROM_UNIXTIME(tInicio/1000))='"
                        + anio + "' and MONTH(FROM_UNIXTIME(tInicio/1000))='" + mes
                        + "' and DAYOFMONTH(FROM_UNIXTIME(tInicio/1000))='" + dia + "';";

                System.err.println(s);
                rs2 = st2.executeQuery(s);
                while (rs2.next()) {
                    linea = intervalos[i] + "\t" +  rs2.getInt("numero") + "\n";
                    bw.write(linea);
                }
            }
            rs2.close();
        }
        //bw.write("\n"+ datosTotales);
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
