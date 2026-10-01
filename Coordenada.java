package projetoesdlinear;

/**
 * Uma posição na malha do mapa, em coordenadas de célula (coluna, linha) --
 * não em pixels. A conversão para pixel é feita por quem desenha, 
 * multiplicando por tamanhoTile.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Coordenada {
    
    public final int coluna;
    public final int linha;
    
    public Coordenada( int coluna, int linha ) {
        this.coluna = coluna;
        this.linha = linha;
    }
    
    @Override
    public boolean equals( Object obj ) {
        if ( !( obj instanceof Coordenada ) ) {
            return false;
        }
        Coordenada c = (Coordenada) obj;
        return c.coluna == coluna && c.linha == linha;
    }
    
    @Override
    public int hashCode() {
        return coluna * 31 + linha;
    }
    
    @Override
    public String toString() {
        return "(" + coluna + ", " + linha + ")";
    }
    
}
