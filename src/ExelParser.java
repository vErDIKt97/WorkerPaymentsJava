import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;

public class ExelParser {

    public static HashMap<String, Integer> parse(File file) {
        //инициализируем потоки
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        XSSFWorkbook workBook = null;
        try {
            workBook = new XSSFWorkbook(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //разбираем первый лист входного файла на объектную модель
        Sheet sheet = workBook.getSheetAt(0);
        Iterator<Row> it = sheet.iterator();
        //проходим по всему листу
        for (int i = 0; i < 4; i++) {
            it.next();
        }
        while (it.hasNext()) {
            Row row = it.next();
            result.put(row.getCell(0).getStringCellValue(), (int) row.getCell(1).getNumericCellValue());

        }

        return result;
    }

}