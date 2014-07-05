package wedo.oa.utils;

import java.io.File;

public class FileUtils {
	private static long MAX_CACHE_SIZE = 2*1024* 1024;// byte

	/**
	 * @param dir
	 * @return ture 在范围内 false 在范围外
	 */
	public static boolean checkDirSize(File dir) {
		long size = 0;
		if (dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				if (file.isFile()) {
					size += file.length();
				}
			}
			if (size <= MAX_CACHE_SIZE)
				return true;
		}
		return false;
	}
}
