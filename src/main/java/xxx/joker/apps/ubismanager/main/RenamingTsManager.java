package xxx.joker.apps.ubismanager.main;

import xxx.joker.apps.ubismanager.common.TsConfigs;
import xxx.joker.apps.ubismanager.logger.LogService;
import xxx.joker.apps.ubismanager.logger.SimpleLog;
import xxx.joker.apps.ubismanager.model.Timesheet;
import xxx.joker.libs.javalibs.utils.JkCsv;
import xxx.joker.libs.javalibs.utils.JkFiles;
import xxx.joker.libs.javalibs.utils.JkStreams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static xxx.joker.libs.javalibs.utils.JkStrings.strf;

/**
 * Created by f.barbano on 08/04/2018.
 */
public class RenamingTsManager {

	private static final SimpleLog logger = LogService.getLogger(RenamingTsManager.class);

	private static final TsConfigs config = TsConfigs.getInstance();

	public static void createRenamingFile(List<Timesheet> timesheets) throws IOException {
		List<String> lines = new ArrayList<>();
		for (Timesheet ts : timesheets) {
			String prefixFilename = strf("%s_dgsig_ts_%s_%s",
				config.getYearMonthPrefix(),
				ts.getResource().getFullName().replace(" ", "_"),
				ts.getResource().getUserID()
			);
			if(!ts.getFilePath().getFileName().toString().startsWith(prefixFilename)) {
				Path fp = ts.getFilePath().toAbsolutePath().normalize();
				lines.add(strf("%s;%s",
					fp,
					fp.getParent().resolve(strf("%s.xlsx", prefixFilename))
				));
			}
		}

		Path outPath = config.getReceivedTsRenamingPath();
		if(lines.isEmpty()) {
			Files.deleteIfExists(outPath);
		} else {
			JkFiles.writeFile(outPath, lines, true);
			logger.info("Created renaming file at '%s'", outPath);
		}
	}

	public static void renameReceivedTs() throws IOException {
		Path fp = config.getReceivedTsRenamingPath();
		if(!Files.exists(fp)) {
			logger.info("Renaming file not found");
			return;
		}
		List<String[]> lines = JkCsv.readLineFields(fp, null);

		List<Path> notFound = JkStreams.mapAndFilter(lines, l -> Paths.get(l[0]), p -> !Files.exists(p));
		if(!notFound.isEmpty()) {
			Files.deleteIfExists(fp);
			logger.info("Unable to rename received ts: some files does not exists anymore");
			return;
		}

		for(String[] line : lines) {
			Path source = Paths.get(line[0]);
			Path target = Paths.get(line[1]);
			JkFiles.moveFileSafely(source, target);
		}

		Files.deleteIfExists(fp);
		logger.info("Received ts renamed correctly");
	}


}
