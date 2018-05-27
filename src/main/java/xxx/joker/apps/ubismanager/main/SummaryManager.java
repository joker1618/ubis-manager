package xxx.joker.apps.ubismanager.main;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import xxx.joker.apps.ubismanager.common.TsConfigs.ExtType;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import xxx.joker.apps.ubismanager.common.TsConfigs;
import xxx.joker.apps.ubismanager.common.TsStuffUtils;
import xxx.joker.apps.ubismanager.exception.TsException;
import xxx.joker.apps.ubismanager.logger.LogService;
import xxx.joker.apps.ubismanager.logger.SimpleLog;
import xxx.joker.apps.ubismanager.model.Resource;
import xxx.joker.apps.ubismanager.model.Timesheet;
import xxx.joker.apps.ubismanager.service.ResourceService;
import xxx.joker.apps.ubismanager.service.UbisHoursPolicyService;
import xxx.joker.libs.javalibs.excel.JkExcelSheet;
import xxx.joker.libs.javalibs.excel.JkExcelUtil;
import xxx.joker.libs.javalibs.utils.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static xxx.joker.libs.javalibs.utils.JkStrings.strf;

/**
 * Created by f.barbano on 07/04/2018.
 */
public class SummaryManager {

	private static final SimpleLog logger = LogService.getLogger(SummaryManager.class);

	private static final String USER_ID = "User ID";
	private static final String HOURS = "ORE";
	private static final String HEADER_CSV_PREFIX = "User_ID;Resource";
	private static final String SUMMARY_XLS_SHEET_SIMPLE = "simple";
	private static final String SUMMARY_XLS_SHEET_POLICY = "policy";

	private static final TsConfigs config = TsConfigs.getInstance();
	private static final ResourceService resourceService = ResourceService.getInstance();


	public static void createSummary() throws IOException, InvalidFormatException {
		// Get received ts path list
		List<Path> pathList = getReceivedTsPathList();
		logger.info("Excel files found: %d", pathList.size());

		// Parse timesheets
		List<Timesheet> timesheets = parseTimesheets(pathList);
		logger.info("Timesheets parsed");

		// Create summary csv
		Path summaryCsv = createSummaryCsv(timesheets);
		logger.info("Created summary csv at '%s'", summaryCsv);

		// Create summary xlsx
		Path summaryXlsx = createSummaryXlsx(timesheets);
		logger.info("Created summary excel at '%s'", summaryXlsx);

		// Create renaming filename
		RenamingTsManager.createRenamingFile(timesheets);
	}

	public static List<Timesheet> parseSummaryCsv() throws IOException {
		List<Timesheet> toRet = new ArrayList<>();
		List<String[]> lines = JkCsv.readLineFields(config.getOutSummaryFilename(ExtType.CSV), null);
		for(String[] line : lines) {
			Resource resource = resourceService.getByUserId(line[0]);
			if(resource != null) {
				Map<LocalDate, Integer> hmap = new TreeMap<>();
				LocalDate ld = config.getDateStartMonth();
				LocalDate endDate = config.getDateEndMonth();
				int colNum = 2;
				while(!ld.isAfter(endDate)) {
					Integer hours = JkConverter.stringToInteger(line[colNum]);
					if(hours != null) {
						hmap.put(ld, hours);
					}
					ld = ld.plusDays(1);
					colNum++;
				}
				Timesheet ts = new Timesheet();
				ts.setResource(resource);
				ts.setHourMap(hmap);
				toRet.add(ts);
			}
		}
		return toRet;
	}

	private static List<Path> getReceivedTsPathList() throws IOException {
		return Files.find(
			config.getReceivedTsFolder(),
			1,
			(p,a) -> Files.isRegularFile(p) && p.toString().endsWith(".xlsx")
		).collect(Collectors.toList());
	}

	private static List<Timesheet> parseTimesheets(List<Path> pathList) {
		List<Timesheet> timesheets = new ArrayList<>();
		for(Path p : pathList) {
			try {
				Timesheet timesheet = parseTimesheetFromReceivedXlsx(p);
				if (timesheet == null) {
					throw new TsException("Error parsing file '%s'", p);
				}

				List<Timesheet> dups = JkStreams.filter(timesheets, t -> t.getResource().equals(timesheet.getResource()));
				if (!dups.isEmpty()) {
					throw new TsException(strf("Duplicated timesheet for '%s' (files: %s and %s)",
						timesheet.getResource().getFullName(),
						dups.get(0).getFilePath().getFileName(),
						p.getFileName()
					));
				}

				timesheets.add(timesheet);
				logger.info("Parsed timesheet - %s: '%s'", timesheet.getResource().getFullName(), p.getFileName());
			} catch(Exception ex) {
				throw new TsException(ex, "Error parsing timesheet from %s", p);
			}
		}

		timesheets.sort(Comparator.comparing(t -> t.getResource().getLastName()));
		return timesheets;
	}

