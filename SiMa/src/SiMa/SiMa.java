/*
 * SiMa : Sipesca Manager
 */
package SiMa;

import ActualizadorLocal.ActualizadorDBLocal;
import SincronizarFusionTables.ActualizadorFT;
import Entorno.Configuracion.Config;
import Entorno.Depuracion.Debug;
import SincronizarFusionTables.PasosPorDia;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.TwitterAgente;

/**
 * Una clase para gobernarlas a todas, una clase para encontrarlas, una clase para atraerlas a todas y atarlas en las
 * tinieblas
 *
 * @author Antonio Fernández Ares (antares.es@gmail.com)
 */
public class SiMa {

  /**
   * Cargamos la configuración
   */
  static Config _c = new Config();
  /**
   * Cargamos variable de depuración
   */
  static Debug _d = new Debug();
  /**
   * Manejador de la actualización en Córdoba
   */
  static ActualizadorLocal.ActualizadorDBLocal _actualizarDB;
  /**
   * Manejador de la actualización en FusionTable
   */
  static ActualizadorFT _actualizarFT;
  /**
   * Manejador de grupos de hebras
   */
  static ThreadGroup tg;

  /**
   * Método principal de la clase
   *
   * @param args Los argumentos de ejecución (Actualmente no utilizados)
   */
  public static void main(String[] args) throws IOException {
    tg = Thread.currentThread().getThreadGroup();

    _actualizarDB = new ActualizadorDBLocal(_c.get("data.ultimo"));
    _actualizarFT = new ActualizadorFT();

    _actualizarFT.start();
    _actualizarDB.start();

    //TwitterAgente _t = new TwitterAgente();
    //_t.publicar("Seguímos haciendo pruebas, ahora con la codificación de caractéres. Ñadú. Perdón por las molestias.");

      try {
      //_actualizarFT.join();
      _actualizarDB.join();
      } catch (InterruptedException ex) {
      Logger.getLogger(SiMa.class.getName()).log(Level.SEVERE, null, ex);
      }
    
  }
}
