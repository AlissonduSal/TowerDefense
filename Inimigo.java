package projetoesdlinear;

/**
 * Um inimigo do jogo.
 * 
 * vidaBase, velocidadeBase e danoBase são os atributos "crus" do tipo do
 * inimigo, valendo pra horda 1 (sem melhoria nenhuma). A cada horda que
 * passa, o inimigo fica mais forte: aplicarMelhoriaDeHorda(horda) escala
 * os três atributos de acordo com o número da horda -- é assim que o jogo
 * fica mais difícil com o tempo, sem precisar criar um tipo de inimigo
 * novo pra cada horda.
 * 
 * getVidaMaxima() / getVelocidade() / getDano() já retornam o valor final,
 * escalado pela horda atual.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Inimigo {
    
    // posição "não posicionado" -- inimigo ainda não colocado no mapa
    public static final int SEM_POSICAO = -1;
    
    // quanto vida/velocidade/dano crescem a cada horda além da primeira
    // (0.15 = 15% a mais por horda)
    private static final double FATOR_MELHORIA_POR_HORDA = 0.15;
    
    private final String nome;
    private final int vidaBase;
    private final double velocidadeBase;
    private final int danoBase;
    
    private int horda;
    
    private int vidaMaxima;
    private int vidaAtual;
    private double velocidade;
    private int dano;
    
    private int coluna = SEM_POSICAO;
    private int linha = SEM_POSICAO;
    
    public Inimigo( String nome, int vidaBase, double velocidadeBase, int danoBase ) {
        
        this.nome = nome;
        this.vidaBase = vidaBase;
        this.velocidadeBase = velocidadeBase;
        this.danoBase = danoBase;
        
        aplicarMelhoriaDeHorda( 1 );
        
    }
    
    /**
     * Escala vida, velocidade e dano de acordo com o número da horda (a
     * horda 1 não tem bônus nenhum -- é exatamente o valor base). Chamado
     * uma vez, quando o inimigo nasce pra uma horda; final porque é
     * chamado direto do construtor.
     */
    public final void aplicarMelhoriaDeHorda( int horda ) {
        
        this.horda = horda;
        double multiplicador = 1 + FATOR_MELHORIA_POR_HORDA * ( horda - 1 );
        
        vidaMaxima = (int) Math.round( vidaBase * multiplicador );
        vidaAtual = vidaMaxima;
        velocidade = velocidadeBase * multiplicador;
        dano = (int) Math.round( danoBase * multiplicador );
        
    }
    
    /** Tira vida do inimigo (ex.: quando uma torre acerta ele). Nunca deixa a vida negativa. */
    public void receberDano( int quantidade ) {
        vidaAtual = Math.max( 0, vidaAtual - quantidade );
    }
    
    public boolean estaMorto() {
        return vidaAtual <= 0;
    }
    
    public void posicionarEm( int coluna, int linha ) {
        this.coluna = coluna;
        this.linha = linha;
    }
    
    public boolean estaPosicionado() {
        return coluna != SEM_POSICAO && linha != SEM_POSICAO;
    }
    
    public int getColuna() {
        return coluna;
    }
    
    public int getLinha() {
        return linha;
    }
    
    public String getNome() {
        return nome;
    }
    
    /** Número da horda em que este inimigo nasceu (define o quanto ele foi melhorado). */
    public int getHorda() {
        return horda;
    }
    
    public int getVidaMaxima() {
        return vidaMaxima;
    }
    
    public int getVidaAtual() {
        return vidaAtual;
    }
    
    public double getVelocidade() {
        return velocidade;
    }
    
    /** Dano que este inimigo causa à base do jogador se alcançar o fim do caminho. */
    public int getDano() {
        return dano;
    }
    
}
