package projetoesdlinear;

import aesd.ds.exceptions.EmptyStackException;
import aesd.ds.implementations.linear.LinkedStack;
import aesd.ds.interfaces.Stack;
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
 * Simulador de pilha.
 * 
 * O visual é um único painel, como uma janela de terminal: uma barra de
 * título/estado em cima, o histórico de comandos e o desenho da pilha no
 * meio, e um prompt de comando embaixo. Não há barra lateral nem cartões
 * separados — tudo é uma peça só, de cima para baixo.
 * 
 * Organização do código:
 *     1) constantes de layout e paleta
 *     2) atributos
 *     3) ciclo de vida (create, update, draw)
 *     4) operações sobre a pilha (empilhar/desempilhar)
 *     5) desenho do painel (moldura, cabeçalho, histórico, prompt)
 *     6) desenho da pilha
 *     7) utilitários de desenho e de texto
 * 
 * @author Prof. Dr. David Buzatto
 */
public class SimuladorPilha extends EngineFrame {

    //==========================================================================
    // 1) CONSTANTES DE LAYOUT E PALETA
    //==========================================================================
    
    private static final int LARGURA_JANELA = 980;
    private static final int ALTURA_JANELA = 640;
    private static final double CENTRO_X = LARGURA_JANELA / 2.0;
    
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
    
    // área da pilha, dentro do corpo
    private static final double BASE_Y = REGUA_RODAPE_Y - 52;
    private static final double LIMITE_SUPERIOR_Y = HISTORICO_Y + MAX_HISTORICO * HISTORICO_LINHA + 30;
    
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
    
    // pilha que passará pelas operações de empilhar e desempilhar
    private Stack<String> pilha;
    
    // cópia dos elementos da pilha (do topo para a base) usada apenas no desenho,
    // pois o update e o draw rodam em threads diferentes
    private List<String> visao = new ArrayList<>();
    private List<Long> enderecosVisao = new ArrayList<>();
    
    // endereço "simulado" de cada elemento, do topo para a base. A JVM não dá
    // acesso ao endereço real de um objeto, então cada push sorteia um valor
    // que só serve para ilustrar que os nós não ficam vizinhos na memória.
    private ArrayDeque<Long> enderecos;
    private Random sorteioEndereco;
    
    // histórico de comandos, mais recente por último
    private List<Comando> historico;
    
    // campo de texto do prompt (sempre focado: é o único campo da tela)
    private String textoCampo;
    private double tempoCursor;
    
    private Token tokenEmpilhar;
    private Token tokenDesempilhar;
    
    // animações
    private double tempoEntrada;
    private final List<NoSaindo> nosSaindo = new CopyOnWriteArrayList<>();
    private double escala;
    private double yEtiquetaTopo;
    
    private int cursorAtual;
    private String fonte;
    
    private static class Comando {
        String texto;
        boolean erro;
    }
    
