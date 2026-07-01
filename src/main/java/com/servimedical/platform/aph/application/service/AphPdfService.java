package com.servimedical.platform.aph.application.service;

import com.servimedical.platform.aph.domain.exception.AphNotFoundException;
import com.servimedical.platform.aph.domain.model.Aph;
import com.servimedical.platform.aph.domain.port.in.GenerateAphPdfUseCase;
import com.servimedical.platform.aph.domain.port.out.AphRepositoryPort;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

/**
 * Generates the APH PDF report. Implements the {@link GenerateAphPdfUseCase} port
 * and depends only on the domain model and outbound repository port.
 */
@Service
public class AphPdfService implements GenerateAphPdfUseCase {

  private PDType1Font currentFont;
  private float currentFontSize;

  private final AphRepositoryPort repository;

  private final PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
  private final PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

  // Tamaño carta: 8.5 x 11 pulgadas.
  private static final PDRectangle PAGE_SIZE = PDRectangle.LETTER;
  private static final float PAGE_WIDTH = PAGE_SIZE.getWidth();
  private static final float PAGE_HEIGHT = PAGE_SIZE.getHeight();

  private static final float MARGIN_X = 14f;
  private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2);

  private static final float SECTION_HEIGHT = 11f;

  private static final float LABEL_HEIGHT = 7.6f;
  private static final float LABEL_VALUE_GAP = 1.4f;
  private static final float VALUE_HEIGHT = 13f;
  private static final float ROW_HEIGHT = LABEL_HEIGHT + LABEL_VALUE_GAP + VALUE_HEIGHT;

  private static final float TITLE_FONT = 12f;
  private static final float HEADER_FONT = 7.6f;
  private static final float SECTION_FONT = 7.0f;
  private static final float LABEL_FONT = 6.2f;
  private static final float VALUE_FONT = 7.4f;
  private static final float FIELD_RADIUS = 2.0f;
  private static final float FIELD_LINE_WIDTH = 0.5f;
  private static final float SECTION_BORDER_WIDTH = 0.5f;

  // Cell padding (used everywhere to avoid text touching borders).
  private static final float CELL_PAD_X = 3f;

  // Section bar background.
  private static final float SECTION_GRAY = 0.82f;
  // Light gray border for value cells (matches reference screenshot).
  private static final float FIELD_BORDER_GRAY = 0.78f;
  // Red used to highlight injured zones on the body silhouettes.
  private static final float INJURY_R = 0.85f;
  private static final float INJURY_G = 0.10f;
  private static final float INJURY_B = 0.10f;

  public AphPdfService(AphRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public byte[] generatePdf(Long aphId) {
    Aph aph = repository.findById(aphId).orElseThrow(() -> new AphNotFoundException(aphId));

    try (PDDocument doc = new PDDocument()) {
      PDPage page1 = new PDPage(PAGE_SIZE);
      doc.addPage(page1);

      try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
        float y = PAGE_HEIGHT - 16f;

        y = drawHeader(cs, doc, y, aph);
        y = drawPatientData(cs, y, aph);
        y = drawEventSiteData(cs, y, aph);
        y = drawVehicleData(cs, y, aph);
        y = drawOwnerData(cs, y, aph);
        y = drawDriverData(cs, y, aph);
        y = drawExternalCause(cs, y, aph);
        y = drawPersonalHistory(cs, y, aph);
        y = drawPhysicalExam(cs, y, aph);
        drawInjuryLocation(cs, doc, y, aph);
        drawFooterCertification(cs);
      }

      PDPage page2 = new PDPage(PAGE_SIZE);
      doc.addPage(page2);
      try (PDPageContentStream cs = new PDPageContentStream(doc, page2)) {
        float y = PAGE_HEIGHT - 16f;
        y = drawDiagnosisAndFindings(cs, y, aph);
        y = drawProcedures(cs, y, aph);
        y = drawMaterials(cs, y, aph);
        drawSignatures(cs, y, aph);
        drawFooterCertification(cs);
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      doc.save(out);
      return out.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
    }
  }

  private float drawHeader(PDPageContentStream cs, PDDocument doc, float y, Aph aph) throws Exception {
    float headerTop = y;
    float headerHeight = 56f;
    float headerBottom = headerTop - headerHeight;

    // Logo placed in the top-left, scaled to fit a fixed bounding box. No outer
    // frame and no internal dividers — the elements float freely.
    float logoBoxX = MARGIN_X + 2f;
    float logoBoxY = headerBottom + 4f;
    float logoBoxW = 60f;
    float logoBoxH = headerHeight - 8f;
    try {
      PDImageXObject logo = loadImage(doc, "static/logo.png");
      float scale = Math.min(logoBoxW / logo.getWidth(), logoBoxH / logo.getHeight());
      float drawW = logo.getWidth() * scale;
      float drawH = logo.getHeight() * scale;
      float drawX = logoBoxX + (logoBoxW - drawW) / 2f;
      float drawY = logoBoxY + (logoBoxH - drawH) / 2f;
      cs.drawImage(logo, drawX, drawY, drawW, drawH);
    } catch (Exception e) {
      // Surface the failure visually so we don't silently lose the logo.
      setFont(cs, normal, 5.5f);
      drawCenteredText(cs, "LOGO", logoBoxX + logoBoxW / 2f, logoBoxY + logoBoxH / 2f);
    }

    // Title block centered on the page.
    setFont(cs, bold, TITLE_FONT);
    drawCenteredText(cs, "ATENCIÓN PRE-HOSPITALARIA", PAGE_WIDTH / 2f, headerTop - 22f);
    setFont(cs, normal, 8f);
    drawCenteredText(cs, "HISTORIA CLÍNICA", PAGE_WIDTH / 2f, headerTop - 36f);

    // Version block on the right.
    float versionCenterX = PAGE_WIDTH - MARGIN_X - 36f;
    setFont(cs, bold, 8.5f);
    drawCenteredText(cs, "FAPH v1", versionCenterX, headerTop - 22f);
    setFont(cs, normal, 7.5f);
    drawCenteredText(cs, "01/03/2025", versionCenterX, headerTop - 36f);

    // Strip below header: Placa | Movil | Atencion Inicial/Remitido/Control | Codigo APH.
    float infoY = headerBottom - 8f;
    setFont(cs, bold, HEADER_FONT);
    drawText(cs, MARGIN_X + 4f, infoY, "PLACA:");
    setFont(cs, normal, HEADER_FONT);
    drawText(cs, MARGIN_X + 38f, infoY, up(nvl(aph.getPlaca())));

    setFont(cs, bold, HEADER_FONT);
    drawText(cs, PAGE_WIDTH / 2f - 96f, infoY, "MÓVIL:");
    setFont(cs, normal, HEADER_FONT);
    drawText(cs, PAGE_WIDTH / 2f - 60f, infoY, up(nvl(aph.getMovil())));

    setFont(cs, bold, HEADER_FONT);
    drawText(cs, PAGE_WIDTH / 2f, infoY, "ATEN. INICIAL/REMITIDO/CTRL:");
    setFont(cs, normal, HEADER_FONT);
    drawText(cs, PAGE_WIDTH / 2f + 122f, infoY,
            up(nvl(aph.getEsAtencionInicialPacienteRemitidoOControl())));

    setFont(cs, bold, HEADER_FONT);
    drawText(cs, PAGE_WIDTH - 128f, infoY, "CÓDIGO APH:");
    setFont(cs, normal, HEADER_FONT);
    drawText(cs, PAGE_WIDTH - 62f, infoY, up(nvl(aph.getCodigo())));

    return headerBottom - 16f;
  }

  private float drawPatientData(PDPageContentStream cs, float y, Aph aph) throws Exception {
    y = section(cs, y, "DATOS DEL PACIENTE");

    y = tableRow(cs, y,
            cell("Tipo ID", nvl(aph.getTipoDocumento()), 1),
            cell("No. de Identificación", nvl(aph.getDocumento()), 2),
            cell("Nombres y Apellidos", fullName(aph), 4),
            cell("Sexo", nvl(aph.getSexo()), 1),
            cell("Código CUPS", nvl(aph.getCodigo()), 1),
            cell("Tipo de traslado", nvl(aph.getTipoTraslado()), 2),
            cell("Prioridad", nvl(aph.getPrioridad()), 1)
    );

    y = tableRow(cs, y,
            cell("Fecha de Nacimiento", fd(aph.getFechaNacimiento()), 3),
            cell("Edad", nvl(aph.getEdad()), 1),
            cell("Estado Civil", nvl(aph.getEstadoCivil()), 2),
            cell("Tipo Población", nvl(aph.getTipoPoblacion()), 2),
            cell("Ocupación", nvl(aph.getOcupacion()), 2),
            cell("Celular", nvl(aph.getCelular()), 2)
    );

    y = tableRow(cs, y,
            cell("Dirección de Residencia", nvl(aph.getDireccion()), 4),
            cell("Cód. Municipio Residencia", nvl(aph.getCodigoMunicipioResidencia()), 2),
            cell("Telefono", nvl(aph.getTelefono()), 2),
            cell("Zona", nvl(aph.getZonaPaciente()), 1),
            cell("Departamento", nvl(aph.getDepartamento()), 2),
            cell("Municipio", nvl(aph.getCiudad()), 2)
    );

    y = tableRow(cs, y,
            cell("Nombres del Acompañante", nvl(aph.getAcompanante()), 4),
            cell("No. de telefono", nvl(aph.getCelularAcompanante()), 2),
            cell("Avisar a", nvl(aph.getAvisarA()), 3),
            cell("Parentesco", nvl(aph.getParentesco()), 2),
            cell("No. de telefono", nvl(aph.getNumeroParaAvisar()), 2)
    );

    y = tableRow(cs, y,
            cell("Aseguradora Responsable del paciente", nvl(aph.getAseguradora()), 5),
            cell("Poliza o No carnet", nvl(aph.getPoliza()), 3),
            cell("Descripción del plan de beneficios", nvl(aph.getPlanBeneficios()), 5)
    );

    return tableRow(cs, y,
            cell("Hora de llegada", ft(aph.getHoraLlegada()), 2),
            cell("Transportado a", nvl(aph.getTransportadoA()), 4),
            cell("Cod Habilitación", nvl(aph.getCodigoHabilitacion()), 3),
            cell("Departamento", nvl(aph.getDepartamentoTraslado()), 2),
            cell("Municipio", nvl(aph.getCiudadTransporte()), 2),
            cell("Estado", nvl(aph.getEstadoPaciente()), 1)
    );
  }

  private float drawExternalCause(PDPageContentStream cs, float y, Aph aph) throws Exception {
    y = section(cs, y, "CAUSA EXTERNA");

    return tableRow(cs, y,
            cell("Causa Externa Origina la Atencion", nvl(aph.getCausaExterna()), 1),
            cell("Motivo de Consulta", nvl(aph.getDiagnosticos()), 1)
    );
  }

  private float drawEventSiteData(PDPageContentStream cs, float y, Aph aph) throws Exception {
    y = section(cs, y, "DATOS SITIO DONDE OCURRIÓ EL EVENTO");

    List<Cell> row1 = new ArrayList<>();
    row1.add(cell("Fecha de Traslado", fd(aph.getFechaAccidente()), 2));
    row1.add(cell("Hora de Traslado", ft(aph.getHoraAccidente()), 2));
    row1.add(cell("Naturaleza del Evento", nvl(aph.getNaturalezaEvento()), 2));
    String otroEvento = nvl(aph.getDescripcionOtroEvento());
    if (!otroEvento.isBlank()) {
      row1.add(cell("Descripción Otro Evento", otroEvento, 2));
    }
    row1.add(cell("Condición Víctima", nvl(aph.getCondicionVictima()), 2));
    y = tableRow(cs, y, row1.toArray(new Cell[0]));

    return tableRow(cs, y,
            cell("Fecha Accidente", fd(aph.getFechaAccidente()), 2),
            cell("Zona", nvl(aph.getZonaOrigen()), 1),
            cell("Departamento", nvl(aph.getDepartamentoOrigen()), 2),
            cell("Municipio", nvl(aph.getMunicipioOrigen()), 2),
            cell("Cód. Municipio Ocurrencia", nvl(aph.getCodigoMunicipioOcurrencia()), 2),
            cell("Dirección Ocurrencia", nvl(aph.getLugarOcurrencia()), 3)
    );
  }

  private float drawVehicleData(PDPageContentStream cs, float y, Aph aph) throws Exception {
    y = section(cs, y, "DATOS VEHÍCULO");

    y = tableRow(cs, y,
            cell("Estado Aseguramiento", nvl(aph.getEstadoAseguramiento()), 2),
            cell("Placa Vehículo", nvl(aph.getPlacaVehiculo()), 2),
            cell("Tipo Vehículo", nvl(aph.getTipoVehiculo()), 2),
            cell("Código Aseguradora", nvl(aph.getCodigoAseguradora()), 2),
            cell("No. Póliza SOAT", nvl(aph.getNumeroPolizaSoat()), 2)
    );

    return tableRow(cs, y,
            cell("Inicio Vigencia", nvl(aph.getFechaInicioVigencia()), 2),
            cell("Fin Vigencia", nvl(aph.getFechaFinVigencia()), 2),
            cell("No. Radicado SIRAS", nvl(aph.getNumeroRadicadoSiras()), 2)
    );
  }

  private float drawOwnerData(PDPageContentStream cs, float y, Aph aph) throws Exception {
    y = section(cs, y, "DATOS PROPIETARIO");

    String nombreCompletoPropietario = nvl(
            (nvl(aph.getPrimerNombrePropietario()) + " "
                    + nvl(aph.getSegundoNombrePropietario()) + " "
                    + nvl(aph.getPrimerApellidoPropietario()) + " "
                    + nvl(aph.getSegundoApellidoPropietario())).trim()
    );

    y = tableRow(cs, y,
            cell("Tipo Documento", nvl(aph.getTipoDocumentoPropietario()), 2),
            cell("No. Documento", nvl(aph.getNumeroDocumentoPropietario()), 2),
            cell("Nombre Completo", nombreCompletoPropietario, 4)
    );

    return tableRow(cs, y,
            cell("Dirección Residencia", nvl(aph.getDireccionResidenciaPropietario()), 4),
            cell("Teléfono", nvl(aph.getTelefonoResidenciaPropietario()), 2),
            cell("Cód. Municipio Residencia", nvl(aph.getCodigoMunicipioResidenciaPropietario()), 2)
    );
  }

  private float drawDriverData(PDPageContentStream cs, float y, Aph aph) throws Exception {
    y = section(cs, y, "DATOS CONDUCTOR");

    String nombreCompletoConductor = nvl(
            (nvl(aph.getPrimerNombreConductorVehiculo()) + " "
                    + nvl(aph.getSegundoNombreConductorVehiculo()) + " "
                    + nvl(aph.getPrimerApellidoConductorVehiculo()) + " "
                    + nvl(aph.getSegundoApellidoConductorVehiculo())).trim()
    );

    y = tableRow(cs, y,
            cell("Tipo Documento", nvl(aph.getTipoDocumentoConductorVehiculo()), 2),
            cell("No. Documento", nvl(aph.getNumeroDocumentoConductorVehiculo()), 2),
            cell("Nombre Completo", nombreCompletoConductor, 4)
    );

    return tableRow(cs, y,
            cell("Dirección Residencia", nvl(aph.getDireccionResidenciaConductorVehiculo()), 4),
            cell("Teléfono", nvl(aph.getTelefonoResidenciaConductorVehiculo()), 2),
            cell("Cód. Municipio Residencia", nvl(aph.getCodigoMunicipioResidenciaConductorVehiculo()), 2)
    );
  }

  private float drawPersonalHistory(PDPageContentStream cs, float y, Aph aph) throws Exception {
    y = section(cs, y, "ANTECEDENTES PERSONALES");

    return tableRow(cs, y,
            cell("Alergias", nvl(aph.getAlergia()), 1),
            cell("Liquidos y Alimentos", nvl(aph.getLiquidos()), 2),
            cell("Medicacion", nvl(aph.getMedicacion()), 2),
            cell("Patologicos", nvl(aph.getPatologicos()), 2)
    );
  }

  private float drawPhysicalExam(PDPageContentStream cs, float y, Aph aph) throws Exception {
    y = dualSection(cs, y, "EXAMEN FISICO", "GLASGOW", 0.68f);

    return tableRow(cs, y,
            cell("PA", nvl(aph.getPresion()), 1),
            cell("FC", nvl(aph.getFrecuenciaCardiaca()), 1),
            cell("FR", nvl(aph.getFrecuenciaRespiratoria()), 1),
            cell("Temp", nvl(aph.getTemperatura()), 1),
            cell("RO", nvl(aph.getRo()), 1),
            cell("RV", nvl(aph.getRv()), 1),
            cell("RM", nvl(aph.getRm()), 1)
    );
  }

  private float drawInjuryLocation(PDPageContentStream cs, PDDocument doc, float y, Aph aph)
          throws Exception {
    y = section(cs, y, "UBICACION DE LAS LESIONES");

    float panelHeight = 170f;
    // No outer box — silhouettes float freely on white background.

    boolean painted = false;
    if (aph.getLesionesImagen() != null && !aph.getLesionesImagen().isBlank()) {
      try {
        drawCapturedBodyImage(cs, doc, aph.getLesionesImagen(), y, panelHeight);
        painted = true;
      } catch (Exception ignored) {
        // fall through to silhouette fallback
      }
    }
    if (!painted) {
      // Vector silhouettes (front + back) with red marks for each lesion zone
      // detected from aph.getLesiones(). This is the preferred fallback because
      // it conveys the same clinical information as the captured image.
      drawBodyFallback(cs, y, panelHeight, aph.getLesiones());
    }

    return y - panelHeight;
  }

  private void drawCapturedBodyImage(
          PDPageContentStream cs,
          PDDocument doc,
          String base64Image,
          float panelTopY,
          float panelHeight
  ) throws Exception {
    byte[] imageBytes = decodeBase64Image(base64Image);

    PDImageXObject bodyImage = PDImageXObject.createFromByteArray(
            doc,
            imageBytes,
            "aph-body-map.png"
    );

    float maxWidth = CONTENT_WIDTH - 30f;
    float maxHeight = panelHeight - 14f;
    float scale = Math.min(maxWidth / bodyImage.getWidth(), maxHeight / bodyImage.getHeight());
    float drawW = bodyImage.getWidth() * scale;
    float drawH = bodyImage.getHeight() * scale;
    float imageX = MARGIN_X + (CONTENT_WIDTH - drawW) / 2f;
    float imageY = panelTopY - panelHeight + (panelHeight - drawH) / 2f;

    cs.drawImage(bodyImage, imageX, imageY, drawW, drawH);
  }

  private byte[] decodeBase64Image(String base64Image) {
    String cleanBase64 = base64Image;

    if (base64Image.contains(",")) {
      cleanBase64 = base64Image.substring(base64Image.indexOf(",") + 1);
    }

    return Base64.getDecoder().decode(cleanBase64);
  }

  private float drawDiagnosisAndFindings(PDPageContentStream cs, float y, Aph aph) throws Exception {
    y = section(cs, y, "DIAGNOSTICOS  /  HALLAZGOS");

    y = diagnosticRow(cs, y, "Diagnostico CIE10", nvl(aph.getDiagnosticos()), 16f);

    return findingsRow(cs, y, "Describa sus\nhallazgos", nvl(aph.getHallazgos()), 42f);
  }

  private float drawProcedures(PDPageContentStream cs, float y, Aph aph) throws Exception {
    y = section(cs, y, "PROCEDIMIENTOS REALIZADOS");

    String procedures = joinList(aph.getProcedimientos());
    return singleValueRow(cs, y, procedures, 22f);
  }

  private float drawMaterials(PDPageContentStream cs, float y, Aph aph) throws Exception {
    y = section(cs, y, "MATERIALES Y DROGAS UTILIZADAS");
    return singleValueRow(cs, y, nvl(aph.getMateriales()), 22f);
  }

  private float drawSignatures(PDPageContentStream cs, float y, Aph aph) throws Exception {
    y = section(cs, y, "FIRMAS / SELLOS");

    float colW = CONTENT_WIDTH / 3f;

    y = tableRow(cs, y,
            cell("Conductor", joinPersonDoc(aph.getConductor(), aph.getDocumentoConductor()), 1),
            cell("Encargado del Traslado", joinPersonDoc(aph.getParamedico(), aph.getDocumentoParamedico()), 1),
            cell("Quien recibe al paciente", joinPersonDoc(aph.getMedico(), aph.getDocumentoMedico()), 1)
    );

    float signatureHeight = 55f;

    for (int i = 0; i < 3; i++) {
      float x = MARGIN_X + (i * colW);
      drawBorder(cs, x, y - signatureHeight, colW, signatureHeight);
    }

    setFont(cs, bold, LABEL_FONT);
    drawCenteredText(cs, "FIRMA  /  PACIENTE O RESPONSABLE", MARGIN_X + (colW / 2f), y - 12f);
    drawCenteredText(cs, "FIRMA  /  SELLO ENCARGADO DEL TRASLADO", MARGIN_X + colW + (colW / 2f), y - 12f);
    drawCenteredText(cs, "FIRMA  /  SELLO QUIEN RECIBE AL PACIENTE", MARGIN_X + (colW * 2f) + (colW / 2f), y - 12f);

    return y - signatureHeight;
  }

  private float section(PDPageContentStream cs, float y, String title) throws Exception {
    setFillGray(cs, SECTION_GRAY);
    fillRect(cs, MARGIN_X, y - SECTION_HEIGHT, CONTENT_WIDTH, SECTION_HEIGHT);
    resetColor(cs);
    drawBorder(cs, MARGIN_X, y - SECTION_HEIGHT, CONTENT_WIDTH, SECTION_HEIGHT);

    setFont(cs, bold, SECTION_FONT);
    drawCenteredText(cs, up(title), PAGE_WIDTH / 2f, y - 7.4f);

    return y - SECTION_HEIGHT;
  }

  private float dualSection(PDPageContentStream cs, float y, String left, String right, float leftRatio)
          throws Exception {
    float leftW = CONTENT_WIDTH * leftRatio;
    float rightW = CONTENT_WIDTH - leftW;

    setFillGray(cs, SECTION_GRAY);
    fillRect(cs, MARGIN_X, y - SECTION_HEIGHT, leftW, SECTION_HEIGHT);
    fillRect(cs, MARGIN_X + leftW, y - SECTION_HEIGHT, rightW, SECTION_HEIGHT);
    resetColor(cs);
    drawBorder(cs, MARGIN_X, y - SECTION_HEIGHT, leftW, SECTION_HEIGHT);
    drawBorder(cs, MARGIN_X + leftW, y - SECTION_HEIGHT, rightW, SECTION_HEIGHT);

    setFont(cs, bold, SECTION_FONT);
    drawCenteredText(cs, up(left), MARGIN_X + (leftW / 2f), y - 7.4f);
    drawCenteredText(cs, up(right), MARGIN_X + leftW + (rightW / 2f), y - 7.4f);

    return y - SECTION_HEIGHT;
  }

  private float tableRow(PDPageContentStream cs, float y, Cell... cells) throws Exception {
    int totalSpan = 0;
    for (Cell cell : cells) {
      totalSpan += cell.span();
    }

    float x = MARGIN_X;

    for (Cell cell : cells) {
      float width = CONTENT_WIDTH * cell.span() / totalSpan;

      // Label sits ABOVE the field, no background.
      setFont(cs, bold, LABEL_FONT);
      String labelText = up(cell.label());
      float labelMax = width - CELL_PAD_X * 2f;
      String labelFit = fitText(labelText, labelMax, bold, LABEL_FONT);
      float labelW = stringWidth(labelFit, bold, LABEL_FONT);
      drawText(cs, x + (width - labelW) / 2f, y - 5.8f, labelFit);

      float valueBoxY = y - LABEL_HEIGHT - LABEL_VALUE_GAP - VALUE_HEIGHT;
      float valueBoxX = x + 1.5f;
      float valueBoxW = width - 3f;

      drawFieldCell(cs, valueBoxX, valueBoxY, valueBoxW, VALUE_HEIGHT);
      drawValueInBox(cs, up(cell.value()), valueBoxX, valueBoxY, valueBoxW, VALUE_HEIGHT,
              normal, VALUE_FONT);

      x += width;
    }

    return y - ROW_HEIGHT;
  }

  /**
   * Draws a uniform value cell: white background, light-gray rounded border,
   * no shadow. Used everywhere a value is rendered so all inputs look consistent.
   */
  private void drawFieldCell(PDPageContentStream cs, float x, float y, float w, float h)
          throws Exception {
    // White fill so values stand out cleanly from the page.
    setFillGray(cs, 1f);
    drawRoundedPath(cs, x, y, w, h, FIELD_RADIUS);
    cs.fill();

    // Light gray rounded border.
    cs.setStrokingColor(FIELD_BORDER_GRAY, FIELD_BORDER_GRAY, FIELD_BORDER_GRAY);
    cs.setLineWidth(FIELD_LINE_WIDTH);
    drawRoundedPath(cs, x, y, w, h, FIELD_RADIUS);
    cs.stroke();
    resetColor(cs);
  }

  private void drawRoundedPath(
          PDPageContentStream cs,
          float x,
          float y,
          float width,
          float height,
          float radius
  ) throws Exception {
    float right = x + width;
    float top = y + height;
    float k = 0.552284749831f;
    float c = radius * k;

    cs.moveTo(x + radius, y);
    cs.lineTo(right - radius, y);
    cs.curveTo(right - radius + c, y, right, y + radius - c, right, y + radius);

    cs.lineTo(right, top - radius);
    cs.curveTo(right, top - radius + c, right - radius + c, top, right - radius, top);

    cs.lineTo(x + radius, top);
    cs.curveTo(x + radius - c, top, x, top - radius + c, x, top - radius);

    cs.lineTo(x, y + radius);
    cs.curveTo(x, y + radius - c, x + radius - c, y, x + radius, y);

    cs.closePath();
  }

  private void drawRoundedBorder(
          PDPageContentStream cs,
          float x,
          float y,
          float width,
          float height,
          float radius
  ) throws Exception {
    cs.setLineWidth(FIELD_LINE_WIDTH);
    drawRoundedPath(cs, x, y, width, height, radius);
    cs.stroke();
  }

  private float inlineRow(PDPageContentStream cs, float y, InlineCell... cells) throws Exception {
    float rowHeight = 19f;
    float width = CONTENT_WIDTH / cells.length;
    float x = MARGIN_X;

    for (InlineCell cell : cells) {
      drawFieldCell(cs, x + 1.5f, y - rowHeight, width - 3f, rowHeight);

      setFont(cs, bold, LABEL_FONT);
      String label = up(cell.label());
      drawText(cs, x + CELL_PAD_X + 3f, y - 12f, label);
      float labelW = stringWidth(label, bold, LABEL_FONT);
      float valueStart = x + CELL_PAD_X + 3f + labelW + 6f;
      float valueMaxW = width - (valueStart - x) - CELL_PAD_X - 3f;

      setFont(cs, normal, VALUE_FONT);
      drawText(cs, valueStart, y - 12f, fitText(up(cell.value()), valueMaxW, normal, VALUE_FONT));

      x += width;
    }

    return y - rowHeight;
  }

  private float diagnosticRow(
          PDPageContentStream cs,
          float y,
          String label,
          String value,
          float height
  ) throws Exception {
    float labelW = 92f;
    float valueW = CONTENT_WIDTH - labelW;

    drawFieldCell(cs, MARGIN_X, y - height, labelW, height);
    drawFieldCell(cs, MARGIN_X + labelW, y - height, valueW, height);

    setFont(cs, bold, LABEL_FONT);
    drawText(cs, MARGIN_X + CELL_PAD_X + 2f, y - 11f, up(label));

    drawValueInBox(cs, up(value), MARGIN_X + labelW, y - height, valueW, height,
            normal, VALUE_FONT);

    return y - height;
  }

  private float findingsRow(
          PDPageContentStream cs,
          float y,
          String label,
          String value,
          float height
  ) throws Exception {
    float labelW = 92f;
    float valueW = CONTENT_WIDTH - labelW;

    drawFieldCell(cs, MARGIN_X, y - height, labelW, height);
    drawFieldCell(cs, MARGIN_X + labelW, y - height, valueW, height);

    setFont(cs, bold, LABEL_FONT);
    String[] labelLines = label.split("\\n");
    for (int i = 0; i < labelLines.length; i++) {
      String line = up(labelLines[i]);
      float lw = stringWidth(line, bold, LABEL_FONT);
      drawText(cs, MARGIN_X + (labelW - lw) / 2f, y - 15f - (i * 8f), line);
    }

    drawValueWrappedInBox(cs, up(value), MARGIN_X + labelW, y - height, valueW, height,
            normal, VALUE_FONT, 9f);

    return y - height;
  }

  private float singleValueRow(PDPageContentStream cs, float y, String value, float height)
          throws Exception {
    drawFieldCell(cs, MARGIN_X, y - height, CONTENT_WIDTH, height);
    drawValueWrappedInBox(cs, up(value), MARGIN_X, y - height, CONTENT_WIDTH, height,
            normal, VALUE_FONT, 9f);
    return y - height;
  }

  private void drawFooterCertification(PDPageContentStream cs) throws Exception {
    float y = 28f;
    float height = 22f;

    drawFieldCell(cs, MARGIN_X, y - height, CONTENT_WIDTH, height);

    setFont(cs, normal, 6.4f);
    drawText(
            cs,
            MARGIN_X + 5f,
            y - 9f,
            "El profesional de la salud certifica que las lesiones en el presente documento corresponden a hallazgos clínicos ocurridos como consecuencia de accidente de transito."
    );
    drawText(
            cs,
            MARGIN_X + 5f,
            y - 18f,
            "Artículo 32 Decreto 056 de 2015 Ministerio de Salud y Protección Social"
    );
  }

  private void drawBodyFallback(
          PDPageContentStream cs, float panelTopY, float panelHeight, List<String> lesiones)
          throws Exception {
    float panelBottom = panelTopY - panelHeight;
    float centerY = (panelTopY + panelBottom) / 2f;
    float figureHeight = panelHeight - 22f;
    float gap = 60f;
    float halfX = MARGIN_X + (CONTENT_WIDTH / 2f);
    float figureHalfWidth = figureHeight * 0.20f;
    float leftCenter = halfX - (gap / 2f) - figureHalfWidth;
    float rightCenter = halfX + (gap / 2f) + figureHalfWidth;

    drawSilhouette(cs, leftCenter, centerY, figureHeight);
    drawSilhouette(cs, rightCenter, centerY, figureHeight);

    // Paint red marks for any lesion keyword that we can map to a body region.
    paintInjuryMarks(cs, lesiones, leftCenter, centerY, figureHeight, /*front*/ true);
    paintInjuryMarks(cs, lesiones, rightCenter, centerY, figureHeight, /*front*/ false);

    setFont(cs, bold, LABEL_FONT);
    drawCenteredText(cs, "VISTA FRONTAL", leftCenter, panelBottom + 6f);
    drawCenteredText(cs, "VISTA DORSAL", rightCenter, panelBottom + 6f);
  }

  /**
   * Renders red rectangles on top of a silhouette for every lesion entry whose
   * normalized text matches a known body region. Lateral keywords ("DERECHA" /
   * "IZQUIERDA") shift the mark horizontally — and are mirrored on the dorsal
   * view because the anatomical right is the visual left in that view.
   */
  private void paintInjuryMarks(
          PDPageContentStream cs,
          List<String> lesiones,
          float cx,
          float cy,
          float h,
          boolean frontView
  ) throws Exception {
    if (lesiones == null || lesiones.isEmpty()) {
      return;
    }
    cs.setNonStrokingColor(INJURY_R, INJURY_G, INJURY_B);
    cs.setStrokingColor(INJURY_R, INJURY_G, INJURY_B);
    cs.setLineWidth(0.5f);

    for (String raw : lesiones) {
      String norm = normalizeForMatch(raw);
      if (norm.isEmpty()) {
        continue;
      }
      String[] tokens = norm.split(":");
      if (tokens.length >= 2) {
        if ("FRONT".equals(tokens[0]) && !frontView) {
          continue;
        }
        if ("BACK".equals(tokens[0]) && frontView) {
          continue;
        }
      }
      // Lateralidad: -1 = izquierda (visual), 0 = central, +1 = derecha (visual).
      int side = 0;
      boolean isRight = norm.contains("DERECH");
      boolean isLeft = norm.contains("IZQUIERD");
      if (norm.contains("RIGHT")) isRight = true;
      if (norm.contains("LEFT")) isLeft = true;
      if (isRight) side = frontView ? +1 : -1; // mirror on dorsal view
      if (isLeft) side = frontView ? -1 : +1;

      // Region detection (first match wins; head/face takes priority over generic).
      Float[] zone = null;
      if (norm.contains("CABEZA") || norm.contains("CRANEO") || norm.contains("FRONTAL")
              || norm.contains("CARA") || norm.contains("HEAD") || norm.contains("FACE")) {
        zone = new Float[]{0f, 0.40f, 0.18f, 0.16f};
      } else if (norm.contains("CUELLO") || norm.contains("CERVICAL") || norm.contains("NECK")) {
        zone = new Float[]{0f, 0.29f, 0.12f, 0.05f};
      } else if (norm.contains("HOMBRO") || norm.contains("SHOULDER")) {
        zone = new Float[]{side * 0.16f, 0.26f, 0.10f, 0.06f};
      } else if (norm.contains("BRAZO") || norm.contains("HUMERO") || norm.contains("ARM")) {
        zone = new Float[]{side * 0.22f, 0.16f, 0.08f, 0.12f};
      } else if (norm.contains("ANTEBRAZO") || norm.contains("MUNECA") || norm.contains("MANO")
              || norm.contains("FOREARM") || norm.contains("WRIST") || norm.contains("HAND")) {
        zone = new Float[]{side * 0.28f, -0.02f, 0.08f, 0.10f};
      } else if (norm.contains("TORAX") || norm.contains("TORACI") || norm.contains("HEMITORA")
              || norm.contains("COSTILLA") || norm.contains("PECHO")
              || norm.contains("CHEST") || norm.contains("THORAX") || norm.contains("RIB")) {
        zone = new Float[]{side * 0.06f, 0.18f, 0.20f, 0.12f};
      } else if (norm.contains("ABDOMEN") || norm.contains("VIENTRE")) {
        zone = new Float[]{side * 0.04f, 0.04f, 0.18f, 0.10f};
      } else if (norm.contains("ESPALDA") || norm.contains("LUMBAR") || norm.contains("DORSAL")
              || norm.contains("BACK")) {
        zone = new Float[]{side * 0.04f, 0.10f, 0.20f, 0.16f};
      } else if (norm.contains("CADERA") || norm.contains("PELVIS") || norm.contains("HIP")) {
        zone = new Float[]{side * 0.05f, -0.10f, 0.18f, 0.08f};
      } else if (norm.contains("RODILLA") || norm.contains("KNEE")) {
        zone = new Float[]{side * 0.06f, -0.28f, 0.07f, 0.05f};
      } else if (norm.contains("MUSLO") || norm.contains("FEMUR") || norm.contains("PIERNA")
              || norm.contains("THIGH") || norm.contains("LEG")) {
        zone = new Float[]{side * 0.06f, -0.22f, 0.07f, 0.10f};
      } else if (norm.contains("TOBILLO") || norm.contains("PIE")
              || norm.contains("ANKLE") || norm.contains("FOOT")) {
        zone = new Float[]{side * 0.06f, -0.44f, 0.08f, 0.05f};
      }

      if (zone != null) {
        float zx = cx + zone[0] * h - (zone[2] * h) / 2f;
        float zy = cy + zone[1] * h - (zone[3] * h) / 2f;
        float zw = zone[2] * h;
        float zh = zone[3] * h;
        // Rounded red mark for a softer clinical look.
        drawRoundedPath(cs, zx, zy, zw, zh, Math.min(2f, Math.min(zw, zh) / 3f));
        cs.fill();
      }
    }
    resetColor(cs);
  }

  private static String normalizeForMatch(String value) {
    if (value == null) {
      return "";
    }
    String n = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toUpperCase(java.util.Locale.ROOT);
    return n;
  }

  /**
   * Draws a simple human silhouette (head, torso, arms, legs) using only PDF
   * primitives, so the layout never collapses to plain text when no body image
   * is available.
   */
  private void drawSilhouette(PDPageContentStream cs, float cx, float cy, float h)
          throws Exception {
    cs.setLineWidth(0.8f);
    cs.setStrokingColor(0.25f, 0.25f, 0.25f);

    float headR = h * 0.09f;
    float headCy = cy + h * 0.41f;
    float neckTop = headCy - headR;
    float shoulderY = cy + h * 0.28f;
    float shoulderHalf = h * 0.20f;
    float waistY = cy + h * 0.02f;
    float waistHalf = h * 0.12f;
    float hipY = cy - h * 0.08f;
    float hipHalf = h * 0.16f;
    float crotchY = cy - h * 0.14f;
    float kneeY = cy - h * 0.30f;
    float footY = cy - h * 0.48f;
    float handY = cy - h * 0.04f;
    float handHalf = h * 0.30f;
    float elbowY = cy + h * 0.10f;

    // Head (circle approximated with bezier).
    float k = 0.552284749831f * headR;
    cs.moveTo(cx - headR, headCy);
    cs.curveTo(cx - headR, headCy + k, cx - k, headCy + headR, cx, headCy + headR);
    cs.curveTo(cx + k, headCy + headR, cx + headR, headCy + k, cx + headR, headCy);
    cs.curveTo(cx + headR, headCy - k, cx + k, headCy - headR, cx, headCy - headR);
    cs.curveTo(cx - k, headCy - headR, cx - headR, headCy - k, cx - headR, headCy);
    cs.closePath();
    cs.stroke();

    // Neck.
    cs.moveTo(cx - headR * 0.45f, neckTop);
    cs.lineTo(cx - headR * 0.45f, shoulderY + h * 0.015f);
    cs.lineTo(cx + headR * 0.45f, shoulderY + h * 0.015f);
    cs.lineTo(cx + headR * 0.45f, neckTop);
    cs.stroke();

    // Torso outline (shoulders -> waist -> hips).
    cs.moveTo(cx - shoulderHalf, shoulderY);
    cs.lineTo(cx - waistHalf, waistY);
    cs.lineTo(cx - hipHalf, hipY);
    cs.lineTo(cx - hipHalf * 0.8f, crotchY);
    cs.lineTo(cx + hipHalf * 0.8f, crotchY);
    cs.lineTo(cx + hipHalf, hipY);
    cs.lineTo(cx + waistHalf, waistY);
    cs.lineTo(cx + shoulderHalf, shoulderY);
    cs.closePath();
    cs.stroke();

    // Arms with a slight bend at the elbow.
    cs.moveTo(cx - shoulderHalf, shoulderY);
    cs.lineTo(cx - shoulderHalf - h * 0.04f, elbowY);
    cs.lineTo(cx - handHalf, handY);
    cs.stroke();
    cs.moveTo(cx + shoulderHalf, shoulderY);
    cs.lineTo(cx + shoulderHalf + h * 0.04f, elbowY);
    cs.lineTo(cx + handHalf, handY);
    cs.stroke();

    // Legs (hip -> knee -> foot, each leg as a closed polygon).
    cs.moveTo(cx - hipHalf * 0.95f, crotchY);
    cs.lineTo(cx - hipHalf * 0.55f, kneeY);
    cs.lineTo(cx - hipHalf * 0.45f, footY);
    cs.lineTo(cx - hipHalf * 0.05f, footY);
    cs.lineTo(cx - hipHalf * 0.10f, kneeY);
    cs.lineTo(cx - hipHalf * 0.05f, crotchY);
    cs.closePath();
    cs.stroke();
    cs.moveTo(cx + hipHalf * 0.95f, crotchY);
    cs.lineTo(cx + hipHalf * 0.55f, kneeY);
    cs.lineTo(cx + hipHalf * 0.45f, footY);
    cs.lineTo(cx + hipHalf * 0.05f, footY);
    cs.lineTo(cx + hipHalf * 0.10f, kneeY);
    cs.lineTo(cx + hipHalf * 0.05f, crotchY);
    cs.closePath();
    cs.stroke();

    resetColor(cs);
  }

  private PDImageXObject loadImage(PDDocument doc, String path) throws Exception {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
      if (in == null) {
        throw new RuntimeException("No se encontró la imagen: " + path);
      }
      return PDImageXObject.createFromByteArray(doc, in.readAllBytes(), path);
    }
  }

  private void setFont(PDPageContentStream cs, PDType1Font font, float size) throws Exception {
    currentFont = font;
    currentFontSize = size;
    cs.setFont(font, size);
  }

  private void drawText(PDPageContentStream cs, float x, float y, String text) throws Exception {
    cs.beginText();
    cs.newLineAtOffset(x, y);
    cs.showText(pdfSafe(text));
    cs.endText();
  }

  private void drawCenteredText(PDPageContentStream cs, String text, float centerX, float y)
          throws Exception {
    String safe = pdfSafe(text);
    float width = getTextWidth(safe);
    drawText(cs, centerX - (width / 2f), y, safe);
  }

  private float getTextWidth(String text) throws Exception {
    if (text == null || text.isBlank() || currentFont == null) {
      return 0f;
    }
    return currentFont.getStringWidth(text) / 1000f * currentFontSize;
  }

  private void drawBorder(PDPageContentStream cs, float x, float y, float w, float h)
          throws Exception {
    cs.setStrokingColor(FIELD_BORDER_GRAY, FIELD_BORDER_GRAY, FIELD_BORDER_GRAY);
    cs.setLineWidth(SECTION_BORDER_WIDTH);
    cs.addRect(x, y, w, h);
    cs.stroke();
    resetColor(cs);
  }

  private void fillRect(PDPageContentStream cs, float x, float y, float w, float h)
          throws Exception {
    cs.addRect(x, y, w, h);
    cs.fill();
  }

  private void setFillGray(PDPageContentStream cs, float gray) throws Exception {
    cs.setNonStrokingColor(gray, gray, gray);
  }

  private void resetColor(PDPageContentStream cs) throws Exception {
    cs.setNonStrokingColor(0f, 0f, 0f);
    cs.setStrokingColor(0f, 0f, 0f);
  }

  /**
   * Trims a value to fit a given width using actual font metrics. Appends "…"
   * (single dot trio) when truncated. Never returns a string wider than maxWidth.
   */
  private String fitText(String value, float maxWidth, PDType1Font font, float fontSize)
          throws Exception {
    String safe = nvl(value);
    if (safe.isEmpty() || maxWidth <= 0f) {
      return safe;
    }
    if (stringWidth(safe, font, fontSize) <= maxWidth) {
      return safe;
    }
    String ellipsis = "...";
    float ellipsisWidth = stringWidth(ellipsis, font, fontSize);
    if (ellipsisWidth >= maxWidth) {
      return "";
    }
    int lo = 0;
    int hi = safe.length();
    while (lo < hi) {
      int mid = (lo + hi + 1) >>> 1;
      float w = stringWidth(safe.substring(0, mid), font, fontSize) + ellipsisWidth;
      if (w <= maxWidth) {
        lo = mid;
      } else {
        hi = mid - 1;
      }
    }
    return safe.substring(0, lo) + ellipsis;
  }

  private float stringWidth(String text, PDType1Font font, float fontSize) throws Exception {
    if (text == null || text.isEmpty()) {
      return 0f;
    }
    return font.getStringWidth(text) / 1000f * fontSize;
  }

  /**
   * Wraps a value into lines that each fit within maxWidth, using actual font
   * metrics. Splits on spaces first, then hard-breaks long single words.
   */
  private List<String> wrapToWidth(String value, float maxWidth, PDType1Font font, float fontSize)
          throws Exception {
    List<String> lines = new ArrayList<>();
    String safe = nvl(value);
    if (safe.isEmpty()) {
      lines.add("");
      return lines;
    }
    String[] words = safe.split("\\s+");
    StringBuilder current = new StringBuilder();
    for (String word : words) {
      String candidate = current.isEmpty() ? word : current + " " + word;
      if (stringWidth(candidate, font, fontSize) <= maxWidth) {
        current.setLength(0);
        current.append(candidate);
      } else {
        if (current.length() > 0) {
          lines.add(current.toString());
          current.setLength(0);
        }
        // Hard-break a single long word.
        if (stringWidth(word, font, fontSize) <= maxWidth) {
          current.append(word);
        } else {
          StringBuilder piece = new StringBuilder();
          for (char ch : word.toCharArray()) {
            piece.append(ch);
            if (stringWidth(piece.toString(), font, fontSize) > maxWidth) {
              piece.deleteCharAt(piece.length() - 1);
              lines.add(piece.toString());
              piece.setLength(0);
              piece.append(ch);
            }
          }
          if (piece.length() > 0) {
            current.append(piece);
          }
        }
      }
    }
    if (current.length() > 0) {
      lines.add(current.toString());
    }
    return lines;
  }

  /**
   * Draws a value inside a cell box. Centers when it fits, otherwise left-aligns
   * with padding and truncates with ellipsis. Never overflows the cell.
   */
  private void drawValueInBox(
          PDPageContentStream cs,
          String value,
          float boxX,
          float boxY,
          float boxW,
          float boxH,
          PDType1Font font,
          float fontSize
  ) throws Exception {
    setFont(cs, font, fontSize);
    float maxInner = boxW - CELL_PAD_X * 2f;
    String safe = nvl(value);
    float w = stringWidth(safe, font, fontSize);
    float textY = boxY + (boxH - fontSize) / 2f + 1.5f;
    if (w <= maxInner) {
      drawText(cs, boxX + (boxW - w) / 2f, textY, safe);
    } else {
      String fit = fitText(safe, maxInner, font, fontSize);
      drawText(cs, boxX + CELL_PAD_X, textY, fit);
    }
  }

  private void drawValueWrappedInBox(
          PDPageContentStream cs,
          String value,
          float boxX,
          float boxY,
          float boxW,
          float boxH,
          PDType1Font font,
          float fontSize,
          float lineHeight
  ) throws Exception {
    setFont(cs, font, fontSize);
    float maxInner = boxW - CELL_PAD_X * 2f;
    List<String> lines = wrapToWidth(value, maxInner, font, fontSize);
    int maxLines = Math.max(1, (int) Math.floor((boxH - 2f) / lineHeight));
    int n = Math.min(lines.size(), maxLines);
    float blockH = n * lineHeight;
    float topY = boxY + boxH - (boxH - blockH) / 2f - fontSize;
    for (int i = 0; i < n; i++) {
      String line = lines.get(i);
      if (i == maxLines - 1 && lines.size() > maxLines) {
        line = fitText(line + " " + String.join(" ", lines.subList(i + 1, lines.size())),
                maxInner, font, fontSize);
      }
      float w = stringWidth(line, font, fontSize);
      drawText(cs, boxX + (boxW - w) / 2f, topY - i * lineHeight, line);
    }
  }

  private static String pdfSafe(String value) {
    return nvl(value)
            .replace("\\", "\\\\")
            .replace("(", "\\(")
            .replace(")", "\\)");
  }

  @SuppressWarnings("unused")
  private static String normalize(String value) {
    if (value == null) {
      return "";
    }
    String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
    return normalized.replaceAll("\\p{M}", "").toLowerCase();
  }

  private static String fullName(Aph aph) {
    return (nvl(aph.getPrimerNombre()) + " " + nvl(aph.getSegundoNombre())
            + " " + nvl(aph.getPrimerApellido()) + " " + nvl(aph.getSegundoApellido()))
            .trim().replaceAll("\\s+", " ");
  }

  private static String joinList(List<String> values) {
    return values == null || values.isEmpty() ? "" : String.join(", ", values);
  }

  private static String joinPersonDoc(String name, String document) {
    return (nvl(name) + " " + nvl(document)).trim();
  }

  private static String nvl(String value) {
    return value != null && !value.isBlank() ? value : "";
  }

  /** Renders a value in uppercase to match the official APH form. */
  private static String up(String value) {
    String safe = nvl(value);
    return safe.isEmpty() ? safe : safe.toUpperCase(java.util.Locale.ROOT);
  }

  private static String fd(Object value) {
    return value != null ? value.toString() : "";
  }

  private static String ft(Object value) {
    return value != null ? value.toString() : "";
  }

  private record Cell(String label, String value, int span) {}

  private record InlineCell(String label, String value) {}

  private static Cell cell(String label, String value, int span) {
    return new Cell(label, value, span);
  }

  private static InlineCell inlineCell(String label, String value) {
    return new InlineCell(label, value);
  }
}
