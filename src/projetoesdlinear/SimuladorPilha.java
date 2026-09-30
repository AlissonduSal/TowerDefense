package projetoesdlinear;

import aesd.ds.exceptions.EmptyStackException;
import aesd.ds.implementations.linear.LinkedStack;
import aesd.ds.interfaces.Stack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulador de pilha.
 * 
 * O visual é um único painel, como uma janela de terminal.
 * @author Prof. Dr. David Buzatto
 */
public class SimuladorPilha extends SimuladorBase {

    private static final double CENTRO_X = LARGURA_JANELA / 2.0;
    private static final double BASE_Y = REGUA_RODAPE_Y - 52;
    private static final double LIMITE_SUPERIOR_Y = HISTORICO_Y + MAX_HISTORICO * HISTORICO_LINHA + 30;

    private Stack<String> pilha;
    private double yEtiquetaTopo;

    public SimuladorPilha() {
        super( "Simulador de Pilha" );
    }

    @Override
    protected void inicializarEstrutura() {
        pilha = new LinkedStack<>();
        pilha.push( "a" );
        enderecos.push( gerarEndereco() );
        pilha.push( "b" );
        enderecos.push( gerarEndereco() );
        pilha.push( "c" );
        enderecos.push( gerarEndereco() );
        
        yEtiquetaTopo = yCentroNo( 0, pilha.getSize(), 1 );
        
        acoes.add(new TokenAcao("[ pop · shift+enter ]", this::remover));
        acoes.add(new TokenAcao("[ push · enter ]", this::adicionar));
        
        registrarComando( "push(\"a\")", false );
        registrarComando( "push(\"b\")", false );
        registrarComando( "push(\"c\")", false );
    }

    @Override
    protected double escalaAlvo() {
        int n = Math.max( 1, pilha.getSize() );
        double necessario = n * NO_ALTURA + ( n - 1 ) * NO_ESPACO;
        return Math.max( 0.4, Math.min( 1, ( BASE_Y - LIMITE_SUPERIOR_Y ) / necessario ) );
    }

    @Override
    protected void atualizarEtiqueta(double delta) {
        double yAlvo = pilha.isEmpty() ? BASE_Y - NO_ALTURA / 2.0 : yCentroNo( 0, pilha.getSize(), escala );
        yEtiquetaTopo += ( yAlvo - yEtiquetaTopo ) * Math.min( 1, delta * 16 );
    }

    @Override
    protected void adicionar() {
        String valor = textoCampo.trim();
        if ( valor.isEmpty() ) {
            registrarComando( "push() sem valor", true );
            return;
        }
        pilha.push( valor );
        enderecos.push( gerarEndereco() );
        textoCampo = "";
        tempoEntrada = 0;
        registrarComando( "push(\"" + valor + "\")", false );
    }

    @Override
    protected void remover() {
        try {
            NoSaindo saindo = new NoSaindo();
            saindo.escala = escala;
            saindo.y = yTopoNo( 0, pilha.getSize(), escala );
            
            String valor = pilha.pop();
            long endereco = enderecos.pop();
            
            saindo.valor = valor;
            saindo.endereco = endereco;
            nosSaindo.add( saindo );
            tempoEntrada = 1000;
            registrarComando( "pop() → \"" + valor + "\"", false );
        } catch ( EmptyStackException exc ) {
            registrarComando( "pop() em pilha vazia", true );
        }
    }

    @Override
    protected void atualizarVisao() {
        List<String> copia = new ArrayList<>();
        for ( String v : pilha ) {
            copia.add( v );
        }
        visao = copia;
        enderecosVisao = new ArrayList<>( enderecos );
    }

