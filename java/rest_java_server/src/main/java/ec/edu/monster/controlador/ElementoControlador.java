package ec.edu.monster.controlador;

import ec.edu.monster.dto.Resultado;
import ec.edu.monster.modelo.Elemento;
import ec.edu.monster.servicios.ElementoServicio;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

/**
 * EJEMPLO de controlador: valida la forma de las entradas (llegan como texto), las convierte a sus tipos reales,
 * delega en el servicio y devuelve siempre un Resultado.
 */
public class ElementoControlador extends ControladorBase {

    private final ElementoServicio elementoServicio;

    @Inject
    public ElementoControlador(ElementoServicio elementoServicio) {
        this.elementoServicio = elementoServicio;
    }

    public Resultado<Elemento> consultar(String codigo) {
        return ejecutar(() -> elementoServicio.consultar(codigo(codigo)));
    }

    public Resultado<List<Elemento>> listar() {
        return ejecutar(elementoServicio::listar);
    }

    public Resultado<Elemento> registrar(String codigo, String nombre, String valor) {
        return ejecutar(() -> {
            String codigoElemento = codigo(codigo);
            String nombreElemento = ValidadorForma.textoHasta(nombre, "nombre", Elemento.LONGITUD_MAXIMA_NOMBRE);
            BigDecimal valorElemento = ValidadorForma.decimalPositivo(valor, "valor",
                    Elemento.MAX_ENTEROS_VALOR, Elemento.MAX_DECIMALES_VALOR);
            return elementoServicio.registrar(codigoElemento, nombreElemento, valorElemento);
        });
    }

    private String codigo(String valor) {
        return ValidadorForma.digitosExactos(valor, "código", Elemento.LONGITUD_CODIGO);
    }
}
