package projetoesdlinear;

import java.util.Arrays;
import java.util.List;
import projetoesdlinear.BancoDeDinheiro;
import projetoesdlinear.Boxeador;
import projetoesdlinear.TacadorDeBolinha;
import projetoesdlinear.Torre;

/**
 * Repositório das torres disponíveis para compra, no mesmo espírito do
 * Mapas.java. Cada entrada de disponiveis() é um "protótipo" -- só serve
 * pra ler nome/custo/alcance/etc. no painel de seleção. Pra colocar uma
 * torre de verdade no mapa, usa criarNova(prototipo), que devolve uma
 * instância nova do mesmo tipo (do jeito que ela nasce, sem melhoria
 * nenhuma).
 * 
 * Pra adicionar uma torre nova: cria a classe dela (extends Torre, igual
 * TacadorDeBolinha / Boxeador / BancoDeDinheiro), inclui uma instância na
 * lista DISPONIVEIS, e mais um "else if" em criarNova().
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Torres {
    
    private static final List<Torre> DISPONIVEIS = Arrays.asList(
        new BancoDeDinheiro(),
        new TacadorDeBolinha(),
        new Boxeador()
        // próximas torres entram aqui, um protótipo por linha
    );
    
    public static List<Torre> disponiveis() {
        return DISPONIVEIS;
    }
    
    /** Cria uma instância nova do mesmo tipo de um protótipo (ou de qualquer outra Torre). */
    public static Torre criarNova( Torre prototipo ) {
        
        if ( prototipo instanceof BancoDeDinheiro ) {
            return new BancoDeDinheiro();
        } else if ( prototipo instanceof TacadorDeBolinha ) {
            return new TacadorDeBolinha();
        } else if ( prototipo instanceof Boxeador ) {
            return new Boxeador();
        }
        
        throw new IllegalArgumentException( "tipo de torre desconhecido: " + prototipo.getNome() );
        
    }
    
}
