package com.servimedical.platform.aph.infrastructure.adapter.in.web;

import com.servimedical.platform.aph.domain.port.in.CreateAphUseCase;
import com.servimedical.platform.aph.domain.port.in.DeleteAphUseCase;
import com.servimedical.platform.aph.domain.port.in.FindAphUseCase;
import com.servimedical.platform.aph.domain.port.in.GenerateAphPdfUseCase;
import com.servimedical.platform.aph.domain.port.in.UpdateAphUseCase;
import com.servimedical.platform.aph.infrastructure.adapter.in.web.dto.AphRequest;
import com.servimedical.platform.aph.infrastructure.adapter.in.web.dto.AphResponse;
import com.servimedical.platform.aph.infrastructure.adapter.in.web.mapper.AphWebMapper;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Driving (inbound) adapter that exposes the APH use cases over HTTP.
 * Depends only on the inbound ports and a web-layer mapper.
 */
@RestController
@RequestMapping("/api/aph")
@RequiredArgsConstructor
public class AphController {

  private final CreateAphUseCase createUseCase;
  private final FindAphUseCase findUseCase;
  private final UpdateAphUseCase updateUseCase;
  private final DeleteAphUseCase deleteUseCase;
  private final GenerateAphPdfUseCase generatePdfUseCase;
  private final AphWebMapper mapper;

  @PostMapping
  public ResponseEntity<AphResponse> create(@Valid @RequestBody AphRequest request) {
    var saved = createUseCase.create(mapper.toDomain(request));
    return ResponseEntity.ok(mapper.toResponse(saved));
  }

  @GetMapping
  public ResponseEntity<List<AphResponse>> findAll() {
    var responses = findUseCase.findAll().stream().map(mapper::toResponse).toList();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<AphResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(mapper.toResponse(findUseCase.findById(id)));
  }

  @PutMapping("/{id}")
  public ResponseEntity<AphResponse> update(@PathVariable Long id,
                                            @Valid @RequestBody AphRequest request) {
    var updated = updateUseCase.update(id, mapper.toDomain(request));
    return ResponseEntity.ok(mapper.toResponse(updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    deleteUseCase.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/pdf")
  public ResponseEntity<ByteArrayResource> downloadPdf(@PathVariable Long id) {
    byte[] pdfBytes = generatePdfUseCase.generatePdf(id);
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=aph_" + id + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(new ByteArrayResource(pdfBytes));
  }
}
