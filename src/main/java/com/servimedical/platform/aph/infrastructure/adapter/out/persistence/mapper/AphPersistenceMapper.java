package com.servimedical.platform.aph.infrastructure.adapter.out.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servimedical.platform.aph.domain.model.Aph;
import com.servimedical.platform.aph.infrastructure.adapter.out.persistence.AphJpaEntity;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Converts between the domain {@link Aph} and the JPA {@link AphJpaEntity}.
 * Encapsulates the JSON serialization of list-valued fields (lesiones, procedimientos),
 * which is a persistence concern that must not leak into the domain.
 */
@Component
@RequiredArgsConstructor
public class AphPersistenceMapper {

  private static final TypeReference<List<String>> LIST_OF_STRING = new TypeReference<>() {};

  private final ObjectMapper objectMapper;

  public AphJpaEntity toEntity(Aph aph) {
    if (aph == null) {
      return null;
    }
    return AphJpaEntity.builder()
            .id(aph.getId())
            .codigo(aph.getCodigo())
            .movil(aph.getMovil())
            .placa(aph.getPlaca())
            .esAtencionInicialPacienteRemitidoOControl(aph.getEsAtencionInicialPacienteRemitidoOControl())
            .traslado(aph.getTraslado())
            .tipoTraslado(aph.getTipoTraslado())
            .prioridad(aph.getPrioridad())
            .fechaAccidente(aph.getFechaAccidente())
            .horaAccidente(aph.getHoraAccidente())
            .lugarOcurrencia(aph.getLugarOcurrencia())
            .naturalezaEvento(aph.getNaturalezaEvento())
            .estadoAseguramiento(aph.getEstadoAseguramiento())
            .placaVehiculo(aph.getPlacaVehiculo())
            .tipoVehiculo(aph.getTipoVehiculo())
            .codigoAseguradora(aph.getCodigoAseguradora())
            .numeroPolizaSoat(aph.getNumeroPolizaSoat())
            .fechaInicioVigencia(aph.getFechaInicioVigencia())
            .fechaFinVigencia(aph.getFechaFinVigencia())
            .numeroRadicadoSiras(aph.getNumeroRadicadoSiras())
            .tipoDocumentoPropietario(aph.getTipoDocumentoPropietario())
            .numeroDocumentoPropietario(aph.getNumeroDocumentoPropietario())
            .primerNombrePropietario(aph.getPrimerNombrePropietario())
            .segundoNombrePropietario(aph.getSegundoNombrePropietario())
            .primerApellidoPropietario(aph.getPrimerApellidoPropietario())
            .segundoApellidoPropietario(aph.getSegundoApellidoPropietario())
            .direccionResidenciaPropietario(aph.getDireccionResidenciaPropietario())
            .telefonoResidenciaPropietario(aph.getTelefonoResidenciaPropietario())
            .codigoMunicipioResidenciaPropietario(aph.getCodigoMunicipioResidenciaPropietario())
            .tipoDocumentoConductorVehiculo(aph.getTipoDocumentoConductorVehiculo())
            .numeroDocumentoConductorVehiculo(aph.getNumeroDocumentoConductorVehiculo())
            .primerNombreConductorVehiculo(aph.getPrimerNombreConductorVehiculo())
            .segundoNombreConductorVehiculo(aph.getSegundoNombreConductorVehiculo())
            .primerApellidoConductorVehiculo(aph.getPrimerApellidoConductorVehiculo())
            .segundoApellidoConductorVehiculo(aph.getSegundoApellidoConductorVehiculo())
            .codigoMunicipioResidenciaConductorVehiculo(aph.getCodigoMunicipioResidenciaConductorVehiculo())
            .direccionResidenciaConductorVehiculo(aph.getDireccionResidenciaConductorVehiculo())
            .telefonoResidenciaConductorVehiculo(aph.getTelefonoResidenciaConductorVehiculo())
            .descripcionOtroEvento(aph.getDescripcionOtroEvento())
            .condicionVictima(aph.getCondicionVictima())
            .codigoMunicipioOcurrencia(aph.getCodigoMunicipioOcurrencia())
            .zonaOrigen(aph.getZonaOrigen())
            .departamentoOrigen(aph.getDepartamentoOrigen())
            .municipioOrigen(aph.getMunicipioOrigen())
            .documento(aph.getDocumento())
            .tipoDocumento(aph.getTipoDocumento())
            .primerApellido(aph.getPrimerApellido())
            .segundoApellido(aph.getSegundoApellido())
            .primerNombre(aph.getPrimerNombre())
            .segundoNombre(aph.getSegundoNombre())
            .estadoCivil(aph.getEstadoCivil())
            .ocupacion(aph.getOcupacion())
            .sexo(aph.getSexo())
            .fechaNacimiento(aph.getFechaNacimiento())
            .edad(aph.getEdad())
            .celular(aph.getCelular())
            .telefono(aph.getTelefono())
            .tipoPoblacion(aph.getTipoPoblacion())
            .acompanante(aph.getAcompanante())
            .celularAcompanante(aph.getCelularAcompanante())
            .avisarA(aph.getAvisarA())
            .parentesco(aph.getParentesco())
            .numeroParaAvisar(aph.getNumeroParaAvisar())
            .numeroParaAvisar2(aph.getNumeroParaAvisar2())
            .direccion(aph.getDireccion())
            .codigoMunicipioResidencia(aph.getCodigoMunicipioResidencia())
            .zonaPaciente(aph.getZonaPaciente())
            .departamento(aph.getDepartamento())
            .ciudad(aph.getCiudad())
            .alergia(aph.getAlergia())
            .patologicos(aph.getPatologicos())
            .medicacion(aph.getMedicacion())
            .liquidos(aph.getLiquidos())
            .aseguradora(aph.getAseguradora())
            .poliza(aph.getPoliza())
            .planBeneficios(aph.getPlanBeneficios())
            .horaLlegada(aph.getHoraLlegada())
            .transportadoA(aph.getTransportadoA())
            .codigoHabilitacion(aph.getCodigoHabilitacion())
            .departamentoTraslado(aph.getDepartamentoTraslado())
            .ciudadTransporte(aph.getCiudadTransporte())
            .estadoPaciente(aph.getEstadoPaciente())
            .causaExterna(aph.getCausaExterna())
            .presion(aph.getPresion())
            .frecuenciaCardiaca(aph.getFrecuenciaCardiaca())
            .frecuenciaRespiratoria(aph.getFrecuenciaRespiratoria())
            .temperatura(aph.getTemperatura())
            .ro(aph.getRo())
            .rv(aph.getRv())
            .rm(aph.getRm())
            .hallazgos(aph.getHallazgos())
            .diagnosticos(aph.getDiagnosticos())
            .lesiones(toJson(aph.getLesiones()))
            .lesionesImagen(aph.getLesionesImagen())
            .procedimientos(toJson(aph.getProcedimientos()))
            .materiales(aph.getMateriales())
            .conductor(aph.getConductor())
            .documentoConductor(aph.getDocumentoConductor())
            .paramedico(aph.getParamedico())
            .documentoParamedico(aph.getDocumentoParamedico())
            .medico(aph.getMedico())
            .documentoMedico(aph.getDocumentoMedico())
            .createdAt(aph.getCreatedAt())
            .updatedAt(aph.getUpdatedAt())
            .build();
  }

