package com.jobscout.configuration;

import org.apache.poi.ss.usermodel.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api/configurations")
public class ConfigurationUploadController {
    private static final List<String> REQUIRED = List.of(
        "company_id", "company_name", "career_url", "enabled", "interaction_type"
    );

    @PostMapping(value="/upload", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String,Object> upload(@RequestPart("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) throw new IllegalArgumentException("Workbook is empty");
        try (InputStream in=file.getInputStream(); Workbook workbook=WorkbookFactory.create(in)) {
            Sheet sheet=workbook.getSheet("Companies");
            if (sheet==null) throw new IllegalArgumentException("Workbook must contain a Companies sheet");
            Row header=sheet.getRow(0);
            if (header==null) throw new IllegalArgumentException("Companies sheet has no header row");
            DataFormatter formatter=new DataFormatter();
            Map<String,Integer> columns=new HashMap<>();
            for (Cell cell:header) columns.put(formatter.formatCellValue(cell).trim().toLowerCase(Locale.ROOT), cell.getColumnIndex());
            List<String> missing=REQUIRED.stream().filter(h -> !columns.containsKey(h)).toList();
            if (!missing.isEmpty()) throw new IllegalArgumentException("Missing columns: " + missing);
            List<Map<String,String>> rows=new ArrayList<>();
            for(int i=1;i<=sheet.getLastRowNum();i++){
                Row row=sheet.getRow(i); if(row==null) continue;
                Map<String,String> values=new LinkedHashMap<>();
                for(String h:REQUIRED){
                    Cell cell=row.getCell(columns.get(h), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    values.put(h, cell==null ? "" : formatter.formatCellValue(cell).trim());
                }
                if(values.values().stream().anyMatch(v -> !v.isBlank())) rows.add(values);
            }
            return Map.of("fileName", Objects.toString(file.getOriginalFilename(),"workbook.xlsx"),
                "sheet","Companies","parsedCompanyRows",rows.size(),"rows",rows,
                "message","Basic workbook parsing succeeded. Full multi-sheet configuration import is not implemented.");
        }
    }
}
