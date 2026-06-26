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

  private static final float SECTION_HEIGHT = 9f;

  private static final float LABEL_HEIGHT = 7.2f;
  private static final float LABEL_VALUE_GAP = 1.8f;
  private static final float VALUE_HEIGHT = 9.8f;
  private static final float ROW_HEIGHT = LABEL_HEIGHT + LABEL_VALUE_GAP + VALUE_HEIGHT;

  private static final float TITLE_FONT = 12f;
  private static final float HEADER_FONT = 7.2f;
  private static final float SECTION_FONT = 6.3f;
  private static final float LABEL_FONT = 5.9f;
  private static final float VALUE_FONT = 6.0f;
  private static final float FIELD_RADIUS = 1.9f;
  private static final float FIELD_LINE_WIDTH = 0.25f;
  private static final float GRAY = 0.76f;

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
    float logoWidth = 50f;
    float logoHeight = 43f;

    float logoX = MARGIN_X + 42f;
    float logoY = headerTop - 51f;

    try {
      PDImageXObject logo = loadImage(doc, "static/logo.png");
      cs.drawImage(logo, logoX, logoY, logoWidth, logoHeight);
    } catch (Exception ignored) {
      // Logo is optional while testing the PDF layout.
    }

    setFont(cs, bold, TITLE_FONT);
    drawCenteredText(cs, "ATENCIÓN PRE-HOSPITALARIA", PAGE_WIDTH / 2f, headerTop - 22f);

    setFont(cs, bold, 8.5f);
    float versionX = PAGE_WIDTH - 105f;
    drawText(cs, versionX, headerTop - 20f, "FAPH v1");
    drawText(cs, versionX - 8f, headerTop - 35f, "01/03/2025");

    setFont(cs, bold, HEADER_FONT);

    float infoY = headerTop - 64f;

    drawText(cs, MARGIN_X + 4f, infoY, "Placa:");
    drawText(cs, MARGIN_X + 34f, infoY, nvl(aph.getPlaca()));

    drawText(cs, PAGE_WIDTH / 2f - 80f, infoY, "Movil:");
    drawText(cs, PAGE_WIDTH / 2f - 44f, infoY, nvl(aph.getMovil()));

    return headerTop - 75f;
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

    return inlineRow(cs, y,
            inlineCell("Causa Externa Origina la Atencion", nvl(aph.getCausaExterna())),
            inlineCell("Motivo de Consulta", nvl(aph.getDiagnosticos()))
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

    float panelHeight = 136f;
    drawBorder(cs, MARGIN_X, y - panelHeight, CONTENT_WIDTH, panelHeight);

    if (aph.getLesionesImagen() != null && !aph.getLesionesImagen().isBlank()) {
      drawCapturedBodyImage(cs, doc, aph.getLesionesImagen(), y);
    } else {
      drawBodyFallback(cs, PAGE_WIDTH / 2f, y - 8f);
    }

    return y - panelHeight;
  }

  private void drawCapturedBodyImage(
          PDPageContentStream cs,
          PDDocument doc,
          String base64Image,
          float panelTopY
  ) throws Exception {
    byte[] imageBytes = decodeBase64Image(base64Image);

    PDImageXObject bodyImage = PDImageXObject.createFromByteArray(
            doc,
            imageBytes,
            "aph-body-map.png"
    );

    float imageWidth = 195f;
    float imageHeight = 122f;
    float imageX = (PAGE_WIDTH - imageWidth) / 2f;
    float imageY = panelTopY - imageHeight - 7f;

    cs.drawImage(bodyImage, imageX, imageY, imageWidth, imageHeight);
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
    drawCenteredText(cs, "Firma  /  Paciente o Responsable", MARGIN_X + (colW / 2f), y - 12f);
    drawCenteredText(cs, "Firma  /  Sello Encargado del Traslado", MARGIN_X + colW + (colW / 2f), y - 12f);
    drawCenteredText(cs, "Firma  /  Sello Quien recibe al paciente", MARGIN_X + (colW * 2f) + (colW / 2f), y - 12f);

    return y - signatureHeight;
  }

  private float section(PDPageContentStream cs, float y, String title) throws Exception {
    setFillGray(cs, GRAY);
    fillRect(cs, MARGIN_X, y - SECTION_HEIGHT, CONTENT_WIDTH, SECTION_HEIGHT);
    resetColor(cs);

    setFont(cs, bold, SECTION_FONT);
    drawCenteredText(cs, title, PAGE_WIDTH / 2f, y - 7f);

    return y - SECTION_HEIGHT;
  }

  private float dualSection(PDPageContentStream cs, float y, String left, String right, float leftRatio)
          throws Exception {
    float leftW = CONTENT_WIDTH * leftRatio;
    float rightW = CONTENT_WIDTH - leftW;

    setFillGray(cs, GRAY);
    fillRect(cs, MARGIN_X, y - SECTION_HEIGHT, leftW, SECTION_HEIGHT);
    fillRect(cs, MARGIN_X + leftW, y - SECTION_HEIGHT, rightW, SECTION_HEIGHT);
    resetColor(cs);

    setFont(cs, bold, SECTION_FONT);
    drawCenteredText(cs, left, MARGIN_X + (leftW / 2f), y - 7f);
    drawCenteredText(cs, right, MARGIN_X + leftW + (rightW / 2f), y - 7f);

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
      drawCenteredText(cs, cell.label(), x + (width / 2f), y - 5.2f);

      float valueBoxY = y - LABEL_HEIGHT - LABEL_VALUE_GAP - VALUE_HEIGHT;

      drawRoundedBorder(cs, x + 1f, valueBoxY, width - 2f, VALUE_HEIGHT, FIELD_RADIUS);

      setFont(cs, normal, VALUE_FONT);
      drawCenteredText(
              cs,
              trimToWidth(cell.value(), width - 6f, VALUE_FONT),
              x + (width / 2f),
              valueBoxY + 3.1f
      );

      x += width;
    }

    return y - ROW_HEIGHT;
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
    cs.stroke();
  }

  private float inlineRow(PDPageContentStream cs, float y, InlineCell... cells) throws Exception {
    float rowHeight = 17f;
    float width = CONTENT_WIDTH / cells.length;
    float x = MARGIN_X;

    for (InlineCell cell : cells) {
      drawBorder(cs, x, y - rowHeight, width, rowHeight);

      setFont(cs, bold, LABEL_FONT);
      drawText(cs, x + 5f, y - 11f, cell.label());

      setFont(cs, normal, VALUE_FONT);
      drawText(cs, x + 175f, y - 11f, trimToWidth(cell.value(), width - 180f, VALUE_FONT));

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
    float labelW = 85f;
    float valueW = CONTENT_WIDTH - labelW;

    drawBorder(cs, MARGIN_X, y - height, labelW, height);
    drawBorder(cs, MARGIN_X + labelW, y - height, valueW, height);

    setFont(cs, bold, LABEL_FONT);
    drawText(cs, MARGIN_X + 4f, y - 11f, label);

    setFont(cs, bold, VALUE_FONT);
    drawCenteredText(
            cs,
            trimToWidth(value, valueW - 4f, VALUE_FONT),
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
    float labelW = 85f;
    float valueW = CONTENT_WIDTH - labelW;

    drawBorder(cs, MARGIN_X, y - height, labelW, height);
    drawBorder(cs, MARGIN_X + labelW, y - height, valueW, height);

    setFont(cs, bold, LABEL_FONT);
    String[] labelLines = label.split("\\n");
    for (int i = 0; i < labelLines.length; i++) {
      drawCenteredText(cs, labelLines[i], MARGIN_X + (labelW / 2f), y - 15f - (i * 8f));
    }

    setFont(cs, normal, 6.0f);
    writeWrapped(cs, MARGIN_X + labelW + 4f, y - 9f, valueW - 8f, value, 5, 7f);

    return y - height;
  }

  private float singleValueRow(PDPageContentStream cs, float y, String value, float height)
          throws Exception {
    drawBorder(cs, MARGIN_X, y - height, CONTENT_WIDTH, height);

    setFont(cs, bold, VALUE_FONT);
    drawCenteredText(
            cs,
            trimToWidth(value, CONTENT_WIDTH - 8f, VALUE_FONT),
            PAGE_WIDTH / 2f,
            y - 10f
    );

    return y - height;
  }

  private void drawFooterCertification(PDPageContentStream cs) throws Exception {
    float y = 24f;
    float height = 21f;

    drawBorder(cs, MARGIN_X, y - height, CONTENT_WIDTH, height);

    setFont(cs, normal, 6.0f);
    drawText(
            cs,
            MARGIN_X + 4f,
            y - 8f,
            "El profesional de la salud certifica que las lesiones en el presente documento corresponden a hallazgos clínicos ocurridos como consecuencia de accidente de transito."
    );
    drawText(
            cs,
            MARGIN_X + 4f,
            y - 16f,
            "Artículo 32 Decreto 056 de 2015 Ministerio de Salud y Protección Social"
    );
  }

  private void drawBodyFallback(PDPageContentStream cs, float centerX, float y) throws Exception {
    setFont(cs, bold, 8f);
    drawCenteredText(cs, "Imagen de lesiones no encontrada", centerX, y - 70f);

    setFont(cs, normal, 6f);
    drawCenteredText(cs, "Agrega static/body-map.png para igualar el formato Word", centerX, y - 82f);
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
    cs.setLineWidth(0.25f);
    cs.addRect(x, y, w, h);
    cs.stroke();
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
