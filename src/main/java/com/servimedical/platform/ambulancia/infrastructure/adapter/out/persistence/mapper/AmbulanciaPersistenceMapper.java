package com.servimedical.platform.ambulancia.infrastructure.adapter.out.persistence.mapper;

import com.servimedical.platform.ambulancia.domain.model.Ambulancia;
import com.servimedical.platform.ambulancia.infrastructure.adapter.out.persistence.AmbulanciaJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AmbulanciaPersistenceMapper {

  public AmbulanciaJpaEntity toEntity(Ambulancia a) {
    if (a == null) return null;
    return AmbulanciaJpaEntity.builder()
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

  public Ambulancia toDomain(AmbulanciaJpaEntity e) {
    if (e == null) return null;
    return Ambulancia.builder()
            .id(e.getId())
            .movil(e.getMovil())
            .placa(e.getPlaca())
            .conductor(e.getConductor())
            .documentoConductor(e.getDocumentoConductor())
            .paramedico(e.getParamedico())
            .documentoParamedico(e.getDocumentoParamedico())
            .tipoTraslado(e.getTipoTraslado())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .build();
  }
}
