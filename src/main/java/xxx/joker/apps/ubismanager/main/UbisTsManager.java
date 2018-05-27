package xxx.joker.apps.ubismanager.main;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import xxx.joker.apps.ubismanager.common.TsConfigs;
import xxx.joker.apps.ubismanager.common.TsConfigs.ExtType;
import xxx.joker.apps.ubismanager.common.TsStuffUtils;
import xxx.joker.apps.ubismanager.logger.LogService;
import xxx.joker.apps.ubismanager.logger.SimpleLog;
import xxx.joker.apps.ubismanager.model.Timesheet;
import xxx.joker.apps.ubismanager.service.UbisHoursPolicyService;
import xxx.joker.apps.ubismanager.service.UbisTsFixedValuesService;
import xxx.joker.libs.javalibs.utils.JkCsv;
import xxx.joker.libs.javalibs.utils.JkFiles;
import xxx.joker.libs.javalibs.utils.JkStreams;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by f.barbano on 07/04/2018.
 */
public class UbisTsManager {

	private static final SimpleLog logger = LogService.getLogger(UbisTsManager.class);

	private static final String VENDOR = "Vendor";

	private static final TsConfigs config = TsConfigs.getInstance();
	private static final UbisHoursPolicyService ubisPolicy = UbisHoursPolicyService.getInstance();
	private static final UbisTsFixedValuesService tsValues = UbisTsFixedValuesService.getInstance();


	public static void createUbisTs() throws IOException, InvalidFormatException {
		// Parse summary CSV to get timesheets
		List<Timesheet> timesheets = SummaryManager.parseSummaryCsv();
		logger.info("Summary CSV parsed: %d timesheets found", timesheets.size());

		// Create UBIS formatted timesheet CSV
		Path csvOutPath = createTimesheetCsv(timesheets);
		logger.info("Created UBIS TS csv at '%s'", csvOutPath);

		// Create UBIS formatted timesheet XLSX
		Path xlsxOutPath = createTimesheetXlsx(timesheets);
		logger.info("Created UBIS TS xlsx at '%s'", xlsxOutPath);
	}

	private static Path createTimesheetCsv(List<Timesheet> timesheets) throws IOException {
		String header = Files.readAllLines(config.getTemplateUbisTsPath(ExtType.CSV)).get(0);
		List<String> lines = new ArrayList<>();
		lines.add(header);
		for(Timesheet ts : timesheets) {
		 	lines.addAll(createTsCsvLines(ts));
		}
		Path outPath = config.getOutUbisTsFilename(ExtType.CSV);
		TsStuffUtils.versionize(outPath);
		JkFiles.writeFile(outPath, lines, false);
		return outPath;
	}
	private static List<String> createTsCsvLines(Timesheet ts) {
		List<String> lines = new ArrayList<>();
		for(Map.Entry<LocalDate, Integer> e : ts.getHourMap().entrySet()) {
			List<String> fields = new ArrayList<>();
			fields.add(tsValues.getVendor());
			fields.add(tsValues.getDayFormat().format(e.getKey()));
			fields.add(ts.getResource().getUserID());
			fields.add(ts.getResource().getFirstName());
			fields.add(ts.getResource().getLastName());
			fields.add(tsValues.getProjectAsset());
			fields.add(tsValues.getProjectAssetID());
			fields.add(ts.getResource().getActivity());
			fields.add(String.valueOf(ubisPolicy.getMultiplier(e.getKey()) * e.getValue()));
			fields.add("");
			fields.add(tsValues.getShCID());
			fields.add("");
			lines.add(JkStreams.join(fields, JkCsv.CSV_SEP));
		}
		return lines;
	}

