package com.servimedical.platform.controller;

import com.servimedical.platform.dto.AphRequest;
import com.servimedical.platform.dto.AphResponse;
import com.servimedical.platform.service.AphService;
import com.servimedical.platform.service.PdfService;
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

@RestController
@RequestMapping("/api/aph")
@RequiredArgsConstructor
public class AphController {

  private final AphService service;
  private final PdfService pdfService;

  @PostMapping
  public ResponseEntity<AphResponse> create(@Valid @RequestBody AphRequest request) {
    return ResponseEntity.ok(service.create(request));
  }

  @GetMapping
  public ResponseEntity<List<AphResponse>> findAll() {
    return ResponseEntity.ok(service.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<AphResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(service.findById(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<AphResponse> update(@PathVariable Long id, @Valid @RequestBody AphRequest request) {
    return ResponseEntity.ok(service.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/pdf")
  public ResponseEntity<ByteArrayResource> downloadPdf(@PathVariable Long id) {
    var resource = pdfService.generatePdf(id);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=aph_" + id + ".pdf")
        .contentType(MediaType.APPLICATION_PDF)
        .body(resource);
  }
}
