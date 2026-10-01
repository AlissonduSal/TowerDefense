package projetoesdlinear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Repositório dos mapas disponíveis para seleção.
 * 
 * Para adicionar um mapa novo: criar um método "criarMapaX()" como o de
 * baixo, retornando um MapaConfig com um id único, e incluir a chamada na
 * lista DISPONIVEIS. Nenhuma outra parte do jogo muda -- a tela de seleção
 * de mapas e o motor do jogo trabalham em cima de MapaConfig, não sabem o
 * que tem dentro de cada um.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Mapas {
    
    private static final int COLUNAS_PADRAO = 20;
    private static final int LINHAS_PADRAO = 15;
    private static final int TILE_PADRAO = 40;
    
    private static final List<MapaConfig> DISPONIVEIS = Arrays.asList(
        criarMapaZigueZague()
        // próximos mapas entram aqui, um por linha
    );
    
    /**
     * Mapa 1: caminho em "onda quadrada" (zigue-zague), baseado na imagem
     * de referência -- entra pela esquerda embaixo e sai pela direita em cima,
     * com dois "morros" e um "vale" no meio.
     */
    private static MapaConfig criarMapaZigueZague() {
        
        List<Coordenada> waypoints = new ArrayList<>();
        waypoints.add( new Coordenada( 0, 11 ) );
        waypoints.add( new Coordenada( 4, 11 ) );
        waypoints.add( new Coordenada( 4, 3 ) );
        waypoints.add( new Coordenada( 9, 3 ) );
        waypoints.add( new Coordenada( 9, 11 ) );
        waypoints.add( new Coordenada( 14, 11 ) );
        waypoints.add( new Coordenada( 14, 3 ) );
        waypoints.add( new Coordenada( 19, 3 ) );
        
        return new MapaConfig( "ziguezague", "Zigue-zague",
                COLUNAS_PADRAO, LINHAS_PADRAO, TILE_PADRAO, waypoints );
        
    }
    
    public static List<MapaConfig> disponiveis() {
        return DISPONIVEIS;
    }
    
    public static MapaConfig padrao() {
        return DISPONIVEIS.get( 0 );
    }
    
    public static MapaConfig porId( String id ) {
        for ( MapaConfig m : DISPONIVEIS ) {
            if ( m.getId().equals( id ) ) {
                return m;
            }
        }
        return padrao();
    }
    
}
