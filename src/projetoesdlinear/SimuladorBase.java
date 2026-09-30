package projetoesdlinear;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class SimuladorBase extends EngineFrame {

    //==========================================================================
    // 1) CONSTANTES DE LAYOUT E PALETA
    //==========================================================================
    
    protected static final int LARGURA_JANELA = 980;
    protected static final int ALTURA_JANELA = 640;
    
    protected static final int FRAME_X = 26;
    protected static final int FRAME_Y = 26;
    protected static final int FRAME_LARGURA = LARGURA_JANELA - FRAME_X * 2;
    protected static final int FRAME_ALTURA = ALTURA_JANELA - FRAME_Y * 2;
    protected static final int CABECALHO_ALTURA = 44;
    protected static final int RODAPE_ALTURA = 56;
    protected static final double REGUA_CABECALHO_Y = FRAME_Y + CABECALHO_ALTURA;
    protected static final double REGUA_RODAPE_Y = FRAME_Y + FRAME_ALTURA - RODAPE_ALTURA;
    
    protected static final int MAX_HISTORICO = 5;
    protected static final double HISTORICO_Y = REGUA_CABECALHO_Y + 26;
    protected static final double HISTORICO_LINHA = 18;
    
    protected static final int NO_LARGURA = 196;
    protected static final int NO_ALTURA = 42;
    protected static final int NO_ESPACO = 24;
    protected static final double PROPORCAO_DADO = 0.68;
    
    protected static final double DURACAO_ENTRADA = 0.18;
    protected static final double DURACAO_SAIDA = 0.26;
    
    protected static final int MAX_CARACTERES = 10;
    protected static final double ALTURA_LETRA = 0.72;
    
    protected static final Color COR_FUNDO        = new Color( 16, 16, 15 );
    protected static final Color COR_SUPERFICIE   = new Color( 23, 23, 21 );
    protected static final Color COR_LINHA        = new Color( 40, 40, 37 );
    protected static final Color COR_LINHA_FORTE  = new Color( 90, 88, 81 );
    protected static final Color COR_TEXTO        = new Color( 224, 221, 212 );
    protected static final Color COR_TEXTO_SUAVE  = new Color( 128, 124, 114 );
    protected static final Color COR_TEXTO_MUDO   = new Color( 78, 76, 70 );
    protected static final Color COR_DESTAQUE     = new Color( 216, 150, 60 );
    
    //==========================================================================
    // 2) ATRIBUTOS
    //==========================================================================
    
    protected List<String> visao;
    protected List<Long> enderecosVisao;
    
    protected ArrayDeque<Long> enderecos;
    protected Random sorteioEndereco;
    
    protected List<Comando> historico;
    
    protected String textoCampo;
    protected double tempoCursor;
    
    protected List<TokenAcao> acoes;
    
    protected double tempoEntrada;
    protected List<NoSaindo> nosSaindo;
    protected double escala;
    
    protected int cursorAtual;
    protected String fonte;
    
    protected static class Comando {
        String texto;
        boolean erro;
    }
    
    protected static class Token {
        double x, y, largura, altura;
        String texto;
        boolean mouseSobre;
        boolean mousePressionado;
        boolean clicado;
        boolean contem( double px, double py ) {
            return px >= x && px <= x + largura && py >= y && py <= y + altura;
        }
    }

    protected static class TokenAcao extends Token {
        Runnable acao;
        public TokenAcao(String texto, Runnable acao) {
            this.texto = texto;
            this.acao = acao;
        }
    }
    
    protected static class NoSaindo {
        String valor;
        long endereco;
        double x;
        double y;
        double escala;
        double tempo;
    }

    public SimuladorBase(String titulo) {
        super( 
            LARGURA_JANELA,
            ALTURA_JANELA,
            titulo,
            60,
            true );
    }

    @Override
    public void create() {
        visao = new ArrayList<>();
        enderecosVisao = new ArrayList<>();
        acoes = new ArrayList<>();
        nosSaindo = new CopyOnWriteArrayList<>();
        
        fonte = escolherFonte( "Cascadia Mono", "Consolas", "JetBrains Mono", "SF Mono", "Menlo", "DejaVu Sans Mono", "Courier New" );
        setDefaultFontName( fonte );
        
        enderecos = new ArrayDeque<>();
        sorteioEndereco = new Random();
        historico = new CopyOnWriteArrayList<>();
        
        textoCampo = "";
        tempoEntrada = 1000;
        escala = 1;
        cursorAtual = MOUSE_CURSOR_DEFAULT;
        
        inicializarEstrutura();
    }
    
    protected abstract void inicializarEstrutura();

    @Override
    public void update( double delta ) {
        int mx = getMouseX();
        int my = getMouseY();
        
        atualizarCampoDeTexto( delta );
        
        for (TokenAcao t : acoes) {
            atualizarToken(t, mx, my);
        }
        
        processarAtalhos();
        
        for (TokenAcao t : acoes) {
            if (t.clicado) {
                t.acao.run();
            }
        }
        
        atualizarAnimacoesEstrutura( delta );
        atualizarCursorDoMouse( mx, my );
    }
    
    protected void processarAtalhos() {
        if ( isKeyPressed( KEY_ENTER ) ) {
            if ( isKeyDown( KEY_SHIFT ) ) {
                remover();
            } else {
                adicionar();
            }
        }
    }

    protected void atualizarAnimacoesEstrutura( double delta ) {
        tempoEntrada += delta;
        for ( NoSaindo n : nosSaindo ) {
            n.tempo += delta;
        }
        nosSaindo.removeIf( n -> n.tempo >= DURACAO_SAIDA );
        escala += ( escalaAlvo() - escala ) * Math.min( 1, delta * 10 );
        
        atualizarEtiqueta(delta);
    }
    
    protected abstract double escalaAlvo();
    protected abstract void atualizarEtiqueta(double delta);
    protected abstract void adicionar();
    protected abstract void remover();

    @Override
    public void draw() {
        atualizarVisao();
        
        clearBackground( COR_FUNDO );
        
        desenharMoldura();
        desenharCabecalho();
        desenharHistorico();
        desenharEstrutura();
        desenharRodape();
    }
    
    protected abstract void atualizarVisao();
    protected abstract void desenharCabecalho();
    protected abstract void desenharEstrutura();
    
    protected void atualizarCampoDeTexto( double delta ) {
        if ( isKeyPressed( KeyEvent.VK_BACK_SPACE ) ) {
            if ( !textoCampo.isEmpty() ) {
                textoCampo = textoCampo.substring( 0, textoCampo.length() - 1 );
            }
            tempoCursor = 0;
        }
        boolean maiuscula = isKeyDown( KEY_SHIFT );
        for ( int i = 0; i < 26; i++ ) {
            if ( isKeyPressed( KeyEvent.VK_A + i ) ) {
                adicionarAoTexto( (char) ( ( maiuscula ? 'A' : 'a' ) + i ) );
            }
        }
        for ( int i = 0; i < 10; i++ ) {
            if ( isKeyPressed( KeyEvent.VK_0 + i ) ) {
                adicionarAoTexto( (char) ( '0' + i ) );
            }
        }
        if ( isKeyPressed( KeyEvent.VK_SPACE ) ) {
            adicionarAoTexto( ' ' );
        }
        if ( isKeyPressed( KeyEvent.VK_MINUS ) ) {
            adicionarAoTexto( '-' );
        }
        if ( isKeyPressed( KeyEvent.VK_COMMA ) ) {
            adicionarAoTexto( ',' );
        }
        tempoCursor += delta;
    }
    
    protected void adicionarAoTexto( char c ) {
        if ( textoCampo.length() < MAX_CARACTERES ) {
            textoCampo += c;
            tempoCursor = 0;
        }
    }
    
    protected void atualizarToken( Token t, int mx, int my ) {
        t.mouseSobre = t.contem( mx, my );
        t.clicado = t.mouseSobre && isMouseButtonPressed( MOUSE_BUTTON_LEFT );
        t.mousePressionado = t.mouseSobre && isMouseButtonDown( MOUSE_BUTTON_LEFT );
    }
    
    protected void atualizarCursorDoMouse( int mx, int my ) {
        boolean sobreAlgum = false;
        for (TokenAcao t : acoes) {
            if (t.mouseSobre) { sobreAlgum = true; break; }
        }
        int novo = sobreAlgum ? MOUSE_CURSOR_POINTING_HAND : MOUSE_CURSOR_DEFAULT;
        if ( novo != cursorAtual ) {
            cursorAtual = novo;
            setMouseCursor( novo );
        }
    }
    
    protected void registrarComando( String texto, boolean erro ) {
        Comando c = new Comando();
        c.texto = texto;
        c.erro = erro;
        historico.add( c );
        while ( historico.size() > MAX_HISTORICO ) {
            historico.remove( 0 );
        }
    }
    
    protected void desenharMoldura() {
        retangulo( FRAME_X, FRAME_Y, FRAME_LARGURA, FRAME_ALTURA, 1, COR_LINHA_FORTE );
    }
    
    protected void desenharHistorico() {
        double x = FRAME_X + 18;
        int total = historico.size();
        int i = 0;
        for ( Comando c : historico ) {
            double y = HISTORICO_Y + i * HISTORICO_LINHA;
            double a = 0.35 + 0.65 * ( i + 1 ) / (double) total;
            Color cor = c.erro ? COR_DESTAQUE : COR_TEXTO_SUAVE;
            texto( c.texto, x, y, 12, alfa( cor, a ) );
            i++;
        }
    }
    
    protected void desenharRodape() {
        linha( FRAME_X, REGUA_RODAPE_Y, FRAME_X + FRAME_LARGURA, REGUA_RODAPE_Y, COR_LINHA );
        double yRodape = REGUA_RODAPE_Y + RODAPE_ALTURA / 2.0;
        
        double atualX = FRAME_X + FRAME_LARGURA - 18;
        for (int i = acoes.size() - 1; i >= 0; i--) {
            posicionarToken(acoes.get(i), atualX, yRodape);
            atualX = acoes.get(i).x - 18;
        }
        
        double x = FRAME_X + 18;
        double cy = REGUA_RODAPE_Y + RODAPE_ALTURA / 2.0;
        int tamanho = 15;
        
        texto( ">", x, cy - tamanho * ALTURA_LETRA / 2, tamanho, COR_DESTAQUE );
        x += larguraTexto( ">", tamanho ) + 12;
        
        double larguraCursor = tamanho * 0.55;
        if ( textoCampo.isEmpty() ) {
            textoCentroV( "digite um valor", x + larguraCursor + 8, cy, tamanho, COR_TEXTO_MUDO );
        } else {
            textoCentroV( textoCampo, x, cy, tamanho, COR_TEXTO );
        }
        
        double xCursor = x + ( textoCampo.isEmpty() ? 0 : larguraTexto( textoCampo, tamanho ) + 2 );
        if ( ( tempoCursor % 1.0 ) < 0.55 ) {
            fillRectangle( xCursor, cy - tamanho * 0.62, larguraCursor, tamanho * 0.86, COR_DESTAQUE );
        }
        
        for (TokenAcao t : acoes) {
            desenharToken(t);
        }
    }
    
    protected void desenharToken( Token t ) {
        if ( t.mouseSobre ) {
            fillRectangle( t.x - 6, t.y - t.altura / 2.0 - 3, t.largura + 12, t.altura + 6,
                    t.mousePressionado ? COR_LINHA_FORTE : COR_LINHA );
        }
        textoCentroV( t.texto, t.x, t.y, 13, t.mouseSobre ? COR_TEXTO : COR_TEXTO_SUAVE );
    }
    
    protected long gerarEndereco() {
        return 0x10000000L + ( sorteioEndereco.nextInt( 0x70000000 ) & 0xFFFFFFF0L );
    }
    
    protected String formatarEndereco( long endereco ) {
        return String.format( "0x%08x", endereco );
    }
    
    protected String formatarMilhar( long valor ) {
        String digitos = String.valueOf( valor );
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < digitos.length(); i++ ) {
            if ( i > 0 && ( digitos.length() - i ) % 3 == 0 ) {
                sb.append( '.' );
            }
            sb.append( digitos.charAt( i ) );
        }
        return sb.toString();
    }
    
    protected void linha( double x1, double y1, double x2, double y2, Color cor ) {
        if ( x1 == x2 && x1 == Math.rint( x1 ) ) {
            x1 += 0.5;
            x2 += 0.5;
        }
        if ( y1 == y2 && y1 == Math.rint( y1 ) ) {
            y1 += 0.5;
            y2 += 0.5;
        }
        setStrokeLineWidth( 1 );
        drawLine( x1, y1, x2, y2, cor );
    }
    
    protected void retangulo( double x, double y, double w, double h, int espessura, Color cor ) {
        setStrokeLineWidth( espessura );
        double m = espessura / 2.0;
        drawRectangle( x + m, y + m, w - espessura, h - espessura, cor );
        setStrokeLineWidth( 1 );
    }
    
    protected Color alfa( Color c, double a ) {
        int alfaFinal = (int) Math.round( Math.max( 0, Math.min( 1, a ) ) * c.getAlpha() );
        return new Color( c.getRed(), c.getGreen(), c.getBlue(), alfaFinal );
    }
    
    protected double easeOutCubic( double t ) {
        t = Math.max( 0, Math.min( 1, t ) );
        return 1 - Math.pow( 1 - t, 3 );
    }
    
    protected void posicionarToken( Token t, double xDireita, double cy ) {
        t.altura = 20;
        t.largura = larguraTexto( t.texto, 13 );
        t.x = xDireita - t.largura;
        t.y = cy;
    }
    
    protected String escolherFonte( String... preferidas ) {
        List<String> instaladas = Arrays.asList( 
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames() );
        for ( String f : preferidas ) {
            if ( instaladas.contains( f ) ) {
                return f;
            }
        }
        return Font.MONOSPACED;
    }
    
    protected void texto( String s, double x, double y, int tamanho, Color cor ) {
        setFontName( fonte );
        setFontStyle( Font.PLAIN );
        desenharTextoNaLinhaBase( s, x, y, tamanho, cor );
    }
    
    protected void textoCentro( String s, double cx, double cy, int tamanho, Color cor ) {
        textoCentroV( s, cx - larguraTexto( s, tamanho ) / 2, cy, tamanho, cor );
    }
    
    protected void textoCentroV( String s, double x, double cy, int tamanho, Color cor ) {
        texto( s, x, cy - tamanho * ALTURA_LETRA / 2, tamanho, cor );
    }
    
    protected void desenharTextoNaLinhaBase( String s, double x, double yTopoLetras, int tamanho, Color cor ) {
        double alturaCaixa = measureTextBounds( "Ag", tamanho ).height;
        drawText( s, Math.round( x ), Math.round( yTopoLetras + tamanho * ALTURA_LETRA - alturaCaixa / 2 ), tamanho, cor );
    }
    
    protected double larguraTexto( String s, int tamanho ) {
        setFontName( fonte );
        setFontStyle( Font.PLAIN );
        return measureText( s, tamanho );
    }
    
    protected double desenharNuloLateral( double x, double y ) {
        y = Math.round( y ) + 0.5;
        linha( x, y - 8, x, y + 8, COR_TEXTO_SUAVE );
        linha( x + 4, y - 5, x + 4, y + 5, COR_TEXTO_SUAVE );
        linha( x + 8, y - 2, x + 8, y + 2, COR_TEXTO_SUAVE );
        double xTexto = x + 20;
        textoCentroV( "null", xTexto, y, 12, COR_TEXTO_SUAVE );
        return xTexto + larguraTexto( "null", 12 );
    }
}
