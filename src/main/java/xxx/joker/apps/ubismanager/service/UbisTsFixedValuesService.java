package xxx.joker.apps.ubismanager.service;

import xxx.joker.apps.ubismanager.common.TsConfigs;
import xxx.joker.apps.ubismanager.logger.LogService;
import xxx.joker.apps.ubismanager.logger.SimpleLog;
import xxx.joker.libs.javalibs.utils.JkCsv;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by f.barbano on 07/04/2018.
 */
public class UbisTsFixedValuesService {

	private static final SimpleLog logger = LogService.getLogger(UbisTsFixedValuesService.class);

	private static UbisTsFixedValuesService instance;

	private static final String KEY_VENDOR = "ubis.ts.vendor";
	private static final String KEY_DAY_FORMAT = "ubis.ts.day.format";
	private static final String KEY_PROJECT_ASSET = "ubis.ts.project.asset";
	private static final String KEY_PROJECT_ASSET_ID = "ubis.ts.project.assetID";
	private static final String KEY_SHC_ID = "ubis.ts.shCID";

	private Map<String, String> valueMap;


	private UbisTsFixedValuesService() {
		init();
	}

	public static UbisTsFixedValuesService getInstance() {
		if(instance == null) {
			instance = new UbisTsFixedValuesService();
		}
		return instance;
	}

	private void init() {
		try {
			Path inPath = TsConfigs.getInstance().getUbisTsFixedValuesPath();
			List<String[]> dataLines = JkCsv.readLineFields(inPath, null, "=");
			valueMap = new HashMap<>();
			dataLines.forEach(l -> valueMap.put(l[0].trim(), l[1].trim()));
			logger.info("Ubis ts: %d fixed values loaded", valueMap.size());

		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public String getVendor() {
		return valueMap.get(KEY_VENDOR);
	}
	public DateTimeFormatter getDayFormat() {
		return DateTimeFormatter.ofPattern(valueMap.get(KEY_DAY_FORMAT));
	}
	public String getProjectAsset() {
		return valueMap.get(KEY_PROJECT_ASSET);
	}
	public String getProjectAssetID() {
		return valueMap.get(KEY_PROJECT_ASSET_ID);
	}
	public String getShCID() {
		return valueMap.get(KEY_SHC_ID);
	}

}
