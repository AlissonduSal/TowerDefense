package projetoesdlinear;

import aesd.ds.exceptions.EmptyQueueException;
import aesd.ds.implementations.linear.LinkedQueue;
import aesd.ds.interfaces.Queue;
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

/**
 * Simulador de fila.
 * 
 * Mesmo estilo do simulador de pilha: um único painel, como uma janela de
 * terminal. A diferença de estrutura aparece no desenho: a fila é FIFO e
 * tem duas pontas ativas — "frente" (onde se desenfileira) e "fim" (onde
 * se enfileira) — por isso os nós ficam em fila horizontal, com a frente
 * ancorada à esquerda e o fim avançando para a direita conforme cresce.
 * 
 * Organização do código:
 *     1) constantes de layout e paleta
 *     2) atributos
 *     3) ciclo de vida (create, update, draw)
 *     4) operações sobre a fila (enfileirar/desenfileirar)
 *     5) desenho do painel (moldura, cabeçalho, histórico, prompt)
 *     6) desenho da fila
 *     7) utilitários de desenho e de texto
 * 
 * @author Prof. Dr. David Buzatto
 */
public class SimuladorFila extends EngineFrame {

    //==========================================================================
    // 1) CONSTANTES DE LAYOUT E PALETA
    //==========================================================================
    
    private static final int LARGURA_JANELA = 980;
    private static final int ALTURA_JANELA = 640;
    private static final double CENTRO_Y = 350;
    
    // moldura do painel (como uma janela de terminal)
    private static final int FRAME_X = 26;
    private static final int FRAME_Y = 26;
    private static final int FRAME_LARGURA = LARGURA_JANELA - FRAME_X * 2;
    private static final int FRAME_ALTURA = ALTURA_JANELA - FRAME_Y * 2;
    private static final int CABECALHO_ALTURA = 44;
    private static final int RODAPE_ALTURA = 56;
    private static final double REGUA_CABECALHO_Y = FRAME_Y + CABECALHO_ALTURA;
    private static final double REGUA_RODAPE_Y = FRAME_Y + FRAME_ALTURA - RODAPE_ALTURA;
    
    // histórico de comandos (dentro do corpo, no topo)
    private static final int MAX_HISTORICO = 5;
    private static final double HISTORICO_Y = REGUA_CABECALHO_Y + 26;
    private static final double HISTORICO_LINHA = 18;
    
    // área da fila, dentro do corpo. A frente fica ancorada em ORIGEM_X e a
    // fila cresce para a direita, até o limite antes de precisar encolher.
    private static final double ORIGEM_X = FRAME_X + 120;
    private static final double LIMITE_DIREITO_X = FRAME_X + FRAME_LARGURA - 110;
    
    // nós
    private static final int NO_LARGURA = 196;
    private static final int NO_ALTURA = 42;
    private static final int NO_ESPACO = 24;
    private static final double PROPORCAO_DADO = 0.68;
    
    // animações (em segundos)
    private static final double DURACAO_ENTRADA = 0.18;
    private static final double DURACAO_SAIDA = 0.26;
    
    private static final int MAX_CARACTERES = 10;
    
    // altura das letras maiúsculas em relação ao tamanho da fonte
    private static final double ALTURA_LETRA = 0.72;
    
    // paleta: fundo quase preto, texto em três intensidades, um único destaque
    private static final Color COR_FUNDO        = new Color( 16, 16, 15 );
    private static final Color COR_SUPERFICIE   = new Color( 23, 23, 21 );
    private static final Color COR_LINHA        = new Color( 40, 40, 37 );
    private static final Color COR_LINHA_FORTE  = new Color( 90, 88, 81 );
    private static final Color COR_TEXTO        = new Color( 224, 221, 212 );
    private static final Color COR_TEXTO_SUAVE  = new Color( 128, 124, 114 );
    private static final Color COR_TEXTO_MUDO   = new Color( 78, 76, 70 );
    private static final Color COR_DESTAQUE     = new Color( 216, 150, 60 );
    
    //==========================================================================
    // 2) ATRIBUTOS
    //==========================================================================
    
    // fila que passará pelas operações de enfileirar e desenfileirar
    private Queue<String> fila;
    
    // cópia dos elementos da fila (da frente para o fim) usada apenas no
    // desenho, pois o update e o draw rodam em threads diferentes
    private List<String> visao = new ArrayList<>();
    private List<Long> enderecosVisao = new ArrayList<>();
    
