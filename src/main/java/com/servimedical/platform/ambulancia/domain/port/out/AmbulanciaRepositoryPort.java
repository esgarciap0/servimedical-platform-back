package com.servimedical.platform.ambulancia.domain.port.out;

import com.servimedical.platform.ambulancia.domain.model.Ambulancia;
import java.util.List;
import java.util.Optional;

public interface AmbulanciaRepositoryPort {

  Ambulancia save(Ambulancia ambulancia);

  List<Ambulancia> findAll();

  Optional<Ambulancia> findById(Long id);

  boolean existsById(Long id);

  void deleteById(Long id);
}
