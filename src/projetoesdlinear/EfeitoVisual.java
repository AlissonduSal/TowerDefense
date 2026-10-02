package projetoesdlinear;

/**
 * Um efeito visual temporário -- uma bolinha voando, um impacto de soco,
 * as estrelinhas do stun, o "K.O." do nocaute, o "+5" flutuante do banco.
 * Dura um tempo fixo na tela e depois é descartado. É puramente visual,
 * não afeta a lógica do jogo (dano já foi aplicado antes de criar o
 * efeito).
 * 
 * @author Prof. Dr. David Buzatto
 */
public class EfeitoVisual {
    
    public enum Tipo {
        PROJETIL_BOLINHA,
        IMPACTO_SOCO,
        STUN,
        NOCAUTE,
        DINHEIRO_GERADO
    }
    
    public final Tipo tipo;
    public final double origemX;
    public final double origemY;
    public final double destinoX;
    public final double destinoY;
    public final double duracaoTotal;
    
    // usado só pelo DINHEIRO_GERADO, pra não precisar de uma subclasse só por isso
    public String texto;
    
    private double tempoRestante;
    
    public EfeitoVisual( Tipo tipo, double origemX, double origemY,
            double destinoX, double destinoY, double duracaoTotal ) {
        
        this.tipo = tipo;
        this.origemX = origemX;
        this.origemY = origemY;
        this.destinoX = destinoX;
        this.destinoY = destinoY;
        this.duracaoTotal = duracaoTotal;
        this.tempoRestante = duracaoTotal;
        
    }
    
    public void atualizar( double delta ) {
        tempoRestante -= delta;
    }
    
    public boolean terminou() {
        return tempoRestante <= 0;
    }
    
    /** De 0 (acabou de nascer) a 1 (prestes a desaparecer). */
    public double getProgresso() {
        return 1 - Math.max( 0, Math.min( 1, tempoRestante / duracaoTotal ) );
    }
    
}
