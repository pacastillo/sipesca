
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
public class CuentaNodosPorDondePasan {

    String fileName;
    File f;
    FileWriter fw;
    BufferedWriter bw;

    public CuentaNodosPorDondePasan(String lFileName) throws IOException {
        fileName = lFileName;
        createFile();
        openFile();
        writeHead();
    }

    public CuentaNodosPorDondePasan() throws IOException {
        fileName = "CuentaNodosPorDondePasan.txt";
        createFile();
        openFile();
        writeHead();
    }

    private void createFile() {
        fileName = System.getProperty("user.dir") + System.getProperty("file.separator") + fileName;
        f = new File(fileName);
    }

    public void writeHead() throws IOException {
        bw.write("NúmeroNodos\tNúmeroDispositivos\tNúmeroTotalDePasos\tNumeroMedioDePasos\tDesviaciónEstandarDePasos\n");
    }

    public void calcular(Conectar cn) throws SQLException, IOException {

        // Cuenta el número de dispositivos que han pasado por cada nodo, osea cuántas veces un dispositivo ha pasado por un nodo determinado.
        Statement st2;
        ResultSet rs2;
        int[] intervalos = {1, 2, 3, 4, 5, 6};
        int numero=0;
        double media = 0, desviacion=0, suma=0;
        String nodo, linea="", s;//, datosTotales="";
        for (int i = 0; i < intervalos.length-1; i++) {
            System.err.println("i:"+i);
                st2 = cn.crearSt();
                s = "select count(distinct(idNodo)) as NNodos, idDispositivo as disp, count(idDispositivo) as numero from paso group by idDispositivo having count(distinct(idNodo))>="+
                        intervalos[i] + " and count(distinct(idNodo))<"+ intervalos[i+1]+ ";";
                rs2 = st2.executeQuery(s);
                System.err.println(s);
                numero=0;
                media =0;
                desviacion=0;
                suma=0;
                rs2.first();
                do{
                    numero++;
                    suma+=rs2.getInt("numero");
                    media+=rs2.getInt("numero");
                    //datosTotales += "\n"+rs2.getInt("numero");
                }while(rs2.next());
                media = media/numero;
                rs2.first();
                do{
                    
                    desviacion+=Math.pow((rs2.getInt("numero")-media),2);
                }while(rs2.next());
                desviacion= Math.sqrt(desviacion/(numero-1));
                
                linea = intervalos[i] + "\t"  + numero +  "\t "+suma+"\t"+media+"\t"+desviacion+"\n";
                bw.write(linea);
                System.err.print(linea);
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