	private static Timesheet parseTimesheetFromReceivedXlsx(Path path) throws IOException, InvalidFormatException {
		List<JkExcelSheet> excelSheets = JkExcelUtil.parseExcelFile(path);
		JkExcelSheet es = excelSheets.get(0);
		Pair<Integer, String> header = getHeader(es);
		Integer firstDayCol = getMatchColumn(header.getValue(), "1");
		Timesheet timesheet = null;

		for(int i = header.getKey() + 1; i < es.getLines().size(); i++) {
			List<String> line = es.getLines().get(i);
			Resource res = resourceService.getByUserId(line.get(0));
			if(res != null) {
				Map<LocalDate, Integer> hmap = getHours(line, firstDayCol);
				if (!hmap.isEmpty()) {
					if(timesheet == null) {
						timesheet = new Timesheet();
						timesheet.setFilePath(path);
						timesheet.setResource(res);
						timesheet.setHourMap(hmap);
					} else {
						throw new TsException("Found multiple rows filled for timesheet '%s': expected 1", path);
					}
				}
			}
		}

		return timesheet;
	}

	private static Pair<Integer, String> getHeader(JkExcelSheet es) {
		List<List<String>> lines = es.getLines();
		for(int rowNum = 0; rowNum < lines.size(); rowNum++) {
			List<String> row = lines.get(rowNum);
			if(USER_ID.equals(row.get(0))) {
				int pos;
				for(pos = row.size() - 1; pos >= 0 && StringUtils.isBlank(row.get(pos)); pos--);
				if(pos >= 0) {
					List<String> headerFields = row.subList(0, pos + 1);
					String header = JkStreams.join(headerFields, JkCsv.CSV_SEP, String::trim);
					return Pair.of(rowNum, header);
				}
			}
		}
		return null;
	}

	private static Integer getMatchColumn(String csvLine, String value) {
		String[] rowFields = JkStrings.splitAllFields(csvLine, JkCsv.CSV_SEP);
		for(int i = 0; i < rowFields.length; i++) {
			if(value.equals(rowFields[i])) {
				return i;
			}
		}
		return null;
	}

	private static Map<LocalDate, Integer> getHours(List<String> row, int firstDayCol) {
		Map<LocalDate, Integer> toRet = new TreeMap<>();

		LocalDate day = config.getDateStartMonth();
		LocalDate end = config.getDateEndMonth();
		int col = firstDayCol;
		while(!day.isAfter(end)) {
			Integer h = JkConverter.stringToInteger(row.get(col));
			if(h != null) {
				toRet.put(day, h);
			}
			col++;
			day = day.plusDays(1);
		}

		return toRet;
	}

	private static Path createSummaryCsv(List<Timesheet> timesheets) throws IOException {
		String header = createHeaderCsv();

		List<String> lines = new ArrayList<>();
		lines.add(header);
		for(Timesheet ts : timesheets) {
			lines.add(createSummaryTsLine(ts));
		}

		Path outPath = config.getOutSummaryFilename(ExtType.CSV);
		TsStuffUtils.versionize(outPath);
		JkFiles.writeFile(outPath, lines, false);

		return outPath;
	}
	private static String createHeaderCsv() {
		String header = HEADER_CSV_PREFIX;
		LocalDate ld = config.getDateStartMonth();
		LocalDate end = config.getDateEndMonth();
		while(!ld.isAfter(end)) {
			header += ";" + ld.getDayOfMonth();
			ld = ld.plusDays(1);
		}
		return header;
	}
	private static String createSummaryTsLine(Timesheet ts) {
		String line = strf("%s;%s", ts.getResource().getUserID(), ts.getResource().getFullName());
		LocalDate ld = config.getDateStartMonth();
		LocalDate end = config.getDateEndMonth();
		while(!ld.isAfter(end)) {
			line += ";";
			Integer h = ts.getHourMap().get(ld);
			if(h != null)	{
				line += h;
			}
			ld = ld.plusDays(1);
		}
		return line;
	}

