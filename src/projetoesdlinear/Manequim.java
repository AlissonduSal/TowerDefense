package projetoesdlinear;

/**
 * Manequim de teste.
 * 
 * Não é um inimigo de horda de verdade -- é só um alvo parado, com bastante
 * vida, que não causa dano nenhum ao jogador. Serve pra testar se as
 * torres estão acertando, com que alcance e quanto dano estão causando,
 * sem precisar esperar uma horda de verdade passar.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Manequim extends Inimigo {
    
    private static final int VIDA_DO_MANEQUIM = 500;
    
    public Manequim( String nome ) {
        // velocidade e dano zerados: o manequim não anda e não causa dano
        super( nome, VIDA_DO_MANEQUIM, 0, 0 );
    }
    
}
