package ru.javaprojects.picnest.app.error;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class DbConstraintMessageCodes {
    private static final Map<String, String> dbConstraintsMap = new HashMap<>();

    static {
        dbConstraintsMap.put("users_unique_email_idx", "error.duplicate.email");
        dbConstraintsMap.put("albums_unique_name_idx", "error.duplicate.album-name");
        dbConstraintsMap.put("foreign key(album_id) references", "album.is-referenced");
    }

    public static Optional<String> getMessageCode(String exceptionMessage) {
        exceptionMessage = exceptionMessage.toLowerCase();
        for (var entry : dbConstraintsMap.entrySet()) {
            if (exceptionMessage.contains(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }
}
