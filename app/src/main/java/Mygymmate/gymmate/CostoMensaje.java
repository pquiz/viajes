package Mygymmate.gymmate;



public class CostoMensaje {
private String nombre;
private String lugar;
private Long fecha;
    public String getNombre() {
        return nombre;
    }

    public CostoMensaje(String nombre, String lugar, Long fecha) {
        this.nombre = nombre;
        this.lugar = lugar;
        this.fecha = fecha;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public Long getFecha() {
        return fecha;
    }

    public void setFecha(Long fecha) {
        this.fecha = fecha;
    }



}
