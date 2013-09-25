/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SincronizarFusionTables;

import java.util.List;

/**
 *
 * @author Antonio Fernández Ares (antares.es@gmail.com)
 */
public class QueryResults {
   final List<String> columnNames;
   final List<String[]> rows;

   public QueryResults(List<String> columnNames, List<String[]> rows) {
     this.columnNames = columnNames;
     this.rows = rows;
   }

  /**
   * Prints the query results.
   *
   * @param the results from the query
   */
  public void print() {
    String sep = "";
    for (int i = 0; i < columnNames.size(); i++) {
      System.out.print(sep + columnNames.get(i));
      sep = ", ";
    }
    System.out.println();

    for (int i = 0; i < rows.size(); i++) {
      String[] rowValues = rows.get(i);
      sep = "";
      for (int j = 0; j < rowValues.length; j++) {
        System.out.print(sep + rowValues[j]);
        sep = ", ";
      }
      System.out.println();
    }
  }
 }
