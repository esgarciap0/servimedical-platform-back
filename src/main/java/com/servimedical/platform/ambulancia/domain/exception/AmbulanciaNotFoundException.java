package com.servimedical.platform.ambulancia.domain.exception;

public class AmbulanciaNotFoundException extends RuntimeException {

  public AmbulanciaNotFoundException(Long id) {
    super("Ambulancia con id " + id + " no encontrada");
  }
}
