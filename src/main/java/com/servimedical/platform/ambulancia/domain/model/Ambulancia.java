package com.servimedical.platform.ambulancia.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ambulancia {

  private Long id;
  private String movil;
  private String placa;
  private String conductor;
  private String documentoConductor;
  private String paramedico;
  private String documentoParamedico;
  private String tipoTraslado;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
