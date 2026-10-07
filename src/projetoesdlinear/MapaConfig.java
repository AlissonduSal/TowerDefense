package projetoesdlinear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Descreve um mapa do jogo: dimensões da malha, elementos (tiles) do mapa
 * e o caminho percorrido pelos inimigos.
 * 
 * Agora construído a partir de uma matriz visual de elementos (códigos como
 * A, A1, AC, AE, AS), onde:
 * - O percurso, entradas e saídas são detectados automaticamente na matriz.
 * - Suporta ramificações/bifurcações de caminhos.
 * - Converte automaticamente em waypoints (cotovelos) para compatibilidade com o jogo.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class MapaConfig {
    
    private static final int TAMANHO_TILE_PADRAO = 40;
    
    private final String id;
    private final String nome;
    private final int colunas;
    private final int linhas;
    private final int tamanhoTile;
    
    private final ElementoMapa[][] gradeElementos;
    private final List<Coordenada> entradas;
    private final List<Coordenada> saidas;
    private final List<List<Coordenada>> rotas;
    private final List<Coordenada> waypoints;
    private final Set<Coordenada> celulasCaminho;
    
    /**
     * Construtor principal a partir de uma matriz bidimensional de elementos.
     */
    public MapaConfig( String id, String nome, int tamanhoTile, String[][] matriz ) {
        
        if ( matriz == null || matriz.length == 0 || matriz[0].length == 0 ) {
            throw new IllegalArgumentException( "A matriz do mapa não pode ser nula ou vazia." );
        }
        
        this.id = id;
        this.nome = nome;
        this.linhas = matriz.length;
        this.colunas = matriz[0].length;
        this.tamanhoTile = tamanhoTile > 0 ? tamanhoTile : TAMANHO_TILE_PADRAO;
        
        for ( int l = 1; l < this.linhas; l++ ) {
            if ( matriz[l].length != this.colunas ) {
                throw new IllegalArgumentException( String.format(
                    "Linha %d possui %d colunas, mas a primeira linha possui %d colunas.",
                    l, matriz[l].length, this.colunas
                ) );
            }
        }
        
        this.gradeElementos = new ElementoMapa[linhas][colunas];
        this.celulasCaminho = new LinkedHashSet<>();
        this.entradas = new ArrayList<>();
        this.saidas = new ArrayList<>();
        
        for ( int l = 0; l < linhas; l++ ) {
            for ( int c = 0; c < colunas; c++ ) {
                ElementoMapa el = ElementoMapa.obterOuCriar( matriz[l][c] );
                gradeElementos[l][c] = el;
                Coordenada coord = new Coordenada( c, l );
                
                if ( el.isCaminho() ) {
                    celulasCaminho.add( coord );
                }
                if ( el.isEntrada() ) {
                    entradas.add( coord );
                }
                if ( el.isSaida() ) {
                    saidas.add( coord );
                }
            }
        }
        
        if ( entradas.isEmpty() && !celulasCaminho.isEmpty() ) {
            // Fallback se não marcada explicitamente: primeira célula de caminho encontrada
            entradas.add( celulasCaminho.iterator().next() );
        }
        if ( saidas.isEmpty() && !celulasCaminho.isEmpty() ) {
            // Fallback: última célula de caminho encontrada
            Coordenada ultima = null;
            for ( Coordenada c : celulasCaminho ) {
                ultima = c;
            }
            if ( ultima != null ) {
                saidas.add( ultima );
            }
        }
        
        // Traça todas as rotas (incluindo possíveis ramificações/bifurcações)
        this.rotas = calcularRotas( entradas, saidas, celulasCaminho, colunas, linhas );
        
        // Calcula waypoints (curvas/cotovelos) da rota principal
        if ( !rotas.isEmpty() ) {
            this.waypoints = extrairWaypoints( rotas.get( 0 ) );
        } else if ( !celulasCaminho.isEmpty() ) {
            this.waypoints = new ArrayList<>( celulasCaminho );
        } else {
            this.waypoints = Collections.emptyList();
        }
        
    }
    
    /**
     * Construtor a partir de array de linhas de texto (com elementos separados por espaço).
     */
    public MapaConfig( String id, String nome, int tamanhoTile, String[] layoutLinhas ) {
        this( id, nome, tamanhoTile, layoutParaMatriz( layoutLinhas ) );
    }
    
    /**
     * Construtor de conveniência com tamanho de tile padrão.
     */
    public MapaConfig( String id, String nome, String[] layoutLinhas ) {
        this( id, nome, TAMANHO_TILE_PADRAO, layoutParaMatriz( layoutLinhas ) );
    }
    
    /**
     * Construtor de conveniência com tamanho de tile padrão para matriz 2D.
     */
    public MapaConfig( String id, String nome, String[][] matriz ) {
        this( id, nome, TAMANHO_TILE_PADRAO, matriz );
    }
    
    /**
     * Construtor legado com waypoints manuais (preservado para compatibilidade).
     */
    public MapaConfig( String id, String nome, int colunas, int linhas,
            int tamanhoTile, List<Coordenada> waypoints ) {
        
        this.id = id;
        this.nome = nome;
        this.colunas = colunas;
        this.linhas = linhas;
        this.tamanhoTile = tamanhoTile;
        this.waypoints = waypoints != null ? new ArrayList<>( waypoints ) : Collections.emptyList();
        this.celulasCaminho = expandirCaminho( this.waypoints );
        
        this.entradas = new ArrayList<>();
        this.saidas = new ArrayList<>();
        if ( !this.waypoints.isEmpty() ) {
            this.entradas.add( this.waypoints.get( 0 ) );
            this.saidas.add( this.waypoints.get( this.waypoints.size() - 1 ) );
        }
        
        this.gradeElementos = new ElementoMapa[linhas][colunas];
        for ( int l = 0; l < linhas; l++ ) {
            for ( int c = 0; c < colunas; c++ ) {
                Coordenada coord = new Coordenada( c, l );
                if ( coord.equals( getEntrada() ) ) {
                    gradeElementos[l][c] = ElementoMapa.obterOuCriar( "AE" );
                } else if ( coord.equals( getSaida() ) ) {
                    gradeElementos[l][c] = ElementoMapa.obterOuCriar( "AS" );
                } else if ( celulasCaminho.contains( coord ) ) {
                    gradeElementos[l][c] = ElementoMapa.obterOuCriar( "AC" );
                } else {
                    gradeElementos[l][c] = ElementoMapa.obterOuCriar( "A" );
                }
            }
        }
        
        this.rotas = new ArrayList<>();
        if ( !celulasCaminho.isEmpty() ) {
            rotas.add( new ArrayList<>( celulasCaminho ) );
        }
        
    }
    
    private static String[][] layoutParaMatriz( String[] layoutLinhas ) {
        if ( layoutLinhas == null || layoutLinhas.length == 0 ) {
            throw new IllegalArgumentException( "Layout do mapa não pode ser nulo ou vazio." );
        }
        String[][] matriz = new String[layoutLinhas.length][];
        for ( int i = 0; i < layoutLinhas.length; i++ ) {
            String linha = layoutLinhas[i].trim();
            if ( linha.isEmpty() ) {
                throw new IllegalArgumentException( "A linha " + i + " do layout está vazia." );
            }
            matriz[i] = linha.split( "\\s+" );
        }
        return matriz;
    }
    
    /**
     * Calcula todas as rotas conectadas entre as entradas e as saídas,
     * suportando bifurcações/ramificações.
     */
    private static List<List<Coordenada>> calcularRotas( List<Coordenada> entradas, List<Coordenada> saidas,
            Set<Coordenada> celulasCaminho, int colunas, int linhas ) {
        
        List<List<Coordenada>> todasRotas = new ArrayList<>();
        Set<Coordenada> conjuntoSaidas = new HashSet<>( saidas );
        
        for ( Coordenada entrada : entradas ) {
            buscarRotas( entrada, new LinkedHashSet<>(), new ArrayList<>(), conjuntoSaidas,
                    celulasCaminho, colunas, linhas, todasRotas );
        }
        
        return todasRotas;
    }
    
    private static void buscarRotas( Coordenada atual, Set<Coordenada> visitados, List<Coordenada> caminhoAtual,
            Set<Coordenada> saidas, Set<Coordenada> celulasCaminho,
            int colunas, int linhas, List<List<Coordenada>> todasRotas ) {
        
        caminhoAtual.add( atual );
        visitados.add( atual );
        
        if ( saidas.contains( atual ) && caminhoAtual.size() > 1 ) {
            todasRotas.add( new ArrayList<>( caminhoAtual ) );
        } else {
            // Vizinhos ortogonais (direita, baixo, esquerda, cima)
            int[] dc = { 1, 0, -1, 0 };
            int[] dl = { 0, 1, 0, -1 };
            
            for ( int i = 0; i < 4; i++ ) {
                int nc = atual.coluna + dc[i];
                int nl = atual.linha + dl[i];
                Coordenada vizinho = new Coordenada( nc, nl );
                
                if ( nc >= 0 && nc < colunas && nl >= 0 && nl < linhas
                        && celulasCaminho.contains( vizinho )
                        && !visitados.contains( vizinho ) ) {
                    buscarRotas( vizinho, visitados, caminhoAtual, saidas,
                            celulasCaminho, colunas, linhas, todasRotas );
                }
            }
        }
        
        visitados.remove( atual );
        caminhoAtual.remove( caminhoAtual.size() - 1 );
    }
    
    /**
     * Extrai os cotovelos / pontos de curva (waypoints) de uma rota ortogonal ordenada.
     */
    private static List<Coordenada> extrairWaypoints( List<Coordenada> rota ) {
        if ( rota == null || rota.isEmpty() ) {
            return Collections.emptyList();
        }
        if ( rota.size() <= 2 ) {
            return new ArrayList<>( rota );
        }
        
        List<Coordenada> wps = new ArrayList<>();
        wps.add( rota.get( 0 ) );
        
        for ( int i = 1; i < rota.size() - 1; i++ ) {
            Coordenada ant = rota.get( i - 1 );
            Coordenada atual = rota.get( i );
            Coordenada prox = rota.get( i + 1 );
            
            int dc1 = atual.coluna - ant.coluna;
            int dl1 = atual.linha - ant.linha;
            int dc2 = prox.coluna - atual.coluna;
            int dl2 = prox.linha - atual.linha;
            
            // Se mudou de direção na malha, este ponto é uma curva (waypoint)
            if ( dc1 != dc2 || dl1 != dl2 ) {
                wps.add( atual );
            }
        }
        
        wps.add( rota.get( rota.size() - 1 ) );
        return wps;
    }
    
    /**
     * Expande os waypoints legados em células contínuas ortogonais.
     */
    private static Set<Coordenada> expandirCaminho( List<Coordenada> waypoints ) {
        
        Set<Coordenada> celulas = new LinkedHashSet<>();
        
        for ( int i = 0; i < waypoints.size() - 1; i++ ) {
            
            Coordenada a = waypoints.get( i );
            Coordenada b = waypoints.get( i + 1 );
            
            if ( a.coluna == b.coluna ) {
                int passo = b.linha >= a.linha ? 1 : -1;
                for ( int l = a.linha; l != b.linha + passo; l += passo ) {
                    celulas.add( new Coordenada( a.coluna, l ) );
                }
            } else if ( a.linha == b.linha ) {
                int passo = b.coluna >= a.coluna ? 1 : -1;
                for ( int c = a.coluna; c != b.coluna + passo; c += passo ) {
                    celulas.add( new Coordenada( c, a.linha ) );
                }
            } else {
                throw new IllegalArgumentException(
                        "waypoints " + a + " e " + b + " não estão alinhados "
                        + "(o caminho só anda na horizontal ou na vertical)" );
            }
            
        }
        
        return celulas;
        
    }
    
    /** Monta a malha inteira do mapa (grama/caminho) para o motor do jogo. */
    public TipoCelula[][] paraGrade() {
        
        TipoCelula[][] grade = new TipoCelula[linhas][colunas];
        
        for ( int l = 0; l < linhas; l++ ) {
            for ( int c = 0; c < colunas; c++ ) {
                grade[l][c] = gradeElementos[l][c].getTipoCelula();
            }
        }
        
        return grade;
        
    }
    
    public ElementoMapa[][] getElementosGrade() {
        return gradeElementos;
    }
    
    public ElementoMapa getElemento( int coluna, int linha ) {
        if ( coluna >= 0 && coluna < colunas && linha >= 0 && linha < linhas ) {
            return gradeElementos[linha][coluna];
        }
        return ElementoMapa.obterOuCriar( "A" );
    }
    
    public boolean ehCaminho( int coluna, int linha ) {
        return celulasCaminho.contains( new Coordenada( coluna, linha ) );
    }
    
    /** Célula onde os inimigos entram no mapa (primeira entrada). */
    public Coordenada getEntrada() {
        if ( !entradas.isEmpty() ) {
            return entradas.get( 0 );
        }
        return !waypoints.isEmpty() ? waypoints.get( 0 ) : null;
    }
    
    /** Célula onde os inimigos saem do mapa / base (primeira saída). */
    public Coordenada getSaida() {
        if ( !saidas.isEmpty() ) {
            return saidas.get( 0 );
        }
        return !waypoints.isEmpty() ? waypoints.get( waypoints.size() - 1 ) : null;
    }
    
    public List<Coordenada> getEntradas() {
        return entradas;
    }
    
    public List<Coordenada> getSaidas() {
        return saidas;
    }
    
    public List<List<Coordenada>> getRotas() {
        return rotas;
    }
    
    public String getId() {
        return id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public int getColunas() {
        return colunas;
    }
    
    public int getLinhas() {
        return linhas;
    }
    
    public int getTamanhoTile() {
        return tamanhoTile;
    }
    
    public List<Coordenada> getWaypoints() {
        return waypoints;
    }
    
}
