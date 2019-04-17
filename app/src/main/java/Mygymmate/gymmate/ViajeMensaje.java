package Mygymmate.gymmate;

import java.util.ArrayList;


public class ViajeMensaje {
    private String nombre;
    private Long fechaInicio;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    private String uuid;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Long fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Long getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Long fechaFin) {
        this.fechaFin = fechaFin;
    }



    public ArrayList<CostoMensaje> getCostos() {
        return costos;
    }

    public void setCostos(ArrayList<CostoMensaje> costos) {
        this.costos = costos;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    private Long fechaFin;

    public ViajeMensaje(String nombre, Long fechaInicio, Long fechaFin, ArrayList<CostoMensaje> costos, String lugar, String rfc, String motivo, String moneda) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.costos = costos;
        this.lugar = lugar;
        this.rfc = rfc;
        this.motivo = motivo;
        this.moneda = moneda;
    }
    public ViajeMensaje(){}


    private ArrayList<CostoMensaje> costos;
    private String lugar;
    private String rfc;
    private String motivo;
    private String moneda;


}
