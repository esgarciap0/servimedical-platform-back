package com.servimedical.platform.aph.application.service;

import com.servimedical.platform.aph.domain.exception.AphNotFoundException;
import com.servimedical.platform.aph.domain.model.Aph;
import com.servimedical.platform.aph.domain.port.in.CreateAphUseCase;
import com.servimedical.platform.aph.domain.port.in.DeleteAphUseCase;
import com.servimedical.platform.aph.domain.port.in.FindAphUseCase;
import com.servimedical.platform.aph.domain.port.in.UpdateAphUseCase;
import com.servimedical.platform.aph.domain.port.out.AphRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that orchestrates APH use cases.
 * Depends only on domain ports — no JPA, no DTOs, no HTTP.
 */
@Service
@RequiredArgsConstructor
public class AphService
        implements CreateAphUseCase, FindAphUseCase, UpdateAphUseCase, DeleteAphUseCase {

  private final AphRepositoryPort repository;

  @Override
  @Transactional
  public Aph create(Aph aph) {
    LocalDateTime now = LocalDateTime.now();
    aph.setId(null);
    aph.setCreatedAt(now);
    aph.setUpdatedAt(now);
    return repository.save(aph);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Aph> findAll() {
    return repository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Aph findById(Long id) {
    return repository.findById(id).orElseThrow(() -> new AphNotFoundException(id));
  }

  @Override
  @Transactional
  public Aph update(Long id, Aph changes) {
    Aph existing = repository.findById(id).orElseThrow(() -> new AphNotFoundException(id));
    applyChanges(existing, changes);
    existing.setUpdatedAt(LocalDateTime.now());
    return repository.save(existing);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    if (!repository.existsById(id)) {
      throw new AphNotFoundException(id);
    }
    repository.deleteById(id);
  }

  private void applyChanges(Aph target, Aph src) {
    target.setCodigo(src.getCodigo());
    target.setMovil(src.getMovil());
    target.setPlaca(src.getPlaca());
    target.setTraslado(src.getTraslado());
    target.setTipoTraslado(src.getTipoTraslado());
    target.setPrioridad(src.getPrioridad());
    target.setFechaAccidente(src.getFechaAccidente());
    target.setHoraAccidente(src.getHoraAccidente());
    target.setLugarOcurrencia(src.getLugarOcurrencia());
    target.setNaturalezaEvento(src.getNaturalezaEvento());
    target.setEstadoAseguramiento(src.getEstadoAseguramiento());
    target.setPlacaVehiculo(src.getPlacaVehiculo());
    target.setTipoVehiculo(src.getTipoVehiculo());
    target.setCodigoAseguradora(src.getCodigoAseguradora());
    target.setNumeroPolizaSoat(src.getNumeroPolizaSoat());
    target.setFechaInicioVigencia(src.getFechaInicioVigencia());
    target.setFechaFinVigencia(src.getFechaFinVigencia());
    target.setNumeroRadicadoSiras(src.getNumeroRadicadoSiras());
    target.setTipoDocumentoPropietario(src.getTipoDocumentoPropietario());
    target.setNumeroDocumentoPropietario(src.getNumeroDocumentoPropietario());
    target.setDescripcionOtroEvento(src.getDescripcionOtroEvento());
    target.setCondicionVictima(src.getCondicionVictima());
    target.setCodigoMunicipioOcurrencia(src.getCodigoMunicipioOcurrencia());
    target.setZonaOrigen(src.getZonaOrigen());
    target.setDepartamentoOrigen(src.getDepartamentoOrigen());
    target.setMunicipioOrigen(src.getMunicipioOrigen());
    target.setDocumento(src.getDocumento());
    target.setTipoDocumento(src.getTipoDocumento());
    target.setPrimerApellido(src.getPrimerApellido());
    target.setSegundoApellido(src.getSegundoApellido());
    target.setPrimerNombre(src.getPrimerNombre());
    target.setSegundoNombre(src.getSegundoNombre());
    target.setEstadoCivil(src.getEstadoCivil());
    target.setOcupacion(src.getOcupacion());
    target.setSexo(src.getSexo());
    target.setFechaNacimiento(src.getFechaNacimiento());
    target.setEdad(src.getEdad());
    target.setCelular(src.getCelular());
    target.setTelefono(src.getTelefono());
    target.setTipoPoblacion(src.getTipoPoblacion());
    target.setAcompanante(src.getAcompanante());
    target.setCelularAcompanante(src.getCelularAcompanante());
    target.setAvisarA(src.getAvisarA());
    target.setParentesco(src.getParentesco());
    target.setNumeroParaAvisar(src.getNumeroParaAvisar());
    target.setNumeroParaAvisar2(src.getNumeroParaAvisar2());
    target.setDireccion(src.getDireccion());
    target.setCodigoMunicipioResidencia(src.getCodigoMunicipioResidencia());
    target.setZonaPaciente(src.getZonaPaciente());
    target.setDepartamento(src.getDepartamento());
    target.setCiudad(src.getCiudad());
    target.setAlergia(src.getAlergia());
    target.setPatologicos(src.getPatologicos());
    target.setMedicacion(src.getMedicacion());
    target.setLiquidos(src.getLiquidos());
    target.setAseguradora(src.getAseguradora());
    target.setPoliza(src.getPoliza());
    target.setPlanBeneficios(src.getPlanBeneficios());
    target.setHoraLlegada(src.getHoraLlegada());
    target.setTransportadoA(src.getTransportadoA());
    target.setCodigoHabilitacion(src.getCodigoHabilitacion());
    target.setDepartamentoTraslado(src.getDepartamentoTraslado());
    target.setCiudadTransporte(src.getCiudadTransporte());
    target.setEstadoPaciente(src.getEstadoPaciente());
    target.setCausaExterna(src.getCausaExterna());
    target.setPresion(src.getPresion());
    target.setFrecuenciaCardiaca(src.getFrecuenciaCardiaca());
    target.setFrecuenciaRespiratoria(src.getFrecuenciaRespiratoria());
    target.setTemperatura(src.getTemperatura());
    target.setRo(src.getRo());
    target.setRv(src.getRv());
    target.setRm(src.getRm());
    target.setHallazgos(src.getHallazgos());
    target.setDiagnosticos(src.getDiagnosticos());
    target.setLesiones(src.getLesiones());
    target.setLesionesImagen(src.getLesionesImagen());
    target.setProcedimientos(src.getProcedimientos());
    target.setMateriales(src.getMateriales());
    target.setConductor(src.getConductor());
    target.setDocumentoConductor(src.getDocumentoConductor());
    target.setParamedico(src.getParamedico());
    target.setDocumentoParamedico(src.getDocumentoParamedico());
    target.setMedico(src.getMedico());
    target.setDocumentoMedico(src.getDocumentoMedico());
  }
}
