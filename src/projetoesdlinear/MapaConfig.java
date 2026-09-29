package projetoesdlinear;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Descreve um mapa do jogo: dimensões da malha e o caminho que os inimigos
 * percorrem, guardado apenas como os "cotovelos" (waypoints) em coordenadas
 * de célula. O caminho é sempre ortogonal -- cada waypoint compartilha a
 * linha ou a coluna do waypoint seguinte -- e as células entre um waypoint
 * e o próximo são preenchidas automaticamente como caminho.
 * 
 * Um mapa novo é só uma instância nova desta classe (ver Mapas.java);
 * nenhuma outra parte do jogo (desenho, colocação de torres, movimento dos
 * inimigos) precisa saber qual mapa está carregado.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class MapaConfig {
    
    private final String id;
    private final String nome;
    private final int colunas;
    private final int linhas;
    private final int tamanhoTile;
    private final List<Coordenada> waypoints;
    
    // calculado uma única vez, a partir dos waypoints
    private final Set<Coordenada> celulasCaminho;
    
    public MapaConfig( String id, String nome, int colunas, int linhas,
            int tamanhoTile, List<Coordenada> waypoints ) {
        
        this.id = id;
        this.nome = nome;
        this.colunas = colunas;
        this.linhas = linhas;
        this.tamanhoTile = tamanhoTile;
        this.waypoints = waypoints;
        this.celulasCaminho = expandirCaminho( waypoints );
        
    }
    
    /**
     * Expande os waypoints em todas as células por onde o caminho passa,
     * andando em linha reta (na horizontal ou na vertical) entre cada par
     * de waypoints consecutivos.
     */
    private static Set<Coordenada> expandirCaminho( List<Coordenada> waypoints ) {
        
        Set<Coordenada> celulas = new LinkedHashSet<>();
        
        for ( int i = 0; i < waypoints.size() - 1; i++ ) {
            
            Coordenada a = waypoints.get( i );
            Coordenada b = waypoints.get( i + 1 );
            
            if ( a.coluna == b.coluna ) {
                
                // segmento vertical
                int passo = b.linha >= a.linha ? 1 : -1;
                for ( int l = a.linha; l != b.linha + passo; l += passo ) {
                    celulas.add( new Coordenada( a.coluna, l ) );
                }
                
            } else if ( a.linha == b.linha ) {
                
                // segmento horizontal
                int passo = b.coluna >= a.coluna ? 1 : -1;
                for ( int c = a.coluna; c != b.coluna + passo; c += passo ) {
                    celulas.add( new Coordenada( c, a.linha ) );
                }
                
            } else {
                throw new IllegalArgumentException(
                        "waypoints " + a + " e " + b + " não estão alinhados "
                        + "(o caminho só anda na horizontal ou na vertical)" );
            }
            
        }
        
        return celulas;
        
    }
    
    /** Monta a malha inteira do mapa (grama/caminho) a partir dos waypoints. */
    public TipoCelula[][] paraGrade() {
        
        TipoCelula[][] grade = new TipoCelula[linhas][colunas];
        
        for ( int l = 0; l < linhas; l++ ) {
            for ( int c = 0; c < colunas; c++ ) {
                grade[l][c] = ehCaminho( c, l ) ? TipoCelula.CAMINHO : TipoCelula.GRAMA;
            }
        }
        
        return grade;
        
    }
    
    public boolean ehCaminho( int coluna, int linha ) {
        return celulasCaminho.contains( new Coordenada( coluna, linha ) );
    }
    
    /** Célula onde os inimigos entram no mapa (primeiro waypoint). */
    public Coordenada getEntrada() {
        return waypoints.get( 0 );
    }
    
    /** Célula onde os inimigos saem do mapa / alcançam a base (último waypoint). */
    public Coordenada getSaida() {
        return waypoints.get( waypoints.size() - 1 );
    }
    
    public String getId() {
        return id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public int getColunas() {
        return colunas;
    }
    
    public int getLinhas() {
        return linhas;
    }
    
    public int getTamanhoTile() {
        return tamanhoTile;
    }
    
    public List<Coordenada> getWaypoints() {
        return waypoints;
    }
    
}
