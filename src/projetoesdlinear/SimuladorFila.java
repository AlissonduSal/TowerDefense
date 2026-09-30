package projetoesdlinear;

import aesd.ds.exceptions.EmptyQueueException;
import aesd.ds.implementations.linear.LinkedQueue;
import aesd.ds.interfaces.Queue;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulador de fila.
 * 
 * Mesmo estilo do simulador de pilha: um único painel, como uma janela de
 * terminal.
 * @author Prof. Dr. David Buzatto
 */
public class SimuladorFila extends SimuladorBase {

    private static final double CENTRO_Y = 350;
    
    private static final double ORIGEM_X = FRAME_X + 120;
    private static final double LIMITE_DIREITO_X = FRAME_X + FRAME_LARGURA - 110;

    private Queue<String> fila;
    private double xEtiquetaFim;

    public SimuladorFila() {
        super( "Simulador de Fila" );
    }

    @Override
    protected void inicializarEstrutura() {
        fila = new LinkedQueue<>();
        fila.enqueue( "a" );
        enderecos.addLast( gerarEndereco() );
        fila.enqueue( "b" );
        enderecos.addLast( gerarEndereco() );
        fila.enqueue( "c" );
        enderecos.addLast( gerarEndereco() );
        
        xEtiquetaFim = xCentroNo( fila.getSize() - 1, fila.getSize(), 1 );
        
        acoes.add(new TokenAcao("[ dequeue · shift+enter ]", this::remover));
        acoes.add(new TokenAcao("[ enqueue · enter ]", this::adicionar));
        
        registrarComando( "enqueue(\"a\")", false );
        registrarComando( "enqueue(\"b\")", false );
        registrarComando( "enqueue(\"c\")", false );
    }

    @Override
    protected double escalaAlvo() {
        int n = Math.max( 1, fila.getSize() );
        double necessario = n * NO_LARGURA + ( n - 1 ) * NO_ESPACO;
        return Math.max( 0.3, Math.min( 1, ( LIMITE_DIREITO_X - ORIGEM_X ) / necessario ) );
    }

    @Override
    protected void atualizarEtiqueta(double delta) {
        double xAlvo = fila.isEmpty() ? ORIGEM_X : xCentroNo( fila.getSize() - 1, fila.getSize(), escala );
        xEtiquetaFim += ( xAlvo - xEtiquetaFim ) * Math.min( 1, delta * 16 );
    }

    @Override
    protected void adicionar() {
        String valor = textoCampo.trim();
        if ( valor.isEmpty() ) {
            registrarComando( "enqueue() sem valor", true );
            return;
        }
        fila.enqueue( valor );
        enderecos.addLast( gerarEndereco() );
        textoCampo = "";
        tempoEntrada = 0;
        registrarComando( "enqueue(\"" + valor + "\")", false );
    }

    @Override
    protected void remover() {
        try {
            NoSaindo saindo = new NoSaindo();
            saindo.escala = escala;
            saindo.x = xEsquerdaNo( 0, fila.getSize(), escala );
            
            String valor = fila.dequeue();
            long endereco = enderecos.removeFirst();
            
            saindo.valor = valor;
            saindo.endereco = endereco;
            nosSaindo.add( saindo );
            tempoEntrada = 1000;
            registrarComando( "dequeue() → \"" + valor + "\"", false );
        } catch ( EmptyQueueException exc ) {
            registrarComando( "dequeue() em fila vazia", true );
        }
    }

