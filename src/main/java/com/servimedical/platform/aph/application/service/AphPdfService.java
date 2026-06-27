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

  private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
  private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();

  private static final float MARGIN_X = 14f;
  private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2);

  private static final float SECTION_HEIGHT = 10.5f;

  private static final float LABEL_HEIGHT = 7.6f;
  private static final float LABEL_VALUE_GAP = 1.6f;
  private static final float VALUE_HEIGHT = 13f;
  private static final float ROW_HEIGHT = LABEL_HEIGHT + LABEL_VALUE_GAP + VALUE_HEIGHT;

  private static final float TITLE_FONT = 12f;
  private static final float HEADER_FONT = 7.6f;
  private static final float SECTION_FONT = 7.0f;
  private static final float LABEL_FONT = 6.2f;
  private static final float VALUE_FONT = 7.4f;
  private static final float FIELD_RADIUS = 2.0f;
  private static final float FIELD_LINE_WIDTH = 0.7f;
  private static final float SECTION_BORDER_WIDTH = 0.35f;

  // Light gray for section headers (matches official APH form).
  private static final float SECTION_GRAY = 0.90f;
  // Very subtle gray fill behind value cells to give a pulished/input look.
  private static final float FIELD_FILL_GRAY = 0.975f;
  // Soft drop-shadow gray under value cells.
  private static final float FIELD_SHADOW_GRAY = 0.86f;
  // Medium-gray stroke for value cell borders (matches reference screenshot).
  private static final float FIELD_BORDER_GRAY = 0.60f;

  public AphPdfService(AphRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public byte[] generatePdf(Long aphId) {
    Aph aph = repository.findById(aphId).orElseThrow(() -> new AphNotFoundException(aphId));

    try (PDDocument doc = new PDDocument()) {
      PDPage page = new PDPage(PDRectangle.A4);
      doc.addPage(page);

      try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
        float y = PAGE_HEIGHT - 16f;

        y = drawHeader(cs, doc, y, aph);
        y = drawPatientData(cs, y, aph);
        y = drawExternalCause(cs, y, aph);
        y = drawPersonalHistory(cs, y, aph);
        y = drawPhysicalExam(cs, y, aph);
        y = drawInjuryLocation(cs, doc, y, aph);
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
    float headerHeight = 60f;
    float headerBottom = headerTop - headerHeight;

    // Outer header frame with two internal dividers (logo | title | version).
    float logoColW = 70f;
    float versionColW = 95f;

    drawBorder(cs, MARGIN_X, headerBottom, CONTENT_WIDTH, headerHeight);
    cs.setStrokingColor(FIELD_BORDER_GRAY, FIELD_BORDER_GRAY, FIELD_BORDER_GRAY);
    cs.setLineWidth(SECTION_BORDER_WIDTH);
    cs.moveTo(MARGIN_X + logoColW, headerBottom);
    cs.lineTo(MARGIN_X + logoColW, headerTop);
    cs.stroke();
    cs.moveTo(MARGIN_X + CONTENT_WIDTH - versionColW, headerBottom);
    cs.lineTo(MARGIN_X + CONTENT_WIDTH - versionColW, headerTop);
    cs.stroke();
    resetColor(cs);

    // Logo placed in the top-left cell of the header, scaled to fit.
    float logoBoxX = MARGIN_X + 4f;
    float logoBoxY = headerBottom + 4f;
    float logoBoxW = logoColW - 8f;
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

    // Title centered in the middle cell.
    setFont(cs, bold, TITLE_FONT);
    drawCenteredText(cs, "ATENCIÓN PRE-HOSPITALARIA", PAGE_WIDTH / 2f, headerTop - 24f);
    setFont(cs, normal, 7.5f);
    drawCenteredText(cs, "HISTORIA CLÍNICA", PAGE_WIDTH / 2f, headerTop - 38f);

    // Version block on the right cell.
    float versionCenterX = MARGIN_X + CONTENT_WIDTH - (versionColW / 2f);
    setFont(cs, bold, 8.5f);
    drawCenteredText(cs, "FAPH v1", versionCenterX, headerTop - 22f);
    setFont(cs, normal, 7.5f);
    drawCenteredText(cs, "01/03/2025", versionCenterX, headerTop - 36f);

    // Strip below header with Placa / Movil.
    float infoY = headerBottom - 11f;
    setFont(cs, bold, HEADER_FONT);
    drawText(cs, MARGIN_X + 4f, infoY, "PLACA:");
    setFont(cs, normal, HEADER_FONT);
    drawText(cs, MARGIN_X + 38f, infoY, up(nvl(aph.getPlaca())));

    setFont(cs, bold, HEADER_FONT);
    drawText(cs, PAGE_WIDTH / 2f - 80f, infoY, "MÓVIL:");
    setFont(cs, normal, HEADER_FONT);
    drawText(cs, PAGE_WIDTH / 2f - 44f, infoY, up(nvl(aph.getMovil())));

    return headerBottom - 18f;
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
            cell("Fecha de traslado", fd(aph.getFechaAccidente()), 2),
            cell("Hora de traslado", ft(aph.getHoraAccidente()), 2),
            cell("Lugar de ocurrencia de la atención", nvl(aph.getLugarOcurrencia()), 5),
            cell("Zona", nvl(aph.getZonaOrigen()), 1),
            cell("Departamento", nvl(aph.getDepartamentoOrigen()), 2),
            cell("Municipio", nvl(aph.getMunicipioOrigen()), 2)
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
            cell("Dirección de Residencia", nvl(aph.getDireccion()), 5),
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

    float panelHeight = 168f;
    drawBorder(cs, MARGIN_X, y - panelHeight, CONTENT_WIDTH, panelHeight);

    boolean painted = false;
    if (aph.getLesionesImagen() != null && !aph.getLesionesImagen().isBlank()) {
      try {
        drawCapturedBodyImage(cs, doc, aph.getLesionesImagen(), y, panelHeight);
        painted = true;
      } catch (Exception ignored) {
        // fall through to fallbacks
      }
    }
    if (!painted) {
      try {
        PDImageXObject body = loadImage(doc, "static/body.jpg");
        float maxW = CONTENT_WIDTH - 24f;
        float maxH = panelHeight - 16f;
        float scale = Math.min(maxW / body.getWidth(), maxH / body.getHeight());
        float drawW = body.getWidth() * scale;
        float drawH = body.getHeight() * scale;
        float drawX = MARGIN_X + (CONTENT_WIDTH - drawW) / 2f;
        float drawY = y - panelHeight + (panelHeight - drawH) / 2f;
        cs.drawImage(body, drawX, drawY, drawW, drawH);
        painted = true;
      } catch (Exception ignored) {
        // fall through to vector silhouette
      }
    }
    if (!painted) {
      drawBodyFallback(cs, y, panelHeight);
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
    return singleValueRow(cs, y, procedures, 13f);
  }

  private float drawMaterials(PDPageContentStream cs, float y, Aph aph) throws Exception {
    y = section(cs, y, "MATERIALES Y DROGAS UTILIZADAS");
    return singleValueRow(cs, y, nvl(aph.getMateriales()), 13f);
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

      setFont(cs, bold, LABEL_FONT);
      drawCenteredText(cs, up(cell.label()), x + (width / 2f), y - 5.6f);

      float valueBoxY = y - LABEL_HEIGHT - LABEL_VALUE_GAP - VALUE_HEIGHT;
      float valueBoxX = x + 1.5f;
      float valueBoxW = width - 3f;

      drawFieldCell(cs, valueBoxX, valueBoxY, valueBoxW, VALUE_HEIGHT);

      setFont(cs, normal, VALUE_FONT);
      drawCenteredText(
              cs,
              trimToWidth(up(cell.value()), valueBoxW - 6f, VALUE_FONT),
              x + (width / 2f),
              valueBoxY + 3.4f
      );

      x += width;
    }

    return y - ROW_HEIGHT;
  }

  /**
   * Draws a uniform value cell: soft drop shadow, very light fill, rounded
   * border. Used everywhere a value is rendered so all inputs look consistent.
   */
  private void drawFieldCell(PDPageContentStream cs, float x, float y, float w, float h)
          throws Exception {
    // Subtle drop shadow (1pt below).
    setFillGray(cs, FIELD_SHADOW_GRAY);
    drawRoundedPath(cs, x + 0.4f, y - 0.6f, w, h, FIELD_RADIUS);
    cs.fill();

    // Field fill.
    setFillGray(cs, FIELD_FILL_GRAY);
    drawRoundedPath(cs, x, y, w, h, FIELD_RADIUS);
    cs.fill();

    // Border on top.
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
      drawText(cs, x + 6f, y - 12f, up(cell.label()));

      setFont(cs, normal, VALUE_FONT);
      drawText(cs, x + 175f, y - 12f, trimToWidth(up(cell.value()), width - 180f, VALUE_FONT));

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
    drawText(cs, MARGIN_X + 5f, y - 11f, up(label));

    setFont(cs, normal, VALUE_FONT);
    drawCenteredText(
            cs,
            trimToWidth(up(value), valueW - 6f, VALUE_FONT),
            MARGIN_X + labelW + (valueW / 2f),
            y - 11f
    );

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
      drawCenteredText(cs, up(labelLines[i]), MARGIN_X + (labelW / 2f), y - 15f - (i * 8f));
    }

    setFont(cs, normal, VALUE_FONT);
    writeWrapped(cs, MARGIN_X + labelW + 5f, y - 10f, valueW - 10f, up(value), 5, 8.5f);

    return y - height;
  }

  private float singleValueRow(PDPageContentStream cs, float y, String value, float height)
          throws Exception {
    drawFieldCell(cs, MARGIN_X, y - height, CONTENT_WIDTH, height);

    setFont(cs, normal, VALUE_FONT);
    drawCenteredText(
            cs,
            trimToWidth(up(value), CONTENT_WIDTH - 10f, VALUE_FONT),
            PAGE_WIDTH / 2f,
            y - (height / 2f) - 1f
    );

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

  private void drawBodyFallback(PDPageContentStream cs, float panelTopY, float panelHeight)
          throws Exception {
    float panelBottom = panelTopY - panelHeight;
    float centerY = (panelTopY + panelBottom) / 2f;
    float figureHeight = panelHeight - 22f;
    float gap = 40f;
    float halfX = MARGIN_X + (CONTENT_WIDTH / 2f);
    float leftCenter = halfX - (gap / 2f) - (figureHeight * 0.18f);
    float rightCenter = halfX + (gap / 2f) + (figureHeight * 0.18f);

    drawSilhouette(cs, leftCenter, centerY, figureHeight);
    drawSilhouette(cs, rightCenter, centerY, figureHeight);

    setFont(cs, bold, LABEL_FONT);
    drawCenteredText(cs, "VISTA FRONTAL", leftCenter, panelBottom + 6f);
    drawCenteredText(cs, "VISTA DORSAL", rightCenter, panelBottom + 6f);
  }

  /**
   * Draws a simple human silhouette (head, torso, arms, legs) using only PDF
   * primitives, so the layout never collapses to plain text when no body image
   * is available.
   */
  private void drawSilhouette(PDPageContentStream cs, float cx, float cy, float h)
          throws Exception {
    cs.setLineWidth(0.6f);
    cs.setStrokingColor(0.35f, 0.35f, 0.35f);

    float headR = h * 0.085f;
    float headCy = cy + h * 0.40f;
    float neckTop = headCy - headR;
    float shoulderY = cy + h * 0.27f;
    float shoulderHalf = h * 0.18f;
    float waistY = cy - h * 0.02f;
    float waistHalf = h * 0.10f;
    float hipY = cy - h * 0.08f;
    float hipHalf = h * 0.14f;
    float footY = cy - h * 0.48f;
    float handY = cy - h * 0.04f;
    float handHalf = h * 0.30f;

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
    cs.lineTo(cx + hipHalf, hipY);
    cs.lineTo(cx + waistHalf, waistY);
    cs.lineTo(cx + shoulderHalf, shoulderY);
    cs.closePath();
    cs.stroke();

    // Arms.
    cs.moveTo(cx - shoulderHalf, shoulderY);
    cs.lineTo(cx - handHalf, handY);
    cs.stroke();
    cs.moveTo(cx + shoulderHalf, shoulderY);
    cs.lineTo(cx + handHalf, handY);
    cs.stroke();

    // Legs.
    cs.moveTo(cx - hipHalf * 0.9f, hipY);
    cs.lineTo(cx - hipHalf * 0.5f, footY);
    cs.lineTo(cx - hipHalf * 0.1f, footY);
    cs.lineTo(cx - hipHalf * 0.1f, hipY);
    cs.stroke();
    cs.moveTo(cx + hipHalf * 0.9f, hipY);
    cs.lineTo(cx + hipHalf * 0.5f, footY);
    cs.lineTo(cx + hipHalf * 0.1f, footY);
    cs.lineTo(cx + hipHalf * 0.1f, hipY);
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

  private void writeWrapped(
          PDPageContentStream cs,
          float x,
          float y,
          float width,
          String value,
          int maxLines,
          float lineHeight
  ) throws Exception {
    List<String> lines = wrap(value, Math.max(20, (int) (width / 3.1f)));

    for (int i = 0; i < Math.min(maxLines, lines.size()); i++) {
      drawText(cs, x, y - (i * lineHeight), lines.get(i));
    }
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

  private static List<String> wrap(String value, int maxChars) {
    if (value == null || value.isBlank()) {
      return List.of("");
    }

    String[] words = value.trim().split("\\s+");
    List<String> lines = new ArrayList<>();
    StringBuilder current = new StringBuilder();

    for (String word : words) {
      if (current.isEmpty()) {
        current.append(word);
      } else if (current.length() + 1 + word.length() <= maxChars) {
        current.append(' ').append(word);
      } else {
        lines.add(current.toString());
        current = new StringBuilder(word);
      }
    }

    if (!current.isEmpty()) {
      lines.add(current.toString());
    }

    return lines;
  }

  private static String trimToWidth(String value, float width, float fontSize) {
    String safe = nvl(value);
    int maxChars = Math.max(1, (int) (width / (fontSize * 0.50f)));

    if (safe.length() <= maxChars) {
      return safe;
    }

    return safe.substring(0, Math.max(0, maxChars - 3)) + "...";
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