	private static Path createSummaryXlsx(List<Timesheet> timesheets) throws IOException, InvalidFormatException {
		Path templatePath = config.getTemplateSummaryPath();

		// Get header
		List<JkExcelSheet> excelSheets = JkExcelUtil.parseExcelFile(templatePath);
		String header = getHeader(excelSheets.get(0)).getValue();

		try (Workbook wbTemplate = openWorkbook(templatePath)) {
			// Simple hours
			Sheet sheetSimple = wbTemplate.getSheet(SUMMARY_XLS_SHEET_SIMPLE);
			fillSummarySheet(sheetSimple, header, timesheets, false);

			// Hours policy applied
			Sheet sheetPolicy = wbTemplate.getSheet(SUMMARY_XLS_SHEET_POLICY);
			fillSummarySheet(sheetPolicy, header, timesheets, true);

			// Persist summary excel
			Path outPath = config.getOutSummaryFilename(ExtType.XLS);
			TsStuffUtils.versionize(outPath);
			JkFiles.copyFile(templatePath, outPath, false);
			try (FileOutputStream fos = new FileOutputStream(outPath.toFile())) {
				wbTemplate.write(fos);
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
	private static void fillSummarySheet(Sheet sheetTemplate, String header, List<Timesheet> timesheets, boolean applyPolicy) {
		JkExcelSheet esTemplate = JkExcelUtil.parseExcelSheet(sheetTemplate);

		Integer firstDayCol = getMatchColumn(header, String.valueOf("1"));
		if (firstDayCol == null) {
			throw new TsException("Header cell valued '1' not found in summary xls template");
		}

		Integer totHoursCol = getMatchColumn(header, HOURS);
		if (totHoursCol == null) {
			throw new TsException("Header cell valued '%s' not found in summary xls template", HOURS);
		}

		for (Timesheet ts : timesheets) {
			Integer resRow = getResourceRow(ts.getResource().getUserID(), esTemplate);
			fillResourceHours(sheetTemplate, ts, applyPolicy, resRow, firstDayCol, totHoursCol);
		}
	}
	private static Integer getResourceRow(String userId, JkExcelSheet es) {
		List<List<String>> lines = es.getLines();
		for(int i = 0; i < lines.size(); i++) {
			if(userId.equals(lines.get(i).get(0))) {
				return i;
			}
		}
		return null;
	}
	private static void fillResourceHours(Sheet sheet, Timesheet ts, boolean applyPolicy, int resourceRow, int firstDayCol, int totHoursCol) {
		int idx = firstDayCol;
		LocalDate ld = config.getDateStartMonth();
		LocalDate end = config.getDateEndMonth();
		Row row = sheet.getRow(resourceRow);
		UbisHoursPolicyService policyService = UbisHoursPolicyService.getInstance();
		while(!ld.isAfter(end)) {
			Integer h = ts.getHourMap().get(ld);
			if(h != null) {
				double mult = applyPolicy ? policyService.getMultiplier(ld) : 1d;
				row.getCell(idx).setCellValue(h * mult);
			}
			idx++;
			ld = ld.plusDays(1);
		}

		String strFormula = createFormulaSumHours(resourceRow, firstDayCol);
		sheet.getRow(resourceRow).getCell(totHoursCol).setCellFormula(strFormula);
		sheet.getRow(resourceRow).getCell(totHoursCol+1).setCellFormula(strf("%s%d/8", getColLetter(totHoursCol), resourceRow+1));
	}
	private static String createFormulaSumHours(int row, int startCol) {
		int endDay = config.getDateEndMonth().getDayOfMonth();
		int endCol = startCol + endDay - 1;

		StringBuilder sb = new StringBuilder();
		sb.append("SUM(");
		sb.append(strf("%s%d", getColLetter(startCol), row+1));
		sb.append(":");
		sb.append(strf("%s%d", getColLetter(endCol), row+1));
		sb.append(")");

		return sb.toString();
	}
	private static String getColLetter(int colIdx) {
		int range = 90 - 65 + 1;
		int block = colIdx / range;
		if(block == 0) {
			return String.valueOf((char)(colIdx + 65));
		}

		String letter = "";
		letter += (char) (65 + block - 1);
		letter += (char) (65 + (colIdx % range));

		return letter;
	}


}
