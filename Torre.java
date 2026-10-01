package projetoesdlinear;

import aesd.ds.exceptions.EmptyStackException;
import aesd.ds.implementations.linear.LinkedStack;
import aesd.ds.interfaces.Stack;

/**
 * Uma torre do jogo.
 * 
 * A melhoria de uma torre não é um valor abstrato -- é feita empilhando
 * OUTRA torre do mesmo tipo (mesmo nome) em cima dela: aplicarMelhoria()
 * exige uma Torre igual (ex.: um "Tacador de bolinha" só melhora com outro
 * "Tacador de bolinha"), e ela entra na pilha de melhorias.
 * 
 * getNivel() é simplesmente o tamanho dessa pilha (quantas vezes a torre já
 * foi melhorada, de 0 até getNivelMaximo()) -- é esse número que cada
 * subclasse usa pra decidir o bônus daquele nível, porque cada torre real
 * (Tacador de bolinha, Boxeador, Banco de dinheiro...) tem sua própria
 * tabela de melhorias, bem diferente uma da outra. Por isso getDano(),
 * getAlcance() e getVelocidadeAtaque() aqui na classe base só devolvem o
 * valor cru (nível 0) -- são as subclasses que sobrescrevem pra aplicar a
 * tabela de cada uma.
 * 
 * removerUltimaMelhoria() desfaz só a melhoria mais recente (LIFO) e
 * devolve a própria Torre que estava empilhada -- ela volta a ser uma
 * torre normal, pronta pra ser posicionada no mapa de novo.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Torre {
    
    // posição "não posicionada" -- torre ainda não colocada no mapa
    public static final int SEM_POSICAO = -1;
    
    private final String nome;
    private final int custoBase;
    protected final double alcanceBase;
    protected final int danoBase;
    protected final double velocidadeAtaqueBase;
    
    // quantas células da malha a torre ocupa (1 = uma célula 1x1)
    private final int espacoOcupado;
    
    // pilha de torres do mesmo tipo usadas como melhoria -- seu tamanho É o nível da torre
    private final Stack<Torre> melhorias;
    
    private int coluna = SEM_POSICAO;
    private int linha = SEM_POSICAO;
    
    // tempo (segundos) até esta torre poder atacar de novo
    private double cooldownAtaque;
    
    public Torre( String nome, int custoBase, double alcanceBase, int danoBase,
            double velocidadeAtaqueBase, int espacoOcupado ) {
        
        this.nome = nome;
        this.custoBase = custoBase;
        this.alcanceBase = alcanceBase;
        this.danoBase = danoBase;
        this.velocidadeAtaqueBase = velocidadeAtaqueBase;
        this.espacoOcupado = espacoOcupado;
        this.melhorias = new LinkedStack<>();
        
    }
    
    /** Duas torres são do mesmo tipo (podem se fundir) se tiverem o mesmo nome. */
    public boolean ehMesmoTipo( Torre outraTorre ) {
        return nome.equals( outraTorre.nome );
    }
    
    /**
     * Melhora esta torre empilhando outraTorre em cima dela. outraTorre
     * precisa ser do mesmo tipo (mesmo nome) desta torre, e esta torre não
     * pode já estar no nível máximo.
     * 
     * @throws IllegalArgumentException se outraTorre não for do mesmo tipo,
     *         ou se for a própria torre
     * @throws IllegalStateException se esta torre já estiver no nível máximo
     */
    public void aplicarMelhoria( Torre outraTorre ) {
        
        if ( outraTorre == this ) {
            throw new IllegalArgumentException( "uma torre não pode melhorar a si mesma" );
        }
        
        if ( !ehMesmoTipo( outraTorre ) ) {
            throw new IllegalArgumentException(
                    "só dá pra melhorar a torre \"" + nome + "\" com outra torre \"" + nome
                    + "\" -- essa aqui é \"" + outraTorre.nome + "\"" );
        }
        
        if ( getNivel() >= getNivelMaximo() ) {
            throw new IllegalStateException( "a torre \"" + nome + "\" já está no nível máximo" );
        }
        
        melhorias.push( outraTorre );
        
    }
    
    /**
     * Desfaz só a melhoria mais recente (desempilha) e devolve a própria
     * Torre que estava empilhada, pronta pra ser posicionada no mapa de
     * novo (ainda com a posição antiga marcada como SEM_POSICAO). Retorna
     * null se esta torre não tiver nenhuma melhoria aplicada.
     */
    public Torre removerUltimaMelhoria() {
        
        if ( melhorias.isEmpty() ) {
            return null;
        }
        
        try {
            return melhorias.pop();
        } catch ( EmptyStackException exc ) {
            return null;
        }
        
    }
    
    /** Avança o cooldown de ataque desta torre. Chamado a cada frame pelo jogo. */
    public void atualizarCooldown( double delta ) {
        if ( cooldownAtaque > 0 ) {
            cooldownAtaque -= delta;
        }
    }
    
    public boolean podeAtacar() {
        return cooldownAtaque <= 0;
    }
    
    /** Reinicia o cooldown depois de atacar, com base na velocidade de ataque atual. */
    public void registrarAtaque() {
        cooldownAtaque = 1.0 / getVelocidadeAtaque();
    }
    
    public void posicionarEm( int coluna, int linha ) {
        this.coluna = coluna;
        this.linha = linha;
    }
    
    public boolean estaPosicionada() {
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
    
    public int getCustoBase() {
        return custoBase;
    }
    
    /** Quantas vezes esta torre já foi melhorada (0 até getNivelMaximo()). */
    public int getNivel() {
        return melhorias.getSize();
    }
    
    /** Quantidade de níveis de melhoria que esta torre tem. Todas as torres do PDF têm 5. */
    public int getNivelMaximo() {
        return 5;
    }
    
    /**
     * Alcance da torre neste nível. Na classe base é sempre o valor cru
     * (sem melhoria nenhuma) -- cada torre real sobrescreve se a tabela
     * dela mexer em alcance.
     */
    public double getAlcance() {
        return alcanceBase;
    }
    
    /**
     * Dano da torre neste nível. Na classe base é sempre o valor cru --
     * cada torre real sobrescreve se a tabela dela mexer em dano.
     */
    public int getDano() {
        return danoBase;
    }
    
    /**
     * Velocidade de ataque da torre neste nível. Na classe base é sempre o
     * valor cru -- cada torre real sobrescreve se a tabela dela mexer em
     * velocidade de ataque.
     */
    public double getVelocidadeAtaque() {
        return velocidadeAtaqueBase;
    }
    
    public int getEspacoOcupado() {
        return espacoOcupado;
    }
    
}
