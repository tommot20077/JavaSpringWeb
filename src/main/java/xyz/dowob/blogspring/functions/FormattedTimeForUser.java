package xyz.dowob.blogspring.functions;

import xyz.dowob.blogspring.model.User;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class FormattedTimeForUser {

    public static String formattedTimeForUser(Date unFormattedTime, User user) {
        Instant instantFromServerDate  = unFormattedTime.toInstant();
        ZoneId serverZoneId  = ZoneId.systemDefault();
        ZonedDateTime zonedDateTimeAtServer = instantFromServerDate.atZone(serverZoneId);
        ZonedDateTime zonedDateTimeAtUTC = zonedDateTimeAtServer.withZoneSameInstant(ZoneId.of("UTC"));
        if (user == null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return zonedDateTimeAtUTC.format(formatter);
        } else {
            String userTimezone = user.getTimezone();
            ZonedDateTime zonedDateTimeAtUser = zonedDateTimeAtUTC.withZoneSameInstant(ZoneId.of(userTimezone));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return zonedDateTimeAtUser.format(formatter);
        }
    }
}
