/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ModeloBD;

/**
 *
 * @author mgarenas
 */

import java.sql.Timestamp;
import java.util.Date;
public class Paso {
    String idNodo;
    String idDispositivo;
    java.sql.Timestamp tinicio;
    java.sql.Timestamp tfin;
    java.util.Date inicio;
    java.util.Date fin;

    public java.util.Date getInicio() {
        return inicio;
    }

    public void setInicio(java.util.Date inicio) {
        this.inicio = inicio;
    }

    public java.util.Date getFin() {
        return fin;
    }

    public void setFin(java.util.Date fin) {
        this.fin = fin;
    }
    

    public Paso(String idNodo, String idDispositivo, Timestamp tinicio, Timestamp tfin) {
        this.idNodo = idNodo;
        this.idDispositivo = idDispositivo;
        this.tinicio = tinicio;
        this.tfin = tfin;
    }
    
    public Paso(String idNodo, String idDispositivo, java.util.Date tInicio, java.util.Date tFin) {
        this.idNodo = idNodo;
        this.idDispositivo = idDispositivo;
        this.inicio = tInicio;
        this.fin = tFin;
        this.tinicio = new Timestamp(tInicio.getTime()) ;
        this.tfin = new Timestamp(tFin.getTime());
    }
    public Paso(String idNodo, String idDispositivo, Long tInicio, Long tFin) {
        this.idNodo = idNodo;
        this.idDispositivo = idDispositivo;
        this.inicio = new Date(tInicio);
        this.fin = new Date(tFin);
        this.tinicio = new Timestamp(tInicio) ;
        this.tfin = new Timestamp(tFin);
    }
    public String getIdNodo() {
        return idNodo;
    }

    public void setIdNodo(String idNodo) {
        this.idNodo = idNodo;
    }

    @Override
    public String toString() {
        return "Paso{" + "idNodo=" + idNodo + ", idDispositivo=" + idDispositivo + ", tinicio=" + tinicio + ", tfin=" + tfin + ", inicio=" + inicio + ", fin=" + fin + '}';
    }


    public String getIdDispositivo() {
        return idDispositivo;
    }

    public void setIdDispositivo(String idDispositivo) {
        this.idDispositivo = idDispositivo;
    }

    public Timestamp getTinicio() {
        return tinicio;
    }

    public void setTinicio(Timestamp tinicio) {
        this.tinicio = tinicio;
    }

    public Timestamp getTfin() {
        return tfin;
    }

    public void setTfin(Timestamp tfin) {
        this.tfin = tfin;
    }

    
}
