package com.servimedical.platform.aph.domain.port.out;

import com.servimedical.platform.aph.domain.model.Aph;
import java.util.List;
import java.util.Optional;

public interface AphRepositoryPort {

  Aph save(Aph aph);

  Optional<Aph> findById(Long id);

  List<Aph> findAll();

  boolean existsById(Long id);

  void deleteById(Long id);
}
