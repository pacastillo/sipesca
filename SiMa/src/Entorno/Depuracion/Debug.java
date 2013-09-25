/**
 * Depuracion Paquete que proporciona herramientas para la depuración por pantalla de la aplicación
 * 
 */
package Entorno.Depuracion;

import Entorno.Configuracion.Config;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Clase encargada de las tareas de depuración y esas cosas
 *
 * @author Antonio Fernández Ares (antares.es@gmail.com)
 */
public class Debug {

    /**
     * Variable de configuración
     */
    private Config _c = new Config();
      
    /**
     * Variables para medida de tiempo
     */
    static long t_start, time;
    /**
     * Variable de formato para número flotantes
     */
    public DecimalFormat df = new DecimalFormat("#.####");
    /**
     * Variable de formato para fechas
     */
    public SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    /**
     * Función de depuración: Devuele una cadena con la hora y fecha actuales
     * formateadas
     *
     * @return Marca con el tiempo actual
     */
    public String timeMarca() {
        return "{" + sdf.format(new Date(System.currentTimeMillis())) + "}";
    }

    /**
     * Función de depuración: Almacena el tiempo actual para realizar una
     * medición posterior
     *
     */
    public void timeCheck() {
        t_start = System.currentTimeMillis();
    }

    /**
     * Devuelve el modo de salida por pantalla
     * @return True - Si el modo Verbose está activado. False -Si el modo Verbose está desactivado
     */
    public boolean isVerbose(){
        return _c.getBool("debug");
    }
    
    /**
     * Función de depuración: Imprime el tiempo transcurrido desde la última
     * marca de tiempo
     *
     * @param reset Booleano que indica si se tiene que es establecer una nueva
     * marca de tiempo tras realizar la medición
     * @return Cadena de texto con la medición de tiempo
     */
    public String timeDisplay(boolean reset) {
        time = System.currentTimeMillis() - t_start;
        String t = "(" + df.format(time / 1000.0 / 60.0) + "min)";
        if (reset) {
            t_start = System.currentTimeMillis();
        }
        return t;
    }

    /**
     * Salida estándar del mensaje cad con una marca de tiempo
     * @param cad mensaje a mostrar por salida estándar
     */
    public void primeOUT(String cad) {
        System.out.println(timeMarca() + " " + cad);
    }
    
    /**
     * Salida de depuración del mensaje cad con una marca de tiempo
     * @param cad mensaje a mostrar por salida estándar
     */
    public void primeVerbose(String cad){
        if(this.isVerbose()) System.out.println(timeMarca() + " " + cad);
    }

    /**
     * Salida de eror del mensaje cad con una marca de tiempo
     * @param cad mensaje a mostrar por salida estándar
     */
    public void primeERR(String cad) {
        System.err.println("E>" + timeMarca() + " " + cad);
    }

    /**
     * Salida estándar del mensaje cad con una marca de tiempo y una etiqueta
     * @param cad mensaje a mostrar por salida estándar
     */
    public void primeOUT(String label,String cad) {
        System.out.println(timeMarca() + "[" + label + "] " + cad);
    }
    
    /**
     * Salida de depuración del mensaje cad con una marca de tiempo y una etiqueta
     * @param cad mensaje a mostrar por salida estándar
     */
    public void primeVerbose(String label,String cad){
        if(this.isVerbose()) System.out.println(timeMarca() + "[" + label + "] " + cad);
    }

    /**
     * Salida de error del mensaje cad con una marca de tiempo y una etiqueta
     * @param cad mensaje a mostrar por salida estándar
     */
    public void primeERR(String label,String cad) {
        System.err.println("E>" + timeMarca() + "[" + label + "] " + cad);
    }

    /**
     * Constructor de la clase de depuración
     */
    public Debug() {
       
    }
}
