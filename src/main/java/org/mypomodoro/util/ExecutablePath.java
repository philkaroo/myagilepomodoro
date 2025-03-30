/*
 * Copyright (C)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mypomodoro.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.CodeSource;
import org.mypomodoro.Main;

/**
 * Utility methods to compute current Executable path and directory
 *
 */
public class ExecutablePath {

    // Full executable path
    // eg \...\...\myAgilePomodoro.jar or .exe
    public static String getExecutablePath() throws UnsupportedEncodingException {
        String executablePath = "";
        try {
            CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
            executablePath = codeSource.getLocation().toURI().getPath();
            executablePath = URLDecode(executablePath);
        } catch (URISyntaxException ex) {
            // nothing to do here
        }
        return executablePath;
    }

    // Full executable directory path with NO file separator at the end
    // eg \...\...
    public static String getExecutableDirectory() throws UnsupportedEncodingException {
        String executableDirectory = "";
        try {
            CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
            File executableFile = new File(codeSource.getLocation().toURI().getPath());
            executableDirectory = executableFile.getParentFile().getPath();
            executableDirectory = URLDecode(executableDirectory);
        } catch (URISyntaxException ex) {
            // nothing to do here
        }
        return executableDirectory;
    }

    // Full jar directory path with file separator at the end
    // eg \...\...\
    public static String getExecutableDirectoryWithSeparator() throws UnsupportedEncodingException {
        String executableDirectoryWithSeparator = getExecutableDirectory();
        if (!executableDirectoryWithSeparator.isEmpty()) {
            executableDirectoryWithSeparator += File.separator;
        }
        return executableDirectoryWithSeparator;
    }

    // Spaces and special charaters decoding
    // Note: URLDecoder might not work properly : http://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
    public static String URLDecode(String aString) throws UnsupportedEncodingException {
        return URLDecoder.decode(aString, "UTF-8");
    }
}
