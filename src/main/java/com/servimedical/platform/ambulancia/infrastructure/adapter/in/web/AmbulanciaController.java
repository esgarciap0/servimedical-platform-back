package com.servimedical.platform.ambulancia.infrastructure.adapter.in.web;

import com.servimedical.platform.ambulancia.domain.port.in.CreateAmbulanciaUseCase;
import com.servimedical.platform.ambulancia.domain.port.in.DeleteAmbulanciaUseCase;
import com.servimedical.platform.ambulancia.domain.port.in.FindAmbulanciaUseCase;
import com.servimedical.platform.ambulancia.domain.port.in.UpdateAmbulanciaUseCase;
import com.servimedical.platform.ambulancia.infrastructure.adapter.in.web.dto.AmbulanciaRequest;
import com.servimedical.platform.ambulancia.infrastructure.adapter.in.web.dto.AmbulanciaResponse;
import com.servimedical.platform.ambulancia.infrastructure.adapter.in.web.mapper.AmbulanciaWebMapper;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ambulancias")
@RequiredArgsConstructor
public class AmbulanciaController {

  private final CreateAmbulanciaUseCase createUseCase;
  private final FindAmbulanciaUseCase findUseCase;
  private final UpdateAmbulanciaUseCase updateUseCase;
  private final DeleteAmbulanciaUseCase deleteUseCase;
  private final AmbulanciaWebMapper mapper;

  @PostMapping
  public ResponseEntity<AmbulanciaResponse> create(@Valid @RequestBody AmbulanciaRequest request) {
    var saved = createUseCase.create(mapper.toDomain(request));
    return ResponseEntity.ok(mapper.toResponse(saved));
  }

  @GetMapping
  public ResponseEntity<List<AmbulanciaResponse>> findAll() {
    var responses = findUseCase.findAll().stream().map(mapper::toResponse).toList();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<AmbulanciaResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(mapper.toResponse(findUseCase.findById(id)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<AmbulanciaResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody AmbulanciaRequest request) {
    var updated = updateUseCase.update(id, mapper.toDomain(request));
    return ResponseEntity.ok(mapper.toResponse(updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    deleteUseCase.delete(id);
    return ResponseEntity.noContent().build();
  }
}
