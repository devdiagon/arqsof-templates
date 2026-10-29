package ec.edu.monster.excepciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CodigoErrorTest {

    @Test
    void cadaEntradaTieneCodigoMensajeYCategoria() {
        for (CodigoError error : CodigoError.values()) {
            assertTrue(error.getCodigo().matches("[A-Z]{2,4}-\\d{3}"),
                    "Código con formato inválido: " + error.getCodigo());
            assertFalse(error.getMensaje().isBlank(), "Sin mensaje: " + error);
            assertNotNull(error.getCategoria(), "Sin categoría: " + error);
        }
    }

    @Test
    void losCodigosNoSeRepiten() {
        Set<String> codigos = new HashSet<>();
        for (CodigoError error : CodigoError.values()) {
            assertTrue(codigos.add(error.getCodigo()), "Código repetido: " + error.getCodigo());
        }
    }

    @Test
    void err500EsTecnicoConMensajeGenerico() {
        assertEquals("ERR-500", CodigoError.ERR_500.getCodigo());
        assertEquals("Error interno del servidor", CodigoError.ERR_500.getMensaje());
        assertEquals(CodigoError.Categoria.TECNICO, CodigoError.ERR_500.getCategoria());
    }

    @Test
    void data001EsDeValidacion() {
        assertEquals("DATA-001", CodigoError.DATA_001.getCodigo());
        assertEquals(CodigoError.Categoria.VALIDACION, CodigoError.DATA_001.getCategoria());
    }
}