    /**
     * Um "botão" que é só texto entre colchetes, como um atalho de barra de
     * status (ex.: [ push · enter ]). Fica destacado com um retângulo sutil
     * quando o mouse passa por cima.
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
        double y;
        double escala;
        double tempo;
    }

    public SimuladorPilha() {

        // cria a janela do jogo ou simulação
        super( 
            LARGURA_JANELA,       // largura
            ALTURA_JANELA,        // altura
            "Simulador de Pilha", // título da janela
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
        
        pilha = new LinkedStack<>();
        pilha.push( "a" );
        enderecos.push( gerarEndereco() );
        pilha.push( "b" );
        enderecos.push( gerarEndereco() );
        pilha.push( "c" );
        enderecos.push( gerarEndereco() );
        
        textoCampo = "";
        tempoEntrada = 1000;
        escala = 1;
        yEtiquetaTopo = yCentroNo( 0, pilha.getSize(), 1 );
        cursorAtual = MOUSE_CURSOR_DEFAULT;
        
        tokenEmpilhar = new Token();
        tokenEmpilhar.texto = "[ push · enter ]";
        tokenDesempilhar = new Token();
        tokenDesempilhar.texto = "[ pop · shift+enter ]";
        
        registrarComando( "push(\"a\")", false );
        registrarComando( "push(\"b\")", false );
        registrarComando( "push(\"c\")", false );
        
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
        atualizarToken( tokenEmpilhar, mx, my );
        atualizarToken( tokenDesempilhar, mx, my );
        
        // atalhos: Enter empilha, Shift+Enter desempilha
        if ( isKeyPressed( KEY_ENTER ) ) {
            if ( isKeyDown( KEY_SHIFT ) ) {
                desempilhar();
            } else {
                empilhar();
            }
        }
        
        if ( tokenEmpilhar.clicado ) {
            empilhar();
        }
        
        if ( tokenDesempilhar.clicado ) {
            desempilhar();
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
        for ( String v : pilha ) {
            copia.add( v );
        }
        visao = copia;
        enderecosVisao = new ArrayList<>( enderecos );
        
        clearBackground( COR_FUNDO );
        
        desenharMoldura();
        desenharCabecalho();
        desenharHistorico();
        desenharPilha();
        desenharRodape();
        
    }
    
    /**
     * Lê o texto digitado.
     * 
     * O JSGE não expõe os caracteres "prontos" do teclado (com acento e
     * maiúscula/minúscula já resolvidas) de um jeito que funcione fora de
     * um GuiTextField pronto da biblioteca. Por isso a leitura aqui é feita
     * tecla a tecla, com isKeyPressed(), o mesmo mecanismo usado por Enter e
     * Shift — o que evita depender de um gancho de teclado à parte, que não
     * é confiável em todo ambiente. A troca é: letras e dígitos funcionam
     * bem, mas sem acentos.
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
        
        // a escala se ajusta suavemente para a pilha sempre caber no painel
        escala += ( escalaAlvo() - escala ) * Math.min( 1, delta * 10 );
        
        // a etiqueta "topo" desliza até o nó do topo
        double yAlvo = pilha.isEmpty() ? BASE_Y - NO_ALTURA / 2.0 : yCentroNo( 0, pilha.getSize(), escala );
        yEtiquetaTopo += ( yAlvo - yEtiquetaTopo ) * Math.min( 1, delta * 16 );
        
    }
    
    private void atualizarCursorDoMouse( int mx, int my ) {
        
        int novo = ( tokenEmpilhar.mouseSobre || tokenDesempilhar.mouseSobre )
                ? MOUSE_CURSOR_POINTING_HAND : MOUSE_CURSOR_DEFAULT;
        
        if ( novo != cursorAtual ) {
            cursorAtual = novo;
            setMouseCursor( novo );
        }
        
    }
    
    //==========================================================================
    // 4) OPERAÇÕES SOBRE A PILHA
    //==========================================================================
    
    private void empilhar() {
        
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
    
    private void desempilhar() {
        
        try {
            
            // guarda a posição do nó do topo antes de removê-lo, para animar a saída
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
        
        textoCentroV( "pilha", xEsq, cy, 15, COR_TEXTO );
        double xDepois = xEsq + larguraTexto( "pilha", 15 ) + 12;
        textoCentroV( "lifo — linkedstack", xDepois, cy, 12, COR_TEXTO_SUAVE );
        
        String topo = visao.isEmpty() ? "-" : visao.get( 0 );
        String status = String.format( "tamanho %02d    topo %s", visao.size(), topo );
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
        posicionarToken( tokenDesempilhar, FRAME_X + FRAME_LARGURA - 18, yRodape );
        posicionarToken( tokenEmpilhar, tokenDesempilhar.x - 18, yRodape );
        
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
        
        desenharToken( tokenEmpilhar );
        desenharToken( tokenDesempilhar );
        
    }
    
    private void desenharToken( Token t ) {
        
        if ( t.mouseSobre ) {
            fillRectangle( t.x - 6, t.y - t.altura / 2.0 - 3, t.largura + 12, t.altura + 6,
                    t.mousePressionado ? COR_LINHA_FORTE : COR_LINHA );
        }
        
        textoCentroV( t.texto, t.x, t.y, 13, t.mouseSobre ? COR_TEXTO : COR_TEXTO_SUAVE );
        
    }
    
    //==========================================================================
    // 6) DESENHO DA PILHA
    //==========================================================================
    
    private void desenharPilha() {
        
        int n = visao.size();
        
        if ( n == 0 ) {
            desenharPilhaVazia();
        } else {
            
            // de baixo para cima
            for ( int i = n - 1; i >= 0; i-- ) {
                
                double y = yTopoNo( i, n, escala );
                double a = 1;
                
                // entrada do novo topo: desce um pouco enquanto aparece
                if ( i == 0 && tempoEntrada < DURACAO_ENTRADA ) {
                    double e = easeOutCubic( tempoEntrada / DURACAO_ENTRADA );
                    y -= ( 1 - e ) * 14;
                    a = e;
                }
                
                desenharNo( visao.get( i ), CENTRO_X, y, escala, a, i == 0, i == n - 1 ? -1 : yTopoNo( i + 1, n, escala ), false );
                desenharEnderecoNo( enderecosVisao.get( i ), y, h( escala ), escala, a );
                
                // diferença de endereço para o nó de baixo: mostra que não estão vizinhos na memória
                if ( i < n - 1 ) {
                    double yProximo = yTopoNo( i + 1, n, escala );
                    desenharDelta( enderecosVisao.get( i ), enderecosVisao.get( i + 1 ), y + h( escala ), yProximo, escala, a );
                }
                
            }
            
            // nomes das três colunas, acima do topo
            double yTopo = yTopoNo( 0, n, escala ) - 18;
            int x = (int) ( CENTRO_X - NO_LARGURA / 2 );
            int larguraDado = (int) Math.round( NO_LARGURA * PROPORCAO_DADO );
            texto( "dado", x + 10, yTopo, 10, COR_TEXTO_MUDO );
            texto( "anterior", x + larguraDado + 10, yTopo, 10, COR_TEXTO_MUDO );
            texto( "endereço (simulado)", CENTRO_X + NO_LARGURA / 2.0 + 16, yTopo, 10, COR_TEXTO_MUDO );
            
        }
        
        // nós que acabaram de sair
        for ( NoSaindo s : nosSaindo ) {
            double e = easeOutCubic( s.tempo / DURACAO_SAIDA );
            double cx = CENTRO_X + e * 30;
            double a = 1 - e;
            desenharNo( s.valor, cx, s.y, s.escala, a, false, -1, true );
            desenharEnderecoNo( s.endereco, s.y, h( s.escala ), s.escala, a, cx );
        }
        
        desenharEtiquetaTopo();
        
    }
    
    /** Altura do nó (em pixels) numa dada escala — evita repetir a mesma conta várias vezes. */
    private double h( double s ) {
        return NO_ALTURA * s;
    }
    
