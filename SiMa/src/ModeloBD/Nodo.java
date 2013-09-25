/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ModeloBD;

/**
 *
 * @author mgarenas
 */
public class Nodo {

    long  idNodo;//: Llave primaria, entero largo con un número aleatorio.
    float latitud;//: Localización norte-sur en formato decimal.
    float longitud;//: Localización este-oeste en formato decimal
    String nombre;//: nombre descriptivo.

    /**
     * @Override
     */
    public String toString(){
        System.err.println("idNodo:"+idNodo+ "...");
        return nombre;
    }
}
