package projetoesdlinear;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Descreve um mapa do jogo configurado a partir de uma matriz visual.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class MapaConfig {
    
    private final String id;
    private final String nome;
    private final int colunas;
    private final int linhas;
    private final int tamanhoTile;
    private final List<Coordenada> waypoints;
    private final Set<Coordenada> celulasCaminho;
    
    public MapaConfig(String id, String nome, int tamanhoTile, String[] layout, Map<String, ElementoMapa> legenda) {
        this.id = id;
        this.nome = nome;
        this.tamanhoTile = tamanhoTile;
        this.linhas = layout.length;
        this.colunas = layout[0].trim().split("\\s+").length;
        
        ElementoMapa[][] gradeStr = new ElementoMapa[linhas][colunas];
        Coordenada entrada = null;
        Coordenada saida = null;
        
        for (int l = 0; l < linhas; l++) {
            String[] tokens = layout[l].trim().split("\\s+");
            for (int c = 0; c < colunas; c++) {
                String token = tokens[c];
                ElementoMapa elemento = legenda.get(token);
                if (elemento == null) {
                    throw new IllegalArgumentException("Elemento não definido na legenda: " + token);
                }
                gradeStr[l][c] = elemento;
                
                if (elemento.isEntrada()) {
                    entrada = new Coordenada(c, l);
                } else if (elemento.isSaida()) {
                    saida = new Coordenada(c, l);
                }
            }
        }
        
        if (entrada == null) throw new IllegalArgumentException("Mapa sem entrada definida!");
        if (saida == null) throw new IllegalArgumentException("Mapa sem saída definida!");
        
        this.waypoints = extrairWaypoints(gradeStr, entrada, saida);
        this.celulasCaminho = expandirCaminho(this.waypoints);
    }
    
    private List<Coordenada> extrairWaypoints(ElementoMapa[][] grade, Coordenada entrada, Coordenada saida) {
        List<Coordenada> pontos = new ArrayList<>();
        pontos.add(entrada);
        
        Coordenada atual = entrada;
        Coordenada anterior = null;
        int dirX = 0;
        int dirY = 0;
        
        while (!atual.equals(saida)) {
            Coordenada proximo = null;
            int[][] vizinhos = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] v : vizinhos) {
                int nc = atual.coluna + v[0];
                int nl = atual.linha + v[1];
                if (nc >= 0 && nc < colunas && nl >= 0 && nl < linhas) {
                    // Evita voltar para a célula anterior
                    if ((anterior == null || !(nc == anterior.coluna && nl == anterior.linha)) &&
                        grade[nl][nc].getTipoCelula() == TipoCelula.CAMINHO) {
                        proximo = new Coordenada(nc, nl);
                        break;
                    }
                }
            }
            
            if (proximo == null) {
                throw new IllegalStateException("Caminho sem saída ou quebrado em " + atual);
            }
            
            int novaDirX = proximo.coluna - atual.coluna;
            int novaDirY = proximo.linha - atual.linha;
            
            if (anterior != null && (novaDirX != dirX || novaDirY != dirY)) {
                pontos.add(atual);
            }
            
            dirX = novaDirX;
            dirY = novaDirY;
            anterior = atual;
            atual = proximo;
        }
        
        pontos.add(saida);
        return pontos;
    }
    
    private static Set<Coordenada> expandirCaminho(List<Coordenada> waypoints) {
        Set<Coordenada> celulas = new LinkedHashSet<>();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Coordenada a = waypoints.get(i);
            Coordenada b = waypoints.get(i + 1);
            if (a.coluna == b.coluna) {
                int passo = b.linha >= a.linha ? 1 : -1;
                for (int l = a.linha; l != b.linha + passo; l += passo) celulas.add(new Coordenada(a.coluna, l));
            } else if (a.linha == b.linha) {
                int passo = b.coluna >= a.coluna ? 1 : -1;
                for (int c = a.coluna; c != b.coluna + passo; c += passo) celulas.add(new Coordenada(c, a.linha));
            }
        }
        return celulas;
    }
    
    public TipoCelula[][] paraGrade() {
        TipoCelula[][] grade = new TipoCelula[linhas][colunas];
        for (int l = 0; l < linhas; l++) {
            for (int c = 0; c < colunas; c++) {
                grade[l][c] = ehCaminho(c, l) ? TipoCelula.CAMINHO : TipoCelula.GRAMA;
            }
        }
        return grade;
    }
    
    public boolean ehCaminho(int coluna, int linha) {
        return celulasCaminho.contains(new Coordenada(coluna, linha));
    }
    
    public Coordenada getEntrada() { return waypoints.get(0); }
    public Coordenada getSaida() { return waypoints.get(waypoints.size() - 1); }
    public String getId() { return id; }
    public String getNome() { return nome; }
    public int getColunas() { return colunas; }
    public int getLinhas() { return linhas; }
    public int getTamanhoTile() { return tamanhoTile; }
    public List<Coordenada> getWaypoints() { return waypoints; }
}
