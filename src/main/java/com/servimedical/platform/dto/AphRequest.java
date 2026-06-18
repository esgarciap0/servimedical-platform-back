package com.servimedical.platform.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AphRequest {

  /* Datos generales */
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

  /* Datos del paciente */
  @NotBlank(message = "El documento es obligatorio")
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

  /* Acompanante */
  private String acompanante;
  private String celularAcompanante;
  private String avisarA;
  private String parentesco;

  /* Ubicacion */
  private String direccion;
  private String zonaPaciente;
  private String departamento;
  private String ciudad;

  /* Antecedentes */
  private String alergia;
  private String patologicos;
  private String medicacion;
  private String liquidos;

  /* Aseguradora */
  private String aseguradora;
  private String poliza;
  private String planBeneficios;

  /* Datos traslado */
  private LocalTime horaLlegada;
  private String transportadoA;
  private String departamentoTraslado;
  private String ciudadTransporte;

  /* Causa externa */
  private String causaExterna;

  /* Examen fisico */
  private String presion;
  private String frecuenciaCardiaca;
  private String frecuenciaRespiratoria;
  private String temperatura;
  private String ro;
  private String rv;
  private String rm;
  private String hallazgos;
  private String diagnosticos;

  /* Lesiones */
  private List<String> lesiones;

  /* Procedimientos */
  private List<String> procedimientos;

  /* Materiales */
  private String materiales;

  /* Tripulacion */
  private String conductor;
  private String paramedico;
  private String medico;
  private String documentoMedico;
}
