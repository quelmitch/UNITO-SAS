package catering.util;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for handling Date conversions in the catering application.
 */
public class DateUtils {
    private static final Logger LOGGER = LogManager.getLogger(DateUtils.class);

    /**
     * Extracts a java.sql.Date from a ResultSet column, interpreting the column as a Unix timestamp
     * or string representation of the timestamp.
     *
     * @param rs         ResultSet to read from
     * @param columnName Name of the column containing the date/timestamp
     * @return java.sql.Date instance or null if the value is NULL or invalid
     */
    public static Date getDateFromResultSet(ResultSet rs, String columnName) {
        try {
            long timestamp = rs.getLong(columnName);
            if (rs.wasNull()) {
                LOGGER.fine(() -> columnName + " is NULL in database");
                return null;
            }
            Date date = parseTimestampToDate(timestamp);
            if (date != null) {
                LOGGER.fine(() -> "Parsed " + columnName + " from timestamp " + timestamp + " to date: " + date);
                return date;
            }

            // Fallback: try to parse as string
            String dateStr = rs.getString(columnName);
            if (dateStr != null && !dateStr.isEmpty()) {
                return parseStringToDate(columnName, dateStr);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "SQLException while reading column " + columnName, ex);
        }
        return null;
    }

    /**
     * Safely converts a String representation of a date into java.sql.Date.
     * Supports SQL date format (yyyy-[m]m-[d]d) or a Unix timestamp string.
     *
     * @param dateStr date string to parse
     * @return java.sql.Date instance or null if input is null/empty or invalid format
     */
    public static Date safeValueOf(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return parseStringToDate("Input", dateStr);
    }


    // HELPERS

    /**
     * Attempts to parse a Unix timestamp into a java.sql.Date, returns null if timestamp invalid.
     */
    private static Date parseTimestampToDate(long timestamp) {
        if (timestamp > 0) {
            return new Date(timestamp);
        }
        return null;
    }

    /**
     * Attempts to parse a string either as SQL date format or Unix timestamp.
     * Logs warnings on failure.
     */
    private static Date parseStringToDate(String sourceName, String dateStr) {
        // Try SQL date format first
        try {
            return Date.valueOf(dateStr);
        } catch (Exception ignored) {}

        // Try parsing as Unix timestamp string
        try {
            long timestamp = Long.parseLong(dateStr);
            Date date = parseTimestampToDate(timestamp);
            if (date != null)
                return date;
        } catch (NumberFormatException ex) {
            LOGGER.warning(() -> "Invalid Unix timestamp in " + sourceName + ": '" + dateStr + "'");
        }

        LOGGER.warning(() -> "Failed to parse date string from " + sourceName + ": '" + dateStr + "'");
        return null;
    }
}
