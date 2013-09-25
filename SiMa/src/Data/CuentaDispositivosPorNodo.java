
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
public class CuentaDispositivosPorNodo {
    String         fileName;
    File           f;
    FileWriter     fw;
    BufferedWriter bw;

    public CuentaDispositivosPorNodo(String lFileName) throws IOException {
        fileName = lFileName;
        createFile();
        openFile();
        writeHead();
    }
    public CuentaDispositivosPorNodo() throws IOException {
        fileName = "CuentaDispositivosPorNodo.txt";
        createFile();
        openFile();
        writeHead();
    }

    private void createFile() {
        fileName = System.getProperty("user.dir") + System.getProperty("file.separator") + fileName;
        f        = new File(fileName);
    }

    public void writeHead() throws IOException {
        bw.write("Nodo\tNombre\tnDispostivos\n");
    }

    public void calcular(Conectar cn) throws SQLException, IOException {

        // Cuenta el número de dispositivos que han pasado por cada nodo, osea cuántas veces un dispositivo ha pasado por un nodo determinado.
        
        Statement st1 = cn.crearSt();
        ResultSet rs = 
                st1.executeQuery("select paso.idNodo, nodo.nombre, count(idDispositivo) from paso, nodo where paso.idNodo=nodo.idNodo group by paso.idNodo;");
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

}


//~ Formatted by Jindent --- http://www.jindent.com