  public Aph toDomain(AphJpaEntity entity) {
    if (entity == null) {
      return null;
    }
    return Aph.builder()
            .id(entity.getId())
            .codigo(entity.getCodigo())
            .movil(entity.getMovil())
            .placa(entity.getPlaca())
            .esAtencionInicialPacienteRemitidoOControl(entity.getEsAtencionInicialPacienteRemitidoOControl())
            .traslado(entity.getTraslado())
            .tipoTraslado(entity.getTipoTraslado())
            .prioridad(entity.getPrioridad())
            .fechaAccidente(entity.getFechaAccidente())
            .horaAccidente(entity.getHoraAccidente())
            .lugarOcurrencia(entity.getLugarOcurrencia())
            .naturalezaEvento(entity.getNaturalezaEvento())
            .estadoAseguramiento(entity.getEstadoAseguramiento())
            .placaVehiculo(entity.getPlacaVehiculo())
            .tipoVehiculo(entity.getTipoVehiculo())
            .codigoAseguradora(entity.getCodigoAseguradora())
            .numeroPolizaSoat(entity.getNumeroPolizaSoat())
            .fechaInicioVigencia(entity.getFechaInicioVigencia())
            .fechaFinVigencia(entity.getFechaFinVigencia())
            .numeroRadicadoSiras(entity.getNumeroRadicadoSiras())
            .tipoDocumentoPropietario(entity.getTipoDocumentoPropietario())
            .numeroDocumentoPropietario(entity.getNumeroDocumentoPropietario())
            .primerNombrePropietario(entity.getPrimerNombrePropietario())
            .segundoNombrePropietario(entity.getSegundoNombrePropietario())
            .primerApellidoPropietario(entity.getPrimerApellidoPropietario())
            .segundoApellidoPropietario(entity.getSegundoApellidoPropietario())
            .direccionResidenciaPropietario(entity.getDireccionResidenciaPropietario())
            .telefonoResidenciaPropietario(entity.getTelefonoResidenciaPropietario())
            .codigoMunicipioResidenciaPropietario(entity.getCodigoMunicipioResidenciaPropietario())
            .tipoDocumentoConductorVehiculo(entity.getTipoDocumentoConductorVehiculo())
            .numeroDocumentoConductorVehiculo(entity.getNumeroDocumentoConductorVehiculo())
            .primerNombreConductorVehiculo(entity.getPrimerNombreConductorVehiculo())
            .segundoNombreConductorVehiculo(entity.getSegundoNombreConductorVehiculo())
            .primerApellidoConductorVehiculo(entity.getPrimerApellidoConductorVehiculo())
            .segundoApellidoConductorVehiculo(entity.getSegundoApellidoConductorVehiculo())
            .codigoMunicipioResidenciaConductorVehiculo(entity.getCodigoMunicipioResidenciaConductorVehiculo())
            .direccionResidenciaConductorVehiculo(entity.getDireccionResidenciaConductorVehiculo())
            .telefonoResidenciaConductorVehiculo(entity.getTelefonoResidenciaConductorVehiculo())
            .descripcionOtroEvento(entity.getDescripcionOtroEvento())
            .condicionVictima(entity.getCondicionVictima())
            .codigoMunicipioOcurrencia(entity.getCodigoMunicipioOcurrencia())
            .zonaOrigen(entity.getZonaOrigen())
            .departamentoOrigen(entity.getDepartamentoOrigen())
            .municipioOrigen(entity.getMunicipioOrigen())
            .documento(entity.getDocumento())
            .tipoDocumento(entity.getTipoDocumento())
            .primerApellido(entity.getPrimerApellido())
            .segundoApellido(entity.getSegundoApellido())
            .primerNombre(entity.getPrimerNombre())
            .segundoNombre(entity.getSegundoNombre())
            .estadoCivil(entity.getEstadoCivil())
            .ocupacion(entity.getOcupacion())
            .sexo(entity.getSexo())
            .fechaNacimiento(entity.getFechaNacimiento())
            .edad(entity.getEdad())
            .celular(entity.getCelular())
            .telefono(entity.getTelefono())
            .tipoPoblacion(entity.getTipoPoblacion())
            .acompanante(entity.getAcompanante())
            .celularAcompanante(entity.getCelularAcompanante())
            .avisarA(entity.getAvisarA())
            .parentesco(entity.getParentesco())
            .numeroParaAvisar(entity.getNumeroParaAvisar())
            .numeroParaAvisar2(entity.getNumeroParaAvisar2())
            .direccion(entity.getDireccion())
            .codigoMunicipioResidencia(entity.getCodigoMunicipioResidencia())
            .zonaPaciente(entity.getZonaPaciente())
            .departamento(entity.getDepartamento())
            .ciudad(entity.getCiudad())
            .alergia(entity.getAlergia())
            .patologicos(entity.getPatologicos())
            .medicacion(entity.getMedicacion())
            .liquidos(entity.getLiquidos())
            .aseguradora(entity.getAseguradora())
            .poliza(entity.getPoliza())
            .planBeneficios(entity.getPlanBeneficios())
            .horaLlegada(entity.getHoraLlegada())
            .transportadoA(entity.getTransportadoA())
            .codigoHabilitacion(entity.getCodigoHabilitacion())
            .departamentoTraslado(entity.getDepartamentoTraslado())
            .ciudadTransporte(entity.getCiudadTransporte())
            .estadoPaciente(entity.getEstadoPaciente())
            .causaExterna(entity.getCausaExterna())
            .presion(entity.getPresion())
            .frecuenciaCardiaca(entity.getFrecuenciaCardiaca())
            .frecuenciaRespiratoria(entity.getFrecuenciaRespiratoria())
            .temperatura(entity.getTemperatura())
            .ro(entity.getRo())
            .rv(entity.getRv())
            .rm(entity.getRm())
            .hallazgos(entity.getHallazgos())
            .diagnosticos(entity.getDiagnosticos())
            .lesiones(fromJson(entity.getLesiones()))
            .lesionesImagen(entity.getLesionesImagen())
            .procedimientos(fromJson(entity.getProcedimientos()))
            .materiales(entity.getMateriales())
            .conductor(entity.getConductor())
            .documentoConductor(entity.getDocumentoConductor())
            .paramedico(entity.getParamedico())
            .documentoParamedico(entity.getDocumentoParamedico())
            .medico(entity.getMedico())
            .documentoMedico(entity.getDocumentoMedico())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
  }

  private String toJson(List<String> list) {
    try {
      return list == null ? "[]" : objectMapper.writeValueAsString(list);
    } catch (Exception e) {
      return "[]";
    }
  }

  private List<String> fromJson(String json) {
    try {
      return json == null ? Collections.emptyList()
              : objectMapper.readValue(json, LIST_OF_STRING);
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }
}
