package projetoesdlinear;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Torre Defesa
 * 
 * O jogo tem quatro telas (ver EstadoJogo):
 *     MENU         -- título e um botão "Jogar"
 *     SELECAO_MAPA -- lista de mapas disponíveis, um botão por mapa
 *     JOGANDO      -- a malha do mapa escolhido, com um botão de pausa
 *     PAUSADO      -- o jogo congelado atrás, com um overlay e os botões
 *                     "Continuar" e "Menu principal"
 * 
 * O mapa em si continua vindo de um MapaConfig (ver Mapas.java) -- a tela
 * JOGANDO não sabe qual mapa está carregado, só desenha o que MapaConfig
 * descreve. Trocar ou adicionar mapas não muda nada aqui: a tela de seleção
 * gera um botão para cada mapa que existir em Mapas.disponiveis().
 * 
 * @author Prof. Dr. David Buzatto
 */
public class JogoTorreDefesa extends EngineFrame {
    
    //==========================================================================
    // JANELA
    //==========================================================================
    
    private static final int LARGURA_JANELA = Mapas.padrao().getColunas() * Mapas.padrao().getTamanhoTile();
    private static final int ALTURA_JANELA = Mapas.padrao().getLinhas() * Mapas.padrao().getTamanhoTile();
    private static final double CENTRO_X = LARGURA_JANELA / 2.0;
    
    //==========================================================================
    // PALETA
    //==========================================================================
    
    // grama: tom sólido, igual ao da imagem de referência, com uma linha
    // bem discreta entre células pra a malha continuar visível
    private static final Color COR_GRAMA = new Color( 70, 141, 30 );
    private static final Color COR_GRAMA_LINHA = new Color( 60, 120, 25 );
    
    // caminho: cores tiradas por amostragem da própria imagem de referência
    private static final Color COR_CAMINHO = new Color( 58, 66, 72 );
    private static final Color COR_BORDA_CAMINHO = new Color( 156, 159, 162 );
    
    // menu e seleção de mapa
    private static final Color COR_FUNDO_MENU = new Color( 18, 26, 19 );
    private static final Color COR_TEXTO_MENU = new Color( 226, 232, 224 );
    private static final Color COR_BOTAO = new Color( 34, 48, 36 );
    private static final Color COR_BOTAO_HOVER = new Color( 48, 68, 50 );
    private static final Color COR_BOTAO_BORDA = new Color( 88, 166, 63 );
    
    private static final Color COR_FUNDO_JOGO = new Color( 10, 10, 10 );
    
    // pausa: fundo escurecido por cima do jogo congelado
    private static final Color COR_OVERLAY_PAUSA = new Color( 0, 0, 0, 150 );
    
    //==========================================================================
    // ATRIBUTOS
    //==========================================================================
    
    private EstadoJogo estado;
    private String fonte;
    
    private Botao botaoJogar;
    private Botao botaoVoltar;
    private Botao botaoPausar;
    private Botao botaoContinuar;
    private Botao botaoMenuPrincipal;
    private List<Botao> botoesMapas;
    private List<TipoCelula[][]> gradesMapas;
    
    private static final int MINI_TILE = 10;
    private static final int CARD_PADDING = 16;
    private static final int CARD_LABEL_ALTURA = 30;
    
    private MapaConfig mapa;
    private TipoCelula[][] grade;
    
    public JogoTorreDefesa() {
        
        super(
            LARGURA_JANELA,
            ALTURA_JANELA,
            "Torre Defesa",
            60,
            true );
        
    }
    
    //==========================================================================
    // CICLO DE VIDA
    //==========================================================================
    
    @Override
    public void create() {
        
        fonte = java.awt.Font.SANS_SERIF;
        estado = EstadoJogo.MENU;
        
        botaoJogar = new Botao( CENTRO_X - 110, ALTURA_JANELA * 0.58, 220, 56, "Jogar" );
        botaoVoltar = new Botao( 24, 24, 100, 36, "< voltar" );
        botaoPausar = new Botao( LARGURA_JANELA - 90, 16, 74, 36, "Pausar" );
        botaoContinuar = new Botao( CENTRO_X - 120, ALTURA_JANELA * 0.46, 240, 56, "Continuar" );
        botaoMenuPrincipal = new Botao( CENTRO_X - 120, ALTURA_JANELA * 0.46 + 72, 240, 56, "Menu principal" );
        botoesMapas = new ArrayList<>();
        gradesMapas = new ArrayList<>();
        
        montarBotoesDosMapas();
        
    }
    
