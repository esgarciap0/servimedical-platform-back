package com.servimedical.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aph")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aph {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

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
  @Column(name = "avisar_a")
  private String avisarA;
  private String parentesco;
  private String numeroParaAvisar;
  private String numeroParaAvisar2;

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
  @Column(name = "transportado_a")
  private String transportadoA;
  private String codigoHabilitacion;
  private String departamentoTraslado;
  private String ciudadTransporte;
  private String estadoPaciente;

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

  @Column(columnDefinition = "TEXT")
  private String hallazgos;

  @Column(columnDefinition = "TEXT")
  private String diagnosticos;

  /* Lesiones (JSON o separado por comas) */
  @Column(columnDefinition = "TEXT")
  private String lesiones;

  @Column(columnDefinition = "TEXT")
  private String lesionesImagen;

  /* Procedimientos */
  @Column(columnDefinition = "TEXT")
  private String procedimientos;

  /* Materiales */
  @Column(columnDefinition = "TEXT")
  private String materiales;

  /* Tripulacion */
  private String conductor;
  private String documentoConductor;
  private String paramedico;
  private String documentoParamedico;
  private String medico;
  private String documentoMedico;

  /* Auditoria */
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
