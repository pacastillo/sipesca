/*
 * Clase para la conexión con la base de datos local
 */
package Entorno.Conectar;

import java.sql.*;
import Entorno.Configuracion.Config;


/**
 * Manejador de la conexión a MySQL con el servidor local
 * @author mgarenas, Antonio Fernández Ares (antares.es@gmail.com)
 */
public class Conectar {

    Config _c = new Config();
    
    Connection conn;

    public Conectar() {
        try {
            //Indicamos el driver y lo instanciamos
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //Creamos la conexion
            String host = _c.get("db.host");
            String base = _c.get("db.basedatos");
            String usuario = _c.get("db.usuario");
            String pass = _c.get("db.contraseña");
            
            conn = DriverManager.getConnection("jdbc:mysql://"+host+"/"+base,usuario, pass);//el tercer parametro es para el password

        } catch (Exception e) {
            System.err.println("E>Error instanciando conexión a la base de datos");
        }
    }

    public void cerrar() throws SQLException {
        conn.close();//se cierra la conexión
    }

    public Statement crearSt() throws SQLException {
        return conn.createStatement();
    }
}
