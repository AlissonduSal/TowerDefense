package projetoesdlinear;

import java.util.List;

/**
 * Um inimigo do jogo.
 * 
 * vidaBase, velocidadeBase e danoBase são os atributos "crus" do tipo do
 * inimigo, valendo pra horda 1 (sem melhoria nenhuma).
 */
public class Inimigo {
    
    public static final int SEM_POSICAO = -1;
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
    
    // Movimentação suave
    private double xReal = SEM_POSICAO;
    private double yReal = SEM_POSICAO;
    private List<Coordenada> caminho;
    private int tamanhoTile;
    private int indiceWaypointAtual = 0;
    
    public Inimigo(String nome, int vidaBase, double velocidadeBase, int danoBase) {
        this.nome = nome;
        this.vidaBase = vidaBase;
        this.velocidadeBase = velocidadeBase;
        this.danoBase = danoBase;
        aplicarMelhoriaDeHorda(1);
    }
    
    public final void aplicarMelhoriaDeHorda(int horda) {
        this.horda = horda;
        double multiplicador = 1 + FATOR_MELHORIA_POR_HORDA * (horda - 1);
        vidaMaxima = (int) Math.round(vidaBase * multiplicador);
        vidaAtual = vidaMaxima;
        velocidade = velocidadeBase * multiplicador;
        dano = (int) Math.round(danoBase * multiplicador);
    }
    
    public void setCaminho(List<Coordenada> waypoints, int tamanhoTile) {
        this.caminho = waypoints;
        this.tamanhoTile = tamanhoTile;
        this.indiceWaypointAtual = 0;
        
        if (waypoints != null && !waypoints.isEmpty()) {
            Coordenada inicio = waypoints.get(0);
            this.coluna = inicio.coluna;
            this.linha = inicio.linha;
            this.xReal = inicio.coluna * tamanhoTile + tamanhoTile / 2.0;
            this.yReal = inicio.linha * tamanhoTile + tamanhoTile / 2.0;
        }
    }
    
    /**
     * @return true se chegou ao fim do caminho
     */
    public boolean atualizarMovimento(double delta) {
        if (caminho == null || indiceWaypointAtual >= caminho.size() - 1) return false;
        if (velocidade <= 0) return false; // Manequim não anda
        
        Coordenada proximo = caminho.get(indiceWaypointAtual + 1);
        double alvoX = proximo.coluna * tamanhoTile + tamanhoTile / 2.0;
        double alvoY = proximo.linha * tamanhoTile + tamanhoTile / 2.0;
        
        double dx = alvoX - xReal;
        double dy = alvoY - yReal;
        double distancia = Math.sqrt(dx * dx + dy * dy);
        
        double passo = velocidade * delta;
        
        if (passo >= distancia) {
            xReal = alvoX;
            yReal = alvoY;
            coluna = proximo.coluna;
            linha = proximo.linha;
            indiceWaypointAtual++;
            
            if (indiceWaypointAtual >= caminho.size() - 1) {
                return true; // Acabou o caminho, ataca a base
            }
        } else {
            xReal += (dx / distancia) * passo;
            yReal += (dy / distancia) * passo;
            
            coluna = (int) (xReal / tamanhoTile);
            linha = (int) (yReal / tamanhoTile);
        }
        return false;
    }
    
    public void receberDano(int quantidade) {
        vidaAtual = Math.max(0, vidaAtual - quantidade);
    }
    
    public boolean estaMorto() { return vidaAtual <= 0; }
    
    public void posicionarEm(int coluna, int linha) {
        this.coluna = coluna;
        this.linha = linha;
        this.xReal = coluna * 40 + 20.0; // aproximado, ajustado no setter
        this.yReal = linha * 40 + 20.0;
    }
    
    public boolean estaPosicionado() {
        return coluna != SEM_POSICAO && linha != SEM_POSICAO;
    }
    
    public int getColuna() { return coluna; }
    public int getLinha() { return linha; }
    public double getXReal() { return xReal; }
    public double getYReal() { return yReal; }
    
    public String getNome() { return nome; }
    public int getHorda() { return horda; }
    public int getVidaMaxima() { return vidaMaxima; }
    public int getVidaAtual() { return vidaAtual; }
    public double getVelocidade() { return velocidade; }
    public int getDano() { return dano; }
}
