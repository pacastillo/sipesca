/*
 * Copyright (C) 2013  Antonio Fernández Ares (antares.es@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package SincronizarFusionTables;

import Entorno.Configuracion.Config;
import Entorno.Depuracion.Debug;
import Entorno.Conectar.Conectar;
import com.google.api.services.fusiontables.model.Sqlresponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase encargada del cálculo y la subida de los
 *
 * @author Antonio Fernández Ares (antares.es@gmail.com)
 */
public class PasosPorDia {

  private Config _c = new Config();
  private Debug _d = new Debug();
  private conectarFusionTables cFT = new conectarFusionTables();
  private String fecha;
  private ResultSet rs;
  private final String TABLAID = _c.get("ft.PASOSPORDIA.ID");
  private final List<String> campos = new ArrayList<>();
  public boolean check = false;

  //ResultSet rs = st.executeQuery("CALL agrupaPasosPorIntervalosNodo('2013-01-07 00:00:00','2013-06-02 00:00:00'," + 60 + ",'" + idNodo + "')");
  public PasosPorDia(String fecha) {
    this.fecha = fecha;
    campos.add("Intervalo");
    campos.add("idNodo");
    campos.add("Total");
  }
  
public PasosPorDia() {
    campos.add("Intervalo");
    campos.add("idNodo");
    campos.add("Total");
    this.setFechaUltima();
  }
  
  
  
  public String setFechaUltima(){
    Sqlresponse r = cFT.select(TABLAID,"Intervalo","","ORDER BY \'Intervalo\' DESC LIMIT 1" );  
    this.fecha = (String) r.getRows().get(0).get(0);
    return fecha;
  }

  public boolean calcular() {
    Conectar conectar = new Conectar();
    try {
      Statement st = conectar.crearSt();
      System.out.println("CALL agrupaPasosPorIntervalosNodosSeparados('" + fecha + "','" + _d.sdf.format(Calendar.getInstance().getTime()) + "','" + 60 * 24 + "')");
      rs = st.executeQuery("CALL agrupaPasosPorIntervalosNodosSeparados('" + fecha + "','" + _d.sdf.format(Calendar.getInstance().getTime()) + "','" + 60 * 24 + "')");

      List<String> valores = new ArrayList<>();

      while (rs.next()) {
        System.err.println("->" + rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3));
        valores.add(rs.getString(1));
        valores.add(rs.getString(2));
        valores.add(rs.getString(3));
        cFT.insert(TABLAID, campos, valores, check);
        valores.clear();
      }
      
       cFT.sync();
    } catch (SQLException ex) {
      Logger.getLogger(PasosPorDia.class.getName()).log(Level.SEVERE, null, ex);
    }


    return false;
  }
}