    @Override
    protected void desenharCabecalho() {
        double xEsq = FRAME_X + 18;
        double xDir = FRAME_X + FRAME_LARGURA - 18;
        double cy = FRAME_Y + CABECALHO_ALTURA / 2.0;
        
        textoCentroV( "pilha", xEsq, cy, 15, COR_TEXTO );
        double xDepois = xEsq + larguraTexto( "pilha", 15 ) + 12;
        textoCentroV( "lifo — linkedstack", xDepois, cy, 12, COR_TEXTO_SUAVE );
        
        String topo = visao.isEmpty() ? "-" : visao.get( 0 );
        String status = String.format( "tamanho %02d    topo %s", visao.size(), topo );
        textoCentroV( status, xDir - larguraTexto( status, 12 ), cy, 12, COR_TEXTO_SUAVE );
        
        linha( FRAME_X, REGUA_CABECALHO_Y, FRAME_X + FRAME_LARGURA, REGUA_CABECALHO_Y, COR_LINHA );
    }

    @Override
    protected void desenharEstrutura() {
        int n = visao.size();
        
        if ( n == 0 ) {
            desenharPilhaVazia();
        } else {
            for ( int i = n - 1; i >= 0; i-- ) {
                double y = yTopoNo( i, n, escala );
                double a = 1;
                if ( i == 0 && tempoEntrada < DURACAO_ENTRADA ) {
                    double e = easeOutCubic( tempoEntrada / DURACAO_ENTRADA );
                    y -= ( 1 - e ) * 14;
                    a = e;
                }
                
                desenharNo( visao.get( i ), CENTRO_X, y, escala, a, i == 0, i == n - 1 ? -1 : yTopoNo( i + 1, n, escala ), false );
                desenharEnderecoNo( enderecosVisao.get( i ), y, h( escala ), escala, a, CENTRO_X );
                
                if ( i < n - 1 ) {
                    double yProximo = yTopoNo( i + 1, n, escala );
                    desenharDelta( enderecosVisao.get( i ), enderecosVisao.get( i + 1 ), y + h( escala ), yProximo, escala, a );
                }
            }
            
            double yTopo = yTopoNo( 0, n, escala ) - 18;
            int x = (int) ( CENTRO_X - NO_LARGURA / 2 );
            int larguraDado = (int) Math.round( NO_LARGURA * PROPORCAO_DADO );
            texto( "dado", x + 10, yTopo, 10, COR_TEXTO_MUDO );
            texto( "anterior", x + larguraDado + 10, yTopo, 10, COR_TEXTO_MUDO );
            texto( "endereço (simulado)", CENTRO_X + NO_LARGURA / 2.0 + 16, yTopo, 10, COR_TEXTO_MUDO );
        }
        
        for ( NoSaindo s : nosSaindo ) {
            double e = easeOutCubic( s.tempo / DURACAO_SAIDA );
            double cx = CENTRO_X + e * 30;
            double a = 1 - e;
            desenharNo( s.valor, cx, s.y, s.escala, a, false, -1, true );
            desenharEnderecoNo( s.endereco, s.y, h( s.escala ), s.escala, a, cx );
        }
        
        desenharEtiquetaTopo();
    }

    private double h( double s ) {
        return NO_ALTURA * s;
    }

    private void desenharPilhaVazia() {
        double y = BASE_Y - NO_ALTURA / 2.0;
        int xNulo = (int) ( CENTRO_X - 90 );
        
        double xFimNulo = desenharNuloLateral( xNulo, y );
        textoCentroV( "pilha vazia", xFimNulo + 28, y, 13, COR_TEXTO_SUAVE );
    }

