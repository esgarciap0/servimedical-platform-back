package com.servimedical.platform.ambulancia.domain.port.in;

import com.servimedical.platform.ambulancia.domain.model.Ambulancia;

public interface CreateAmbulanciaUseCase {
  Ambulancia create(Ambulancia ambulancia);
}
