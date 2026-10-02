package projetoesdlinear;

/**
 * Banco de dinheiro.
 * 
 * Não ataca (dano, alcance e velocidade de ataque da classe base ficam em
 * 0 e não são usados) -- em vez disso, gera uma quantia de dinheiro a cada
 * alguns segundos. Ocupa 2 células só pra ficar visualmente diferente das
 * outras torres (1 célula).
 * 
 * Tabela de melhorias (balanceada, diferente do PDF original):
 *     nível 1: -1 segundo pra gerar dinheiro
 *     nível 2: -1 segundo pra gerar dinheiro
 *     nível 3: +5 no dinheiro gerado a cada ciclo
 *     nível 4: -2 segundos pra gerar dinheiro
 *     nível 5: desconto de 50% em QUALQUER compra de torre, o jogo
 *              inteiro, enquanto o banco estiver neste nível (não é mais
 *              um uso único) -- ver temDescontoAtivo()
 * 
 * O desconto do nível 5 é só a informação "está ativo ou não"; quem lê
 * isso e realmente abate o preço na hora de comprar é o JogoTorreDefesa
 * (ele soma temDescontoAtivo() de todos os bancos no mapa).
 * 
 * @author Prof. Dr. David Buzatto
 */
public class BancoDeDinheiro extends Torre {
    
    private static final int DINHEIRO_GERADO_BASE = 5;
    private static final double TEMPO_GERACAO_BASE = 5;
    private static final int BONUS_DINHEIRO_NIVEL_3 = 5;
    public static final double FRACAO_DESCONTO_NIVEL_5 = 0.5;
    
    // quanto o tempo de geração diminui em cada nível, em ordem (índice 0 = nível 1)
    private static final double[] REDUCAO_DE_TEMPO_POR_NIVEL = { 1, 1, 0, 2, 0 };
    
    // tempo (segundos) até o banco gerar dinheiro de novo
    private double cooldownGeracao = TEMPO_GERACAO_BASE;
    
    public BancoDeDinheiro() {
        super( "Banco de dinheiro", 20, 0, 0, 0, 2 );
    }
    
    /** Avança o cooldown de geração de dinheiro. Chamado a cada frame pelo jogo. */
    public void atualizarCooldownGeracao( double delta ) {
        if ( cooldownGeracao > 0 ) {
            cooldownGeracao -= delta;
        }
    }
    
    public boolean prontoParaGerar() {
        return cooldownGeracao <= 0;
    }
    
    /** Reinicia o cooldown depois de gerar dinheiro, com base no tempo de geração atual. */
    public void registrarGeracao() {
        cooldownGeracao = getTempoGeracao();
    }
    
    /** Quanto dinheiro o banco gera a cada ciclo: base + 5 a partir do nível 3. */
    public int getDinheiroGerado() {
        
        int total = DINHEIRO_GERADO_BASE;
        if ( getNivel() >= 3 ) {
            total += BONUS_DINHEIRO_NIVEL_3;
        }
        return total;
        
    }
    
    /** De quanto em quanto tempo (segundos) o banco gera dinheiro, já considerando o nível. */
    public double getTempoGeracao() {
        
        double total = TEMPO_GERACAO_BASE;
        for ( int i = 0; i < getNivel(); i++ ) {
            total -= REDUCAO_DE_TEMPO_POR_NIVEL[i];
        }
        return total;
        
    }
    
    /** A partir do nível 5, este banco dá 50% de desconto em qualquer torre comprada. */
    public boolean temDescontoAtivo() {
        return getNivel() >= 5;
    }
    
}
