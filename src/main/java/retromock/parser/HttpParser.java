package retromock.parser;

import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedString;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpParser {

    private static final Pattern STATUS_LINE_PATTERN = Pattern.compile("HTTP/1\\.1 (?<statusCode>\\d{3}) (?<statusReason>.+)");
    private static final Pattern HEADER_PATTERN = Pattern.compile("(?<name>[a-zA-Z-]+): (?<value>.+)");

    public static Response parse(String url, BufferedReader input) throws IOException {
        Status status = status(input);
        return new Response(
                url,
                status.code(),
                status.reason(),
                headers(input),
                body(input)
        );
    }

    public static Response parse(String url, FileReader input) throws IOException {
        try (BufferedReader reader = new BufferedReader(input)) {
            return parse(url, reader);
        }
    }

    public static Response parse(String url, File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return parse(url, reader);
        }
    }

    public static Response parse(String url, Path path) throws IOException {
        return parse(url, path.toFile());
    }

    private interface Status {
        int code();
        String reason();
    }

    private static Status status(BufferedReader input) throws IOException {
        String statusLine = input.readLine();
        if (statusLine == null || statusLine.isEmpty()) {
            throw invalidStatusLine();
        }
        final Matcher matcher = STATUS_LINE_PATTERN.matcher(statusLine);
        if (matcher.find()) {
            return new Status() {
                public int code() { return Integer.parseInt(matcher.group("statusCode")); }
                public String reason() { return matcher.group("statusReason"); }
            };
        } else {
            throw invalidStatusLine();
        }
    }

    private static RuntimeException invalidStatusLine() {
        throw new IllegalArgumentException("Input does not begin with a status line");
    }

    private static List<Header> headers(BufferedReader input) throws IOException {
        List<Header> headers = new ArrayList<>();
        String line;
        while (isNotEmptyOrNull((line = input.readLine()))) {
            Matcher m = HEADER_PATTERN.matcher(line);
            if (m.find()) {
                headers.add(new Header(m.group("name"), m.group("value")));
            }
        }
        return headers;
    }

    private static boolean isNotEmptyOrNull(final String line) {
        return line != null && !line.isEmpty();
    }

    private static TypedInput body(BufferedReader input) throws IOException {
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            body.append(line).append('\n');
        }
        return new TypedString(body.toString());
    }
}
