package com.servimedical.platform.aph.domain.port.in;

public interface GenerateAphPdfUseCase {

  byte[] generatePdf(Long aphId);
}
