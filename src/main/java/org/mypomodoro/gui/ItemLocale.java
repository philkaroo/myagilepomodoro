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
package org.mypomodoro.gui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mypomodoro.Main;

/**
 * Locale
 *
 */
public class ItemLocale {

    private final Locale locale;
    private final String localeText;

    public ItemLocale(Locale locale, String localeText) {
        this.locale = locale;
        this.localeText = localeText;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLocaleText() {
        return localeText;
    }

    @Override
    public String toString() {
        return localeText;
    }

    /**
     * Browse directory labels for properties files and extract locales
     *
     * @return
     */
    public static List<ItemLocale> getLocalesFromPropertiesTitlefiles() {
        List<ItemLocale> vLocales = new ArrayList<ItemLocale>();
        try {
            String propertiesFiles[] = getResourceListing(Main.class,
                    "org/mypomodoro/labels/");
            if (propertiesFiles.length > 0) {
                String filePrefix = "mypomodoro_";
                int filePrefixLength = filePrefix.length();
                String fileExtension = ".properties";
                int fileExtensionLength = fileExtension.length();
                for (String propertiesFile : propertiesFiles) {
                    String regularExpression = filePrefix
                            + "[a-z]{2}_[A-Z]{2}_[a-zA-Z]+" + fileExtension; // with
                    // variant
                    Pattern pat = Pattern.compile(regularExpression);
                    Matcher mat = pat.matcher(propertiesFile);
                    if (mat.find()) {
                        Locale l = new Locale(propertiesFile.substring(0 + filePrefixLength, 2 + filePrefixLength), propertiesFile.substring(3 + filePrefixLength, 5 + filePrefixLength), propertiesFile.substring(6 + filePrefixLength, propertiesFile.length() - fileExtensionLength));
                        vLocales.add(new ItemLocale(l, l.getDisplayLanguage()
                                + " (" + l.getDisplayCountry() + ")" + " ("
                                + l.getVariant() + ")"));
                    } else {
                        regularExpression = filePrefix + "[a-z]{2}_[A-Z]{2}"
                                + fileExtension; // without variant
                        pat = Pattern.compile(regularExpression);
                        mat = pat.matcher(propertiesFile);
                        if (mat.find()) {
                            Locale l = new Locale(propertiesFile.substring(0 + filePrefixLength, 2 + filePrefixLength), propertiesFile.substring(3 + filePrefixLength, 5 + filePrefixLength));
                            vLocales.add(new ItemLocale(l, l.getDisplayLanguage()
                                    + " ("
                                    + l.getDisplayCountry() + ")"));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Main.logger.error("", ex);
        } catch (URISyntaxException ex) {
            Main.logger.error("", ex);
        } finally {
            if (vLocales.isEmpty()) {
                vLocales.add(new ItemLocale(Main.preferences.getLocale(), Main.preferences.getLocale().getDisplayLanguage()
                        + " ("
                        + Main.preferences.getLocale().getDisplayCountry() + ")"));
            }
        }
        return vLocales;
    }

    /**
     * List directory contents for a resource folder. Not recursive. This is
     * basically a brute-force implementation. Works for regular files and also
     * JARs.
     *
     * @author Greg Briggs
     * @param clazz Any java class that lives in the same place as the resources
     * you want.
     * @param path Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException
     * @throws IOException
     */
    private static String[] getResourceListing(Class<?> clazz, String path)
            throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
        }
        if (dirURL == null) {
            /*
             * In case of a jar file, we can't actually find a directory. Have
             * to assume the same jar as clazz.
             */
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }
        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5,
                    dirURL.getPath().indexOf("!")); // strip out only the JAR
            // file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries
            // in jar
            Set<String> result = new HashSet<String>(); // avoid duplicates in
            // case it is a
            // subdirectory
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { // filter according to the path
                    String entry = name.substring(path.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        // if it is a subdirectory, we just return the directory
                        // name
                        entry = entry.substring(0, checkSubdir);
                    }
                    result.add(entry);
                }
            }
            return result.toArray(new String[result.size()]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL "
                + dirURL);
    }
}
