package ec.edu.monster.excepciones;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExcepcionMonsterTest {

    @Test
    void usaElMensajePorDefectoDelCatalogo() {
        ExcepcionMonster e = new ExcepcionMonster(CodigoError.ELE_001);

        assertEquals(CodigoError.ELE_001.getMensaje(), e.getMessage());
    }

    @Test
    void elMensajeEspecificoReemplazaAlPorDefecto() {
        ExcepcionMonster e = new ExcepcionMonster(CodigoError.DATA_001, "El campo valor es obligatorio");

        assertEquals("El campo valor es obligatorio", e.getMessage());
        assertEquals(CodigoError.DATA_001, e.getError());
    }

    @Test
    void exponeCodigoYCategoriaDelCatalogo() {
        ExcepcionMonster e = new ExcepcionMonster(CodigoError.ELE_002);

        assertEquals("ELE-002", e.getCodigo());
        assertEquals(CodigoError.Categoria.CONFLICTO, e.getCategoria());
    }
}
