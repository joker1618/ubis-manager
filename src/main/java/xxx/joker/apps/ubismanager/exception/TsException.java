package xxx.joker.apps.ubismanager.exception;

import static xxx.joker.libs.javalibs.utils.JkStrings.strf;

/**
 * Created by f.barbano on 30/03/2018.
 */
public class TsException extends RuntimeException {

	public TsException(Throwable t) {
		super(t);
	}

	public TsException(Throwable cause, String format, Object... params) {
		super(strf(format, params), cause);
	}

	public TsException(String format, Object... params) {
		super(strf(format, params));
	}
}
