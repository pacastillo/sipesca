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
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.Fusiontables.Query.Sql;
import com.google.api.services.fusiontables.Fusiontables.Table.Delete;
import com.google.api.services.fusiontables.FusiontablesScopes;
import com.google.api.services.fusiontables.model.Column;
import com.google.api.services.fusiontables.model.Table;
import com.google.api.services.fusiontables.model.TableList;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author Antonio Fernández Ares (antares.es@gmail.com)
 */
public class ejemplo extends Thread {

    private static final String APPLICATION_NAME = "";
    /**
     * Fichero donde se almacenan las credenciales de usuario
     *
     */
    private static final java.io.File DATA_STORE_FILE = new java.io.File("/home/Antonio Fernández Ares (antares.es@gmail.com)/client_secrets.json");
    
        /**
     * Directorio donde se almacenan las credenciales de usuario
     *
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File("/home/Antonio Fernández Ares (antares.es@gmail.com)/");
    
    
    /**
     * Instancia global del link.
     */
    private static FileDataStoreFactory dataStoreFactory;
    /**
     * Instancia del transporte HTTP
     */
    private static HttpTransport httpTransport;
    /**
     * Instancia del JSON factory
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Fusiontables fusiontables;

    private static Credential authorize() throws Exception {
        FileInputStream _f = new FileInputStream(DATA_STORE_FILE);
        
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new  InputStreamReader(_f));
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

    @Override
    public void run() {
        
        try {
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
      // authorization
      Credential credential = authorize();
      // set up global FusionTables instance
      fusiontables = new Fusiontables.Builder(
          httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
      // run commands
      listTables();
      String tableId = createTable();
      insertData(tableId);
      showRows(tableId);
      deleteTable(tableId);
      // success!
      return;
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
    }
    
    // =========================== BORRAR ============================
    
      /**
   * @param tableId
   * @throws IOException
   */
  private static void showRows(String tableId) throws IOException {
      System.out.println("Showing Rows From Table");

    Sql sql = fusiontables.query().sql("SELECT Text,Number,Location,Date FROM " + tableId);

    try {
      sql.execute();
    } catch (IllegalArgumentException e) {
      // For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
      // been thrown.
      // Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
      // http://code.google.com/p/google-api-java-client/issues/detail?id=545
    }
  }

  /** List tables for the authenticated user. */
  private static void listTables() throws IOException {
    System.out.println("Listing My Tables");

    // Fetch the table list
    Fusiontables.Table.List listTables = fusiontables.table().list();
    TableList tablelist = listTables.execute();

    if (tablelist.getItems() == null || tablelist.getItems().isEmpty()) {
      System.out.println("No tables found!");
      return;
    }

    for (Table table : tablelist.getItems()) {
     System.out.println(table);
     System.out.println("/n");
    }
  }

  /** Create a table for the authenticated user. */
  private static String createTable() throws IOException {
    System.out.println("Create Sample Table");

    // Create a new table
    Table table = new Table();
    table.setName(UUID.randomUUID().toString());
    table.setIsExportable(false);
    table.setDescription("Sample Table");

    // Set columns for new table
    table.setColumns(Arrays.asList(new Column().setName("Text").setType("STRING"),
        new Column().setName("Number").setType("NUMBER"),
        new Column().setName("Location").setType("LOCATION"),
        new Column().setName("Date").setType("DATETIME")));

    // Adds a new column to the table.
    Fusiontables.Table.Insert t = fusiontables.table().insert(table);
    Table r = t.execute();

    System.out.println(r);

    return r.getTableId();
  }

  /** Inserts a row in the newly created table for the authenticated user. */
  private static void insertData(String tableId) throws IOException {
    Sql sql = fusiontables.query().sql("INSERT INTO " + tableId + " (Text,Number,Location,Date) "
        + "VALUES (" + "'Google Inc', " + "1, " + "'1600 Amphitheatre Parkway Mountain View, "
        + "CA 94043, USA','" + new DateTime(new Date()) + "')");

    try {
      sql.execute();
    } catch (IllegalArgumentException e) {
      // For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
      // been thrown.
      // Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
      // http://code.google.com/p/google-api-java-client/issues/detail?id=545
    }
  }

  /** Deletes a table for the authenticated user. */
  private static void deleteTable(String tableId) throws IOException {
    System.out.println("Delete Sample Table");
    // Deletes a table
    Delete delete = fusiontables.table().delete(tableId);
    delete.execute();
  }
    
    // =========================== BORRAR ============================
    
    
    
    
    
}
