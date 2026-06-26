package com.servimedical.platform.aph.infrastructure.adapter.in.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AphResponse {

  private Long id;
  private String codigo;
  private String movil;
  private String placa;
  private String traslado;
  private String tipoTraslado;
  private String prioridad;
  private LocalDate fechaAccidente;
  private LocalTime horaAccidente;
  private String lugarOcurrencia;
  private String zonaOrigen;
  private String departamentoOrigen;
  private String municipioOrigen;

  private String documento;
  private String primerApellido;
  private String segundoApellido;
  private String primerNombre;
  private String segundoNombre;
  private String estadoCivil;
  private String ocupacion;
  private String sexo;
  private LocalDate fechaNacimiento;
  private String edad;
  private String celular;
  private String telefono;

  private String acompanante;
  private String celularAcompanante;
  private String avisarA;
  private String parentesco;
  private String numeroParaAvisar;
  private String numeroParaAvisar2;

  private String direccion;
  private String zonaPaciente;
  private String departamento;
  private String ciudad;

  private String alergia;
  private String patologicos;
  private String medicacion;
  private String liquidos;

  private String aseguradora;
  private String poliza;
  private String planBeneficios;

  private LocalTime horaLlegada;
  private String transportadoA;
  private String codigoHabilitacion;
  private String departamentoTraslado;
  private String ciudadTransporte;
  private String estadoPaciente;

  private String causaExterna;

  private String presion;
  private String frecuenciaCardiaca;
  private String frecuenciaRespiratoria;
  private String temperatura;
  private String ro;
  private String rv;
  private String rm;
  private String hallazgos;
  private String diagnosticos;

  private List<String> lesiones;
  private String lesionesImagen;
  private List<String> procedimientos;

  private String materiales;
  private String conductor;
  private String documentoConductor;
  private String paramedico;
  private String documentoParamedico;
  private String medico;
  private String documentoMedico;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
