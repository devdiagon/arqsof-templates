/**
 * Convención SOAP (R3): todos los elementos de los DTO van CALIFICADOS con el namespace del servicio.
 * Sin este archivo JAXB los espera sin calificar y los campos llegan {@code null} sin ningún error.
 * NO borrar. Todo DTO nuevo debe estar en este paquete.
 */
@XmlSchema(namespace = ConfiguracionServicio.NAMESPACE, elementFormDefault = XmlNsForm.QUALIFIED)
package ec.edu.monster.dto;

import ec.edu.monster.ws.ConfiguracionServicio;
import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
