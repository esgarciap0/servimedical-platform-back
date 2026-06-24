package com.servimedical.platform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servimedical.platform.dto.AphRequest;
import com.servimedical.platform.dto.AphResponse;
import com.servimedical.platform.entity.Aph;
import com.servimedical.platform.repository.AphRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AphService {

  private final AphRepository repository;
  private final ObjectMapper objectMapper;

  @Transactional
  public AphResponse create(AphRequest request) {
    var entity = toEntity(request);
    entity.setCreatedAt(LocalDateTime.now());
    entity.setUpdatedAt(LocalDateTime.now());
    var saved = repository.save(entity);
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<AphResponse> findAll() {
    return repository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public AphResponse findById(Long id) {
    var entity = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("APH no encontrado con id: " + id));
    return toResponse(entity);
  }

  @Transactional
  public AphResponse update(Long id, AphRequest request) {
    var entity = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("APH no encontrado con id: " + id));
    updateEntity(entity, request);
    entity.setUpdatedAt(LocalDateTime.now());
    var saved = repository.save(entity);
    return toResponse(saved);
  }

  @Transactional
  public void delete(Long id) {
    if (!repository.existsById(id)) {
      throw new RuntimeException("APH no encontrado con id: " + id);
    }
    repository.deleteById(id);
  }

  private Aph toEntity(AphRequest r) {
    var entity = new Aph();
    updateEntity(entity, r);
    return entity;
  }

  private void updateEntity(Aph entity, AphRequest r) {
    entity.setCodigo(r.getCodigo());
    entity.setMovil(r.getMovil());
    entity.setPlaca(r.getPlaca());
    entity.setTraslado(r.getTraslado());
    entity.setTipoTraslado(r.getTipoTraslado());
    entity.setPrioridad(r.getPrioridad());
    entity.setFechaAccidente(r.getFechaAccidente());
    entity.setHoraAccidente(r.getHoraAccidente());
    entity.setLugarOcurrencia(r.getLugarOcurrencia());
    entity.setZonaOrigen(r.getZonaOrigen());
    entity.setDepartamentoOrigen(r.getDepartamentoOrigen());
    entity.setMunicipioOrigen(r.getMunicipioOrigen());
    entity.setDocumento(r.getDocumento());
    entity.setPrimerApellido(r.getPrimerApellido());
    entity.setSegundoApellido(r.getSegundoApellido());
    entity.setPrimerNombre(r.getPrimerNombre());
    entity.setSegundoNombre(r.getSegundoNombre());
    entity.setEstadoCivil(r.getEstadoCivil());
    entity.setOcupacion(r.getOcupacion());
    entity.setSexo(r.getSexo());
    entity.setFechaNacimiento(r.getFechaNacimiento());
    entity.setEdad(r.getEdad());
    entity.setCelular(r.getCelular());
    entity.setTelefono(r.getTelefono());
    entity.setAcompanante(r.getAcompanante());
    entity.setCelularAcompanante(r.getCelularAcompanante());
    entity.setAvisarA(r.getAvisarA());
    entity.setParentesco(r.getParentesco());
    entity.setNumeroParaAvisar(r.getNumeroParaAvisar());
    entity.setNumeroParaAvisar2(r.getNumeroParaAvisar2());
    entity.setDireccion(r.getDireccion());
    entity.setZonaPaciente(r.getZonaPaciente());
    entity.setDepartamento(r.getDepartamento());
    entity.setCiudad(r.getCiudad());
    entity.setAlergia(r.getAlergia());
    entity.setPatologicos(r.getPatologicos());
    entity.setMedicacion(r.getMedicacion());
    entity.setLiquidos(r.getLiquidos());
    entity.setAseguradora(r.getAseguradora());
    entity.setPoliza(r.getPoliza());
    entity.setPlanBeneficios(r.getPlanBeneficios());
    entity.setHoraLlegada(r.getHoraLlegada());
    entity.setTransportadoA(r.getTransportadoA());
    entity.setCodigoHabilitacion(r.getCodigoHabilitacion());
    entity.setDepartamentoTraslado(r.getDepartamentoTraslado());
    entity.setCiudadTransporte(r.getCiudadTransporte());
    entity.setEstadoPaciente(r.getEstadoPaciente());
    entity.setCausaExterna(r.getCausaExterna());
    entity.setPresion(r.getPresion());
    entity.setFrecuenciaCardiaca(r.getFrecuenciaCardiaca());
    entity.setFrecuenciaRespiratoria(r.getFrecuenciaRespiratoria());
    entity.setTemperatura(r.getTemperatura());
    entity.setRo(r.getRo());
    entity.setRv(r.getRv());
    entity.setRm(r.getRm());
    entity.setHallazgos(r.getHallazgos());
    entity.setDiagnosticos(r.getDiagnosticos());
    entity.setLesiones(toJson(r.getLesiones()));
    entity.setLesionesImagen(r.getLesionesImagen());
    entity.setProcedimientos(toJson(r.getProcedimientos()));
    entity.setMateriales(r.getMateriales());
    entity.setConductor(r.getConductor());
    entity.setDocumentoConductor(r.getDocumentoConductor());
    entity.setParamedico(r.getParamedico());
    entity.setDocumentoParamedico(r.getDocumentoParamedico());
    entity.setMedico(r.getMedico());
    entity.setDocumentoMedico(r.getDocumentoMedico());
  }

  public AphResponse toResponse(Aph entity) {
    return AphResponse.builder()
        .id(entity.getId())
        .codigo(entity.getCodigo())
        .movil(entity.getMovil())
        .placa(entity.getPlaca())
        .traslado(entity.getTraslado())
        .tipoTraslado(entity.getTipoTraslado())
        .prioridad(entity.getPrioridad())
        .fechaAccidente(entity.getFechaAccidente())
        .horaAccidente(entity.getHoraAccidente())
        .lugarOcurrencia(entity.getLugarOcurrencia())
        .zonaOrigen(entity.getZonaOrigen())
        .departamentoOrigen(entity.getDepartamentoOrigen())
        .municipioOrigen(entity.getMunicipioOrigen())
        .documento(entity.getDocumento())
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
        .acompanante(entity.getAcompanante())
        .celularAcompanante(entity.getCelularAcompanante())
        .avisarA(entity.getAvisarA())
        .parentesco(entity.getParentesco())
        .numeroParaAvisar(entity.getNumeroParaAvisar())
        .numeroParaAvisar2(entity.getNumeroParaAvisar2())
        .direccion(entity.getDireccion())
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
          : objectMapper.readValue(json, new TypeReference<List<String>>() {});
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }
}
