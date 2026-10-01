package projetoesdlinear;

/**
 * Tacador de bolinha.
 * 
 * Torre simples de ataque à distância: joga bola nos inimigos e dá dano.
 * A partir do nível 3, as bolas passam a ricochetear entre inimigos (até
 * 3 deles); no nível 5 o ricochete deixa de ter limite.
 * 
 * Tabela de melhorias (ver o PDF):
 *     nível 1: +1 dano
 *     nível 2: +1 dano
 *     nível 3: ricocheteia entre até 3 inimigos
 *     nível 4: +2 dano
 *     nível 5: ricochete sem limite
 * 
 * @author Prof. Dr. David Buzatto
 */
public class TacadorDeBolinha extends Torre {
    
    /** Quantidade de ricochete a partir do nível 5: sem limite. */
    public static final int RICOCHETE_ILIMITADO = -1;
    
    // quanto o dano aumenta em cada nível, em ordem (índice 0 = nível 1)
    private static final int[] GANHO_DE_DANO_POR_NIVEL = { 1, 1, 0, 2, 0 };
    
    public TacadorDeBolinha() {
        super( "Tacador de bolinha", 20, 3, 1, 1, 1 );
    }
    
    @Override
    public int getDano() {
        
        int total = danoBase;
        for ( int i = 0; i < getNivel(); i++ ) {
            total += GANHO_DE_DANO_POR_NIVEL[i];
        }
        return total;
        
    }
    
    /**
     * Quantos inimigos a bola ricocheteia, contando o primeiro atingido.
     * 1 = não ricocheteia (só acerta um inimigo).
     */
    public int getQuantidadeDeRicochete() {
        
        if ( getNivel() >= 5 ) {
            return RICOCHETE_ILIMITADO;
        } else if ( getNivel() >= 3 ) {
            return 3;
        }
        return 1;
        
    }
    
}
