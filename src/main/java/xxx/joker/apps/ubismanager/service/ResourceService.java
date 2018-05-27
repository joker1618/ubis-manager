package xxx.joker.apps.ubismanager.service;

import xxx.joker.apps.ubismanager.common.TsConfigs;
import xxx.joker.apps.ubismanager.logger.LogService;
import xxx.joker.apps.ubismanager.logger.SimpleLog;
import xxx.joker.apps.ubismanager.model.Resource;
import xxx.joker.libs.javalibs.utils.JkCsv;
import xxx.joker.libs.javalibs.utils.JkStreams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by f.barbano on 07/04/2018.
 */
public class ResourceService {

	private static final SimpleLog logger = LogService.getLogger(ResourceService.class);
	private static final String HEADER_RESOURCES = "USER_ID;LAST_NAME;FIRST_NAME;ACTIVITY;EMAIL";

	private static ResourceService instance;

	private List<Resource> resourceList;


	private ResourceService() {
		init();
	}

	public static ResourceService getInstance() {
		if(instance == null) {
			instance = new ResourceService();
		}
		return instance;
	}

	public Resource getByUserId(String userId) {
		List<Resource> filter = JkStreams.filter(resourceList, r -> userId.equals(r.getUserID()));
		return filter.isEmpty() ? null : filter.get(0);
	}

	private void init() {
		try {
			TsConfigs config = TsConfigs.getInstance();
			List<String[]> dataLines = JkCsv.readLineFields(config.getResourcesPath(), HEADER_RESOURCES);
			resourceList = new ArrayList<>();
			for (String[] line : dataLines) {
				Resource resource = new Resource();
				resource.setUserID(line[0]);
				resource.setLastName(line[1]);
				resource.setFirstName(line[2]);
				resource.setActivity(line[3]);
				resource.setEmail(line[4]);
				resourceList.add(resource);
			}
			logger.info("Resources found: %d", resourceList.size());

		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
	}


}
