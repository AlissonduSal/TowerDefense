package projetoesdlinear;

/**
 * Banco de dinheiro.
 * 
 * Não ataca (dano, alcance e velocidade de ataque da classe base ficam em
 * 0 e não são usados) -- em vez disso, gera uma quantia de dinheiro a cada
 * alguns segundos. Ocupa 2 células só pra ficar visualmente diferente das
 * outras torres (1 célula).
 * 
 * Tabela de melhorias (ver o PDF):
 *     nível 1: -1 segundo pra gerar dinheiro
 *     nível 2: -1 segundo pra gerar dinheiro
 *     nível 3: destrava o empréstimo
 *     nível 4: -2 segundos pra gerar dinheiro
 *     nível 5: destrava um desconto de 50% numa próxima compra de torre
 *              (só pode ser usado uma vez)
 * 
 * O empréstimo e a aplicação do desconto na compra de outra torre ainda
 * não têm a mecânica de jogo ligada (dependem do sistema de economia do
 * jogador, que ainda não existe) -- aqui só ficam os métodos que dizem se
 * já estão destravados.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class BancoDeDinheiro extends Torre {
    
    private static final int DINHEIRO_GERADO_BASE = 5;
    private static final double TEMPO_GERACAO_BASE = 5;
    private static final double DESCONTO_NIVEL_5 = 0.5;
    
    // quanto o tempo de geração diminui em cada nível, em ordem (índice 0 = nível 1)
    private static final double[] REDUCAO_DE_TEMPO_POR_NIVEL = { 1, 1, 0, 2, 0 };
    
    private boolean descontoJaUsado;
    
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
    
    /** Quanto dinheiro o banco gera a cada ciclo (não muda com o nível, só o tempo muda). */
    public int getDinheiroGerado() {
        return DINHEIRO_GERADO_BASE;
    }
    
    /** De quanto em quanto tempo (segundos) o banco gera dinheiro, já considerando o nível. */
    public double getTempoGeracao() {
        
        double total = TEMPO_GERACAO_BASE;
        for ( int i = 0; i < getNivel(); i++ ) {
            total -= REDUCAO_DE_TEMPO_POR_NIVEL[i];
        }
        return total;
        
    }
    
    /** A partir do nível 3, o banco destrava a possibilidade de pegar empréstimo. */
    public boolean temEmprestimoDisponivel() {
        return getNivel() >= 3;
    }
    
    /** Se ainda existe o desconto de 50% do nível 5 disponível pra usar (só uma vez). */
    public boolean temDescontoDisponivel() {
        return getNivel() >= 5 && !descontoJaUsado;
    }
    
    /**
     * Consome o desconto do nível 5 e devolve a fração de desconto (0.5 =
     * 50%). Se não tiver desconto disponível, devolve 0 e não consome nada.
     */
    public double usarDesconto() {
        
        if ( !temDescontoDisponivel() ) {
            return 0;
        }
        
        descontoJaUsado = true;
        return DESCONTO_NIVEL_5;
        
    }
    
}
