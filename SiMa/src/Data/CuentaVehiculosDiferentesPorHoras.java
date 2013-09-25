
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
public class CuentaVehiculosDiferentesPorHoras {

    String fileName;
    File f;
    FileWriter fw;
    BufferedWriter bw;

    public CuentaVehiculosDiferentesPorHoras(String lFileName) throws IOException {
        fileName = lFileName;
        createFile();
        openFile();
        writeHead();
    }

    public CuentaVehiculosDiferentesPorHoras() throws IOException {
        fileName = "CuentaVehiculosDiferentesPorHoras.txt";
        createFile();
        openFile();
        writeHead();
    }

    private void createFile() {
        fileName = System.getProperty("user.dir") + System.getProperty("file.separator") + fileName;
        f = new File(fileName);
    }

    public void writeHead() throws IOException {
        bw.write("Nodo\tnombre\tDispositivosDiferentes\tRangoHorario\n");
    }

    public void calcular(Conectar cn) throws SQLException, IOException {

        // Cuenta el número de dispositivos que han pasado por cada nodo, osea cuántas veces un dispositivo ha pasado por un nodo determinado.
        Statement st2;
        ResultSet rs2;
        int[] intervalos = {0, 7, 10, 13, 16, 20, 24};
        int numero=0;
        String nodo, linea="", s;
        for (int i = 0; i < 6; i++) {
            System.err.println("i:"+i);
            Statement st1 = cn.crearSt();
            ResultSet rs = st1.executeQuery("select idNodo, nombre from nodo;");
            while (rs.next()) {
                nodo =  rs.getObject(1).toString()+"";
                st2 = cn.crearSt();
                s = "select paso.idNodo, nodo.nombre, count(distinct(idDispositivo)) as numero, HOUR(FROM_UNIXTIME(tInicio/1000))"+
                        " as hour from paso, nodo where paso.idNodo=nodo.idNodo "+
                        " and nodo.idNodo='"+nodo+"' group by nodo.IdNodo, hour  having "+
                        " hour>"+intervalos[i]+" and hour<="+intervalos[i+1]+";";
                System.err.println(s);
                rs2 = st2.executeQuery(s);
                numero=0;
                while(rs2.next()){
                    numero+=rs2.getInt("numero");;
                    
                }
                linea = nodo + "\t" + rs.getObject(2) + "\t" + numero +  "\t "+intervalos[i]+":00-"+intervalos[i+1]+":00\n";
                bw.write(linea);
                System.err.print(linea);
            }
            rs.close();
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
