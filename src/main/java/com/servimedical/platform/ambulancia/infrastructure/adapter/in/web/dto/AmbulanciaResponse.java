package com.servimedical.platform.ambulancia.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AmbulanciaResponse {

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