    private void desenharPilhaVazia() {
        
        double y = BASE_Y - NO_ALTURA / 2.0;
        int xNulo = (int) ( CENTRO_X - 90 );
        
        double xFimNulo = desenharNuloLateral( xNulo, y );
        textoCentroV( "pilha vazia", xFimNulo + 28, y, 13, COR_TEXTO_SUAVE );
        
    }
    
    /**
     * Desenha um nó, com duas células: [ dado | anterior ].
     * 
     * @param yProximo posição y do topo do nó de baixo, ou negativo se este é o último (anterior = null).
     */
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
        
        // valor
        int tamanho = Math.max( 9, Math.min( 18, (int) ( h * 0.45 ) ) );
        String texto = valor;
        while ( tamanho > 9 && larguraTexto( texto, tamanho ) > larguraDado - 20 ) {
            tamanho--;
        }
        textoCentro( texto, x + larguraDado / 2.0, y + h / 2.0, tamanho, alfa( saindo ? COR_TEXTO_SUAVE : COR_TEXTO, a ) );
        
        // ponteiro anterior: ponto de origem e seta até o nó de baixo
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
    
    /**
     * Rótulo "@0x......" com o endereço simulado do nó, alinhado à direita dele.
     * xCentro é opcional — usado só pelos nós que estão saindo, que se deslocam.
     */
    private void desenharEnderecoNo( long endereco, double yTop, double alturaNo, double s, double a ) {
        desenharEnderecoNo( endereco, yTop, alturaNo, s, a, CENTRO_X );
    }
    
