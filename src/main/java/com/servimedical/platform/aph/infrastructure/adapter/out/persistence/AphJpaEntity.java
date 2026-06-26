package com.servimedical.platform.aph.infrastructure.adapter.out.persistence;

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

/**
 * JPA persistence entity for APH. Lives in the outbound adapter layer.
 * The domain model {@link com.servimedical.platform.aph.domain.model.Aph}
 * is decoupled from this representation.
 */
@Entity
@Table(name = "aph")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AphJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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
  private String naturalezaEvento;
  private String estadoAseguramiento;
  private String placaVehiculo;
  private String tipoVehiculo;
  private String codigoAseguradora;
  private String numeroPolizaSoat;
  private String fechaInicioVigencia;
  private String fechaFinVigencia;
  private String numeroRadicadoSiras;
  private String tipoDocumentoPropietario;
  private String numeroDocumentoPropietario;
  @Column(length = 30)
  private String primerNombrePropietario;
  @Column(length = 30)
  private String segundoNombrePropietario;
  private String descripcionOtroEvento;
  private String condicionVictima;
  private String codigoMunicipioOcurrencia;
  private String zonaOrigen;
  private String departamentoOrigen;
  private String municipioOrigen;

  private String documento;
  private String tipoDocumento;
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
  private String tipoPoblacion;

  private String acompanante;
  private String celularAcompanante;
  @Column(name = "avisar_a")
  private String avisarA;
  private String parentesco;
  private String numeroParaAvisar;
  private String numeroParaAvisar2;

  private String direccion;
  private String codigoMunicipioResidencia;
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
  @Column(name = "transportado_a")
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

  @Column(columnDefinition = "TEXT")
  private String hallazgos;

  @Column(columnDefinition = "TEXT")
  private String diagnosticos;

  @Column(columnDefinition = "TEXT")
  private String lesiones;

  @Column(columnDefinition = "TEXT")
  private String lesionesImagen;

  @Column(columnDefinition = "TEXT")
  private String procedimientos;

  @Column(columnDefinition = "TEXT")
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