    /**
     * Um "card" por mapa disponível, empilhado verticalmente e centralizado.
     * Cada card é clicável (é um Botao) e tem, dentro da própria área, uma
     * miniatura da malha do mapa (grama/caminho) desenhada em desenharCardMapa,
     * então o jogador já vê o traçado do mapa antes de escolher.
     */
    private void montarBotoesDosMapas() {
        
        List<MapaConfig> disponiveis = Mapas.disponiveis();
        
        double thumbLargura = Mapas.padrao().getColunas() * MINI_TILE;
        double thumbAltura = Mapas.padrao().getLinhas() * MINI_TILE;
        double cardLargura = thumbLargura + CARD_PADDING * 2;
        double cardAltura = thumbAltura + CARD_LABEL_ALTURA + CARD_PADDING * 2;
        double espaco = 20;
        double yInicial = 110;
        
        for ( int i = 0; i < disponiveis.size(); i++ ) {
            double y = yInicial + i * ( cardAltura + espaco );
            botoesMapas.add( new Botao( CENTRO_X - cardLargura / 2, y, cardLargura, cardAltura, disponiveis.get( i ).getNome() ) );
            gradesMapas.add( disponiveis.get( i ).paraGrade() );
        }
        
    }
    
    private void carregarMapa( MapaConfig novoMapa ) {
        mapa = novoMapa;
        grade = mapa.paraGrade();
    }
    
    @Override
    public void update( double delta ) {
        
        int mx = getMouseX();
        int my = getMouseY();
        boolean clicou = isMouseButtonPressed( MOUSE_BUTTON_LEFT );
        boolean mouseSobreAlgumBotao = false;
        
        switch ( estado ) {
            
            case MENU:
                botaoJogar.mouseSobre = botaoJogar.contem( mx, my );
                mouseSobreAlgumBotao = botaoJogar.mouseSobre;
                if ( clicou && botaoJogar.mouseSobre ) {
                    estado = EstadoJogo.SELECAO_MAPA;
                }
                break;
                
            case SELECAO_MAPA:
                botaoVoltar.mouseSobre = botaoVoltar.contem( mx, my );
                mouseSobreAlgumBotao = botaoVoltar.mouseSobre;
                if ( clicou && botaoVoltar.mouseSobre ) {
                    estado = EstadoJogo.MENU;
                }
                for ( int i = 0; i < botoesMapas.size(); i++ ) {
                    Botao b = botoesMapas.get( i );
                    b.mouseSobre = b.contem( mx, my );
                    mouseSobreAlgumBotao = mouseSobreAlgumBotao || b.mouseSobre;
                    if ( clicou && b.mouseSobre ) {
                        carregarMapa( Mapas.disponiveis().get( i ) );
                        estado = EstadoJogo.JOGANDO;
                    }
                }
                break;
                
            case JOGANDO:
                botaoPausar.mouseSobre = botaoPausar.contem( mx, my );
                mouseSobreAlgumBotao = botaoPausar.mouseSobre;
                if ( clicou && botaoPausar.mouseSobre ) {
                    estado = EstadoJogo.PAUSADO;
                }
                // ainda sem lógica de jogo (torres, inimigos, ondas) -- só o mapa por enquanto
                break;
                
            case PAUSADO:
                botaoContinuar.mouseSobre = botaoContinuar.contem( mx, my );
                botaoMenuPrincipal.mouseSobre = botaoMenuPrincipal.contem( mx, my );
                mouseSobreAlgumBotao = botaoContinuar.mouseSobre || botaoMenuPrincipal.mouseSobre;
                if ( clicou && botaoContinuar.mouseSobre ) {
                    estado = EstadoJogo.JOGANDO;
                }
                if ( clicou && botaoMenuPrincipal.mouseSobre ) {
                    estado = EstadoJogo.MENU;
                }
                break;
                
        }
        
        setMouseCursor( mouseSobreAlgumBotao ? MOUSE_CURSOR_POINTING_HAND : MOUSE_CURSOR_DEFAULT );
        
    }
    
