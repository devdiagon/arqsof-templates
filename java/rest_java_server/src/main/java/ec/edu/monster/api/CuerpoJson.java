package ec.edu.monster.api;

import ec.edu.monster.api.contrato.ConvencionRest;
import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionMonster;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Cuerpo JSON de una petición, leído como texto. Los nombres de campo se buscan sin distinguir
 * mayúsculas y los desconocidos se ignoran (R4). Un campo ausente o nulo se entrega como {@code null},
 * para que el validador del controlador diga «es obligatorio».
 */
public final class CuerpoJson {

    private final Map<String, JsonValue> campos;

    private CuerpoJson(Map<String, JsonValue> campos) {
        this.campos = campos;
    }

    /**
     * @throws ExcepcionMonster DATA-001 si el cuerpo está vacío, no es JSON válido o no es un objeto
     */
    public static CuerpoJson leer(String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            throw cuerpoInvalido();
        }
        try (JsonReader lector = Json.createReader(new StringReader(cuerpo))) {
            if (!(lector.readValue() instanceof JsonObject objeto)) {
                throw cuerpoInvalido();
            }
            Map<String, JsonValue> campos = new HashMap<>();
            objeto.forEach((nombre, valor) -> campos.put(nombre.toLowerCase(Locale.ROOT), valor));
            return new CuerpoJson(campos);
        } catch (JsonException | IllegalStateException e) {
            throw cuerpoInvalido();
        }
    }

    /**
     * Valor del campo como texto. Un texto JSON se entrega tal cual; un número o booleano, con su
     * representación escrita (el validador decide si es válido); un objeto, una lista o un nulo,
     * como {@code null}.
     */
    public String texto(String campo) {
        JsonValue valor = campos.get(campo.toLowerCase(Locale.ROOT));
        if (valor == null) {
            return null;
        }
        return switch (valor.getValueType()) {
            case STRING -> ((JsonString) valor).getString();
            case NUMBER, TRUE, FALSE -> valor.toString();
            default -> null;
        };
    }

    private static ExcepcionMonster cuerpoInvalido() {
        return new ExcepcionMonster(CodigoError.DATA_001, ConvencionRest.MENSAJE_CUERPO_INVALIDO);
    }
}
