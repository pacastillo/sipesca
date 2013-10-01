/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SincronizarFusionTables;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.Fusiontables.Query.Sql;
import com.google.api.services.fusiontables.FusiontablesScopes;
import com.google.api.services.fusiontables.model.Sqlresponse;
import com.google.api.services.fusiontables.model.Table;
import com.google.api.services.fusiontables.model.TableList;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Proporciona una interfaz de comunicación con las tablas en la nube mediante
 * GOOGLE FUSION TABLES
 *
 * @author Antonio Fernández Ares (antares.es@gmail.com)
 */
public class conectarFusionTables {

    Entorno.Configuracion.Config _c = new Entorno.Configuracion.Config();
    /**
     * Nombre de la aplicación registrada en Google App
     */
    private final String APPLICATION_NAME = _c.get("ft.APPLICATION_NAME");
    /**
     * Fichero donde se almacenan las credenciales de usuario
     */
    private final java.io.File DATA_STORE_FILE = new java.io.File("/home/antares/.sipesca/client_secrets.json");
    /**
     * Directorio donde se almacenan las credenciales de usuario
     */
    private final java.io.File DATA_STORE_DIR = new java.io.File("/home/antares/.sipesca/");
    /**
     * Instancia global del link.
     */
    private FileDataStoreFactory dataStoreFactory;
    /**
     * Instancia del transporte HTTP
     */
    private static HttpTransport httpTransport;
    /**
     * Instancia del JSON factory
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    /**
     * Instancia del manejador de fusiontable
     */
    private Fusiontables fusiontables;

    private Credential authorize() throws Exception {
        FileInputStream _f = new FileInputStream(DATA_STORE_FILE);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(_f));
        //new InputStreamReader(ejemplo.class.getResourceAsStream("/client_secrets.json")));

        if (clientSecrets.getDetails().getClientId().startsWith("Enter") || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println(
                    "Enter Client ID and Secret from https://code.google.com/apis/console/?api=fusiontables "
                    + "into fusiontables-cmdline-sample/src/main/resources/client_secrets.json");
            System.exit(1);
        }

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singleton(FusiontablesScopes.FUSIONTABLES)).setDataStoreFactory(
                dataStoreFactory).build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public conectarFusionTables() {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            Credential credential = authorize();
            fusiontables = new Fusiontables.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(conectarFusionTables.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(conectarFusionTables.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public Fusiontables.Table.List listaTablas() {
        try {
            // Fetch the table list
            Fusiontables.Table.List listTables = fusiontables.table().list();
            TableList tablelist = listTables.execute();

            if (tablelist.getItems() == null || tablelist.getItems().isEmpty()) {
                System.out.println("No tables found!");
                return null;
            }

            for (Table table : tablelist.getItems()) {
                System.out.println(table);
                System.out.println("/n");
            }

            return listTables;

        } catch (IOException ex) {
            Logger.getLogger(conectarFusionTables.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Sqlresponse sql(String query) {
        try {
            Sql sql = fusiontables.query().sql(query);
            return sql.execute();
        } catch (IOException ex) {
            Logger.getLogger(conectarFusionTables.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;


    }

    /**
     * Función que inserta una nueva tupla en la tabla alojada en FusionTables
     *
     * @param tabla identificador de la tabla
     * @param campos listado de campos (separads por comas) de la tabla
     * @param valores listado de valores (separados por comas) a insertar
     * @return
     */
    public Sqlresponse insert(String tabla, String campos, String valores) {
        String peticion = "INSERT INTO " + tabla + "(" + campos + ") VALUES (\" " + valores + "\")";
        return this.sql(peticion);
    }

    /**
     * Función que actualiza una nueva tupla en la tabla alojada en FusionTables
     *
     * @param tabla identificador de la tabla
     * @param campo listado de campos (separads por comas) de la tabla
     * @param valor listado de valores (separados por comas) a insertar
     * @param ROWID identificador de la tupla
     * @return
     */
    public Sqlresponse update(String tabla, String campo, String valor, String ROWID) {
        String peticion = "UPDATE " + tabla + " SET " + campo + " = \"" + valor + "\" WHERE ROWID = "
                + "\'" + ROWID + "\'";
        System.err.println(peticion);
        return this.sql(peticion);
    }

    /**
     * Función que actualiza una tupla en la tabla alojada en FusionTables
     *
     * @param tabla identificador de la tabla
     * @param campo campo que será actualizado
     * @param valor valor que será actualizado
     * @param ROWID identificador de la tupla
     * @return
     */
    public Sqlresponse update(String tabla, String campo, String valor, List<List<Object>> ROWIDs) {
        String peticion = "";

        for (Object ite : ROWIDs) {

            peticion = peticion + "UPDATE " + tabla + " SET " + campo + " = \"" + valor + "\" WHERE ROWID = "
                    + "\'" + ((String) ((List<String>) ite).get(0)) + "\'; ";
            System.err.println(peticion);
        }
        System.err.println(peticion);
        this.sql(peticion);
        return null;
    }

    /**
     * Función que elimina una tupla en la tabla alojada en FusionTables
     *
     * @param tabla identificador de la tabla
     * @param ROWID identificador de la tupla
     * @return
     */
    public Sqlresponse delete(String tabla, String ROWID) {
        String peticion = "DELETE FROM " + tabla + "\" WHERE ROWID = " + "\'" + ROWID + "\'";
        System.err.println(peticion);
        return this.sql(peticion);
    }

    /**
     * Función que elimina una tupla en la tabla alojada en FusionTables
     *
     * @param tabla identificador de la tabla
     * @param ROWID identificador de la tupla
     * @return
     */
    public Sqlresponse delete(String tabla, List<List<Object>> ROWIDs) {
        String peticion;

        for (Object ite : ROWIDs) {
            peticion = "DELETE FROM " + tabla + "\" WHERE ROWID = " + "\'" + ((String) ((List<String>) ite).get(0)) + "\';";
            this.sql(peticion);
            System.err.println(peticion);
        }

        return null;
    }
}