    //==========================================================================
    // DESENHO
    //==========================================================================
    
    @Override
    public void draw() {
        
        switch ( estado ) {
            case MENU:
                desenharMenu();
                break;
            case SELECAO_MAPA:
                desenharSelecaoMapa();
                break;
            case JOGANDO:
                desenharJogo();
                desenharBotao( botaoPausar );
                break;
            case PAUSADO:
                desenharJogo();
                desenharPausa();
                break;
        }
        
    }
    
    private void desenharMenu() {
        
        clearBackground( COR_FUNDO_MENU );
        
        textoCentro( "TORRE DEFESA", CENTRO_X, ALTURA_JANELA * 0.36, 40, COR_TEXTO_MENU );
        
        desenharBotao( botaoJogar );
        
    }
    
    private void desenharSelecaoMapa() {
        
        clearBackground( COR_FUNDO_MENU );
        
        textoCentro( "Selecione o mapa", CENTRO_X, 60, 24, COR_TEXTO_MENU );
        
        desenharBotao( botaoVoltar );
        for ( int i = 0; i < botoesMapas.size(); i++ ) {
            desenharCardMapa( botoesMapas.get( i ), gradesMapas.get( i ) );
        }
        
    }
    
    private void desenharBotao( Botao b ) {
        
        fillRectangle( b.x, b.y, b.largura, b.altura, b.mouseSobre ? COR_BOTAO_HOVER : COR_BOTAO );
        
        setStrokeLineWidth( 2 );
        drawRectangle( b.x + 1, b.y + 1, b.largura - 2, b.altura - 2, COR_BOTAO_BORDA );
        setStrokeLineWidth( 1 );
        
        textoCentro( b.texto, b.x + b.largura / 2, b.y + b.altura / 2, 16, COR_TEXTO_MENU );
        
    }
    
    /**
     * Card clicável de um mapa na tela de seleção: fundo, uma miniatura da
     * malha (grama/caminho) do próprio mapa, e o nome embaixo.
     */
    private void desenharCardMapa( Botao b, TipoCelula[][] gradeDoMapa ) {
        
        fillRectangle( b.x, b.y, b.largura, b.altura, b.mouseSobre ? COR_BOTAO_HOVER : COR_BOTAO );
        
        setStrokeLineWidth( 2 );
        drawRectangle( b.x + 1, b.y + 1, b.largura - 2, b.altura - 2, COR_BOTAO_BORDA );
        setStrokeLineWidth( 1 );
        
        double thumbX = b.x + CARD_PADDING;
        double thumbY = b.y + CARD_PADDING;
        
        for ( int l = 0; l < gradeDoMapa.length; l++ ) {
            for ( int c = 0; c < gradeDoMapa[l].length; c++ ) {
                
                double x = thumbX + c * MINI_TILE;
                double y = thumbY + l * MINI_TILE;
                
                Color cor = gradeDoMapa[l][c] == TipoCelula.CAMINHO ? COR_CAMINHO : COR_GRAMA;
                
                fillRectangle( x, y, MINI_TILE, MINI_TILE, cor );
                
            }
        }
        
        double thumbAltura = gradeDoMapa.length * MINI_TILE;
        textoCentro( b.texto, b.x + b.largura / 2, thumbY + thumbAltura + CARD_LABEL_ALTURA / 2.0, 16, COR_TEXTO_MENU );
        
    }
    
    /**
     * Desenha o jogo (já congelado atrás, chamado antes deste método) com um
     * overlay escuro por cima e os botões de continuar/voltar ao menu.
     */
    private void desenharPausa() {
        
        fillRectangle( 0, 0, LARGURA_JANELA, ALTURA_JANELA, COR_OVERLAY_PAUSA );
        
        textoCentro( "PAUSADO", CENTRO_X, ALTURA_JANELA * 0.46 - 50, 32, COR_TEXTO_MENU );
        
        desenharBotao( botaoContinuar );
        desenharBotao( botaoMenuPrincipal );
        
    }
    
