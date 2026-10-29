package ec.edu.monster.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.dto.Resultado;
import ec.edu.monster.excepciones.CodigoError;
import org.junit.jupiter.api.Test;

class CodigoHttpTest {

    @Test
    void todoExitoEsHttp200() {
        assertEquals(200, CodigoHttp.deRespuesta(Resultado.Estado.OK.name(), Resultado.CODIGO_EXITO));
    }

    @Test
    void cadaCategoriaTieneSuHttp() {
        assertEquals(400, CodigoHttp.deCategoria(CodigoError.Categoria.VALIDACION));
        assertEquals(401, CodigoHttp.deCategoria(CodigoError.Categoria.NO_AUTORIZADO));
        assertEquals(404, CodigoHttp.deCategoria(CodigoError.Categoria.NO_ENCONTRADO));
        assertEquals(405, CodigoHttp.deCategoria(CodigoError.Categoria.METODO_NO_PERMITIDO));
        assertEquals(409, CodigoHttp.deCategoria(CodigoError.Categoria.CONFLICTO));
        assertEquals(415, CodigoHttp.deCategoria(CodigoError.Categoria.TIPO_NO_SOPORTADO));
        assertEquals(422, CodigoHttp.deCategoria(CodigoError.Categoria.OPERACION_INVALIDA));
        assertEquals(500, CodigoHttp.deCategoria(CodigoError.Categoria.TECNICO));
    }

    @Test
    void cadaCodigoDelCatalogoSaleConElHttpDeSuCategoria() {
        assertEquals(400, CodigoHttp.deRespuesta("ERROR", CodigoError.DATA_001.getCodigo()));
        assertEquals(404, CodigoHttp.deRespuesta("ERROR", CodigoError.ELE_001.getCodigo()));
        assertEquals(409, CodigoHttp.deRespuesta("ERROR", CodigoError.ELE_002.getCodigo()));
        assertEquals(404, CodigoHttp.deRespuesta("ERROR", CodigoError.ERR_404.getCodigo()));
        assertEquals(405, CodigoHttp.deRespuesta("ERROR", CodigoError.ERR_405.getCodigo()));
        assertEquals(415, CodigoHttp.deRespuesta("ERROR", CodigoError.ERR_415.getCodigo()));
        assertEquals(500, CodigoHttp.deRespuesta("ERROR", CodigoError.ERR_500.getCodigo()));
    }

    @Test
    void todaEntradaDelCatalogoTieneHttpDeError() {
        for (CodigoError error : CodigoError.values()) {
            int http = CodigoHttp.deRespuesta("ERROR", error.getCodigo());
            assertEquals(CodigoHttp.deCategoria(error.getCategoria()), http, error.getCodigo());
            assertTrue(http >= 400, "un error nunca sale 2xx: " + error.getCodigo());
        }
    }

    @Test
    void unCodigoDesconocidoSaleComoErrorTecnico() {
        assertEquals(500, CodigoHttp.deRespuesta("ERROR", "XXX-999"));
        assertEquals(500, CodigoHttp.deRespuesta("ERROR", null));
    }
}
