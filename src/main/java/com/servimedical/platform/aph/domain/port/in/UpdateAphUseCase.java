package com.servimedical.platform.aph.domain.port.in;

import com.servimedical.platform.aph.domain.model.Aph;

public interface UpdateAphUseCase {

  Aph update(Long id, Aph aph);
}
