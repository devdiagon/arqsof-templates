package ec.edu.monster.api;

import ec.edu.monster.dto.Resultado;
import ec.edu.monster.excepciones.CodigoError;

/**
 * Decide el código HTTP de una respuesta (R7): 200 en todo éxito y, en un error, el HTTP de la
 * categoría de su entrada del catálogo. Nunca depende del código individual: agregar un error nuevo
 * solo exige agregar una entrada al catálogo.
 */
public final class CodigoHttp {

    public static final int OK = 200;

    private CodigoHttp() {
    }

    public static int deCategoria(CodigoError.Categoria categoria) {
        return switch (categoria) {
            case VALIDACION -> 400;
            case NO_AUTORIZADO -> 401;
            case NO_ENCONTRADO -> 404;
            case METODO_NO_PERMITIDO -> 405;
            case CONFLICTO -> 409;
            case TIPO_NO_SOPORTADO -> 415;
            case OPERACION_INVALIDA -> 422;
            case TECNICO -> 500;
        };
    }

    /** HTTP de una respuesta ya mapeada: 200 si el estado es OK; si no, el de la categoría del código. */
    public static int deRespuesta(String estado, String codigo) {
        if (Resultado.Estado.OK.name().equals(estado)) {
            return OK;
        }
        for (CodigoError error : CodigoError.values()) {
            if (error.getCodigo().equals(codigo)) {
                return deCategoria(error.getCategoria());
            }
        }
        return deCategoria(CodigoError.Categoria.TECNICO);
    }
}
