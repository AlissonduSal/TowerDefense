package projetoesdlinear;

/**
 * Boxeador.
 * 
 * Torre corpo a corpo (alcance de só 1 quadrado) que foca em bater muito
 * rápido. A partir do nível 3 ganha uma chance de stunar o inimigo por
 * alguns segundos a cada golpe; no nível 5 ganha uma chance (bem menor)
 * de nocautear o inimigo na hora (hit kill, não funciona em inimigos com
 * capacidades especiais).
 * 
 * Tabela de melhorias (ver o PDF):
 *     nível 1: +1 velocidade de ataque
 *     nível 2: +1 velocidade de ataque
 *     nível 3: chance de stunar o inimigo por 2 segundos
 *     nível 4: +2 velocidade de ataque
 *     nível 5: chance (menor que a do stun) de nocautear o inimigo
 * 
 * O PDF não define os números exatos das chances de stun/nocaute -- os
 * valores abaixo (CHANCE_DE_STUN / CHANCE_DE_NOCAUTE) são um ponto de
 * partida razoável pra ajustar depois de testar o jogo.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Boxeador extends Torre {
    
    private static final double CHANCE_DE_STUN = 0.20;
    private static final double CHANCE_DE_NOCAUTE = 0.05;
    private static final double DURACAO_STUN_SEGUNDOS = 2;
    
    // quanto a velocidade de ataque aumenta em cada nível, em ordem (índice 0 = nível 1)
    private static final double[] GANHO_DE_VELOCIDADE_POR_NIVEL = { 1, 1, 0, 2, 0 };
    
    public Boxeador() {
        super( "Boxeador", 40, 1, 1, 2, 1 );
    }
    
    @Override
    public double getVelocidadeAtaque() {
        
        double total = velocidadeAtaqueBase;
        for ( int i = 0; i < getNivel(); i++ ) {
            total += GANHO_DE_VELOCIDADE_POR_NIVEL[i];
        }
        return total;
        
    }
    
    /** Chance (0 a 1) de cada golpe stunar o inimigo. 0 antes do nível 3. */
    public double getChanceDeStun() {
        return getNivel() >= 3 ? CHANCE_DE_STUN : 0;
    }
    
    public double getDuracaoStunSegundos() {
        return DURACAO_STUN_SEGUNDOS;
    }
    
    /** Chance (0 a 1) de cada golpe nocautear (hit kill) o inimigo. 0 antes do nível 5. */
    public double getChanceDeNocaute() {
        return getNivel() >= 5 ? CHANCE_DE_NOCAUTE : 0;
    }
    
}
