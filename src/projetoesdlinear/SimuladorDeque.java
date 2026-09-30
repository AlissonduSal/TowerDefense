package projetoesdlinear;

import aesd.ds.exceptions.EmptyDequeException;
import aesd.ds.implementations.linear.DoublyLinkedDeque;
import aesd.ds.interfaces.Deque;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulador de deque:
 *     Simula as operações de inserir no início e no fim e remover do início
 *     e do fim de uma deque encadeada/ligada/dinâmica.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class SimuladorDeque extends SimuladorBase {

    private static final double CENTRO_Y = 350;
    
    private static final double ORIGEM_X = FRAME_X + 120;
    private static final double LIMITE_DIREITO_X = FRAME_X + FRAME_LARGURA - 110;

    private Deque<String> deque;
    private double xEtiquetaFim;
    private double xEtiquetaFrente;

    public SimuladorDeque() {
        super( "Simulador de Deque" );
    }

    @Override
    protected void inicializarEstrutura() {
        deque = new DoublyLinkedDeque<>();
        deque.addLast( "a" );
        enderecos.addLast( gerarEndereco() );
        deque.addLast( "b" );
        enderecos.addLast( gerarEndereco() );
        deque.addLast( "c" );
        enderecos.addLast( gerarEndereco() );
        
        xEtiquetaFim = xCentroNo( deque.getSize() - 1, deque.getSize(), 1 );
        xEtiquetaFrente = xCentroNo( 0, deque.getSize(), 1 );
        
        acoes.add(new TokenAcao("[ removeLast ]", this::removerFim));
        acoes.add(new TokenAcao("[ removeFirst · shift+enter ]", this::removerInicio));
        acoes.add(new TokenAcao("[ addLast ]", this::adicionarFim));
        acoes.add(new TokenAcao("[ addFirst · enter ]", this::adicionarInicio));
        
        registrarComando( "addLast(\"a\")", false );
        registrarComando( "addLast(\"b\")", false );
        registrarComando( "addLast(\"c\")", false );
    }

    @Override
    protected void processarAtalhos() {
        if ( isKeyPressed( KEY_ENTER ) ) {
            if ( isKeyDown( KEY_SHIFT ) ) {
                removerInicio();
            } else {
                adicionarInicio();
            }
        }
    }

    @Override
    protected double escalaAlvo() {
        int n = Math.max( 1, deque.getSize() );
        double necessario = n * NO_LARGURA + ( n - 1 ) * NO_ESPACO;
        return Math.max( 0.3, Math.min( 1, ( LIMITE_DIREITO_X - ORIGEM_X ) / necessario ) );
    }

    @Override
    protected void atualizarEtiqueta(double delta) {
        double xFimAlvo = deque.isEmpty() ? ORIGEM_X : xCentroNo( deque.getSize() - 1, deque.getSize(), escala );
        xEtiquetaFim += ( xFimAlvo - xEtiquetaFim ) * Math.min( 1, delta * 16 );
        
        double xFrenteAlvo = deque.isEmpty() ? ORIGEM_X : xCentroNo( 0, deque.getSize(), escala );
        xEtiquetaFrente += ( xFrenteAlvo - xEtiquetaFrente ) * Math.min( 1, delta * 16 );
    }

    private void adicionarInicio() {
        String valor = textoCampo.trim();
        if ( valor.isEmpty() ) {
            registrarComando( "addFirst() sem valor", true );
            return;
        }
        deque.addFirst( valor );
        enderecos.addFirst( gerarEndereco() );
        textoCampo = "";
        tempoEntrada = 0;
        registrarComando( "addFirst(\"" + valor + "\")", false );
    }
    
    private void adicionarFim() {
        String valor = textoCampo.trim();
        if ( valor.isEmpty() ) {
            registrarComando( "addLast() sem valor", true );
            return;
        }
        deque.addLast( valor );
        enderecos.addLast( gerarEndereco() );
        textoCampo = "";
        tempoEntrada = 0;
        registrarComando( "addLast(\"" + valor + "\")", false );
    }
    
    @Override
    protected void adicionar() {}

    @Override
    protected void remover() {}

    private void removerInicio() {
        try {
            NoSaindo saindo = new NoSaindo();
            saindo.escala = escala;
            saindo.x = xEsquerdaNo( 0, deque.getSize(), escala );
            saindo.y = CENTRO_Y;
            
            String valor = deque.removeFirst();
            long endereco = enderecos.removeFirst();
            
            saindo.valor = valor;
            saindo.endereco = endereco;
            nosSaindo.add( saindo );
            tempoEntrada = 1000;
            registrarComando( "removeFirst() → \"" + valor + "\"", false );
        } catch ( EmptyDequeException exc ) {
            registrarComando( "removeFirst() em deque vazia", true );
        }
    }
    
    private void removerFim() {
        try {
            NoSaindo saindo = new NoSaindo();
            saindo.escala = escala;
            saindo.x = xEsquerdaNo( Math.max(0, deque.getSize()-1), deque.getSize(), escala );
            saindo.y = CENTRO_Y;
            
            String valor = deque.removeLast();
            long endereco = enderecos.removeLast();
            
            saindo.valor = valor;
            saindo.endereco = endereco;
            nosSaindo.add( saindo );
            tempoEntrada = 1000;
            registrarComando( "removeLast() → \"" + valor + "\"", false );
        } catch ( EmptyDequeException exc ) {
            registrarComando( "removeLast() em deque vazia", true );
        }
    }

    @Override
    protected void atualizarVisao() {
        List<String> copia = new ArrayList<>();
        for ( String v : deque ) {
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
        
        textoCentroV( "deque", xEsq, cy, 15, COR_TEXTO );
        double xDepois = xEsq + larguraTexto( "deque", 15 ) + 12;
        textoCentroV( "doubly-linked deque", xDepois, cy, 12, COR_TEXTO_SUAVE );
        
        String frenteTxt = visao.isEmpty() ? "-" : visao.get( 0 );
        String fimTxt = visao.isEmpty() ? "-" : visao.get( visao.size() - 1 );
        String status = String.format( "tamanho %02d    início %s    fim %s", visao.size(), frenteTxt, fimTxt );
        textoCentroV( status, xDir - larguraTexto( status, 12 ), cy, 12, COR_TEXTO_SUAVE );
        
        linha( FRAME_X, REGUA_CABECALHO_Y, FRAME_X + FRAME_LARGURA, REGUA_CABECALHO_Y, COR_LINHA );
    }

    @Override
    protected void desenharEstrutura() {
        int n = visao.size();
        
        if ( n == 0 ) {
            desenharDequeVazia();
        } else {
            for ( int i = 0; i < n; i++ ) {
                double xEsq = xEsquerdaNo( i, n, escala );
                double a = 1;
                
                double xProximo = ( i == n - 1 ) ? -1 : xEsquerdaNo( i + 1, n, escala );
                double xAnterior = ( i == 0 ) ? -1 : xEsquerdaNo( i - 1, n, escala ) + NO_LARGURA * escala;
                
                desenharNo( visao.get( i ), CENTRO_Y, xEsq, escala, a, (i == 0 || i == n - 1), xProximo, xAnterior, false );
                
                double cxNo = xEsq + NO_LARGURA * escala / 2.0;
                desenharEnderecoNo( enderecosVisao.get( i ), cxNo, escala, a );
                
                if ( i < n - 1 && escala > 0.55 ) {
                    double cxDepois = xEsquerdaNo( i + 1, n, escala ) + NO_LARGURA * escala / 2.0;
                    desenharDelta( enderecosVisao.get( i ), enderecosVisao.get( i + 1 ), ( cxNo + cxDepois ) / 2.0, escala, a );
                }
            }
            
            double yHeader = CENTRO_Y - NO_ALTURA / 2.0 - 18;
            int wAnt = (int) Math.round( NO_LARGURA * escala * 0.25 );
            int wDado = (int) Math.round( NO_LARGURA * escala * 0.5 );
            texto( "ant", ORIGEM_X + 10, yHeader, 10, COR_TEXTO_MUDO );
            texto( "dado", ORIGEM_X + wAnt + 10, yHeader, 10, COR_TEXTO_MUDO );
            texto( "próx", ORIGEM_X + wAnt + wDado + 10, yHeader, 10, COR_TEXTO_MUDO );
            
            desenharEtiquetaFim();
        }
        
        for ( NoSaindo s : nosSaindo ) {
            double e = easeOutCubic( s.tempo / DURACAO_SAIDA );
            double y = s.y + e * 30;
            double a = 1 - e;
            desenharNo( s.valor, y, s.x, s.escala, a, false, -1, -1, true );
            desenharEnderecoNo( s.endereco, s.x + NO_LARGURA * s.escala / 2.0, s.escala, a );
        }
        
        desenharEtiquetaFrente();
    }

    private void desenharDequeVazia() {
        double xFimNulo = desenharNuloLateral( ORIGEM_X, CENTRO_Y );
        textoCentroV( "deque vazia", xFimNulo + 28, CENTRO_Y, 13, COR_TEXTO_SUAVE );
    }

    private void desenharNo( String valor, double cy, double xEsq, double s, double a, boolean ehExtremo, double xProximo, double xAnterior, boolean saindo ) {
        int w = (int) Math.round( NO_LARGURA * s );
        int h = NO_ALTURA;
        int x = (int) Math.round( xEsq );
        int y = (int) Math.round( cy - h / 2.0 );
        
        int wAnt = (int) Math.round( w * 0.25 );
        int wDado = (int) Math.round( w * 0.5 );
        
        Color borda = ehExtremo ? COR_DESTAQUE : ( saindo ? COR_TEXTO_MUDO : COR_LINHA_FORTE );
        
        fillRectangle( x, y, w, h, alfa( COR_SUPERFICIE, a ) );
        retangulo( x, y, w, h, ehExtremo ? 2 : 1, alfa( borda, a ) );
        linha( x + wAnt, y, x + wAnt, y + h, alfa( borda, a * 0.7 ) );
        linha( x + wAnt + wDado, y, x + wAnt + wDado, y + h, alfa( borda, a * 0.7 ) );
        
        int tamanho = Math.max( 9, Math.min( 18, (int) ( h * 0.45 ) ) );
        String texto = valor;
        while ( tamanho > 9 && larguraTexto( texto, tamanho ) > wDado - 10 ) {
            tamanho--;
        }
        textoCentro( texto, x + wAnt + wDado / 2.0, y + h / 2.0, tamanho, alfa( saindo ? COR_TEXTO_SUAVE : COR_TEXTO, a ) );
        
        double xpProx = x + wAnt + wDado + ( w - wAnt - wDado ) / 2.0;
        double ypProx = Math.round( cy );
        fillCircle( xpProx, ypProx, 3, alfa( COR_TEXTO_SUAVE, a ) );
        
        double xpAnt = x + wAnt / 2.0;
        double ypAnt = Math.round( cy );
        fillCircle( xpAnt, ypAnt, 3, alfa( COR_TEXTO_SUAVE, a ) );
        
        if ( saindo ) {
            return;
        }
        
        if ( xProximo >= 0 ) {
            desenharSetaHorizontal( xpProx, ypProx, Math.round( xProximo ), alfa( COR_TEXTO_SUAVE, a ) );
        } else {
            double xNulo = x + w + 12;
            linha( xpProx, ypProx, xNulo, ypProx, alfa( COR_TEXTO_SUAVE, a ) );
            desenharNuloLateral( xNulo, ypProx );
        }
        
        if ( xAnterior >= 0 ) {
            double cyBaixo = ypProx + 10;
            linha( xpAnt, ypAnt, xpAnt, cyBaixo, alfa( COR_TEXTO_SUAVE, a ) );
            linha( xpAnt, cyBaixo, Math.round( xAnterior ) - 5, cyBaixo, alfa( COR_TEXTO_SUAVE, a ) );
            fillTriangle( Math.round( xAnterior ), cyBaixo, Math.round( xAnterior ) + 6, cyBaixo - 4, Math.round( xAnterior ) + 6, cyBaixo + 4, alfa( COR_TEXTO_SUAVE, a ) );
        } else {
            double xNulo = x - 12;
            linha( xpAnt, ypAnt, xNulo, ypAnt, alfa( COR_TEXTO_SUAVE, a ) );
            desenharNuloEsquerda( xNulo, ypAnt );
        }
    }

    private void desenharNuloEsquerda( double x, double y ) {
        y = Math.round( y ) + 0.5;
        linha( x, y - 8, x, y + 8, COR_TEXTO_SUAVE );
        linha( x - 4, y - 5, x - 4, y + 5, COR_TEXTO_SUAVE );
        linha( x - 8, y - 2, x - 8, y + 2, COR_TEXTO_SUAVE );
        double xTexto = x - 35;
        textoCentroV( "null", xTexto, y, 12, COR_TEXTO_SUAVE );
    }

    private void desenharEnderecoNo( long endereco, double cx, double s, double a ) {
        double y = CENTRO_Y + NO_ALTURA / 2.0 + 16;
        int tamanho = (int) Math.round( Math.max( 8, Math.min( 11, 11 * s ) ) );
        textoCentro( "@" + formatarEndereco( endereco ), cx, y, tamanho, alfa( COR_TEXTO_SUAVE, a ) );
    }

    private void desenharDelta( long enderecoA, long enderecoB, double cx, double s, double a ) {
        double y = CENTRO_Y + NO_ALTURA / 2.0 + 34;
        long diferenca = Math.abs( enderecoA - enderecoB );
        int tamanho = (int) Math.round( Math.max( 8, Math.min( 10, 10 * s ) ) );
        String texto = "Δ " + formatarMilhar( diferenca );
        textoCentro( texto, cx, y, tamanho, alfa( COR_TEXTO_MUDO, a ) );
    }

    private void desenharEtiquetaFrente() {
        double x = Math.round( xEtiquetaFrente ) + 0.5;
        double yPonta = CENTRO_Y + NO_ALTURA / 2.0 + 30;
        double yTopoConector = yPonta + 30;
        
        linha( x, yTopoConector, x, yPonta + 8, COR_DESTAQUE );
        fillTriangle( x, yPonta, x - 4, yPonta + 8, x + 4, yPonta + 8, COR_DESTAQUE );
        
        textoCentro( "início", x, yTopoConector + 12, 13, COR_DESTAQUE );
    }

    private void desenharEtiquetaFim() {
        double x = Math.round( xEtiquetaFim ) + 0.5;
        double yPonta = CENTRO_Y - NO_ALTURA / 2.0 - 2;
        double yTopoConector = yPonta - 30;
        
        linha( x, yTopoConector, x, yPonta - 8, COR_DESTAQUE );
        fillTriangle( x, yPonta, x - 4, yPonta - 8, x + 4, yPonta - 8, COR_DESTAQUE );
        
        textoCentro( "fim", x, yTopoConector - 12, 13, COR_DESTAQUE );
    }

    private void desenharSetaHorizontal( double x1, double y, double x2, Color cor ) {
        linha( x1, y, x2 - 6, y, cor );
        fillTriangle( x2, y, x2 - 8, y - 4, x2 - 8, y + 4, cor );
    }

    private double xEsquerdaNo( int i, int n, double s ) {
        return ORIGEM_X + i * ( NO_LARGURA + NO_ESPACO ) * s;
    }
    
    private double xCentroNo( int i, int n, double s ) {
        return xEsquerdaNo( i, n, s ) + NO_LARGURA * s / 2;
    }
    
    public static void main( String[] args ) {
        new SimuladorDeque();
    }
}
