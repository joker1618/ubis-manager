package xxx.joker.apps.ubismanager.logger;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by f.barbano on 21/08/2017.
 */
class SimpleLogImpl implements SimpleLog {

	private Logger logger;

	SimpleLogImpl(Logger logger) {
		this.logger = logger;
	}

	@Override
	public synchronized void error(Throwable t) {
		error(t, null);
	}
//	@Override
//	public synchronized void error(String mex, Object... params) {
//		error(null, mex, params);
//	}
	@Override
	public synchronized void error(Throwable t, String mex, Object... params) {
		logThrowable(t, Level.SEVERE, mex, params);
	}
//	@Override
//	public synchronized void warn(Throwable t) {
//		warn(t, null);
//	}
	@Override
	public synchronized void warn(String mex, Object... params) {
		logger.log(Level.WARNING, String.format(mex, params));
	}
//	@Override
//	public synchronized void warn(Throwable t, String mex, Object... params) {
//		logThrowable(t, LogLevel.WARNING, mex, params);
//	}
	@Override
	public synchronized void info(String mex, Object... params) {
		logger.log(Level.INFO, String.format(mex, params));
	}

	@Override
	public synchronized void config(String mex, Object... params) {
		logger.log(Level.CONFIG, String.format(mex, params));
	}
	@Override

	public synchronized void fine(String mex, Object... params) {
		logger.log(Level.FINE, String.format(mex, params));
	}

	@Override
	public synchronized void fine(List<String> lines) {
		lines.forEach(this::fine);
	}


	private void logThrowable(Throwable t, Level level, String mex, Object... params) {
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(mex)) {
			sb.append(String.format(mex, params)).append("\n");
		}
		sb.append(toStringStackTrace(t));
		logger.log(level, sb.toString());
	}
	private String toStringStackTrace(Throwable t) {
		StringBuilder sb = new StringBuilder();
		Throwable selected = t;
		while (selected != null) {
			sb.append(selected).append("\n");
			Arrays.stream(selected.getStackTrace()).forEach(el -> sb.append("\tat ").append(el).append("\n"));
			selected = selected.getCause();
		}
		return sb.toString().trim();
	}
}
