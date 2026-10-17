/**
 * DTO de las respuestas del servidor.
 *
 * {@code @XmlSchema} con {@code elementFormDefault = QUALIFIED} hace que los CAMPOS de los DTO (estado, codigo,
 * mensaje, ...) pertenezcan al namespace del servicio (regla R3 de CONVENCION_SOAP.md). Sin este archivo JAXB los
 * busca SIN namespace y, como el servidor los envía calificados, todos llegan {@code null} sin error.
 * Debe estar en este mismo paquete. (La capa JAX-WS —envoltorio, parámetros y retorno— se califica con
 * {@code targetNamespace} en el contrato.)
 */
@XmlSchema(namespace = EspacioNombres.SERVICIO, elementFormDefault = XmlNsForm.QUALIFIED)
package ec.edu.monster.dto;

import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