    @Override
    protected void atualizarVisao() {
        List<String> copia = new ArrayList<>();
        for ( String v : fila ) {
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
        
        textoCentroV( "fila", xEsq, cy, 15, COR_TEXTO );
        double xDepois = xEsq + larguraTexto( "fila", 15 ) + 12;
        textoCentroV( "fifo — linkedqueue", xDepois, cy, 12, COR_TEXTO_SUAVE );
        
        String frenteTxt = visao.isEmpty() ? "-" : visao.get( 0 );
        String fimTxt = visao.isEmpty() ? "-" : visao.get( visao.size() - 1 );
        String status = String.format( "tamanho %02d    frente %s    fim %s", visao.size(), frenteTxt, fimTxt );
        textoCentroV( status, xDir - larguraTexto( status, 12 ), cy, 12, COR_TEXTO_SUAVE );
        
        linha( FRAME_X, REGUA_CABECALHO_Y, FRAME_X + FRAME_LARGURA, REGUA_CABECALHO_Y, COR_LINHA );
    }

    @Override
    protected void desenharEstrutura() {
        int n = visao.size();
        
        if ( n == 0 ) {
            desenharFilaVazia();
        } else {
            for ( int i = 0; i < n; i++ ) {
                double xEsq = xEsquerdaNo( i, n, escala );
                double a = 1;
                
                if ( i == n - 1 && tempoEntrada < DURACAO_ENTRADA ) {
                    double e = easeOutCubic( tempoEntrada / DURACAO_ENTRADA );
                    xEsq += ( 1 - e ) * 16;
                    a = e;
                }
                
                double xProximo = ( i == n - 1 ) ? -1 : xEsquerdaNo( i + 1, n, escala );
                desenharNo( visao.get( i ), CENTRO_Y, xEsq, escala, a, i == 0, xProximo, false );
                
                double cxNo = xEsq + NO_LARGURA * escala / 2.0;
                desenharEnderecoNo( enderecosVisao.get( i ), cxNo, escala, a );
                
                if ( i < n - 1 && escala > 0.55 ) {
                    double cxDepois = xEsquerdaNo( i + 1, n, escala ) + NO_LARGURA * escala / 2.0;
                    desenharDelta( enderecosVisao.get( i ), enderecosVisao.get( i + 1 ), ( cxNo + cxDepois ) / 2.0, escala, a );
                }
            }
            
            double yHeader = CENTRO_Y - NO_ALTURA / 2.0 - 18;
            int larguraDado = (int) Math.round( NO_LARGURA * escala * PROPORCAO_DADO );
            texto( "dado", ORIGEM_X + 10, yHeader, 10, COR_TEXTO_MUDO );
            texto( "próximo", ORIGEM_X + larguraDado + 10, yHeader, 10, COR_TEXTO_MUDO );
            
            if ( n > 1 ) {
                desenharEtiquetaFim();
            }
        }
        
        for ( NoSaindo s : nosSaindo ) {
            double e = easeOutCubic( s.tempo / DURACAO_SAIDA );
            double x = s.x - e * 30;
            double a = 1 - e;
            desenharNo( s.valor, CENTRO_Y, x, s.escala, a, false, -1, true );
            desenharEnderecoNo( s.endereco, x + NO_LARGURA * s.escala / 2.0, s.escala, a );
        }
        
        desenharEtiquetaFrente();
    }

    private void desenharFilaVazia() {
        double xFimNulo = desenharNuloLateral( ORIGEM_X, CENTRO_Y );
        textoCentroV( "fila vazia", xFimNulo + 28, CENTRO_Y, 13, COR_TEXTO_SUAVE );
    }

    private void desenharNo( String valor, double cy, double xEsq, double s, double a, boolean ehFrente, double xProximo, boolean saindo ) {
        int w = (int) Math.round( NO_LARGURA * s );
        int h = NO_ALTURA;
        int x = (int) Math.round( xEsq );
        int y = (int) Math.round( cy - h / 2.0 );
        int larguraDado = (int) Math.round( w * PROPORCAO_DADO );
        
        Color borda = ehFrente ? COR_DESTAQUE : ( saindo ? COR_TEXTO_MUDO : COR_LINHA_FORTE );
        
        fillRectangle( x, y, w, h, alfa( COR_SUPERFICIE, a ) );
        retangulo( x, y, w, h, ehFrente ? 2 : 1, alfa( borda, a ) );
        linha( x + larguraDado, y, x + larguraDado, y + h, alfa( borda, a * 0.7 ) );
        
        int tamanho = Math.max( 9, Math.min( 18, (int) ( h * 0.45 ) ) );
        String texto = valor;
        while ( tamanho > 9 && larguraTexto( texto, tamanho ) > larguraDado - 20 ) {
            tamanho--;
        }
        textoCentro( texto, x + larguraDado / 2.0, y + h / 2.0, tamanho, alfa( saindo ? COR_TEXTO_SUAVE : COR_TEXTO, a ) );
        
        double xp = x + larguraDado + ( w - larguraDado ) / 2.0;
        double yp = Math.round( cy );
        fillCircle( xp, yp, 3, alfa( COR_TEXTO_SUAVE, a ) );
        
        if ( saindo ) {
            return;
        }
        
        if ( xProximo >= 0 ) {
            desenharSetaHorizontal( xp, yp, Math.round( xProximo ), alfa( COR_TEXTO_SUAVE, a ) );
        } else {
            double xNulo = x + w + 24;
            linha( xp, yp, xNulo, yp, alfa( COR_TEXTO_SUAVE, a ) );
            desenharNuloLateral( xNulo, yp );
        }
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
        double y = Math.round( CENTRO_Y ) + 0.5;
        double xPonta = ORIGEM_X;
        
        linha( xPonta - 40, y, xPonta - 5, y, COR_DESTAQUE );
        fillTriangle( xPonta, y, xPonta - 8, y - 4, xPonta - 8, y + 4, COR_DESTAQUE );
        
        double w = larguraTexto( "frente", 13 );
        textoCentroV( "frente", xPonta - 40 - 10 - w, y, 13, COR_DESTAQUE );
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
        new SimuladorFila();
    }
}