    private void desenharJogo() {
        
        clearBackground( COR_FUNDO_JOGO );
        
        int tile = mapa.getTamanhoTile();
        
        for ( int l = 0; l < mapa.getLinhas(); l++ ) {
            for ( int c = 0; c < mapa.getColunas(); c++ ) {
                desenharCelula( c, l, tile );
            }
        }
        
    }
    
    private void desenharCelula( int coluna, int linha, int tile ) {
        
        double x = coluna * tile;
        double y = linha * tile;
        
        if ( grade[linha][coluna] == TipoCelula.CAMINHO ) {
            fillRectangle( x, y, tile, tile, COR_CAMINHO );
            desenharBordasDoCaminho( coluna, linha, x, y, tile );
        } else {
            fillRectangle( x, y, tile, tile, COR_GRAMA );
            desenharGradeSutil( coluna, linha, x, y, tile );
        }
        
    }
    
    /**
     * Linha bem discreta entre células de grama vizinhas, só pra a malha
     * continuar perceptível -- não desenha em cima do caminho, pra ele ficar
     * limpo como na imagem de referência.
     */
    private void desenharGradeSutil( int coluna, int linha, double x, double y, int tile ) {
        
        if ( coluna + 1 < mapa.getColunas() && !ehCaminho( coluna + 1, linha ) ) {
            drawLine( x + tile, y, x + tile, y + tile, COR_GRAMA_LINHA );
        }
        if ( linha + 1 < mapa.getLinhas() && !ehCaminho( coluna, linha + 1 ) ) {
            drawLine( x, y + tile, x + tile, y + tile, COR_GRAMA_LINHA );
        }
        
    }
    
    /**
     * Desenha uma linha clara em cada lado da célula de caminho que faz
     * fronteira com grama (ou com a borda do mapa), imitando o acostamento
     * de uma estrada -- é o que dá o contorno visto na imagem de referência.
     */
    private void desenharBordasDoCaminho( int coluna, int linha, double x, double y, int tile ) {
        
        setStrokeLineWidth( 2 );
        
        if ( !ehCaminho( coluna, linha - 1 ) ) {
            drawLine( x, y, x + tile, y, COR_BORDA_CAMINHO );
        }
        if ( !ehCaminho( coluna, linha + 1 ) ) {
            drawLine( x, y + tile, x + tile, y + tile, COR_BORDA_CAMINHO );
        }
        if ( !ehCaminho( coluna - 1, linha ) ) {
            drawLine( x, y, x, y + tile, COR_BORDA_CAMINHO );
        }
        if ( !ehCaminho( coluna + 1, linha ) ) {
            drawLine( x + tile, y, x + tile, y + tile, COR_BORDA_CAMINHO );
        }
        
        setStrokeLineWidth( 1 );
        
    }
    
    private boolean ehCaminho( int coluna, int linha ) {
        if ( coluna < 0 || coluna >= mapa.getColunas() || linha < 0 || linha >= mapa.getLinhas() ) {
            return false;
        }
        return grade[linha][coluna] == TipoCelula.CAMINHO;
    }
    
    //==========================================================================
    // TEXTO
    //==========================================================================
    
    private double larguraTexto( String s, int tamanho ) {
        setFontName( fonte );
        return measureText( s, tamanho );
    }
    
    /** Texto centralizado horizontal e verticalmente em (cx, cy). */
    private void textoCentro( String s, double cx, double cy, int tamanho, Color cor ) {
        setFontName( fonte );
        double w = larguraTexto( s, tamanho );
        double alturaCaixa = measureTextBounds( "Ag", tamanho ).height;
        drawText( s, Math.round( cx - w / 2 ), Math.round( cy - alturaCaixa / 2 ), tamanho, cor );
    }
    
    public static void main( String[] args ) {
        new JogoTorreDefesa();
    }
    
}
