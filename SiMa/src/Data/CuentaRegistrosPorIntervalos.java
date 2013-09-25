
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
public class CuentaRegistrosPorIntervalos {

    String fileName;
    File f;
    FileWriter fw;
    BufferedWriter bw;

    public CuentaRegistrosPorIntervalos(String lFileName) throws IOException {
        fileName = lFileName;
        createFile();
        openFile();
        writeHead();
    }

    public CuentaRegistrosPorIntervalos() throws IOException {
        fileName = "CuentaRegistrosPorIntervalos.txt";
        createFile();
        openFile();
        writeHead();
    }

    private void createFile() {
        fileName = System.getProperty("user.dir") + System.getProperty("file.separator") + fileName;
        f = new File(fileName);
    }

    public void writeHead() throws IOException {
        bw.write("Nodo\tNombre\tnDispostivos\tIntervalo\n");
    }

    public void calcular(Conectar cn) throws SQLException, IOException {

        // Cuenta el número de dispositivos que han pasado por cada nodo, osea cuántas veces un dispositivo ha pasado por un nodo determinado.
        Statement st2;
        ResultSet rs2;
        int[] intervalos = {0, 5, 10, 15, 20, 25, 10000};
        int numero=0;
        String nodo, linea="", s;
        for (int i = 0; i < 6; i++) {
            System.err.println("i:"+i);
            Statement st1 = cn.crearSt();
            ResultSet rs = st1.executeQuery("select idNodo, nombre from nodo;");
            while (rs.next()) {
                nodo =  rs.getObject(1).toString()+"";
                st2 = cn.crearSt();
                //s = "select count(*) from paso  where idNodo ='" + nodo + "' group by idDispositivo having count(idDispositivo) <= "+
                //        intervalos[i+1]+ " and count(idDispositivo)> "+ intervalos[i] + ";";
                //System.err.println(s);
                rs2 = st2.executeQuery("select count(*) from paso where idNodo ='" + nodo + "' group by idDispositivo having count(idDispositivo) <= "+
                        intervalos[i+1]+ " and count(idDispositivo)> "+ intervalos[i] + ";");
                numero=0;
                while(rs2.next()){
                    numero++;
                }
                linea = nodo + "\t" + rs.getObject(2) + "\t" + numero +  "\t >"+intervalos[i]+"&&<"+intervalos[i+1]+"\n";
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
