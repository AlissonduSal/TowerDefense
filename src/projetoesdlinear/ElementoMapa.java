package projetoesdlinear;

/**
 * Representa um elemento visual na matriz do mapa.
 */
public class ElementoMapa {
    
    private final String codigo;
    private final TipoCelula tipoCelula;
    private final boolean ehEntrada;
    private final boolean ehSaida;
    
    public ElementoMapa(String codigo, TipoCelula tipoCelula, boolean ehEntrada, boolean ehSaida) {
        this.codigo = codigo;
        this.tipoCelula = tipoCelula;
        this.ehEntrada = ehEntrada;
        this.ehSaida = ehSaida;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public TipoCelula getTipoCelula() {
        return tipoCelula;
    }
    
    public boolean isEntrada() {
        return ehEntrada;
    }
    
    public boolean isSaida() {
        return ehSaida;
    }
}
