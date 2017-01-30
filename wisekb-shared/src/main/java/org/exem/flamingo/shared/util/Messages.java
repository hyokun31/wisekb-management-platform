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
package org.exem.flamingo.shared.util;

import java.text.MessageFormat;
import java.util.*;

public class Messages {

    private static final String BUNDLE_NAME = "locale.messages";

    private static final ResourceBundle RESOURCE_BUNDLE = UTF8ResourceBundle.getBundle(BUNDLE_NAME);

    private static Map<String, ResourceBundle> bundleCache = new HashMap();

    private static Map<String, String> resourceCache = new HashMap();

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String getResources(Locale locale) {
        if (!resourceCache.containsKey(locale.toString())) {
            ResourceBundle bundle = UTF8ResourceBundle.getBundle(BUNDLE_NAME, locale);
            resourceCache.put(locale.toString(), getResources(bundle));
            bundleCache.put(locale.toString(), bundle);
        }
        return resourceCache.get(locale.toString());
    }

    public static Map toMap(Locale locale) {
        Map map = new HashMap();
        if (!resourceCache.containsKey(locale.toString())) {
            ResourceBundle bundle = UTF8ResourceBundle.getBundle(BUNDLE_NAME, locale);
            resourceCache.put(locale.toString(), getResources(bundle));
            bundleCache.put(locale.toString(), bundle);
        }

        ResourceBundle rb = bundleCache.get(locale.toString());
        Set<String> keys = rb.keySet();
        for (String key : keys) {
            map.put(key, rb.getString(key));
        }
        return map;
    }

    public static String getResources() {
        return getResources(RESOURCE_BUNDLE);
    }

    public static String getResources(ResourceBundle resourceBundle) {
        StringBuilder builder = new StringBuilder();
        Set<String> keys = resourceBundle.keySet();
        for (String key : keys) {
            String value = resourceBundle.getString(key);
            builder.append(key).append("=").append(value).append("\n");
        }
        return builder.toString();
    }

    public static String getString(String key, Object... params) {
        try {
            return MessageFormat.format(RESOURCE_BUNDLE.getString(key), params);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}