    // endereço "simulado" de cada elemento, da frente para o fim. A JVM não
    // dá acesso ao endereço real de um objeto, então cada enqueue sorteia um
    // valor que só serve para ilustrar que os nós não ficam vizinhos na
    // memória. addLast/removeFirst mantêm essa fila auxiliar em FIFO, junto
    // com a fila de verdade.
    private ArrayDeque<Long> enderecos;
    private Random sorteioEndereco;
    
    // histórico de comandos, mais recente por último
    private List<Comando> historico;
    
    // campo de texto do prompt (sempre focado: é o único campo da tela)
    private String textoCampo;
    private double tempoCursor;
    
    private Token tokenEnfileirar;
    private Token tokenDesenfileirar;
    
    // animações
    private double tempoEntrada;
    private final List<NoSaindo> nosSaindo = new CopyOnWriteArrayList<>();
    private double escala;
    private double xEtiquetaFim;
    
    private int cursorAtual;
    private String fonte;
    
    private static class Comando {
        String texto;
        boolean erro;
    }
    
    /**
     * Um "botão" que é só texto entre colchetes, como um atalho de barra de
     * status (ex.: [ enqueue · enter ]). Fica destacado com um retângulo
     * sutil quando o mouse passa por cima.
     */
    private static class Token {
        double x, y, largura, altura;
        String texto;
        boolean mouseSobre;
        boolean mousePressionado;
        boolean clicado;
        boolean contem( double px, double py ) {
            return px >= x && px <= x + largura && py >= y && py <= y + altura;
        }
    }
    
    private static class NoSaindo {
        String valor;
        long endereco;
        double x;
        double escala;
        double tempo;
    }

    public SimuladorFila() {

        // cria a janela do jogo ou simulação
        super( 
            LARGURA_JANELA,       // largura
            ALTURA_JANELA,        // altura
            "Simulador de Fila",  // título da janela
            60,                   // 60 quadros por segundo
            true );               // ativa a suavização (antialiasing)

    }
    
    //==========================================================================
    // 3) CICLO DE VIDA
    //==========================================================================

    /**
     * Processa a entrada inicial fornecida pelo usuário e cria
     * e/ou inicializa os objetos/contextos/variáveis do jogo ou simulação.
     */
    @Override
    public void create() {
        
        // atenção: dentro do create() o contexto gráfico ainda não existe,
        // então só é possível configurar a fonte *padrão* aqui
        fonte = escolherFonte( "Cascadia Mono", "Consolas", "JetBrains Mono", "SF Mono", "Menlo", "DejaVu Sans Mono", "Courier New" );
        setDefaultFontName( fonte );
        
        // o EngineFrame chama create() ainda durante o super(), antes dos
        // inicializadores de instância rodarem, então esses campos (que
        // antes tinham "= new ...()" na declaração) precisam ser
        // inicializados aqui manualmente, antes do primeiro uso
        enderecos = new ArrayDeque<>();
        sorteioEndereco = new Random();
        historico = new CopyOnWriteArrayList<>();
        
        fila = new LinkedQueue<>();
        fila.enqueue( "a" );
        enderecos.addLast( gerarEndereco() );
        fila.enqueue( "b" );
        enderecos.addLast( gerarEndereco() );
        fila.enqueue( "c" );
        enderecos.addLast( gerarEndereco() );
        
        textoCampo = "";
        tempoEntrada = 1000;
        escala = 1;
        xEtiquetaFim = xCentroNo( fila.getSize() - 1, fila.getSize(), 1 );
        cursorAtual = MOUSE_CURSOR_DEFAULT;
        
        tokenEnfileirar = new Token();
        tokenEnfileirar.texto = "[ enqueue · enter ]";
        tokenDesenfileirar = new Token();
        tokenDesenfileirar.texto = "[ dequeue · shift+enter ]";
        
        registrarComando( "enqueue(\"a\")", false );
        registrarComando( "enqueue(\"b\")", false );
        registrarComando( "enqueue(\"c\")", false );
        
    }

