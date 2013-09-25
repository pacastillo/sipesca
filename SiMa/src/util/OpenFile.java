
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 *
 * @author mgarenas
 */
public class OpenFile {

    public static InputStream get( String fileName) {
        InputStream url = null;
            try {
                url = OpenFile.class.getResourceAsStream(fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }

        return url;
    }

    public static void main(String[] args) {
    System.out.println(OpenFile.get("parametrosPorDefecto.properties"));
    System.out.println(System.getProperty("user.dir"));
    }
}


//~ Formatted by Jindent --- http://www.jindent.com

