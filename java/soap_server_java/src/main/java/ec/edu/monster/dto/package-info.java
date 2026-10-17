/**
 * Tipos de respuesta del contrato SOAP.
 *
 * {@code @XmlSchema} con {@code elementFormDefault = QUALIFIED} hace que los CAMPOS de los tipos (estado, codigo,
 * mensaje, ...) pertenezcan al namespace del servicio (regla R3 de CONVENCION_SOAP.md). Sin este archivo JAXB los deja
 * SIN namespace y un cliente .NET los leería vacíos. Debe estar en este mismo paquete.
 * (La capa JAX-WS —envoltorio, parámetros y retorno— se califica con {@code targetNamespace} en el servicio.)
 */
@XmlSchema(namespace = EspacioNombres.SERVICIO, elementFormDefault = XmlNsForm.QUALIFIED)
package ec.edu.monster.dto;

import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