    /**
     * Atualiza os objetos/contextos/variáveis do jogo ou simulação.
     * O parâmetro delta contém o tempo que passou entre o quadro
     * anterior e o quadro atual.
     */
    @Override
    public void update( double delta ) {
        
        int mx = getMouseX();
        int my = getMouseY();
        
        atualizarCampoDeTexto( delta );
        atualizarToken( tokenEnfileirar, mx, my );
        atualizarToken( tokenDesenfileirar, mx, my );
        
        // atalhos: Enter enfileira, Shift+Enter desenfileira
        if ( isKeyPressed( KEY_ENTER ) ) {
            if ( isKeyDown( KEY_SHIFT ) ) {
                desenfileirar();
            } else {
                enfileirar();
            }
        }
        
        if ( tokenEnfileirar.clicado ) {
            enfileirar();
        }
        
        if ( tokenDesenfileirar.clicado ) {
            desenfileirar();
        }
        
        atualizarAnimacoes( delta );
        atualizarCursorDoMouse( mx, my );
        
    }

    /**
     * Desenha o estado dos objetos/contextos/variáveis do jogo ou simulação.
     */
    @Override
    public void draw() {
        
        List<String> copia = new ArrayList<>();
        for ( String v : fila ) {
            copia.add( v );
        }
        visao = copia;
        enderecosVisao = new ArrayList<>( enderecos );
        
        clearBackground( COR_FUNDO );
        
        desenharMoldura();
        desenharCabecalho();
        desenharHistorico();
        desenharFila();
        desenharRodape();
        
    }
    
    /**
     * Lê o texto digitado, tecla a tecla com isKeyPressed() — o mesmo
     * mecanismo usado por Enter e Shift. Funciona bem para letras e
     * dígitos, mas sem acentos (veja o simulador de pilha para o motivo).
     */
    private void atualizarCampoDeTexto( double delta ) {
        
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
        
        tempoCursor += delta;
        
    }
    
    private void adicionarAoTexto( char c ) {
        if ( textoCampo.length() < MAX_CARACTERES ) {
            textoCampo += c;
            tempoCursor = 0;
        }
    }
    
    private void atualizarToken( Token t, int mx, int my ) {
        t.mouseSobre = t.contem( mx, my );
        t.clicado = t.mouseSobre && isMouseButtonPressed( MOUSE_BUTTON_LEFT );
        t.mousePressionado = t.mouseSobre && isMouseButtonDown( MOUSE_BUTTON_LEFT );
    }
    
    private void atualizarAnimacoes( double delta ) {
        
        tempoEntrada += delta;
        
        for ( NoSaindo n : nosSaindo ) {
            n.tempo += delta;
        }
        nosSaindo.removeIf( n -> n.tempo >= DURACAO_SAIDA );
        
        // a escala se ajusta suavemente para a fila sempre caber no painel
        escala += ( escalaAlvo() - escala ) * Math.min( 1, delta * 10 );
        
        // a etiqueta "fim" desliza até o último nó (o mais recente)
        double xAlvo = fila.isEmpty() ? ORIGEM_X : xCentroNo( fila.getSize() - 1, fila.getSize(), escala );
        xEtiquetaFim += ( xAlvo - xEtiquetaFim ) * Math.min( 1, delta * 16 );
        
    }
    
    private void atualizarCursorDoMouse( int mx, int my ) {
        
        int novo = ( tokenEnfileirar.mouseSobre || tokenDesenfileirar.mouseSobre )
                ? MOUSE_CURSOR_POINTING_HAND : MOUSE_CURSOR_DEFAULT;
        
        if ( novo != cursorAtual ) {
            cursorAtual = novo;
            setMouseCursor( novo );
        }
        
    }
    
    //==========================================================================
    // 4) OPERAÇÕES SOBRE A FILA
    //==========================================================================
    
