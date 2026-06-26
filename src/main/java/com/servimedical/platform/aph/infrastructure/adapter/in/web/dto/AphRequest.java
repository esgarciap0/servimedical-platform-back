package com.servimedical.platform.aph.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AphRequest {

  @NotBlank(message = "Código es obligatorio")
  private String codigo;
  @NotBlank(message = "Móvil es obligatorio")
  private String movil;
  @NotBlank(message = "Placa es obligatorio")
  private String placa;
  @NotBlank(message = "Traslado es obligatorio")
  private String traslado;
  @NotBlank(message = "Tipo de traslado es obligatorio")
  private String tipoTraslado;
  @NotBlank(message = "Prioridad es obligatorio")
  private String prioridad;
  @NotNull(message = "Fecha de accidente es obligatorio")
  private LocalDate fechaAccidente;
  @NotNull(message = "Hora de accidente es obligatorio")
  private LocalTime horaAccidente;
  @NotBlank(message = "Lugar de ocurrencia es obligatorio")
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
  private String descripcionOtroEvento;
  private String condicionVictima;
  private String codigoMunicipioOcurrencia;
  @NotBlank(message = "Zona de origen es obligatorio")
  private String zonaOrigen;
  @NotBlank(message = "Departamento de origen es obligatorio")
  private String departamentoOrigen;
  @NotBlank(message = "Municipio de origen es obligatorio")
  private String municipioOrigen;

  @NotBlank(message = "Documento es obligatorio")
  private String documento;
  @NotBlank(message = "Tipo de documento es obligatorio")
  private String tipoDocumento;
  @NotBlank(message = "Primer apellido es obligatorio")
  private String primerApellido;
  @NotBlank(message = "Segundo apellido es obligatorio")
  private String segundoApellido;
  @NotBlank(message = "Primer nombre es obligatorio")
  private String primerNombre;
  @NotBlank(message = "Segundo nombre es obligatorio")
  private String segundoNombre;
  @NotBlank(message = "Estado civil es obligatorio")
  private String estadoCivil;
  @NotBlank(message = "Ocupación es obligatorio")
  private String ocupacion;
  @NotBlank(message = "Sexo es obligatorio")
  private String sexo;
  @NotNull(message = "Fecha de nacimiento es obligatorio")
  private LocalDate fechaNacimiento;
  @NotBlank(message = "Edad es obligatorio")
  private String edad;
  @NotBlank(message = "Celular es obligatorio")
  private String celular;
  @NotBlank(message = "Teléfono es obligatorio")
  private String telefono;
  private String tipoPoblacion;

  @NotBlank(message = "Acompañante es obligatorio")
  private String acompanante;
  @NotBlank(message = "Celular del acompañante es obligatorio")
  private String celularAcompanante;
  @NotBlank(message = "Avisar a es obligatorio")
  private String avisarA;
  @NotBlank(message = "Parentesco es obligatorio")
  private String parentesco;
  private String numeroParaAvisar;
  private String numeroParaAvisar2;

  @NotBlank(message = "Dirección es obligatorio")
  private String direccion;
  private String codigoMunicipioResidencia;
  @NotBlank(message = "Zona del paciente es obligatorio")
  private String zonaPaciente;
  @NotBlank(message = "Departamento es obligatorio")
  private String departamento;
  @NotBlank(message = "Ciudad es obligatorio")
  private String ciudad;

  @NotBlank(message = "Alergias es obligatorio")
  private String alergia;
  @NotBlank(message = "Patológicos es obligatorio")
  private String patologicos;
  @NotBlank(message = "Medicación es obligatorio")
  private String medicacion;
  @NotBlank(message = "Líquidos es obligatorio")
  private String liquidos;

  @NotBlank(message = "Aseguradora es obligatorio")
  private String aseguradora;
  @NotBlank(message = "Póliza es obligatorio")
  private String poliza;
  @NotBlank(message = "Plan de beneficios es obligatorio")
  private String planBeneficios;

  @NotNull(message = "Hora de llegada es obligatorio")
  private LocalTime horaLlegada;
  @NotBlank(message = "Transportado a es obligatorio")
  private String transportadoA;
  private String codigoHabilitacion;
  @NotBlank(message = "Departamento de traslado es obligatorio")
  private String departamentoTraslado;
  @NotBlank(message = "Ciudad de transporte es obligatorio")
  private String ciudadTransporte;
  private String estadoPaciente;

  @NotBlank(message = "Causa externa es obligatorio")
  private String causaExterna;

  @NotBlank(message = "Presión es obligatorio")
  private String presion;
  @NotBlank(message = "Frecuencia cardíaca es obligatorio")
  private String frecuenciaCardiaca;
  @NotBlank(message = "Frecuencia respiratoria es obligatorio")
  private String frecuenciaRespiratoria;
  @NotBlank(message = "Temperatura es obligatorio")
  private String temperatura;
  @NotBlank(message = "RO es obligatorio")
  private String ro;
  @NotBlank(message = "RV es obligatorio")
  private String rv;
  @NotBlank(message = "RM es obligatorio")
  private String rm;
  @NotBlank(message = "Hallazgos es obligatorio")
  private String hallazgos;
  @NotBlank(message = "Diagnósticos es obligatorio")
  private String diagnosticos;

  @NotEmpty(message = "Seleccione al menos una lesión")
  private List<String> lesiones;
  private String lesionesImagen;

  @NotEmpty(message = "Seleccione al menos un procedimiento")
  private List<String> procedimientos;

  @NotBlank(message = "Materiales es obligatorio")
  private String materiales;

  @NotBlank(message = "Conductor es obligatorio")
  private String conductor;
  private String documentoConductor;
  @NotBlank(message = "Paramédico es obligatorio")
  private String paramedico;
  private String documentoParamedico;
  @NotBlank(message = "Médico es obligatorio")
  private String medico;
  @NotBlank(message = "Documento médico es obligatorio")
  private String documentoMedico;
}
