package com.servimedical.platform.service;

import com.servimedical.platform.dto.AphResponse;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PdfService {

  private final AphRepository repository;
  private final AphService aphService;

  private static final int MARGIN = 40;
  private static final int LINE_HEIGHT = 14;
  private static final int COLUMN_WIDTH = 200;

  private final PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
  private final PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

  public ByteArrayResource generatePdf(Long aphId) {
    var aph = repository.findById(aphId)
        .orElseThrow(() -> new RuntimeException("APH no encontrado con id: " + aphId));

    try (var document = new PDDocument()) {
      var page = new PDPage(PDRectangle.A4);
      document.addPage(page);

      try (var content = new PDPageContentStream(document, page)) {
        content.setFont(bold, 16);
        content.beginText();
        content.newLineAtOffset(MARGIN, 750);
        content.showText("FORMATO ATENCION PRE-HOSPITALARIA");
        content.endText();

        content.setFont(normal, 10);
        int y = 720;

        y = addSection(content, y, "DATOS GENERALES");
        y = addField(content, y, "Codigo APH", aph.getCodigo());
        y = addField(content, y, "Movil", aph.getMovil());
        y = addField(content, y, "Placa", aph.getPlaca());
        y = addField(content, y, "Tipo Traslado", aph.getTipoTraslado());
        y = addField(content, y, "Prioridad", aph.getPrioridad());
        y = addField(content, y, "Fecha Accidente", aph.getFechaAccidente() != null ? aph.getFechaAccidente().toString() : "");
        y = addField(content, y, "Lugar", aph.getLugarOcurrencia());

        y = addSection(content, y, "DATOS DEL PACIENTE");
        y = addField(content, y, "Documento", aph.getDocumento());
        y = addField(content, y, "Nombres", aph.getPrimerNombre() + " " + (aph.getSegundoNombre() != null ? aph.getSegundoNombre() : ""));
        y = addField(content, y, "Apellidos", aph.getPrimerApellido() + " " + (aph.getSegundoApellido() != null ? aph.getSegundoApellido() : ""));
        y = addField(content, y, "Sexo", aph.getSexo());
        y = addField(content, y, "Edad", aph.getEdad());
        y = addField(content, y, "Celular", aph.getCelular());

        y = addSection(content, y, "CAUSA EXTERNA");
        y = addField(content, y, "Causa", aph.getCausaExterna());

        y = addSection(content, y, "EXAMEN FISICO");
        y = addField(content, y, "Presion Arterial", aph.getPresion());
        y = addField(content, y, "Frec. Cardiaca", aph.getFrecuenciaCardiaca());
        y = addField(content, y, "Frec. Respiratoria", aph.getFrecuenciaRespiratoria());
        y = addField(content, y, "Temperatura", aph.getTemperatura());
        y = addField(content, y, "Glasgow", "RO: " + aph.getRo() + " RV: " + aph.getRv() + " RM: " + aph.getRm());

        y = addSection(content, y, "DIAGNOSTICOS / HALLAZGOS");
        y = addMultiline(content, y, "Diagnosticos", aph.getDiagnosticos());
        y = addMultiline(content, y, "Hallazgos", aph.getHallazgos());

        y = addSection(content, y, "PROCEDIMIENTOS REALIZADOS");
        var response = aphService.toResponse(aph);
        y = addMultiline(content, y, "Procedimientos", String.join(", ", response.getProcedimientos() != null ? response.getProcedimientos() : List.of()));

        y = addSection(content, y, "MATERIALES Y DROGAS");
        y = addMultiline(content, y, "Materiales", aph.getMateriales());

        y = addSection(content, y, "TRIPULACION");
        y = addField(content, y, "Conductor", aph.getConductor());
        y = addField(content, y, "Paramedico", aph.getParamedico());
        y = addField(content, y, "Medico responsable", aph.getMedico());
      }

      var baos = new ByteArrayOutputStream();
      document.save(baos);
      return new ByteArrayResource(baos.toByteArray());
    } catch (Exception e) {
      throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
    }
  }

  private int addSection(PDPageContentStream content, int y, String text) throws Exception {
    content.setFont(bold, 11);
    content.beginText();
    content.newLineAtOffset(MARGIN, y);
    content.showText("--- " + text + " ---");
    content.endText();
    return y - LINE_HEIGHT;
  }

  private int addField(PDPageContentStream content, int y, String label, String value) throws Exception {
    content.setFont(normal, 9);
    content.beginText();
    content.newLineAtOffset(MARGIN, y);
    content.showText(label + ":");
    content.endText();
    content.setFont(bold, 9);
    content.beginText();
    content.newLineAtOffset(MARGIN + COLUMN_WIDTH, y);
    content.showText(value != null ? value : "");
    content.endText();
    return y - LINE_HEIGHT;
  }

  private int addMultiline(PDPageContentStream content, int y, String label, String value) throws Exception {
    y = addField(content, y, label, "");
    if (value == null || value.isBlank()) {
      return y;
    }
    content.setFont(normal, 9);
    var lines = value.split("\\r?\\n");
    for (var line : lines) {
      content.beginText();
      content.newLineAtOffset(MARGIN + COLUMN_WIDTH, y);
      content.showText(line.length() > 80 ? line.substring(0, 80) + "..." : line);
      content.endText();
      y -= LINE_HEIGHT;
    }
    return y;
  }
}
