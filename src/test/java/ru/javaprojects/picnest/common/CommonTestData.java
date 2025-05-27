package ru.javaprojects.picnest.common;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CommonTestData {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final String HOME_URL = "/";

    public static final String ACTION_ATTRIBUTE = "action";
    public static final String PRIORITIES_ATTRIBUTE = "priorities";
    public static final String TECHNOLOGIES_ATTRIBUTE = "technologies";
    public static final String ARCHITECTURES_ATTRIBUTE = "architectures";

    public static final String ID_PARAM = "id";
    public static final String NAME_PARAM = "name";
    public static final String LOGO_FILE_NAME_PARAM = "logo.fileName";
    public static final String LOGO_FILE_LINK_PARAM = "logo.fileLink";
    public static final String LOGO_INPUTTED_FILE_BYTES_PARAM = "logo.inputtedFileBytes";
    public static final String POPULAR_PARAM = "popular";
    public static final String BY_AUTHOR_PARAM = "by-author";

    public static final long NOT_EXISTING_ID = 100;
    public static final String HTML_TEXT = "<h1>name</h1>";
    public static final String LONG_STRING = "MoreThan32CharactersLengthxxxxxxxxaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    public static final String INVALID_URL = "some-invalid-url.com";
    public static final String EMPTY_STRING = "";

    public static MultiValueMap<String, String> getPageableParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", "0");
        params.add("size", "2");
        return params;
    }

    public static LocalDateTime parseLocalDateTime(String date) {
        return DATE_TIME_FORMATTER.parse(date, LocalDateTime::from);
    }
}
