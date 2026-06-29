package com.servimedical.platform.ambulancia.application.service;

import com.servimedical.platform.ambulancia.domain.exception.AmbulanciaNotFoundException;
import com.servimedical.platform.ambulancia.domain.model.Ambulancia;
import com.servimedical.platform.ambulancia.domain.port.in.CreateAmbulanciaUseCase;
import com.servimedical.platform.ambulancia.domain.port.in.DeleteAmbulanciaUseCase;
import com.servimedical.platform.ambulancia.domain.port.in.FindAmbulanciaUseCase;
import com.servimedical.platform.ambulancia.domain.port.in.UpdateAmbulanciaUseCase;
import com.servimedical.platform.ambulancia.domain.port.out.AmbulanciaRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AmbulanciaService
        implements CreateAmbulanciaUseCase, FindAmbulanciaUseCase, UpdateAmbulanciaUseCase, DeleteAmbulanciaUseCase {

  private final AmbulanciaRepositoryPort repository;

  @Override
  @Transactional
  public Ambulancia create(Ambulancia ambulancia) {
    LocalDateTime now = LocalDateTime.now();
    ambulancia.setId(null);
    ambulancia.setCreatedAt(now);
    ambulancia.setUpdatedAt(now);
    return repository.save(ambulancia);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Ambulancia> findAll() {
    return repository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Ambulancia findById(Long id) {
    return repository.findById(id).orElseThrow(() -> new AmbulanciaNotFoundException(id));
  }

  @Override
  @Transactional
  public Ambulancia update(Long id, Ambulancia changes) {
    Ambulancia existing = repository.findById(id).orElseThrow(() -> new AmbulanciaNotFoundException(id));
    existing.setMovil(changes.getMovil());
    existing.setPlaca(changes.getPlaca());
    existing.setConductor(changes.getConductor());
    existing.setDocumentoConductor(changes.getDocumentoConductor());
    existing.setParamedico(changes.getParamedico());
    existing.setDocumentoParamedico(changes.getDocumentoParamedico());
    existing.setTipoTraslado(changes.getTipoTraslado());
    existing.setUpdatedAt(LocalDateTime.now());
    return repository.save(existing);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    if (!repository.existsById(id)) {
      throw new AmbulanciaNotFoundException(id);
    }
    repository.deleteById(id);
  }
}
