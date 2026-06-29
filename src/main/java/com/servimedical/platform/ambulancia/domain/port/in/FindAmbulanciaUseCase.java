package com.servimedical.platform.ambulancia.domain.port.in;

import com.servimedical.platform.ambulancia.domain.model.Ambulancia;
import java.util.List;

public interface FindAmbulanciaUseCase {
  List<Ambulancia> findAll();
  Ambulancia findById(Long id);
}
