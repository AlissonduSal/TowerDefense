package projetoesdlinear;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Representa um elemento (tile/terreno) do mapa.
 * 
 * Em vez de cadastrar manualmente cada combinação (AC, AE, AS, BC, etc.),
 * a classe analisa dinamicamente o ÚLTIMO CARACTERE do código:
 * - Último caractere 'C' -> Atributo de CAMINHO
 * - Último caractere 'E' -> Atributo de ENTRADA (e caminho)
 * - Último caractere 'S' -> Atributo de SAÍDA (e caminho)
 * - Demais -> Célula padrão (GRAMA)
 * 
 * A classe é extensível: novos atributos/sufixos podem ser adicionados
 * a qualquer momento usando {@link #registrarRegraSufixo(char, ConfiguradorRegra)}.
 */
public class ElementoMapa {

    @FunctionalInterface
    public interface ConfiguradorRegra {
        void configurar( Builder builder );
    }

    public static class Builder {
        private final String codigo;
        private String codigoBase;
        private Character sufixo;
        private TipoCelula tipoCelula = TipoCelula.GRAMA;
        private boolean entrada = false;
        private boolean saida = false;
        private final Map<String, Object> atributos = new HashMap<>();

        public Builder( String codigo ) {
            this.codigo = codigo;
            this.codigoBase = codigo;
        }

        public Builder tipoCelula( TipoCelula tipoCelula ) {
            this.tipoCelula = tipoCelula;
            return this;
        }

        public Builder entrada( boolean entrada ) {
            this.entrada = entrada;
            if ( entrada ) {
                this.tipoCelula = TipoCelula.CAMINHO;
            }
            return this;
        }

        public Builder saida( boolean saida ) {
            this.saida = saida;
            if ( saida ) {
                this.tipoCelula = TipoCelula.CAMINHO;
            }
            return this;
        }

        public Builder atributo( String chave, Object valor ) {
            this.atributos.put( chave, valor );
            return this;
        }

        public ElementoMapa build() {
            return new ElementoMapa( this );
        }
    }

    private static final Map<Character, ConfiguradorRegra> REGRAS_SUFIXO = new ConcurrentHashMap<>();
    private static final Map<String, ElementoMapa> CACHE = new ConcurrentHashMap<>();

    static {
        // Regra para caminho ('C' / 'c')
        registrarRegraSufixo( 'C', builder -> {
            builder.tipoCelula( TipoCelula.CAMINHO );
            builder.atributo( "caminho", true );
        } );

        // Regra para entrada ('E' / 'e')
        registrarRegraSufixo( 'E', builder -> {
            builder.tipoCelula( TipoCelula.CAMINHO );
            builder.entrada( true );
            builder.atributo( "caminho", true );
            builder.atributo( "entrada", true );
        } );

        // Regra para saída ('S' / 's')
        registrarRegraSufixo( 'S', builder -> {
            builder.tipoCelula( TipoCelula.CAMINHO );
            builder.saida( true );
            builder.atributo( "caminho", true );
            builder.atributo( "saida", true );
        } );
    }

    private final String codigo;
    private final String codigoBase;
    private final Character sufixo;
    private final TipoCelula tipoCelula;
    private final boolean entrada;
    private final boolean saida;
    private final Map<String, Object> atributos;

    private ElementoMapa( Builder builder ) {
        this.codigo = builder.codigo;
        this.codigoBase = builder.codigoBase;
        this.sufixo = builder.sufixo;
        this.tipoCelula = builder.tipoCelula;
        this.entrada = builder.entrada;
        this.saida = builder.saida;
        this.atributos = Collections.unmodifiableMap( new HashMap<>( builder.atributos ) );
    }

    /**
     * Permite registrar facilmente novos atributos para qualquer caractere final.
     * Exemplo de uso no futuro:
     * <pre>
     * ElementoMapa.registrarRegraSufixo( 'O', b -> {
     *     b.tipoCelula( TipoCelula.GRAMA );
     *     b.atributo( "obstaculo", true );
     * } );
     * </pre>
     */
    public static void registrarRegraSufixo( char sufixo, ConfiguradorRegra regra ) {
        if ( regra != null ) {
            REGRAS_SUFIXO.put( Character.toUpperCase( sufixo ), regra );
            CACHE.clear();
        }
    }

    /**
     * Analisa dinamicamente o código do elemento inspecionando o seu último caractere.
     */
    public static ElementoMapa obterOuCriar( String codigo ) {
        if ( codigo == null || codigo.trim().isEmpty() ) {
            codigo = "A";
        }

        String cod = codigo.trim();
        String chave = cod.toUpperCase();

        ElementoMapa existente = CACHE.get( chave );
        if ( existente != null ) {
            return existente;
        }

        Builder builder = new Builder( cod );
        char ultimoChar = Character.toUpperCase( cod.charAt( cod.length() - 1 ) );

        ConfiguradorRegra regra = REGRAS_SUFIXO.get( ultimoChar );
        if ( regra != null ) {
            builder.sufixo = ultimoChar;
            builder.codigoBase = cod.length() > 1 ? cod.substring( 0, cod.length() - 1 ) : cod;
            regra.configurar( builder );
        } else {
            // Sem sufixo especial: elemento base normal (ex: Grama A, A1, B, B2...)
            builder.codigoBase = cod;
            builder.tipoCelula( TipoCelula.GRAMA );
        }

        ElementoMapa novo = builder.build();
        CACHE.put( chave, novo );
        return novo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCodigoBase() {
        return codigoBase;
    }

    public Character getSufixo() {
        return sufixo;
    }

    public TipoCelula getTipoCelula() {
        return tipoCelula;
    }

    public boolean isEntrada() {
        return entrada;
    }

    public boolean isSaida() {
        return saida;
    }

    public boolean isCaminho() {
        return tipoCelula == TipoCelula.CAMINHO;
    }

    public boolean temAtributo( String chave ) {
        return atributos.containsKey( chave );
    }

    public Object getAtributo( String chave ) {
        return atributos.get( chave );
    }

    public Map<String, Object> getAtributos() {
        return atributos;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( !( o instanceof ElementoMapa ) ) return false;
        ElementoMapa that = (ElementoMapa) o;
        return Objects.equals( codigo.toUpperCase(), that.codigo.toUpperCase() );
    }

    @Override
    public int hashCode() {
        return codigo.toUpperCase().hashCode();
    }

    @Override
    public String toString() {
        return codigo;
    }

}
