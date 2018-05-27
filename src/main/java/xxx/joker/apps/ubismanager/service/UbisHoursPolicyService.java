package xxx.joker.apps.ubismanager.service;


import xxx.joker.apps.ubismanager.common.TsConfigs;
import xxx.joker.apps.ubismanager.logger.LogService;
import xxx.joker.apps.ubismanager.logger.SimpleLog;
import xxx.joker.libs.javalibs.utils.JkCsv;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by f.barbano on 07/04/2018.
 */
public class UbisHoursPolicyService {

	private static final SimpleLog logger = LogService.getLogger(UbisHoursPolicyService.class);
	private static final String HEADER_HOURS_POLICY = "WEEKDAY;MULTIPLIER";

	private static UbisHoursPolicyService instance;

	private Map<Integer, Double> policyMap;

	private UbisHoursPolicyService() {
		init();
	}

	public static UbisHoursPolicyService getInstance() {
		if(instance == null) {
			instance = new UbisHoursPolicyService();
		}
		return instance;
	}

	private void init() {
		try {
			Path inPath = TsConfigs.getInstance().getUbisHoursPolicyPath();
			List<String[]> dataLines = JkCsv.readLineFields(inPath, HEADER_HOURS_POLICY);
			policyMap = new TreeMap<>();
			for (String[] line : dataLines) {
				policyMap.put(Integer.parseInt(line[0]), Double.parseDouble(line[1]));
			}
			logger.info("Ubis hours policy loaded");

		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
	}


	public Double getMultiplier(LocalDate day) {
		return policyMap.get(day.getDayOfWeek().getValue());
	}

}
