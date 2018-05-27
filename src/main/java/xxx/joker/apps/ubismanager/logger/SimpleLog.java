package xxx.joker.apps.ubismanager.logger;

import java.util.List;

/**
 * Created by f.barbano on 21/08/2017.
 */
public interface SimpleLog {

	void error(Throwable t);
//	void error(String mex, Object... params);
	void error(Throwable t, String mex, Object... params);
//
//	void warn(Throwable t);
//	void warn(Throwable t, String mex, Object... params);
	void warn(String mex, Object... params);

	void info(String mex, Object... params);
	void config(String mex, Object... params);
	void fine(String mex, Object... params);
	void fine(List<String> lines);

}
