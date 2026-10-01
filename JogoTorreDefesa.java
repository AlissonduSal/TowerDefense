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
 * Na tela JOGANDO tem também: um painel de torres embaixo (igual o menu de
 * seleção do Bloons), o dinheiro do jogador, e três Manequim espalhados
 * pelo mapa só pra testar as torres atacando. Clicar num botão do painel
 * seleciona o tipo de torre; clicar numa célula de grama livre compra e
 * posiciona uma torre nova ali; clicar numa torre já colocada com o mesmo
 * tipo selecionado compra outra e funde (Torre.aplicarMelhoria()) --
 * exatamente a mecânica de melhoria por fusão que o Torre.java já tinha.
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
    
    // uma cor por tipo de torre, só pra diferenciar visualmente por enquanto
    // (ainda não tem arte de verdade)
    private static final Color COR_TORRE_BANCO = new Color( 212, 175, 55 );
    private static final Color COR_TORRE_TACADOR = new Color( 90, 140, 220 );
    private static final Color COR_TORRE_BOXEADOR = new Color( 200, 70, 60 );
    private static final Color COR_TORRE_GENERICA = new Color( 150, 150, 150 );
    private static final Color COR_BORDA_TORRE = new Color( 20, 20, 20 );
    
    // manequins: uma bolinha colorida por manequim, só pra diferenciar
    private static final Color[] CORES_MANEQUINS = {
        new Color( 220, 70, 70 ), new Color( 70, 120, 220 ), new Color( 230, 200, 60 )
    };
    private static final Color COR_BORDA_MANEQUIM = new Color( 20, 20, 20 );
    
    // painel de torres (embaixo, estilo Bloons) e HUD
    private static final Color COR_PAINEL_FUNDO = new Color( 24, 24, 26 );
    private static final Color COR_BOTAO_SELECIONADO = new Color( 88, 166, 63 );
    private static final Color COR_BOTAO_DESABILITADO = new Color( 40, 40, 40 );
    private static final Color COR_PREVIEW_ALCANCE = new Color( 255, 255, 255, 60 );
    
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
    
    private static final int DINHEIRO_INICIAL = 20; // dá exatamente pro Banco de dinheiro
    private static final double ALTURA_PAINEL = 74;
    
    private int dinheiro;
    private List<Torre> torresPosicionadas;
    private Torre tipoDeTorreSelecionada;
    private List<Botao> botoesTorres;
    private List<Manequim> manequins;
    
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
        botoesTorres = new ArrayList<>();
        
        montarBotoesDosMapas();
        montarBotoesDeTorres();
        
    }
    
    /** Um botão por torre em Torres.disponiveis(), lado a lado no painel de baixo. */
    private void montarBotoesDeTorres() {
        
        List<Torre> disponiveis = Torres.disponiveis();
        
        double largura = 160;
        double altura = 54;
        double espaco = 12;
        double larguraTotal = disponiveis.size() * largura + ( disponiveis.size() - 1 ) * espaco;
        double x = CENTRO_X - larguraTotal / 2;
        double y = ALTURA_JANELA - ALTURA_PAINEL + ( ALTURA_PAINEL - altura ) / 2.0;
        
        for ( Torre t : disponiveis ) {
            botoesTorres.add( new Botao( x, y, largura, altura, t.getNome() ) );
            x += largura + espaco;
        }
        
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
        
        dinheiro = DINHEIRO_INICIAL;
        torresPosicionadas = new ArrayList<>();
        tipoDeTorreSelecionada = null;
        
        posicionarManequins();
        
    }
    
    /**
     * Espalha os três manequins de teste pela malha, cada um perto de uma
     * fração diferente da largura do mapa (20%, 50%, 80%), procurando a
     * célula de grama livre mais próxima daquele ponto.
     */
    private void posicionarManequins() {
        
        manequins = new ArrayList<>();
        double[] fracoesDeColuna = { 0.2, 0.5, 0.8 };
        
        for ( int i = 0; i < fracoesDeColuna.length; i++ ) {
            
            int colunaAlvo = (int) ( mapa.getColunas() * fracoesDeColuna[i] );
            int linhaAlvo = mapa.getLinhas() / 2;
            int[] posicao = encontrarCelulaDeGramaMaisProxima( colunaAlvo, linhaAlvo );
            
            Manequim m = new Manequim( "Manequim " + ( i + 1 ) );
            m.posicionarEm( posicao[0], posicao[1] );
            manequins.add( m );
            
        }
        
    }
    
    /**
     * Varre a malha em anéis cada vez maiores a partir de (coluna, linha)
     * até achar uma célula de grama livre -- assim os manequins sempre
     * acham um lugar válido, não importa o desenho do mapa.
     */
    private int[] encontrarCelulaDeGramaMaisProxima( int coluna, int linha ) {
        
        int raioMaximo = mapa.getColunas() + mapa.getLinhas();
        
        for ( int raio = 0; raio < raioMaximo; raio++ ) {
            for ( int dl = -raio; dl <= raio; dl++ ) {
                for ( int dc = -raio; dc <= raio; dc++ ) {
                    
                    int c = coluna + dc;
                    int l = linha + dl;
                    
                    if ( c >= 0 && c < mapa.getColunas() && l >= 0 && l < mapa.getLinhas()
                            && grade[l][c] == TipoCelula.GRAMA ) {
                        return new int[]{ c, l };
                    }
                    
                }
            }
        }
        
        return new int[]{ 0, 0 }; // não deveria acontecer num mapa com grama sobrando
        
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
                
                List<Torre> tiposDisponiveis = Torres.disponiveis();
                for ( int i = 0; i < botoesTorres.size(); i++ ) {
                    Botao b = botoesTorres.get( i );
                    b.mouseSobre = b.contem( mx, my );
                    mouseSobreAlgumBotao = mouseSobreAlgumBotao || b.mouseSobre;
                    if ( clicou && b.mouseSobre ) {
                        Torre prototipo = tiposDisponiveis.get( i );
                        tipoDeTorreSelecionada = ( tipoDeTorreSelecionada == prototipo ) ? null : prototipo;
                    }
                }
                
                // clique no mapa (fora do painel de baixo) com uma torre selecionada
                if ( clicou && tipoDeTorreSelecionada != null && my < ALTURA_JANELA - ALTURA_PAINEL ) {
                    tentarColocarOuMelhorarTorre( mx, my );
                }
                
                atualizarTorres( delta );
                
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
    // TORRES E COMBATE
    //==========================================================================
    
    /**
     * Célula clicada no mapa com um tipo de torre selecionado no painel:
     * se estiver livre (e for grama), compra e posiciona uma torre nova;
     * se já tiver uma torre do MESMO tipo, compra outra e funde nela
     * (Torre.aplicarMelhoria()) -- essa é a mecânica de melhoria por
     * fusão. Não faz nada se não tiver dinheiro, se a célula for caminho,
     * se tiver uma torre de outro tipo, ou se a torre já estiver no nível
     * máximo.
     */
    private void tentarColocarOuMelhorarTorre( int mx, int my ) {
        
        int tile = mapa.getTamanhoTile();
        int coluna = mx / tile;
        int linha = my / tile;
        
        if ( coluna < 0 || coluna >= mapa.getColunas() || linha < 0 || linha >= mapa.getLinhas() ) {
            return;
        }
        
        if ( grade[linha][coluna] == TipoCelula.CAMINHO ) {
            return;
        }
        
        int custo = tipoDeTorreSelecionada.getCustoBase();
        if ( dinheiro < custo ) {
            return;
        }
        
        Torre torreNaCelula = encontrarTorreEm( coluna, linha );
        
        if ( torreNaCelula != null ) {
            
            if ( !torreNaCelula.ehMesmoTipo( tipoDeTorreSelecionada )
                    || torreNaCelula.getNivel() >= torreNaCelula.getNivelMaximo() ) {
                return;
            }
            
            dinheiro -= custo;
            torreNaCelula.aplicarMelhoria( Torres.criarNova( tipoDeTorreSelecionada ) );
            
        } else {
            
            dinheiro -= custo;
            Torre novaTorre = Torres.criarNova( tipoDeTorreSelecionada );
            novaTorre.posicionarEm( coluna, linha );
            torresPosicionadas.add( novaTorre );
            
        }
        
    }
    
    private Torre encontrarTorreEm( int coluna, int linha ) {
        
        for ( Torre t : torresPosicionadas ) {
            if ( t.getColuna() == coluna && t.getLinha() == linha ) {
                return t;
            }
        }
        
        return null;
        
    }
    
    /**
     * A cada frame: o Banco de dinheiro avança seu próprio cooldown e
     * gera dinheiro quando pronto; as demais torres avançam o cooldown de
     * ataque e, quando prontas, atacam o manequim vivo mais próximo que
     * estiver dentro do alcance.
     */
    private void atualizarTorres( double delta ) {
        
        for ( Torre torre : torresPosicionadas ) {
            
            if ( torre instanceof BancoDeDinheiro ) {
                
                BancoDeDinheiro banco = (BancoDeDinheiro) torre;
                banco.atualizarCooldownGeracao( delta );
                if ( banco.prontoParaGerar() ) {
                    dinheiro += banco.getDinheiroGerado();
                    banco.registrarGeracao();
                }
                continue;
                
            }
            
            torre.atualizarCooldown( delta );
            if ( torre.podeAtacar() ) {
                Manequim alvo = encontrarAlvoMaisProximo( torre );
                if ( alvo != null ) {
                    alvo.receberDano( torre.getDano() );
                    torre.registrarAtaque();
                }
            }
            
        }
        
    }
    
    /** O manequim vivo mais próximo desta torre que ainda está dentro do alcance dela. */
    private Manequim encontrarAlvoMaisProximo( Torre torre ) {
        
        Manequim maisProximo = null;
        double menorDistancia = Double.MAX_VALUE;
        
        for ( Manequim m : manequins ) {
            
            if ( m.estaMorto() ) {
                continue;
            }
            
            double dx = m.getColuna() - torre.getColuna();
            double dy = m.getLinha() - torre.getLinha();
            double distancia = Math.sqrt( dx * dx + dy * dy );
            
            if ( distancia <= torre.getAlcance() && distancia < menorDistancia ) {
                menorDistancia = distancia;
                maisProximo = m;
            }
            
        }
        
        return maisProximo;
        
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
                desenharCenaDoJogo( true );
                break;
            case PAUSADO:
                desenharCenaDoJogo( false );
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
    
    /**
     * Tudo da tela de jogo, nesta ordem: malha, preview do alcance (só se
     * interativo -- na tela de pausa não tem sentido), manequins, torres
     * colocadas, painel de compra e o HUD de dinheiro. O botão de pausa
     * só aparece quando interativo; na pausa quem desenha por cima é
     * desenharPausa(), chamado depois deste método.
     */
    private void desenharCenaDoJogo( boolean interativo ) {
        
        desenharJogo();
        
        if ( interativo ) {
            desenharPreviewDeAlcance();
        }
        
        desenharManequins();
        desenharTorres();
        desenharPainelDeTorres();
        desenharHud();
        
        if ( interativo ) {
            desenharBotao( botaoPausar );
        }
        
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
    
    /** Círculo translúcido mostrando o alcance da torre selecionada, na célula sob o mouse. */
    private void desenharPreviewDeAlcance() {
        
        if ( tipoDeTorreSelecionada == null ) {
            return;
        }
        
        int mx = getMouseX();
        int my = getMouseY();
        
        if ( my >= ALTURA_JANELA - ALTURA_PAINEL ) {
            return;
        }
        
        int tile = mapa.getTamanhoTile();
        int coluna = mx / tile;
        int linha = my / tile;
        
        double cx = coluna * tile + tile / 2.0;
        double cy = linha * tile + tile / 2.0;
        double raio = tipoDeTorreSelecionada.getAlcance() * tile;
        
        fillCircle( cx, cy, raio, COR_PREVIEW_ALCANCE );
        
    }
    
    /** As torres coloridas por tipo, com o nível empilhado escrito no meio se for > 0. */
    private void desenharTorres() {
        
        int tile = mapa.getTamanhoTile();
        
        for ( Torre t : torresPosicionadas ) {
            
            double x = t.getColuna() * tile;
            double y = t.getLinha() * tile;
            
            fillRectangle( x + 4, y + 4, tile - 8, tile - 8, corDaTorre( t ) );
            setStrokeLineWidth( 2 );
            drawRectangle( x + 4, y + 4, tile - 8, tile - 8, COR_BORDA_TORRE );
            setStrokeLineWidth( 1 );
            
            if ( t.getNivel() > 0 ) {
                textoCentro( "" + t.getNivel(), x + tile / 2.0, y + tile / 2.0, 14, COR_TEXTO_MENU );
            }
            
        }
        
    }
    
    /** Cor de cada tipo de torre -- placeholder até ter arte de verdade. */
    private Color corDaTorre( Torre t ) {
        
        if ( t instanceof BancoDeDinheiro ) {
            return COR_TORRE_BANCO;
        } else if ( t instanceof TacadorDeBolinha ) {
            return COR_TORRE_TACADOR;
        } else if ( t instanceof Boxeador ) {
            return COR_TORRE_BOXEADOR;
        }
        
        return COR_TORRE_GENERICA;
        
    }
    
    /** Os manequins de teste, como bolinhas coloridas com a vida escrita em cima. */
    private void desenharManequins() {
        
        int tile = mapa.getTamanhoTile();
        
        for ( int i = 0; i < manequins.size(); i++ ) {
            
            Manequim m = manequins.get( i );
            if ( m.estaMorto() ) {
                continue;
            }
            
            double cx = m.getColuna() * tile + tile / 2.0;
            double cy = m.getLinha() * tile + tile / 2.0;
            double raio = tile * 0.35;
            
            fillCircle( cx, cy, raio + 2, COR_BORDA_MANEQUIM );
            fillCircle( cx, cy, raio, CORES_MANEQUINS[ i % CORES_MANEQUINS.length ] );
            
            textoCentro( m.getVidaAtual() + "/" + m.getVidaMaxima(), cx, cy - raio - 12, 11, COR_TEXTO_MENU );
            
        }
        
    }
    
    /**
     * Painel de baixo, estilo Bloons: um botão por torre disponível,
     * mostrando nome e custo, destacando a selecionada e apagando as que
     * o jogador não tem dinheiro pra comprar.
     */
    private void desenharPainelDeTorres() {
        
        fillRectangle( 0, ALTURA_JANELA - ALTURA_PAINEL, LARGURA_JANELA, ALTURA_PAINEL, COR_PAINEL_FUNDO );
        
        setStrokeLineWidth( 2 );
        drawLine( 0, ALTURA_JANELA - ALTURA_PAINEL, LARGURA_JANELA, ALTURA_JANELA - ALTURA_PAINEL, COR_BOTAO_BORDA );
        setStrokeLineWidth( 1 );
        
        List<Torre> disponiveis = Torres.disponiveis();
        for ( int i = 0; i < botoesTorres.size(); i++ ) {
            desenharBotaoDeTorre( botoesTorres.get( i ), disponiveis.get( i ) );
        }
        
    }
    
    private void desenharBotaoDeTorre( Botao b, Torre prototipo ) {
        
        boolean selecionada = tipoDeTorreSelecionada == prototipo;
        boolean temDinheiro = dinheiro >= prototipo.getCustoBase();
        
        Color fundo;
        if ( selecionada ) {
            fundo = COR_BOTAO_SELECIONADO;
        } else if ( !temDinheiro ) {
            fundo = COR_BOTAO_DESABILITADO;
        } else {
            fundo = b.mouseSobre ? COR_BOTAO_HOVER : COR_BOTAO;
        }
        
        fillRectangle( b.x, b.y, b.largura, b.altura, fundo );
        
        setStrokeLineWidth( selecionada ? 3 : 2 );
        drawRectangle( b.x + 1, b.y + 1, b.largura - 2, b.altura - 2, corDaTorre( prototipo ) );
        setStrokeLineWidth( 1 );
        
        textoCentro( prototipo.getNome(), b.x + b.largura / 2, b.y + 17, 12, COR_TEXTO_MENU );
        textoCentro( "$" + prototipo.getCustoBase(), b.x + b.largura / 2, b.y + 38, 13, COR_TEXTO_MENU );
        
    }
    
    private void desenharHud() {
        texto( "Dinheiro: $" + dinheiro, 16, 16, 18, COR_TEXTO_MENU );
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
    
    /** Texto simples, ancorado no canto superior esquerdo em (x, y). */
    private void texto( String s, double x, double y, int tamanho, Color cor ) {
        setFontName( fonte );
        drawText( s, Math.round( x ), Math.round( y ), tamanho, cor );
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
