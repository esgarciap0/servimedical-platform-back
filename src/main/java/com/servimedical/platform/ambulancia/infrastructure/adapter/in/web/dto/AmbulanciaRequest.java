package com.servimedical.platform.ambulancia.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AmbulanciaRequest {

  @NotBlank(message = "Móvil es obligatorio")
  private String movil;

  @NotBlank(message = "Placa es obligatorio")
  private String placa;

  private String conductor;
  private String documentoConductor;
  private String paramedico;
  private String documentoParamedico;
  private String tipoTraslado;
}
