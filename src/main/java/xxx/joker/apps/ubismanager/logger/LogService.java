package xxx.joker.apps.ubismanager.logger;

import org.apache.commons.lang3.StringUtils;
import xxx.joker.libs.javalibs.datetime.JkTime;
import xxx.joker.libs.javalibs.utils.JkConverter;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.logging.*;

import static xxx.joker.libs.javalibs.utils.JkStrings.strf;

/**
 * Created by f.barbano on 30/03/2018.
 */
public class LogService {

	public static SimpleLog getLogger(Class<?> loggerName) {
		Logger logger = Logger.getLogger(loggerName.getName());
		return new SimpleLogImpl(logger);
	}

	public static void init(Path workingFolder) throws IOException {
		// Turn off global logger
		Logger global = Logger.getLogger("");
		global.setLevel(Level.OFF);
		Arrays.stream(global.getHandlers()).forEach(global::removeHandler);

		// Config root logger
		Logger rootLogger = Logger.getLogger("xxx.joker.apps.ubismanager");
		rootLogger.setUseParentHandlers(false);
		rootLogger.setLevel(Level.ALL);

		// Console handler
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.INFO);
		consoleHandler.setFormatter(getSimpleFormatter());
		rootLogger.addHandler(consoleHandler);


		// File log
//		Path logPath = workingFolder.resolve(strf("log.%s", dtime));
//		FileHandler fh = new FileHandler(logPath.toString(), 1024*1024, 2, true);
//		fh.setLevel(Level.ALL);
//		fh.setFormatter(getSimpleFormatter());
//		rootLogger.addHandler(fh);
	}

	private static Formatter getSimpleFormatter() {
		return new Formatter() {
			@Override
			public String format(LogRecord record) {
				String mex = super.formatMessage(record);
				return strf("%s%s", mex, StringUtils.LF);
//				Level level = record.getLevel();
//				return strf("%-7s - %s%s", level, mex, StringUtils.LF);
			}
		};
	}
}
