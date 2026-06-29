package com.servimedical.platform.ambulancia.infrastructure.adapter.out.persistence;

import com.servimedical.platform.ambulancia.domain.model.Ambulancia;
import com.servimedical.platform.ambulancia.domain.port.out.AmbulanciaRepositoryPort;
import com.servimedical.platform.ambulancia.infrastructure.adapter.out.persistence.mapper.AmbulanciaPersistenceMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AmbulanciaPersistenceAdapter implements AmbulanciaRepositoryPort {

  private final AmbulanciaJpaRepository jpaRepository;
  private final AmbulanciaPersistenceMapper mapper;

  @Override
  public Ambulancia save(Ambulancia ambulancia) {
    AmbulanciaJpaEntity saved = jpaRepository.save(mapper.toEntity(ambulancia));
    return mapper.toDomain(saved);
  }

  @Override
  public List<Ambulancia> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Override
  public Optional<Ambulancia> findById(Long id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public boolean existsById(Long id) {
    return jpaRepository.existsById(id);
  }

  @Override
  public void deleteById(Long id) {
    jpaRepository.deleteById(id);
  }
}
