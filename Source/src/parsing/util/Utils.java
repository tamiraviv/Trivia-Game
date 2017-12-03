package parsing.util;

import parsing.generic.Callback;
import parsing.ValueType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Utils {

    private static DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
    private static DateTimeFormatter dateformatter2 = DateTimeFormatter.ofPattern("yyyy-dd-MM", Locale.ENGLISH);

    public static void reduceEntitiesByAttributeFromCollectionWithMatcher(String filePath, List<Callback> callbacks) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
        String line = reader.readLine();
        while (line != null) {
            String[] split = line.split("\t");
            Row row = new Row(split);
            for (Callback callback : callbacks) {
                if (callback.map(row)) {
                    callback.reduce(row);
                }
            }
            line = reader.readLine();
        }
        reader.close();
    }

    public static void reduceEntitiesByAttributeFromCollectionWithMatcher(String filePath, Callback callback) throws IOException {
        reduceEntitiesByAttributeFromCollectionWithMatcher(filePath, Collections.singletonList(callback));
    }

    public static float parseFloat(String string) {
        return Float.parseFloat(string.substring(1, string.indexOf("^") - 1));
    }

    public static long parseLong(String string) {
        return Long.parseLong(string.substring(1, string.indexOf("^") - 1));
    }

    public static String parseName(String string) {
        int pos = string.lastIndexOf("@");
        if (pos == -1 || pos == 1) {
            return string;
        }
        return string.substring(1, pos - 1);
    }

    public static String parseString(String string) {
        return string.substring(1, string.length()-1);
    }

    public static LocalDate parseDate(String string) {
        String dateString = string.substring(1, string.indexOf("^") - 1);
        int sep1 = dateString.indexOf("#");
        int sep2 = dateString.indexOf("-");
        if (sep1 != -1) {
            if (sep1 != 5 && sep1 != 8 || sep2 != 4) {
                return null;
            }
            dateString = dateString.substring(0, sep1 - 1);
            if (dateString.length() == 4) {
                dateString += "-01-01";
            }
            if (dateString.length() == 7) {
                dateString += "-01";
            }
        }
        try {
            return LocalDate.parse(dateString, dateformatter);
        } catch (DateTimeException e1) {
            try {
                return LocalDate.parse(dateString, dateformatter2);
            } catch (DateTimeException e2) {
                return null;
            }
        }
    }

    public static Object parseValue(String string, ValueType type) {
        switch (type) {
            case FLOAT:
                return parseFloat(string);
            case LONG:
                return parseLong(string);
            case NAME:
                return parseName(string);
            case STRING:
                return parseString(string);
            case DATE:
                return parseDate(string);
        }
        throw new AssertionError();
    }

    public static Date localDateToDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        } else {
            return Date.valueOf(localDate);
        }
    }
}
