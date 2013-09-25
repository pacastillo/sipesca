/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ModeloBD;

/**
 *
 * @author mgarenas
 */
public class Dispositivo {

 String idDispositivo;//: Cadena tras el hash de la MAC Bluetooth de un dispositivo.
 String majordeviceclass;//: Cadena con la característica obtenida de la MAC Bluetooth acerca de la procedencia y capacidades del dispositivo.
 String   minordeviceclass;//: Cadena con la característica obtenida de la MAC Bluetooth acerca de la procedencia y capacidades del dispositivo.
 String   serviceclass;//: Cadena con la característica obtenida de la MAC Bluetooth acerca de la procedencia y capacidades del dispositivo.
 String   fabricante;//: Cadena con el nombre del fabricante del dispositivo Bluetooth detectado.


    /**
     * @Override
     */
    public String toString(){
        return "idDispositivo:"+idDispositivo+ "...";
    }
}
