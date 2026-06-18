package com.servimedical.platform.service;

import com.servimedical.platform.entity.Aph;
import com.servimedical.platform.repository.AphRepository;
import java.io.ByteArrayOutputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PdfService {

  private final AphRepository repository;
  private final AphService aphService;

  private final PDType1Font fBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
  private final PDType1Font fNorm = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

  private static final int ML = 25;
  private static final int PW = 545;
  private static final int LH = 13;

  public ByteArrayResource generatePdf(Long aphId) {
    var aph = repository.findById(aphId)
        .orElseThrow(() -> new RuntimeException("APH no encontrado con id: " + aphId));

    try (var doc = new PDDocument()) {
      var page = new PDPage(PDRectangle.A4);
      doc.addPage(page);

      try (var cs = new PDPageContentStream(doc, page)) {
        int y = 800;

        y = header(cs, doc, y, aph.getPlaca(), aph.getMovil());

        y = section(cs, y, "DATOS DEL PACIENTE");
        y = row4(cs, y, "Tipo ID", inferType(aph.getDocumento()), "No. de Identificacion", nvl(aph.getDocumento()),
            "Nombres y Apellidos", nom(aph), "Sexo", nvl(aph.getSexo()));
        y = row3(cs, y, "Codigo CUPS", nvl(aph.getCodigo()), "Tipo de traslado", nvl(aph.getTipoTraslado()), "Prioridad", nvl(aph.getPrioridad()));

        y = section(cs, y, "LUGAR DE OCURRENCIA");
        y = row3(cs, y, "Fecha de traslado", fd(aph.getFechaAccidente()), "Hora de traslado", ft(aph.getHoraAccidente()), "Lugar de ocurrencia", nvl(aph.getLugarOcurrencia()));
        y = row3(cs, y, "Zona", nvl(aph.getZonaOrigen()), "Departamento", nvl(aph.getDepartamentoOrigen()), "Municipio", nvl(aph.getMunicipioOrigen()));

        y = row3(cs, y, "Fecha de Nacimiento", fd(aph.getFechaNacimiento()), "Edad", nvl(aph.getEdad()), "Estado Civil", nvl(aph.getEstadoCivil()));
        y = row2(cs, y, "Ocupacion", nvl(aph.getOcupacion()), "Direccion de Residencia", nvl(aph.getDireccion()));
        y = row3(cs, y, "Telefono", nvl(aph.getTelefono()), "Zona", nvl(aph.getZonaPaciente()), "Departamento", nvl(aph.getDepartamento()));
        y = row2(cs, y, "Celular", nvl(aph.getCelular()), "Municipio", nvl(aph.getCiudad()));

        y = section(cs, y, "ACOMPANANTE");
        y = row2(cs, y, "Nombres del Acompanante", nvl(aph.getAcompanante()), "No. de telefono", nvl(aph.getCelularAcompanante()));
        y = row3(cs, y, "Avisar a", nvl(aph.getAvisarA()), "Parentesco", nvl(aph.getParentesco()), "No. de telefono", nvl(aph.getTelefono()));

        y = section(cs, y, "ASEGURADORA");
        y = row3(cs, y, "Aseguradora Responsable", nvl(aph.getAseguradora()), "Poliza o No carnet", nvl(aph.getPoliza()), "Plan de beneficios", nvl(aph.getPlanBeneficios()));

        y = section(cs, y, "DATOS DE TRASLADO");
        y = row2(cs, y, "Hora de llegada", ft(aph.getHoraLlegada()), "Transportado a", nvl(aph.getTransportadoA()));
        y = row2(cs, y, "Departamento", nvl(aph.getDepartamentoTraslado()), "Municipio", nvl(aph.getCiudadTransporte()));

        y = section(cs, y, "CAUSA EXTERNA");
        y = row2(cs, y, "Causa Externa", nvl(aph.getCausaExterna()), "Motivo de Consulta", nvl(aph.getDiagnosticos()));
        y = row3(cs, y, "Alergias", nvl(aph.getAlergia()), "Antecedentes Personales", nvl(aph.getPatologicos()), "Liquidos y Alimentos", nvl(aph.getLiquidos()));
        y = row1(cs, y, "Medicacion", nvl(aph.getMedicacion()));

        y = section(cs, y, "EXAMEN FISICO");
        y = row4(cs, y, "PA", nvl(aph.getPresion()), "FC", nvl(aph.getFrecuenciaCardiaca()), "FR", nvl(aph.getFrecuenciaRespiratoria()), "Temp", nvl(aph.getTemperatura()));
        y = row3(cs, y, "Glasgow RO", nvl(aph.getRo()), "RV", nvl(aph.getRv()), "RM", nvl(aph.getRm()));
        y = row1(cs, y, "Hallazgos", nvl(aph.getHallazgos()));

        y = section(cs, y, "DIAGNOSTICOS CIE10");
        y = box(cs, y, nvl(aph.getDiagnosticos()));

        y = section(cs, y, "PROCEDIMIENTOS REALIZADOS");
        var resp = aphService.toResponse(aph);
        y = box(cs, y, String.join(", ", resp.getProcedimientos() != null ? resp.getProcedimientos() : List.of()));

        y = section(cs, y, "MATERIALES Y DROGAS UTILIZADAS");
        y = box(cs, y, nvl(aph.getMateriales()));

        y = section(cs, y, "FIRMAS / SELLOS");
        y = row2(cs, y, "Conductor", nvl(aph.getConductor()), "Encargado del Traslado", nvl(aph.getParamedico()));
        y = row2(cs, y, "Quien recibe al paciente", nvl(aph.getMedico()), "Firma Paciente / Responsable", "");

        y -= 4;
        feet(cs, y);
      }

      var out = new ByteArrayOutputStream();
      doc.save(out);
      return new ByteArrayResource(out.toByteArray());
    } catch (Exception e) {
      throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
    }
  }

  private int header(PDPageContentStream cs, PDDocument doc, int y, String placa, String movil) throws Exception {
    var logo = PDImageXObject.createFromFile("src/main/resources/static/logo.png", doc);
    cs.drawImage(logo, ML + 2, y - 34, 30, 30);
    cs.setFont(fBold, 15);
    text(cs, ML + 38, y - 10, "ATENCION PRE-HOSPITALARIA");
    cs.setFont(fNorm, 7);
    text(cs, ML + 38, y - 22, "FAPH v1   01/03/2025");
    cs.setFont(fNorm, 8);
    text(cs, ML + PW - 110, y - 10, "Placa: " + nvl(placa));
    text(cs, ML + PW - 110, y - 22, "Movil: " + nvl(movil));
    y -= 38;
    line(cs, ML, y, ML + PW, y);
    return y - 5;
  }

  private int section(PDPageContentStream cs, int y, String label) throws Exception {
    cs.setFont(fBold, 8);
    cs.setNonStrokingColor(0x25 / 255f, 0xa8 / 255f, 0xb7 / 255f);
    text(cs, ML, y - 3, label);
    cs.setNonStrokingColor(0, 0, 0);
    line(cs, ML, y - 5, ML + PW, y - 5);
    return y - LH;
  }

  private int row1(PDPageContentStream cs, int y, String l1, String v1) throws Exception {
    return rowN(cs, y, new String[][]{{l1, v1}});
  }

  private int row2(PDPageContentStream cs, int y, String l1, String v1, String l2, String v2) throws Exception {
    return rowN(cs, y, new String[][]{{l1, v1}, {l2, v2}});
  }

  private int row3(PDPageContentStream cs, int y, String l1, String v1, String l2, String v2, String l3, String v3) throws Exception {
    return rowN(cs, y, new String[][]{{l1, v1}, {l2, v2}, {l3, v3}});
  }

  private int row4(PDPageContentStream cs, int y, String l1, String v1, String l2, String v2, String l3, String v3, String l4, String v4) throws Exception {
    return rowN(cs, y, new String[][]{{l1, v1}, {l2, v2}, {l3, v3}, {l4, v4}});
  }

  private int rowN(PDPageContentStream cs, int y, String[][] cells) throws Exception {
    int n = cells.length;
    int cw = PW / n;
    for (int i = 0; i < n; i++) {
      int x = ML + i * cw;
      int w = (i == n - 1) ? PW - i * cw : cw;
      rect(cs, x, y - LH, w, LH);
      cs.setFont(fBold, 6);
      text(cs, x + 2, y - 4, cells[i][0] + ":");
      cs.setFont(fNorm, 7);
      text(cs, x + 2, y - LH + 3, trunc(cells[i][1], w / 5));
    }
    return y - LH;
  }

  private int box(PDPageContentStream cs, int y, String text) throws Exception {
    int h = 22;
    rect(cs, ML, y - h, PW, h);
    cs.setFont(fNorm, 7);
    text(cs, ML + 3, y - 8, trunc(text, 120));
    return y - h;
  }

  private int feet(PDPageContentStream cs, int y) throws Exception {
    cs.setFont(fNorm, 5.5f);
    text(cs, ML, y, "El profesional de la salud certifica que las lesiones en el presente documento corresponden a hallazgos");
    text(cs, ML, y - 7, "clinicos ocurridos como consecuencia de accidente de transito. Art. 32 Decreto 056 de 2015 Minsalud.");
    return y - 14;
  }

  private void rect(PDPageContentStream cs, int x, int y, int w, int h) throws Exception {
    cs.setLineWidth(0.5f);
    cs.addRect(x, y, w, h);
    cs.stroke();
  }

  private void line(PDPageContentStream cs, int x1, int y1, int x2, int y2) throws Exception {
    cs.setLineWidth(0.5f);
    cs.moveTo(x1, y1);
    cs.lineTo(x2, y2);
    cs.stroke();
  }

  private void text(PDPageContentStream cs, int x, int y, String s) throws Exception {
    cs.beginText();
    cs.newLineAtOffset(x, y);
    cs.showText(s);
    cs.endText();
  }

  private static String nom(Aph a) {
    return nvl(a.getPrimerNombre()) + " " + nvl(a.getPrimerApellido());
  }

  private static String trunc(String s, int max) {
    if (s == null || s.length() <= max) return s != null ? s : "";
    return s.substring(0, max - 3) + "...";
  }

  private static String inferType(String doc) {
    if (doc == null || doc.isBlank()) return "";
    int l = doc.length();
    return l >= 10 ? "CC" : l >= 6 ? "CE" : "TI";
  }

  private static String nvl(String v) { return v != null && !v.isBlank() ? v : ""; }
  private static String fd(Object d) { return d != null ? d.toString() : ""; }
  private static String ft(Object t) { return t != null ? t.toString() : ""; }
}
