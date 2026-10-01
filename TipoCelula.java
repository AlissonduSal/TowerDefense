package projetoesdlinear;

/**
 * Tipo de cada célula da malha do mapa.
 * 
 * @author Prof. Dr. David Buzatto
 */
public enum TipoCelula {
    
    // célula de grama: livre para o jogador posicionar torres
    GRAMA,
    
    // célula de caminho: por onde os inimigos caminham; não recebe torre
    CAMINHO;
    
}
