/**
 * Copyright (C) 2011 Flamingo Project (http://www.cloudine.io).
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wisekb.shared.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Zips {

	public static void zip(final File dir, final File zipName) throws IOException, IllegalArgumentException {
		final String[] entries = dir.list();
		final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipName));

		String prefix = dir.getAbsolutePath();
		if (!prefix.endsWith(File.separator)) {
			prefix += File.separator;
		}

		for (final String entry : entries) {
			File f = new File(dir, entry);
			zip(out, f, prefix, zipName.getName().substring(0, zipName.getName().length() - 4));
		}
		IOUtils.closeQuietly(out);
	}

	private static void zip(final ZipOutputStream out, final File f, final String prefix, final String root) throws IOException {
		if (f.isDirectory()) {
			final File[] files = f.listFiles();
			if (files != null) {
				for (final File child : files) {
					zip(out, child, prefix, root);
				}
			}
		} else {
			final FileInputStream in = new FileInputStream(f);
			final ZipEntry entry = new ZipEntry(root + "/" + f.getPath().replace(prefix, ""));
			out.putNextEntry(entry);
			IOUtils.copy(in, out);
			IOUtils.closeQuietly(in);
		}
	}

	private Zips() {
		// no-op
	}
}