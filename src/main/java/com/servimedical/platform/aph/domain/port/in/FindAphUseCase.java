package com.servimedical.platform.aph.domain.port.in;

import com.servimedical.platform.aph.domain.model.Aph;
import java.util.List;

public interface FindAphUseCase {

  Aph findById(Long id);

  List<Aph> findAll();
}
