package ec.edu.monster.excepciones;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Errores de plataforma REST dados de alta en el catálogo (CONVENCION_REST.md 2.1). */
class CodigoErrorPlataformaTest {

    @Test
    void err404EsDeCategoriaNoEncontrado() {
        assertEquals("ERR-404", CodigoError.ERR_404.getCodigo());
        assertEquals("Ruta no encontrada", CodigoError.ERR_404.getMensaje());
        assertEquals(CodigoError.Categoria.NO_ENCONTRADO, CodigoError.ERR_404.getCategoria());
    }

    @Test
    void err405TieneSuPropiaCategoria() {
        assertEquals("ERR-405", CodigoError.ERR_405.getCodigo());
        assertEquals("Método no permitido", CodigoError.ERR_405.getMensaje());
        assertEquals(CodigoError.Categoria.METODO_NO_PERMITIDO, CodigoError.ERR_405.getCategoria());
    }

    @Test
    void err415TieneSuPropiaCategoria() {
        assertEquals("ERR-415", CodigoError.ERR_415.getCodigo());
        assertEquals("Tipo de contenido no soportado", CodigoError.ERR_415.getMensaje());
        assertEquals(CodigoError.Categoria.TIPO_NO_SOPORTADO, CodigoError.ERR_415.getCategoria());
    }
}
