package com.servimedical.platform.aph.infrastructure.adapter.out.persistence;

import com.servimedical.platform.aph.domain.model.Aph;
import com.servimedical.platform.aph.domain.port.out.AphRepositoryPort;
import com.servimedical.platform.aph.infrastructure.adapter.out.persistence.mapper.AphPersistenceMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Driven (outbound) adapter that implements the {@link AphRepositoryPort}
 * using Spring Data JPA. The application layer depends only on the port.
 */
@Component
@RequiredArgsConstructor
public class AphPersistenceAdapter implements AphRepositoryPort {

  private final AphJpaRepository repository;
  private final AphPersistenceMapper mapper;

  @Override
  public Aph save(Aph aph) {
    var saved = repository.save(mapper.toEntity(aph));
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Aph> findById(Long id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Aph> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Override
  public boolean existsById(Long id) {
    return repository.existsById(id);
  }

  @Override
  public void deleteById(Long id) {
    repository.deleteById(id);
  }
}
