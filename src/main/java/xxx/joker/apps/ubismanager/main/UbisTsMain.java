package xxx.joker.apps.ubismanager.main;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import xxx.joker.apps.ubismanager.common.TsConfigs;
import xxx.joker.apps.ubismanager.logger.LogService;
import xxx.joker.apps.ubismanager.logger.SimpleLog;

import java.io.IOException;
import java.util.Arrays;

import static xxx.joker.libs.javalibs.utils.JkConsole.display;

/**
 * Created by f.barbano on 07/04/2018.
 */
public class UbisTsMain {

	private static final SimpleLog logger = LogService.getLogger(UbisTsMain.class);

	enum TsAction {
		CREATE_SUMMARY(0),
		CREATE_UBIS_TS(1),
		RENAME_RECEIVED_TS(2),
		;
		int value;
		TsAction(int value) {
			this.value = value;
		}
	}

	public static void main(String[] args) throws IOException, InvalidFormatException {
		LogService.init(null);
		TsAction tsAction = checkInput(args);

		switch (tsAction) {
			case CREATE_SUMMARY:
				SummaryManager.createSummary();
				break;
			case CREATE_UBIS_TS:
				UbisTsManager.createUbisTs();
				break;
			case RENAME_RECEIVED_TS:
				RenamingTsManager.renameReceivedTs();
				break;
			default:
				break;
		}

	}

	private static TsAction checkInput(String[] args) throws IOException {
		if(args.length == 2) {
			TsConfigs.loadProperties(args[0]);
			TsAction tsAction = TsAction.values()[Integer.parseInt(args[1])];
			TsConfigs config = TsConfigs.getInstance();
			logger.info("Base folder: %s", config.getBaseFolder());
			logger.info("Month: %02d-%d", config.getMonth(), config.getYear());
			logger.info("Action: %d -> %s", tsAction.value, tsAction);
			return tsAction;
		}

		display("USAGE:  ubisTs  <CONFIG_PATH>  <ACTION_NUM>\n");
		Arrays.stream(TsAction.values()).forEach(a -> display("\t%d: %s", a.value, a.name()));
		System.exit(1);
		return null;
	}

}
