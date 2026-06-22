package com.servimedical.platform.service;

import com.servimedical.platform.entity.Aph;
import com.servimedical.platform.repository.AphRepository;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class PdfService {

  private final AphRepository repository;
  private final AphService aphService;

  private final PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
  private final PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);

  private static final int ML = 25;
  private static final int PW = 545;
  private static final int GC = 6;

  public PdfService(AphRepository repository, AphService aphService) {
    this.repository = repository;
    this.aphService = aphService;
  }

  public ByteArrayResource generatePdf(Long aphId) {
    var aph = repository.findById(aphId)
            .orElseThrow(() -> new RuntimeException("APH no encontrado con id: " + aphId));

    try (var doc = new PDDocument()) {
      var page = new PDPage(PDRectangle.A4);
      doc.addPage(page);

      try (var cs = new PDPageContentStream(doc, page)) {
        int y = 806;
        y = header(cs, doc, y, aph.getPlaca(), aph.getMovil());

        y = sectionBar(cs, y, "DATOS DEL PACIENTE");
        y = row(cs, y, new Cell[] {
                c("Tipo ID", inferType(aph.getDocumento()), 1),
                c("No. de Identificacion", nvl(aph.getDocumento()), 2),
                c("Nombres y Apellidos", nom(aph), 3),
                c("Sexo", nvl(aph.getSexo()), 1)
        });
        y = row(cs, y, new Cell[] {
                c("Codigo CUPS", nvl(aph.getCodigo()), 1),
                c("Tipo de traslado", nvl(aph.getTipoTraslado()), 2),
                c("Prioridad", nvl(aph.getPrioridad()), 1),
                c("Traslado", nvl(aph.getTraslado()), 2)
        });
        y = row(cs, y, new Cell[] {
                c("Fecha de Nacimiento", fd(aph.getFechaNacimiento()), 2),
                c("Edad", nvl(aph.getEdad()), 1),
                c("Estado Civil", nvl(aph.getEstadoCivil()), 2),
                c("Ocupacion", nvl(aph.getOcupacion()), 2),
                c("Celular", nvl(aph.getCelular()), 2)
        });
        y = row(cs, y, new Cell[] {
                c("Direccion de Residencia", nvl(aph.getDireccion()), 3),
                c("Telefono", nvl(aph.getTelefono()), 1),
                c("Zona", nvl(aph.getZonaPaciente()), 1),
                c("Departamento", nvl(aph.getDepartamento()), 2),
                c("Municipio", nvl(aph.getCiudad()), 2)
        });

        y = sectionBar(cs, y, "LUGAR DE OCURRENCIA");
        y = row(cs, y, new Cell[] {
                c("Fecha de traslado", fd(aph.getFechaAccidente()), 2),
                c("Hora de traslado", ft(aph.getHoraAccidente()), 2),
                c("Lugar de ocurrencia", nvl(aph.getLugarOcurrencia()), 3),
                c("Zona", nvl(aph.getZonaOrigen()), 1),
                c("Departamento", nvl(aph.getDepartamentoOrigen()), 1),
                c("Municipio", nvl(aph.getMunicipioOrigen()), 1)
        });

        y = sectionBar(cs, y, "ACOMPANANTE");
        y = row(cs, y, new Cell[] {
                c("Nombres del Acompanante", nvl(aph.getAcompanante()), 3),
                c("No. de telefono", nvl(aph.getCelularAcompanante()), 2),
                c("Avisar a", nvl(aph.getAvisarA()), 2),
                c("Parentesco", nvl(aph.getParentesco()), 1),
                c("No. de telefono", nvl(aph.getTelefono()), 1)
        });

        y = sectionBar(cs, y, "ASEGURADORA");
        y = row(cs, y, new Cell[] {
                c("Aseguradora Responsable del paciente", nvl(aph.getAseguradora()), 3),
                c("Poliza o No carnet", nvl(aph.getPoliza()), 2),
                c("Descripcion del plan de beneficios", nvl(aph.getPlanBeneficios()), 3)
        });
        y = row(cs, y, new Cell[] {
                c("Hora de llegada", ft(aph.getHoraLlegada()), 2),
                c("Transportado a", nvl(aph.getTransportadoA()), 3),
                c("Cod. Habilitacion", "", 2),
                c("Departamento", nvl(aph.getDepartamentoTraslado()), 1),
                c("Municipio", nvl(aph.getCiudadTransporte()), 1),
                c("Estado", "", 1)
        });

        y = sectionBar(cs, y, "CAUSA EXTERNA");
        y = row(cs, y, new Cell[] {
                c("Causa Externa", nvl(aph.getCausaExterna()), 3),
                c("Motivo de Consulta", nvl(aph.getDiagnosticos()), 3)
        });
        y = row(cs, y, new Cell[] {
                c("Alergias", nvl(aph.getAlergia()), 2),
                c("Patologicos", nvl(aph.getPatologicos()), 2),
                c("Medicacion", nvl(aph.getMedicacion()), 2),
                c("Liquidos", nvl(aph.getLiquidos()), 2)
        });

        y = sectionBar(cs, y, "EXAMEN FISICO");
        y = row(cs, y, new Cell[] {
                c("PA", nvl(aph.getPresion()), 1),
                c("FC", nvl(aph.getFrecuenciaCardiaca()), 1),
                c("FR", nvl(aph.getFrecuenciaRespiratoria()), 1),
                c("Temp", nvl(aph.getTemperatura()), 1),
                c("RO", nvl(aph.getRo()), 1),
                c("RV", nvl(aph.getRv()), 1),
                c("RM", nvl(aph.getRm()), 1),
                c("Hallazgos", nvl(aph.getHallazgos()), 3)
        });

        y = sectionBar(cs, y, "UBICACION DE LAS LESIONES");
        y = injuryPanel(cs, y, aphService.toResponse(aph).getLesiones());

        y = sectionBar(cs, y, "DIAGNOSTICOS / HALLAZGOS");
        y = block(cs, y, "Diagnostico CIE10", nvl(aph.getDiagnosticos()), 2);
        y = block(cs, y, "Describe sus hallazgos", nvl(aph.getHallazgos()), 3);

        y = sectionBar(cs, y, "PROCEDIMIENTOS REALIZADOS");
        y = block(cs, y, "Procedimientos", joinList(aphService.toResponse(aph).getProcedimientos()), 2);

        y = sectionBar(cs, y, "MATERIALES Y DROGAS UTILIZADAS");
        y = block(cs, y, "Materiales", nvl(aph.getMateriales()), 2);

        y = sectionBar(cs, y, "FIRMAS / SELLOS");
        signatures(cs, y, aph);
      }

      var out = new ByteArrayOutputStream();
      doc.save(out);
      return new ByteArrayResource(out.toByteArray());
    } catch (Exception e) {
      throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
    }
  }

  private int header(PDPageContentStream cs, PDDocument doc, int y, String placa, String movil) throws Exception {
    var logo = loadLogo(doc);
    cs.drawImage(logo, ML + 34, y - 52, 50, 50);

    cs.setFont(bold, 12.0f);
    text(cs, ML + 168, y - 16, "ATENCION PRE-HOSPITALARIA");

    cs.setFont(normal, 8.0f);
    text(cs, ML + 498, y - 11, "FAPH v1");
    text(cs, ML + 486, y - 24, "01/03/2025");

    cs.setFont(bold, 8.6f);
    text(cs, ML + 12, y - 66, "Placa: " + nvl(placa));
    text(cs, ML + 186, y - 66, "Movil: " + nvl(movil));

    return y - 80;
  }

  private int sectionBar(PDPageContentStream cs, int y, String label) throws Exception {
    int h = 12;
    cs.setNonStrokingColor(0.68f, 0.68f, 0.68f);
    rectFilled(cs, ML, y - h, PW, h);
    cs.setNonStrokingColor(0, 0, 0);
    cs.setFont(bold, 7.8f);
    float textWidth = label.length() * 4.0f;
    text(cs, ML + (PW / 2) - (textWidth / 2), y - 8, label);
    return y - h - 2;
  }

  private int row(PDPageContentStream cs, int y, Cell[] cells) throws Exception {
    int total = 0;
    for (var c : cells) total += c.span();

    int rowHeight = 34;
    int usableWidth = PW - (GC * (cells.length - 1));
    int x = ML;

    for (int i = 0; i < cells.length; i++) {
      var cell = cells[i];
      int w = (int) Math.round((double) usableWidth * cell.span() / total);
      int labelY = y - 6;
      int boxX = x + 1;
      int boxW = Math.max(18, w - 2);
      int boxY = y - 31;
      int boxH = 16;

      cs.setFont(bold, 7.4f);
      text(cs, x + 2, labelY, cell.label() + ":");

      cs.setNonStrokingColor(0.98f, 0.98f, 0.98f);
      rectFilled(cs, boxX, boxY, boxW, boxH);
      cs.setNonStrokingColor(0, 0, 0);
      cs.setLineWidth(0.65f);
      rect(cs, boxX, boxY, boxW, boxH);

      cs.setFont(normal, 7.15f);
      String value = trunc(cell.value(), Math.max(18, w / 3));
      float valueWidth = Math.max(8f, value.length() * 2.8f);
      float valueX = boxX + (boxW / 2f) - (valueWidth / 2f);
      valueX = Math.max(boxX + 2, Math.min(valueX, boxX + boxW - valueWidth - 2));
      text(cs, valueX, boxY + 4, value);

      x += w + GC;
    }

    return y - rowHeight;
  }

  private int block(PDPageContentStream cs, int y, String label, String value, int lines) throws Exception {
    int h = 20 + (lines * 10);
    cs.setFont(bold, 7.4f);
    text(cs, ML + 3, y - 6, label + ":");
    cs.setNonStrokingColor(0.96f, 0.96f, 0.96f);
    rectFilled(cs, ML, y - h, PW, h - 10);
    cs.setNonStrokingColor(0, 0, 0);
    cs.setLineWidth(0.7f);
    rect(cs, ML, y - h, PW, h - 10);

    cs.setFont(normal, 6.9f);
    writeWrapped(cs, ML + 3, y - 18, PW - 10, value, lines);

    return y - h;
  }

  private int injuryPanel(PDPageContentStream cs, int y, List<String> injuries) throws Exception {
    int panelHeight = 128;
    rect(cs, ML, y - panelHeight, PW, panelHeight);

    int centerX = ML + (PW / 2);
    int bodyTop = y - 16;
    int bodyBottom = y - 110;

    drawBodySilhouette(cs, centerX - 26, bodyTop, false);
    drawBodySilhouette(cs, centerX + 28, bodyTop, true);

    if (injuries != null && !injuries.isEmpty()) {
      cs.setNonStrokingColor(1.0f, 0f, 0f);
      cs.setStrokingColor(1.0f, 0f, 0f);
      for (var inj : injuries) {
        markInjury(cs, centerX, bodyTop, bodyBottom, inj);
      }
      cs.setNonStrokingColor(0f, 0f, 0f);
      cs.setStrokingColor(0f, 0f, 0f);
    } else {
      cs.setFont(normal, 6.9f);
      text(cs, ML + 3, y - 14, "Sin lesiones seleccionadas");
    }

    return y - panelHeight;
  }

  private void drawBodySilhouette(PDPageContentStream cs, int x, int topY, boolean backView) throws Exception {
    int headX = x + 20;
    int headY = topY - 6;

    cs.setLineWidth(0.9f);

    drawCircle(cs, headX + 8, headY - 6, 6.5f);

    cs.addRect(x + 14, topY - 33, 12, 25);
    cs.stroke();

    cs.moveTo(x + 20, topY - 33);
    cs.lineTo(x + 8, topY - 57);
    cs.lineTo(x + 3, topY - 57);
    cs.lineTo(x + 14, topY - 33);
    cs.stroke();

    cs.moveTo(x + 26, topY - 33);
    cs.lineTo(x + 38, topY - 57);
    cs.lineTo(x + 43, topY - 57);
    cs.lineTo(x + 30, topY - 33);
    cs.stroke();

    cs.moveTo(x + 17, topY - 57);
    cs.lineTo(x + 12, topY - 89);
    cs.moveTo(x + 28, topY - 57);
    cs.lineTo(x + 33, topY - 89);
    cs.stroke();

    cs.moveTo(x + 14, topY - 89);
    cs.lineTo(x + 11, topY - 111);
    cs.moveTo(x + 30, topY - 89);
    cs.lineTo(x + 33, topY - 111);
    cs.stroke();

    cs.moveTo(x + 14, topY - 111);
    cs.lineTo(x + 12, topY - 125);
    cs.moveTo(x + 30, topY - 111);
    cs.lineTo(x + 32, topY - 125);
    cs.stroke();

    if (backView) {
      cs.moveTo(x + 20, topY - 33);
      cs.lineTo(x + 20, topY - 89);
      cs.stroke();
    } else {
      cs.moveTo(x + 20, topY - 33);
      cs.lineTo(x + 20, topY - 89);
      cs.stroke();

      cs.moveTo(x + 14, topY - 47);
      cs.lineTo(x + 26, topY - 47);
      cs.stroke();
    }
  }

  private void markInjury(PDPageContentStream cs, int centerX, int topY, int bottomY, String injury) throws Exception {
    String text = injury == null ? "" : injury.toLowerCase();

    int x = centerX;
    int y = topY - 42;

    if (text.contains("cabeza")) {
      x = centerX - 35;
      y = topY - 8;
    } else if (text.contains("cara")) {
      x = centerX - 35;
      y = topY - 18;
    } else if (text.contains("torax") || text.contains("pecho")) {
      x = centerX - 35;
      y = topY - 38;
    } else if (text.contains("abdomen") || text.contains("cadera")) {
      x = centerX + 35;
      y = topY - 45;
    } else if (text.contains("brazo") || text.contains("hombro")) {
      x = centerX - 35;
      y = topY - 30;
    } else if (text.contains("pierna") || text.contains("rodilla") || text.contains("muslo")) {
      x = centerX - 35;
      y = bottomY + 18;
    }

    drawCircle(cs, x, y, 4.5f);
  }

  private int signatures(PDPageContentStream cs, int y, Aph aph) throws Exception {
    int h = 98;
    rect(cs, ML, y - h, PW, h);

    int colW = PW / 3;
    int x1 = ML;
    int x2 = ML + colW;
    int x3 = ML + (colW * 2);

    cs.setFont(bold, 7.3f);
    text(cs, x1 + 10, y - 10, "Conductor");
    text(cs, x2 + 10, y - 10, "Encargado del Traslado");
    text(cs, x3 + 10, y - 10, "Quien recibe al paciente");

    cs.setFont(normal, 7.0f);
    text(cs, x1 + 10, y - 23, nvl(aph.getConductor()) + " " + nvl(aph.getDocumentoMedico()));
    text(cs, x2 + 10, y - 23, nvl(aph.getParamedico()) + " " + nvl(aph.getDocumentoMedico()));
    text(cs, x3 + 10, y - 24, "");

    cs.setFont(bold, 7.0f);
    text(cs, x1 + 10, y - 35, "Firma / Paciente o Responsable");
    text(cs, x2 + 10, y - 35, "Firma / Sello Encargado del Traslado");
    text(cs, x3 + 10, y - 35, "Firma / Sello Quien recibe al paciente");

    cs.setFont(normal, 6.6f);
    text(cs, ML + 5, y - 85, "El profesional de la salud certifica que las lesiones en el presente documento corresponden a hallazgos clinicos ocurridos como consecuencia de accidente de transito. Articulo 32 Decreto 056 de 2015 Ministerio de Salud y Proteccion Social");

    return y - h;
  }

  private int labelValueField(PDPageContentStream cs, int y, String label, String value, int width, boolean centered) throws Exception {
    cs.setFont(bold, 7.4f);
    text(cs, ML + 3, y - 6, label + ":");
    rect(cs, ML, y - 20, width, 14);
    cs.setFont(normal, 7.4f);
    float x = centered ? ML + (width / 2f) - Math.min(40, width / 6f) : ML + 3;
    text(cs, x, y - 15, value);
    return y - 24;
  }

  private void drawCircle(PDPageContentStream cs, float centerX, float centerY, float radius) throws Exception {
    float k = 0.552284749831f;
    float c = radius * k;

    cs.moveTo(centerX, centerY + radius);
    cs.curveTo(centerX + c, centerY + radius, centerX + radius, centerY + c, centerX + radius, centerY);
    cs.curveTo(centerX + radius, centerY - c, centerX + c, centerY - radius, centerX, centerY - radius);
    cs.curveTo(centerX - c, centerY - radius, centerX - radius, centerY - c, centerX - radius, centerY);
    cs.curveTo(centerX - radius, centerY + c, centerX - c, centerY + radius, centerX, centerY + radius);
    cs.fill();
  }

  private PDImageXObject loadLogo(PDDocument doc) throws Exception {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("static/logo.png")) {
      if (in == null) {
        throw new RuntimeException("No se encontro el logo en static/logo.png");
      }
      return PDImageXObject.createFromByteArray(doc, in.readAllBytes(), "logo.png");
    }
  }

  private void rect(PDPageContentStream cs, int x, int y, int w, int h) throws Exception {
    cs.setLineWidth(0.5f);
    cs.addRect(x, y, w, h);
    cs.stroke();
  }

  private void rectFilled(PDPageContentStream cs, int x, int y, int w, int h) throws Exception {
    cs.addRect(x, y, w, h);
    cs.fill();
  }

  private void text(PDPageContentStream cs, float x, float y, String s) throws Exception {
    cs.beginText();
    cs.newLineAtOffset(x, y);
    cs.showText(escape(s));
    cs.endText();
  }

  private void writeWrapped(PDPageContentStream cs, int x, int y, int w, String value, int maxLines) throws Exception {
    var lines = wrap(value, Math.max(14, w / 5));
    int limit = Math.min(lines.size(), maxLines);
    for (int i = 0; i < limit; i++) {
      text(cs, x, y - (i * 9), lines.get(i));
    }
  }

  private static List<String> wrap(String value, int maxChars) {
    if (value == null || value.isBlank()) {
      return List.of("");
    }

    var words = value.trim().split("\\s+");
    var lines = new ArrayList<String>();
    var current = new StringBuilder();

    for (var word : words) {
      if (current.length() == 0) {
        current.append(word);
      } else if (current.length() + 1 + word.length() <= maxChars) {
        current.append(' ').append(word);
      } else {
        lines.add(current.toString());
        current = new StringBuilder(word);
      }
    }

    if (current.length() > 0) {
      lines.add(current.toString());
    }

    return lines;
  }

  private static String joinList(List<String> values) {
    return values == null || values.isEmpty() ? "" : String.join(", ", values);
  }

  private static String escape(String s) {
    return s == null ? "" : s.replace("(", "\\(").replace(")", "\\)");
  }

  private static String nom(Aph a) {
    return nvl(a.getPrimerNombre()) + " " + nvl(a.getPrimerApellido());
  }

  private static String trunc(String s, int max) {
    if (s == null || s.length() <= max) {
      return s != null ? s : "";
    }
    return s.substring(0, max - 3) + "...";
  }

  private static String inferType(String doc) {
    if (doc == null || doc.isBlank()) return "";
    int l = doc.length();
    return l >= 10 ? "CC" : l >= 6 ? "CE" : "TI";
  }

  private static String nvl(String v) {
    return v != null && !v.isBlank() ? v : "";
  }

  private static String fd(Object d) {
    return d != null ? d.toString() : "";
  }

  private static String ft(Object t) {
    return t != null ? t.toString() : "";
  }

  private record Cell(String label, String value, int span) {}

  private static Cell c(String label, String value, int span) {
    return new Cell(label, value, span);
  }
}