    private void desenharNo( String valor, double cx, double yTop, double s, double a, boolean ehTopo, double yProximo, boolean saindo ) {
        int w = NO_LARGURA;
        int h = (int) Math.round( NO_ALTURA * s );
        int x = (int) Math.round( cx - w / 2.0 );
        int y = (int) Math.round( yTop );
        int larguraDado = (int) Math.round( w * PROPORCAO_DADO );
        
        Color borda = ehTopo ? COR_DESTAQUE : ( saindo ? COR_TEXTO_MUDO : COR_LINHA_FORTE );
        
        fillRectangle( x, y, w, h, alfa( COR_SUPERFICIE, a ) );
        retangulo( x, y, w, h, ehTopo ? 2 : 1, alfa( borda, a ) );
        linha( x + larguraDado, y, x + larguraDado, y + h, alfa( borda, a * 0.7 ) );
        
        int tamanho = Math.max( 9, Math.min( 18, (int) ( h * 0.45 ) ) );
        String texto = valor;
        while ( tamanho > 9 && larguraTexto( texto, tamanho ) > larguraDado - 20 ) {
            tamanho--;
        }
        textoCentro( texto, x + larguraDado / 2.0, y + h / 2.0, tamanho, alfa( saindo ? COR_TEXTO_SUAVE : COR_TEXTO, a ) );
        
        double xp = Math.round( x + larguraDado + ( w - larguraDado ) / 2.0 ) + 0.5;
        double yp = y + h / 2.0;
        fillCircle( xp, yp, 3, alfa( COR_TEXTO_SUAVE, a ) );
        
        if ( saindo ) {
            return;
        }
        
        if ( yProximo >= 0 ) {
            desenharSeta( xp, yp, xp, Math.round( yProximo ), alfa( COR_TEXTO_SUAVE, a ) );
        } else {
            double yNulo = y + h + 24;
            linha( xp, yp, xp, yNulo, alfa( COR_TEXTO_SUAVE, a ) );
            desenharNulo( xp, yNulo );
        }
    }

    private void desenharEnderecoNo( long endereco, double yTop, double alturaNo, double s, double a, double cx ) {
        double x = cx + NO_LARGURA / 2.0 + 16;
        double y = yTop + alturaNo / 2.0;
        int tamanho = Math.max( 8, Math.min( 11, (int) Math.round( 11 * s ) ) );
        textoCentroV( "@" + formatarEndereco( endereco ), x, y, tamanho, alfa( COR_TEXTO_SUAVE, a ) );
    }

    private void desenharDelta( long enderecoA, long enderecoB, double yTop, double yBase, double s, double a ) {
        double x = CENTRO_X + NO_LARGURA / 2.0 + 16;
        double y = ( yTop + yBase ) / 2.0;
        long diferenca = Math.abs( enderecoA - enderecoB );
        int tamanho = Math.max( 8, Math.min( 10, (int) Math.round( 10 * s ) ) );
        
        String texto = "Δ " + formatarMilhar( diferenca );
        textoCentroV( texto, x, y, tamanho, alfa( COR_TEXTO_MUDO, a ) );
    }

    private void desenharEtiquetaTopo() {
        boolean vazia = visao.isEmpty();
        double y = Math.round( yEtiquetaTopo ) + 0.5;
        double xPonta = vazia ? CENTRO_X - 90 - 7 : CENTRO_X - NO_LARGURA / 2.0 - 1;
        
        linha( xPonta - 40, y, xPonta - 5, y, COR_DESTAQUE );
        fillTriangle( xPonta, y, xPonta - 8, y - 4, xPonta - 8, y + 4, COR_DESTAQUE );
        
        double w = larguraTexto( "topo", 13 );
        textoCentroV( "topo", xPonta - 40 - 10 - w, y, 13, COR_DESTAQUE );
    }

    private void desenharNulo( double x, double y ) {
        linha( x - 8, y, x + 8, y, COR_TEXTO_SUAVE );
        linha( x - 5, y + 4, x + 5, y + 4, COR_TEXTO_SUAVE );
        linha( x - 2, y + 8, x + 2, y + 8, COR_TEXTO_SUAVE );
        texto( "null", x + 18, y - 1, 12, COR_TEXTO_SUAVE );
    }

    private void desenharSeta( double x1, double y1, double x2, double y2, Color cor ) {
        linha( x1, y1, x2, y2 - 6, cor );
        fillTriangle( x2, y2, x2 - 4, y2 - 8, x2 + 4, y2 - 8, cor );
    }

    private double yTopoNo( int i, int n, double s ) {
        int deBaixo = n - 1 - i;
        return BASE_Y - deBaixo * ( NO_ALTURA + NO_ESPACO ) * s - NO_ALTURA * s;
    }

    private double yCentroNo( int i, int n, double s ) {
        return yTopoNo( i, n, s ) + NO_ALTURA * s / 2;
    }
    
    public static void main( String[] args ) {
        new SimuladorPilha();
    }
}
