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
  private String esAtencionInicialPacienteRemitidoOControl;
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
  private String primerNombrePropietario;
  private String segundoNombrePropietario;
  private String primerApellidoPropietario;
  private String segundoApellidoPropietario;
  private String direccionResidenciaPropietario;
  private String telefonoResidenciaPropietario;
  private String codigoMunicipioResidenciaPropietario;
  private String tipoDocumentoConductorVehiculo;
  private String numeroDocumentoConductorVehiculo;
  private String primerNombreConductorVehiculo;
  private String segundoNombreConductorVehiculo;
  private String primerApellidoConductorVehiculo;
  private String segundoApellidoConductorVehiculo;
  private String codigoMunicipioResidenciaConductorVehiculo;
  private String direccionResidenciaConductorVehiculo;
  private String telefonoResidenciaConductorVehiculo;
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
