package projetoesdlinear;

import java.util.Arrays;
import java.util.List;

/**
 * Repositório dos mapas disponíveis para seleção.
 * 
 * Agora os mapas são desenhados diretamente em uma matriz visual de strings!
 * Elementos definidos:
 * - A: Grama padrão
 * - A1, A2...: Variações de grama
 * - AC, BC...: Células de caminho (qualquer código terminado em 'C')
 * - AE, BE...: Entrada do caminho (qualquer código terminado em 'E')
 * - AS, BS...: Saída do caminho (qualquer código terminado em 'S')
 * 
 * Nenhuma coordenada manual (ex: waypoints.add) é mais necessária. O percurso
 * é detectado e os waypoints são gerados automaticamente.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Mapas {
    
    private static final int TILE_PADRAO = 40;
    
    private static final List<MapaConfig> DISPONIVEIS = Arrays.asList(
        criarMapaZigueZague()
        // próximos mapas entram aqui, um por linha
    );
    
    /**
     * Mapa 1: caminho em "onda quadrada" (zigue-zague), baseado na imagem
     * de referência -- entra pela esquerda embaixo e sai pela direita em cima,
     * com dois "morros" e um "vale" no meio.
     * 
     * Dimensões: 20 colunas x 15 linhas.
     */
    private static MapaConfig criarMapaZigueZague() {
        
        String[] layout = {
            /* 00 */ "A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A",
            /* 01 */ "A   A1  A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A1  A   A",
            /* 02 */ "A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A",
            /* 03 */ "A   A   A   A   AC  AC  AC  AC  AC  AC  A   A   A   A   AC  AC  AC  AC  AC  AS",
            /* 04 */ "A   A   A   A   AC  A   A   A   A   AC  A   A   A   A   AC  A   A   A   A   A",
            /* 05 */ "A   A   A   A   AC  A   A1  A   A   AC  A   A   A1  A   AC  A   A   A   A   A",
            /* 06 */ "A   A   A   A   AC  A   A   A   A   AC  A   A   A   A   AC  A   A   A   A   A",
            /* 07 */ "A   A   A   A   AC  A   A   A   A   AC  A   A   A   A   AC  A   A   A   A   A",
            /* 08 */ "A   A   A   A   AC  A   A   A1  A   AC  A   A   A   A   AC  A   A1  A   A   A",
            /* 09 */ "A   A   A   A   AC  A   A   A   A   AC  A   A   A   A   AC  A   A   A   A   A",
            /* 10 */ "A   A   A   A   AC  A   A   A   A   AC  A   A   A   A   AC  A   A   A   A   A",
            /* 11 */ "AE  AC  AC  AC  AC  A   A   A   A   AC  AC  AC  AC  AC  AC  A   A   A   A   A",
            /* 12 */ "A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A",
            /* 13 */ "A   A   A1  A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A1  A",
            /* 14 */ "A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A   A"
        };
        
        return new MapaConfig( "ziguezague", "Zigue-zague", TILE_PADRAO, layout );
        
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
