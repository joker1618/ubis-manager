package xxx.joker.apps.ubismanager.model;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

/**
 * Created by f.barbano on 30/03/2018.
 */
public class Timesheet {

	private Path filePath;
	private Resource resource;
	private Map<LocalDate, Integer> hourMap;


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Timesheet)) return false;

		Timesheet timesheet = (Timesheet) o;

		return filePath != null ? filePath.equals(timesheet.filePath) : timesheet.filePath == null;
	}

	@Override
	public int hashCode() {
		return filePath != null ? filePath.hashCode() : 0;
	}

	public Path getFilePath() {
		return filePath;
	}

	public void setFilePath(Path filePath) {
		this.filePath = filePath;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public Map<LocalDate, Integer> getHourMap() {
		return hourMap;
	}

	public void setHourMap(Map<LocalDate, Integer> hourMap) {
		this.hourMap = hourMap;
	}
}
