/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SincronizarFusionTables;

import Entorno.Configuracion.Config;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.Service.GDataRequest.RequestType;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;
import com.google.gdata.util.ContentType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Antonio Fernández Ares (antares.es@gmail.com)
 */
public class sincronizarFusionTables extends Thread {
    
    static Config _c = new Config();

    /**
     * Todas las peticiones a google fusion tables comienzan con esta URL
     */
    private static final String SERVICE_URL = _c.get("ft.SERVICE_URL");
    /**
     * Servicio para manejar las respuestas de Google Fusion Tables
     */
    private GoogleService service;

    /**
     * Autentica la cuenta para el servicio empleando la cuenta de Gmail
     *
     * @param email dirección de correo
     * @param password contraseña del correo
     * @throws AuthenticationException cuando falla la autenticación
     */
    public sincronizarFusionTables(String email, String password) throws AuthenticationException {
        service = new GoogleService("fusiontables", "fusiontables.ApiExample");
        service.setUserCredentials(email, password);
    }

    /**
     *
     * @param token
     * @throws AuthenticationException
     */
    public sincronizarFusionTables(String token) {
        try {
            service = new GoogleService("fusiontables", "Sipesca");
            service.setUserToken(token);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public QueryResults query(String query, boolean isUsingEncId) throws IOException, ServiceException {

        String lowercaseQuery = query.toLowerCase();
        String encodedQuery = URLEncoder.encode(query, "UTF-8");

        GDataRequest request;
        // If the query is a select, describe, or show query, run a GET request.
        if (lowercaseQuery.startsWith("select")
                || lowercaseQuery.startsWith("describe")
                || lowercaseQuery.startsWith("show")) {
            URL url = new URL(SERVICE_URL + "?sql=" + encodedQuery + "&encid=" + isUsingEncId);
            request = service.getRequestFactory().getRequest(RequestType.QUERY, url,
                    ContentType.TEXT_PLAIN);
        } else {
            // Otherwise, run a POST request.
            URL url = new URL(SERVICE_URL + "?encid=" + isUsingEncId);
            request = service.getRequestFactory().getRequest(RequestType.INSERT, url,
                    new ContentType("application/x-www-form-urlencoded"));
            OutputStreamWriter writer = new OutputStreamWriter(request.getRequestStream());
            writer.append("sql=" + encodedQuery);
            writer.flush();
        }

        request.execute();

        return getResults(request);

    }

    /**
     * Returns the Fusion Tables CSV response as a {@code QueryResults} object.
     *
     * @return an object containing a list of column names and a list of row
     * values from the Fusion Tables response
     */
    private QueryResults getResults(GDataRequest request)
            throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(request.getResponseStream());
        BufferedReader bufferedStreamReader = new BufferedReader(inputStreamReader);
        CSVReader reader = new CSVReader(bufferedStreamReader);
        // The first line is the column names, and the remaining lines are the rows.
        List<String[]> csvLines = reader.readAll();
        List<String> columns = Arrays.asList(csvLines.get(0));
        List<String[]> rows = csvLines.subList(1, csvLines.size());
        QueryResults results = new QueryResults(columns, rows);
        return results;
    }

    @Override
    public void run() {
        try {
            boolean useEncId = true;

            
            System.out.println("--- Create a table ---");
            QueryResults results = this.query("CREATE TABLE demo (name:STRING, date:DATETIME)", useEncId);
            results.print();
            
            System.out.println("--- Create a table ---");
            results = this.query("CREATE TABLE demo (name:STRING, date:DATETIME)", useEncId);
            results.print();
            String tableId = (results.rows.get(0))[0];

            System.out.println("--- Insert data into the table ---");
            results = this.query("INSERT INTO " + tableId + " (name, date) VALUES ('bob', '1/1/2012')",
                    useEncId);
            results.print();

            System.out.println("--- Insert more data into the table ---");
            results = this.query("INSERT INTO " + tableId + " (name, date) VALUES ('george', '1/4/2012')",
                    useEncId);
            results.print();

            System.out.println("--- Select data from the table ---");
            results = this.query("SELECT * FROM " + tableId + " WHERE date > '1/3/2012'", useEncId);
            results.print();

            System.out.println("--- Drop the table ---");
            results = this.query("DROP TABLE " + tableId, useEncId);
            results.print();
        } catch (IOException ex) {
            Logger.getLogger(sincronizarFusionTables.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(sincronizarFusionTables.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
