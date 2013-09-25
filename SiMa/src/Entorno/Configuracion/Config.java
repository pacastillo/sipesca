package Entorno.Configuracion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Clase manejadora de la configuración de la aplicación
 * Emplea dos archivos Properties de configuración, uno global en el propio JAR (sólo lectura) y uno local en el home del usuario (permite sobreescribir las propiedades)
 * @author Antonio Fernández Ares (antares.es@gmail.com)
 */
public class Config {

    /*
     * Fichero de propiedades global
     */
    public Properties _c_global = new Properties();
    
    /*
     * Ficheros de propiedades local
     */
    public Properties _c_local = new Properties();

    /*
     * Método que carga la configuración de los ficheros properties
     */
    private void cargarConfiguracion() {
        try {
            _c_global.load(Config.class.getResourceAsStream("config.properties"));
            File fichero = new File(System.getProperty("user.home") + "/" + _c_global.getProperty("directorio_configuracion") + "/config.properties");
            _c_local.load(new FileInputStream(fichero));
        } catch (Exception ex) {
            System.err.println("No se ha podido cargar el fichero de Configuración");
        }
    }

    /**
     * Devuelve el valor de una determinada clave
     * @param key La clave a buscar
     * @return Busca la clave primero en la configuración local, si no eśtá especificada, en la configuración global, si no existe, se devuelve null
     */
    public String get(String key) {
        if (_c_local.getProperty(key) != null) {
            return _c_local.getProperty(key);
        } else {
            return _c_global.getProperty(key);
        }
    }

     /**
     * Devuelve el valor de una determinada clave en formato entero
     * @param key La clave a buscar
     * @return Busca la clave primero en la configuración local, si no eśtá especificada, en la configuración global, si no existe, se devuelve null
     */
    public int getInt(String key) {
        return Integer.parseInt(this.get(key));
    }
    
     /**
     * Devuelve el valor de una determinada clave en formato booleano
     * @param key La clave a buscar
     * @return Busca la clave primero en la configuración local, si no eśtá especificada, en la configuración global, si no existe, se devuelve null
     */
    public boolean getBool (String key){
        return Boolean.parseBoolean(this.get(key));
    }

     /**
     * Inserta o modifica el valor de una determinada clave en la configuración local
     * @param key La clave bajo la que se almacenará el valor
     * @param valor El valor que se asociará a la clave
     */
    public void set(String key, String valor) {
        try {
            _c_local.setProperty(key, valor);
            _c_local.store(new FileOutputStream(System.getProperty("user.home") + "/" + _c_global.getProperty("directorio_configuracion") + "/config.properties"), null);
        } catch (Exception ex) {
            System.err.println("No se ha podido cargar el fichero de Configuración");
        }

    }

    /**
     * Constructor de la clase
     * @
     */
    public Config() {
        this.cargarConfiguracion();
    }
}
