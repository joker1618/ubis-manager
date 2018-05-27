package xxx.joker.apps.ubismanager.common;

import xxx.joker.libs.javalibs.config.JkAbstractConfigs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import static xxx.joker.libs.javalibs.utils.JkStrings.strf;

/**
 * Created by f.barbano on 07/04/2018.
 */
public class TsConfigs extends JkAbstractConfigs {

	private static final TsConfigs instance = new TsConfigs();

	public enum ExtType {
		CSV("csv"),
		XLS("xlsx"),
		;
	    String ext;
		ExtType(String ext) {
			this.ext = ext;
		}
	}

	private TsConfigs() {}

	public static TsConfigs getInstance() {
		return instance;
	}

	public static void loadProperties(String configPath) throws IOException {
		instance.loadConfigFile(configPath);
	}

	public Path getBaseFolder() {
		return getPath("baseFolder");
	}

	public Path getResourcesPath() {
		return getPath("resources.path");
	}
	public Path getUbisHoursPolicyPath() {
		return getPath("ubis.hours.policy.path");
	}
	public Path getUbisTsFixedValuesPath() {
		return getPath("ubis.ts.fixed.values.path");
	}
	public Path getTemplateSummaryPath() {
		return getPath("template.summary.path");
	}
	public Path getTemplateUbisTsPath(ExtType extType) {
		return Paths.get(strf("%s.%s", getString("template.ubis.ts.path"), extType.ext));
	}

	public Path getReceivedTsFolder() {
		return getPath("received.ts.folder");
	}
	public Path getReceivedTsRenamingPath() {
		return getPath("received.ts.renaming.path");
	}

	public Path getOutSummaryFilename(ExtType extType) {
		return Paths.get(strf("%s.%s", getString("out.summary.filename"), extType.ext));
	}
	public Path getOutUbisTsFilename(ExtType extType) {
		return Paths.get(strf("%s.%s", getString("out.ubis.ts.filename"), extType.ext));
	}

	public int getYear() {
		return getInt("year");
	}
	public int getMonth() {
		return getInt("month");
	}

	public String getYearMonthPrefix() {
		Integer month = getInt("month");
		Integer year = getInt("year");
		return strf("%4d%02d", year, month);
	}
	public LocalDate getDateStartMonth() {
		Integer month = getInt("month");
		Integer year = getInt("year");
		return LocalDate.of(year, month, 1);
	}
	public LocalDate getDateEndMonth() {
		return getDateStartMonth().plusMonths(1).minusDays(1);
	}

}
