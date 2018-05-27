package xxx.joker.apps.ubismanager.common;

import xxx.joker.libs.javalibs.utils.JkFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static xxx.joker.libs.javalibs.utils.JkFiles.moveFile;
import static xxx.joker.libs.javalibs.utils.JkStrings.strf;

/**
 * Created by f.barbano on 27/05/2018.
 */
public class TsStuffUtils {

	public static Path versionize(Path targetPath) throws IOException {
		if(!Files.exists(targetPath)) {
			return null;
		}

		targetPath = targetPath.toAbsolutePath().normalize();
		Path folder = targetPath.getParent();
		String fileName = targetPath.getFileName().toString();
		Path newPath = null;
		int v = 0;
		while(newPath == null || Files.exists(newPath)) {
			v++;
			newPath = folder.resolve(strf("%s.v%02d", fileName, v));
		}

		JkFiles.moveFile(targetPath, newPath, false);
		return newPath;
	}
	
}
