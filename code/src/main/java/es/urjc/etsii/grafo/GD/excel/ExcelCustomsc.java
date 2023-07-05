package es.urjc.etsii.grafo.GD.excel;

import es.urjc.etsii.grafo.GD.model.GDInstance;
import es.urjc.etsii.grafo.GD.model.GDSolution;
import es.urjc.etsii.grafo.io.serializers.excel.ExcelCustomizer;
import es.urjc.etsii.grafo.solver.services.events.AbstractEventStorage;
import es.urjc.etsii.grafo.solver.services.events.types.MorkEvent;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public class ExcelCustomsc extends ExcelCustomizer {

    private final AbstractEventStorage<GDSolution, GDInstance> abstractEventStorage;

    public ExcelCustomsc(AbstractEventStorage<GDSolution, GDInstance> abstractEventStorage) {
        this.abstractEventStorage = abstractEventStorage;
    }

    @Override
    public void customize(XSSFWorkbook excelBook) {
        List<MorkEvent> morkEvents = this.abstractEventStorage.getAllEvents().toList();

        for (Object morkEvent : morkEvents) {
            if (morkEvent instanceof EvolutionEvent event) {
                var worksheet = excelBook.getSheet("Evolution");
                if (worksheet == null) {
                    worksheet = excelBook.createSheet("Evolution");
                    Row row = worksheet.createRow(0);
                    row.createCell(0).setCellValue("Algorithm");
                    row.createCell(1).setCellValue("Instance");
                    for (int i = 2; i < event.getMaxIter()+2; i++) {
                        row.createCell(i).setCellValue(i - 1);
                    }
                }
                int lastRow = worksheet.getLastRowNum();
                var row = worksheet.createRow(++lastRow);
                row.createCell(0).setCellValue(event.getAlgorithm());
                row.createCell(1).setCellValue(event.getInstance());
                for (int i = 2; i < event.getEvolution().size() + 2; i++) {
                    row.createCell(i).setCellValue(event.getEvolution().get(i - 2));
                }
            }
        }
    }
}
