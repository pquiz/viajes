package Mygymmate.gymmate;


public class CostoMensaje {
    private String clave;
    private String establecimiento;
    private String concepto;
    private String folio;
    private String pdf;
    private String moneda;
    private Double monto;
    private Double adicional;
    private Double iva;
    private String xml;
    private String ticket;
    private Long fecha;
    private String uuid;

    public CostoMensaje() {
    }

    public CostoMensaje(String clave, String establecimiento, String concepto, String folio, String pdf, String moneda, Double monto, Double adicional, Double iva, String xml, Long fecha) {
        this.clave = clave;
        this.establecimiento = establecimiento;
        this.concepto = concepto;
        this.folio = folio;
        this.pdf = pdf;
        this.moneda = moneda;
        this.monto = monto;
        this.adicional = adicional;
        this.iva = iva;
        this.xml = xml;
        this.fecha = fecha;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(String establecimiento) {
        this.establecimiento = establecimiento;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public String getPdf() {
        return pdf;
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public Double getAdicional() {
        return adicional;
    }

    public void setAdicional(Double adicional) {
        this.adicional = adicional;
    }

    public Double getIva() {
        return iva;
    }

    public void setIva(Double iva) {
        this.iva = iva;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public Long getFecha() {
        return fecha;
    }

    public void setFecha(Long fecha) {
        this.fecha = fecha;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
