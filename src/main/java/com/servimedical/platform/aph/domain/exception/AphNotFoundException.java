package com.servimedical.platform.aph.domain.exception;

public class AphNotFoundException extends RuntimeException {

  public AphNotFoundException(Long id) {
    super("APH no encontrado con id: " + id);
  }
}
