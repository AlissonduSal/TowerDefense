package projetoesdlinear;

/**
 * Botão retangular clicável, simples e reaproveitável entre as telas do
 * jogo (menu, seleção de mapa, e as que vierem depois). Esta classe só
 * cuida da área, do hover e da posição -- quem desenha decide a aparência,
 * e quem chama contem() decide o que acontece no clique.
 * 
 * @author Prof. Dr. David Buzatto
 */
public class Botao {
    
    public double x, y, largura, altura;
    public String texto;
    public boolean mouseSobre;
    
    public Botao( double x, double y, double largura, double altura, String texto ) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.texto = texto;
    }
    
    public boolean contem( double px, double py ) {
        return px >= x && px <= x + largura && py >= y && py <= y + altura;
    }
    
}