    private void enfileirar() {
        
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
    
    private void desenfileirar() {
        
        try {
            
            // guarda a posição do nó da frente antes de removê-lo, para animar a saída
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
    
    private void registrarComando( String texto, boolean erro ) {
        
        Comando c = new Comando();
        c.texto = texto;
        c.erro = erro;
        historico.add( c );
        
        while ( historico.size() > MAX_HISTORICO ) {
            historico.remove( 0 );
        }
        
    }
    
    //==========================================================================
    // 5) DESENHO DO PAINEL
    //==========================================================================
    
    private void desenharMoldura() {
        retangulo( FRAME_X, FRAME_Y, FRAME_LARGURA, FRAME_ALTURA, 1, COR_LINHA_FORTE );
    }
    
    private void desenharCabecalho() {
        
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
    
    private void desenharHistorico() {
        
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
    
    private void desenharRodape() {
        
        linha( FRAME_X, REGUA_RODAPE_Y, FRAME_X + FRAME_LARGURA, REGUA_RODAPE_Y, COR_LINHA );
        
        // posiciona os tokens (da direita para a esquerda), agora que já é possível medir texto
        double yRodape = REGUA_RODAPE_Y + RODAPE_ALTURA / 2.0;
        posicionarToken( tokenDesenfileirar, FRAME_X + FRAME_LARGURA - 18, yRodape );
        posicionarToken( tokenEnfileirar, tokenDesenfileirar.x - 18, yRodape );
        
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
        
        desenharToken( tokenEnfileirar );
        desenharToken( tokenDesenfileirar );
        
    }
    
    private void desenharToken( Token t ) {
        
        if ( t.mouseSobre ) {
            fillRectangle( t.x - 6, t.y - t.altura / 2.0 - 3, t.largura + 12, t.altura + 6,
                    t.mousePressionado ? COR_LINHA_FORTE : COR_LINHA );
        }
        
        textoCentroV( t.texto, t.x, t.y, 13, t.mouseSobre ? COR_TEXTO : COR_TEXTO_SUAVE );
        
    }
    
    //==========================================================================
    // 6) DESENHO DA FILA
    //==========================================================================
    
    private void desenharFila() {
        
        int n = visao.size();
        
        if ( n == 0 ) {
            desenharFilaVazia();
        } else {
            
            for ( int i = 0; i < n; i++ ) {
                
                double xEsq = xEsquerdaNo( i, n, escala );
                double a = 1;
                
                // entrada do novo elemento no fim: escorrega da direita enquanto aparece
                if ( i == n - 1 && tempoEntrada < DURACAO_ENTRADA ) {
                    double e = easeOutCubic( tempoEntrada / DURACAO_ENTRADA );
                    xEsq += ( 1 - e ) * 16;
                    a = e;
                }
                
                double xProximo = ( i == n - 1 ) ? -1 : xEsquerdaNo( i + 1, n, escala );
                desenharNo( visao.get( i ), CENTRO_Y, xEsq, escala, a, i == 0, xProximo, false );
                
                double cxNo = xEsq + NO_LARGURA * escala / 2.0;
                desenharEnderecoNo( enderecosVisao.get( i ), cxNo, escala, a );
                
                // diferença de endereço para o próximo nó: mostra que não estão vizinhos na memória.
                // some quando o espaço fica curto demais, pra não sobrepor o rótulo de endereço.
                if ( i < n - 1 && escala > 0.55 ) {
                    double cxDepois = xEsquerdaNo( i + 1, n, escala ) + NO_LARGURA * escala / 2.0;
                    desenharDelta( enderecosVisao.get( i ), enderecosVisao.get( i + 1 ), ( cxNo + cxDepois ) / 2.0, escala, a );
                }
                
            }
            
            // nomes das três colunas, acima do nó da frente
            double yHeader = CENTRO_Y - NO_ALTURA / 2.0 - 18;
            int larguraDado = (int) Math.round( NO_LARGURA * escala * PROPORCAO_DADO );
            texto( "dado", ORIGEM_X + 10, yHeader, 10, COR_TEXTO_MUDO );
            texto( "próximo", ORIGEM_X + larguraDado + 10, yHeader, 10, COR_TEXTO_MUDO );
            
            // com um só elemento, frente e fim são o mesmo nó — mostrar as duas
            // setas ali só deixaria a cena poluída, então mostra apenas "frente"
            // (o cabeçalho já confirma que frente e fim são iguais nesse caso)
            if ( n > 1 ) {
                desenharEtiquetaFim();
            }
            
        }
        
        // nó que acabou de sair pela frente
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
    
    /**
     * Desenha um nó, com duas células: [ dado | próximo ].
     * 
     * @param xProximo posição x da esquerda do nó seguinte (mais perto do fim), ou negativo se este é o último (próximo = null).
     */
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
        
        // valor
        int tamanho = Math.max( 9, Math.min( 18, (int) ( h * 0.45 ) ) );
        String texto = valor;
        while ( tamanho > 9 && larguraTexto( texto, tamanho ) > larguraDado - 20 ) {
            tamanho--;
        }
        textoCentro( texto, x + larguraDado / 2.0, y + h / 2.0, tamanho, alfa( saindo ? COR_TEXTO_SUAVE : COR_TEXTO, a ) );
        
        // ponteiro próximo: ponto de origem e seta até o nó seguinte (à direita)
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
    
    /**
     * Rótulo "@0x......" com o endereço simulado do nó, centralizado abaixo dele.
     */
    private void desenharEnderecoNo( long endereco, double cx, double s, double a ) {
        
        double y = CENTRO_Y + NO_ALTURA / 2.0 + 16;
        int tamanho = (int) Math.round( Math.max( 8, Math.min( 11, 11 * s ) ) );
        
        textoCentro( "@" + formatarEndereco( endereco ), cx, y, tamanho, alfa( COR_TEXTO_SUAVE, a ) );
        
    }
    
    /**
     * Diferença entre dois endereços simulados, escrita abaixo do vão entre
     * dois nós — o número grande e sem padrão é o ponto: eles não são vizinhos.
     */
    private void desenharDelta( long enderecoA, long enderecoB, double cx, double s, double a ) {
        
        double y = CENTRO_Y + NO_ALTURA / 2.0 + 34;
        long diferenca = Math.abs( enderecoA - enderecoB );
        int tamanho = (int) Math.round( Math.max( 8, Math.min( 10, 10 * s ) ) );
        
        String texto = "Δ " + formatarMilhar( diferenca );
        textoCentro( texto, cx, y, tamanho, alfa( COR_TEXTO_MUDO, a ) );
        
    }
    
    /**
     * Sorteia um endereço "simulado" de 32 bits, no estilo 0xXXXXXXXX. A JVM
     * não permite ler o endereço real de um objeto (e ele pode até mudar de
     * lugar durante a execução, por causa do coletor de lixo), então este
     * número serve só para ilustrar a ideia — cada enqueue ganha um valor
     * nunca usado antes.
     */
    private long gerarEndereco() {
        return 0x10000000L + ( sorteioEndereco.nextInt( 0x70000000 ) & 0xFFFFFFF0L );
    }
    
    private String formatarEndereco( long endereco ) {
        return String.format( "0x%08x", endereco );
    }
    
    /** Separa a cada três dígitos com ponto, sem depender de Locale. */
    private String formatarMilhar( long valor ) {
        
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
    
    /**
     * Etiqueta "frente": aponta sempre para ORIGEM_X, com a fila vazia ou não
     * — é o nó da frente (quando existe) ou o próprio símbolo de null.
     */
    private void desenharEtiquetaFrente() {
        
        double y = Math.round( CENTRO_Y ) + 0.5;
        double xPonta = ORIGEM_X;
        
        linha( xPonta - 40, y, xPonta - 5, y, COR_DESTAQUE );
        fillTriangle( xPonta, y, xPonta - 8, y - 4, xPonta - 8, y + 4, COR_DESTAQUE );
        
        double w = larguraTexto( "frente", 13 );
        textoCentroV( "frente", xPonta - 40 - 10 - w, y, 13, COR_DESTAQUE );
        
    }
    
    /**
     * Etiqueta "fim": desliza por cima do último nó (o mais recém-enfileirado).
     * Só é desenhada quando a fila tem pelo menos um elemento.
     */
    private void desenharEtiquetaFim() {
        
        double x = Math.round( xEtiquetaFim ) + 0.5;
        double yPonta = CENTRO_Y - NO_ALTURA / 2.0 - 2;
        double yTopoConector = yPonta - 30;
        
        linha( x, yTopoConector, x, yPonta - 8, COR_DESTAQUE );
        fillTriangle( x, yPonta, x - 4, yPonta - 8, x + 4, yPonta - 8, COR_DESTAQUE );
        
        textoCentro( "fim", x, yTopoConector - 12, 13, COR_DESTAQUE );
        
    }
    
    private double desenharNuloLateral( double x, double y ) {
        
        y = Math.round( y ) + 0.5;
        
        linha( x, y - 8, x, y + 8, COR_TEXTO_SUAVE );
        linha( x + 4, y - 5, x + 4, y + 5, COR_TEXTO_SUAVE );
        linha( x + 8, y - 2, x + 8, y + 2, COR_TEXTO_SUAVE );
        double xTexto = x + 20;
        textoCentroV( "null", xTexto, y, 12, COR_TEXTO_SUAVE );
        return xTexto + larguraTexto( "null", 12 );
        
    }
    
    private void desenharSetaHorizontal( double x1, double y, double x2, Color cor ) {
        linha( x1, y, x2 - 6, y, cor );
        fillTriangle( x2, y, x2 - 8, y - 4, x2 - 8, y + 4, cor );
    }
    
    //--------------------------------------------------------------------------
    // geometria da fila
    //--------------------------------------------------------------------------
    
    private double escalaAlvo() {
        int n = Math.max( 1, fila.getSize() );
        double necessario = n * NO_LARGURA + ( n - 1 ) * NO_ESPACO;
        return Math.max( 0.3, Math.min( 1, ( LIMITE_DIREITO_X - ORIGEM_X ) / necessario ) );
    }
    
    /** Posição x da borda esquerda do nó de índice i (0 = frente, ancorada em ORIGEM_X). */
    private double xEsquerdaNo( int i, int n, double s ) {
        return ORIGEM_X + i * ( NO_LARGURA + NO_ESPACO ) * s;
    }
    
    private double xCentroNo( int i, int n, double s ) {
        return xEsquerdaNo( i, n, s ) + NO_LARGURA * s / 2;
    }
    
    //==========================================================================
    // 7) UTILITÁRIOS DE DESENHO E DE TEXTO
    //==========================================================================
    
    private void linha( double x1, double y1, double x2, double y2, Color cor ) {
        
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
    
    private void retangulo( double x, double y, double w, double h, int espessura, Color cor ) {
        
        setStrokeLineWidth( espessura );
        double m = espessura / 2.0;
        drawRectangle( x + m, y + m, w - espessura, h - espessura, cor );
        setStrokeLineWidth( 1 );
        
    }
    
    private Color alfa( Color c, double a ) {
        int alfaFinal = (int) Math.round( Math.max( 0, Math.min( 1, a ) ) * c.getAlpha() );
        return new Color( c.getRed(), c.getGreen(), c.getBlue(), alfaFinal );
    }
    
    private double easeOutCubic( double t ) {
        t = Math.max( 0, Math.min( 1, t ) );
        return 1 - Math.pow( 1 - t, 3 );
    }
    
    private void posicionarToken( Token t, double xDireita, double cy ) {
        t.altura = 20;
        t.largura = larguraTexto( t.texto, 13 );
        t.x = xDireita - t.largura;
        t.y = cy;
    }
    
    //--------------------------------------------------------------------------
    // texto
    //--------------------------------------------------------------------------
    
    private String escolherFonte( String... preferidas ) {
        
        List<String> instaladas = Arrays.asList( 
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames() );
        
        for ( String f : preferidas ) {
            if ( instaladas.contains( f ) ) {
                return f;
            }
        }
        
        return Font.MONOSPACED;
        
    }
    
    /** Texto com o canto superior esquerdo das letras maiúsculas em (x, y). */
    private void texto( String s, double x, double y, int tamanho, Color cor ) {
        setFontName( fonte );
        setFontStyle( Font.PLAIN );
        desenharTextoNaLinhaBase( s, x, y, tamanho, cor );
    }
    
    /** Texto centralizado horizontalmente em cx e verticalmente em cy. */
    private void textoCentro( String s, double cx, double cy, int tamanho, Color cor ) {
        textoCentroV( s, cx - larguraTexto( s, tamanho ) / 2, cy, tamanho, cor );
    }
    
    /** Texto com início em x e centralizado verticalmente em cy. */
    private void textoCentroV( String s, double x, double cy, int tamanho, Color cor ) {
        texto( s, x, cy - tamanho * ALTURA_LETRA / 2, tamanho, cor );
    }
    
    /**
     * O drawText posiciona a linha de base em y + metade da altura da caixa de
     * texto, e essa altura muda de fonte para fonte. Aqui compensamos isso para
     * que (x, y) seja sempre o topo das letras maiúsculas.
     */
    private void desenharTextoNaLinhaBase( String s, double x, double yTopoLetras, int tamanho, Color cor ) {
        double alturaCaixa = measureTextBounds( "Ag", tamanho ).height;
        drawText( s, Math.round( x ), Math.round( yTopoLetras + tamanho * ALTURA_LETRA - alturaCaixa / 2 ), tamanho, cor );
    }
    
    private double larguraTexto( String s, int tamanho ) {
        setFontName( fonte );
        setFontStyle( Font.PLAIN );
        return measureText( s, tamanho );
    }
    
    public static void main( String[] args ) {
        new SimuladorFila();
    }
    
}
