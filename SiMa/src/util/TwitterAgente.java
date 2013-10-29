/*
 * Copyright (C) 2013 antares
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
package util;

import Entorno.Depuracion.Debug;
import Entorno.Configuracion.Config;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.Client;

/**
 * Clase encargada de publicaer en twitter y esas cosas
 * @author antares
 */
public class TwitterAgente {
  Config _c = new Config();
  Debug _d = new Debug();
  
  WebResource _w;
  Client client;

  public TwitterAgente() {
  }
  
  public void publicar(String m){
    
    com.sun.jersey.api.client.config.ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig(); // SSL configuration
    client = Client.create(config);
    
    _w = client.resource( _c.get("twitter.url") + "?m=" + m.replaceAll(" ","%20"));
    _w.get(String.class);
    //System.err.println(_w.;
  }
  
  
}