    private void desenharEnderecoNo( long endereco, double yTop, double alturaNo, double s, double a, double cx ) {
        
        double x = cx + NO_LARGURA / 2.0 + 16;
        double y = yTop + alturaNo / 2.0;
        int tamanho = Math.max( 8, Math.min( 11, (int) Math.round( 11 * s ) ) );
        
        textoCentroV( "@" + formatarEndereco( endereco ), x, y, tamanho, alfa( COR_TEXTO_SUAVE, a ) );
        
    }
    
    /**
     * Diferença entre dois endereços simulados, escrita no meio do vão entre
     * dois nós — o número grande e sem padrão é o ponto: eles não são vizinhos.
     */
    private void desenharDelta( long enderecoA, long enderecoB, double yTop, double yBase, double s, double a ) {
        
        double x = CENTRO_X + NO_LARGURA / 2.0 + 16;
        double y = ( yTop + yBase ) / 2.0;
        long diferenca = Math.abs( enderecoA - enderecoB );
        int tamanho = Math.max( 8, Math.min( 10, (int) Math.round( 10 * s ) ) );
        
        String texto = "Δ " + formatarMilhar( diferenca );
        textoCentroV( texto, x, y, tamanho, alfa( COR_TEXTO_MUDO, a ) );
        
    }
    
    /**
     * Sorteia um endereço "simulado" de 32 bits, no estilo 0xXXXXXXXX. A JVM
     * não permite ler o endereço real de um objeto (e ele pode até mudar de
     * lugar durante a execução, por causa do coletor de lixo), então este
     * número serve só para ilustrar a ideia — cada push ganha um valor nunca
     * usado antes.
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
     * Etiqueta "topo": texto, linha e ponta de seta apontando para o nó do topo.
     */
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
    
    private double desenharNuloLateral( double x, double y ) {
        
        y = Math.round( y ) + 0.5;
        
        linha( x, y - 8, x, y + 8, COR_TEXTO_SUAVE );
        linha( x + 4, y - 5, x + 4, y + 5, COR_TEXTO_SUAVE );
        linha( x + 8, y - 2, x + 8, y + 2, COR_TEXTO_SUAVE );
        double xTexto = x + 20;
        textoCentroV( "null", xTexto, y, 12, COR_TEXTO_SUAVE );
        return xTexto + larguraTexto( "null", 12 );
        
    }
    
    private void desenharSeta( double x1, double y1, double x2, double y2, Color cor ) {
        linha( x1, y1, x2, y2 - 6, cor );
        fillTriangle( x2, y2, x2 - 4, y2 - 8, x2 + 4, y2 - 8, cor );
    }
    
    //--------------------------------------------------------------------------
    // geometria da pilha
    //--------------------------------------------------------------------------
    
    private double escalaAlvo() {
        int n = Math.max( 1, pilha.getSize() );
        double necessario = n * NO_ALTURA + ( n - 1 ) * NO_ESPACO;
        return Math.max( 0.4, Math.min( 1, ( BASE_Y - LIMITE_SUPERIOR_Y ) / necessario ) );
    }
    
    private double yTopoNo( int i, int n, double s ) {
        int deBaixo = n - 1 - i;
        return BASE_Y - deBaixo * ( NO_ALTURA + NO_ESPACO ) * s - NO_ALTURA * s;
    }
    
    private double yCentroNo( int i, int n, double s ) {
        return yTopoNo( i, n, s ) + NO_ALTURA * s / 2;
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
        new SimuladorPilha();
    }
    
}
