package com.servimedical.platform.ambulancia.infrastructure.adapter.in.web.mapper;

import com.servimedical.platform.ambulancia.domain.model.Ambulancia;
import com.servimedical.platform.ambulancia.infrastructure.adapter.in.web.dto.AmbulanciaRequest;
import com.servimedical.platform.ambulancia.infrastructure.adapter.in.web.dto.AmbulanciaResponse;
import org.springframework.stereotype.Component;

@Component
public class AmbulanciaWebMapper {

  public Ambulancia toDomain(AmbulanciaRequest r) {
    if (r == null) return null;
    return Ambulancia.builder()
            .movil(r.getMovil())
            .placa(r.getPlaca())
            .conductor(r.getConductor())
            .documentoConductor(r.getDocumentoConductor())
            .paramedico(r.getParamedico())
            .documentoParamedico(r.getDocumentoParamedico())
            .tipoTraslado(r.getTipoTraslado())
            .build();
  }

  public AmbulanciaResponse toResponse(Ambulancia a) {
    if (a == null) return null;
    return AmbulanciaResponse.builder()
            .id(a.getId())
            .movil(a.getMovil())
            .placa(a.getPlaca())
            .conductor(a.getConductor())
            .documentoConductor(a.getDocumentoConductor())
            .paramedico(a.getParamedico())
            .documentoParamedico(a.getDocumentoParamedico())
            .tipoTraslado(a.getTipoTraslado())
            .createdAt(a.getCreatedAt())
            .updatedAt(a.getUpdatedAt())
            .build();
  }
}