	public static Path createTimesheetXlsx(List<Timesheet> timesheets) throws IOException, InvalidFormatException {
		Path templatePath = config.getTemplateUbisTsPath(ExtType.XLS);

		try (Workbook wb = openWorkbook(templatePath)) {
			Sheet sheet = wb.getSheetAt(0);

			// Create cell style
			CellStyle cellStyle = createCellStyle(wb);

			// Fill xlsx sheet
			int rowNum = getHeaderRow(sheet);
			for(Timesheet ts : timesheets) {
				for(Map.Entry<LocalDate, Integer> e : ts.getHourMap().entrySet()) {
					rowNum++;
					Row row = sheet.createRow(rowNum);
					createCell(row, 0, cellStyle).setCellValue(tsValues.getVendor());
					createCell(row, 1, cellStyle).setCellValue(tsValues.getDayFormat().format(e.getKey()));
					createCell(row, 2, cellStyle).setCellValue(ts.getResource().getUserID());
					createCell(row, 3, cellStyle).setCellValue(ts.getResource().getFirstName());
					createCell(row, 4, cellStyle).setCellValue(ts.getResource().getLastName());
					createCell(row, 5, cellStyle).setCellValue(tsValues.getProjectAsset());
					createCell(row, 6, cellStyle).setCellValue(tsValues.getProjectAssetID());
					createCell(row, 7, cellStyle).setCellValue(ts.getResource().getActivity());
					createCell(row, 8, cellStyle, CellType.NUMERIC).setCellValue(ubisPolicy.getMultiplier(e.getKey()) * e.getValue());
					createCell(row, 9, cellStyle).setCellValue("");
					createCell(row, 10, cellStyle).setCellValue(tsValues.getShCID());
					createCell(row, 11, cellStyle).setCellValue("");
					// Add bordered cell for next 3 optional fields
					createCell(row, 12, cellStyle).setCellValue("");
					createCell(row, 13, cellStyle).setCellValue("");
					createCell(row, 14, cellStyle).setCellValue("");
				}
			}

			// Persist ubis ts xlsx
			Path outPath = config.getOutUbisTsFilename(ExtType.XLS);
			TsStuffUtils.versionize(outPath);
			JkFiles.copyFile(templatePath, outPath, false);
			try (FileOutputStream fos = new FileOutputStream(outPath.toFile())) {
				wb.write(fos);
			}

			return outPath;
		}
	}
	private static Workbook openWorkbook(Path path) throws IOException, InvalidFormatException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(path.toFile());
			return WorkbookFactory.create(fis);

		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}
	private static int getHeaderRow(Sheet sheet) {
		int rowEnd = sheet.getLastRowNum() + 1;
		for(int rowNum = 0; rowNum < rowEnd; rowNum++) {
			Cell firstCell = sheet.getRow(rowNum).getCell(0);
			if(firstCell != null && VENDOR.equals(firstCell.getStringCellValue())) {
				return rowNum;
			}
		}
		return -100;
	}
	private static CellStyle createCellStyle(Workbook workbook) {
		CellStyle cs = workbook.createCellStyle();

		// Set borders
//		cs.setBorderTop(BorderStyle.MEDIUM);
		cs.setBorderBottom(BorderStyle.THIN);
		cs.setBorderLeft(BorderStyle.THIN);
		cs.setBorderRight(BorderStyle.THIN);

		Font font = workbook.createFont();
		font.setFontName("Calibri");
		font.setFontHeightInPoints((short)10);
		cs.setFont(font);

		cs.setAlignment(HorizontalAlignment.CENTER);

		return cs;
	}
	private static Cell createCell(Row row, int colNum, CellStyle cellStyle) {
		Cell cell = row.createCell(colNum);
		cell.setCellStyle(cellStyle);  cell.setCellType(CellType.NUMERIC);
		return createCell(row, colNum, cellStyle, CellType.STRING);
	}
	private static Cell createCell(Row row, int colNum, CellStyle cellStyle, CellType cellType) {
		Cell cell = row.createCell(colNum);
		cell.setCellStyle(cellStyle);
		cell.setCellType(cellType);
		return cell;
	}

}
