package projetoesdlinear;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositório dos mapas disponíveis para seleção.
 * 
 * Agora construído a partir de uma matriz visual de elementos (código)
 * ao invés de waypoints manuais.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Mapas {
    
    private static final int TILE_PADRAO = 40;
    
    // Legenda dos elementos do mapa (A, B, C, A1, B2, etc.)
    private static final Map<String, ElementoMapa> LEGENDA = new HashMap<>();
    
    static {
        // Grama (livre para colocar torres)
        LEGENDA.put("A", new ElementoMapa("A", TipoCelula.GRAMA, false, false));
        LEGENDA.put("A1", new ElementoMapa("A1", TipoCelula.GRAMA, false, false));
        
        // Caminho (onde os inimigos andam)
        LEGENDA.put("C", new ElementoMapa("C", TipoCelula.CAMINHO, false, false));
        
        // Entrada (onde os inimigos nascem)
        LEGENDA.put("E", new ElementoMapa("E", TipoCelula.CAMINHO, true, false));
        
        // Saída (onde os inimigos chegam na base)
        LEGENDA.put("S", new ElementoMapa("S", TipoCelula.CAMINHO, false, true));
    }
    
    private static final List<MapaConfig> DISPONIVEIS = Arrays.asList(
        criarMapaZigueZague()
        // próximos mapas entram aqui, um por linha
    );
    
    /**
     * Mapa 1: caminho em "onda quadrada" (zigue-zague).
     */
    private static MapaConfig criarMapaZigueZague() {
        
        String[] layout = {
            "A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A",
            "A  A1 A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A1 A  A",
            "A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A",
            "A  A  A  A  C  C  C  C  C  C  A  A  A  A  C  C  C  C  C  S",
            "A  A  A  A  C  A  A  A  A  C  A  A  A  A  C  A  A  A  A  A",
            "A  A  A  A  C  A  A1 A  A  C  A  A  A1 A  C  A  A  A  A  A",
            "A  A  A  A  C  A  A  A  A  C  A  A  A  A  C  A  A  A  A  A",
            "A  A  A  A  C  A  A  A  A  C  A  A  A  A  C  A  A  A  A  A",
            "A  A  A  A  C  A  A  A1 A  C  A  A  A  A  C  A  A1 A  A  A",
            "A  A  A  A  C  A  A  A  A  C  A  A  A  A  C  A  A  A  A  A",
            "A  A  A  A  C  A  A  A  A  C  A  A  A  A  C  A  A  A  A  A",
            "E  C  C  C  C  A  A  A  A  C  C  C  C  C  C  A  A  A  A  A",
            "A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A",
            "A  A  A1 A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A1 A",
            "A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A  A"
        };
        
        return new MapaConfig("ziguezague", "Zigue-zague", TILE_PADRAO, layout, LEGENDA);
        
    }
    
    public static List<MapaConfig> disponiveis() {
        return DISPONIVEIS;
    }
    
    public static MapaConfig padrao() {
        return DISPONIVEIS.get(0);
    }
    
    public static MapaConfig porId(String id) {
        for (MapaConfig m : DISPONIVEIS) {
            if (m.getId().equals(id)) {
                return m;
            }
        }
        return padrao();
    }
    
}
