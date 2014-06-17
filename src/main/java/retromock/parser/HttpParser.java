package retromock.parser;

import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a flat file into a {@linkplain retrofit.client.Response} object.
 *
 * @since 2014-06-17
 */
public class HttpParser {

    private static final Pattern STATUS_LINE_PATTERN = Pattern.compile("HTTP/1\\.1 (?<statusCode>\\d{3}) (?<statusReason>.+)");
    private static final Pattern HEADER_PATTERN = Pattern.compile("(?<name>[a-zA-Z-]+): (?<value>.+)");
    private static final String DEFAULT_CONTENT_TYPE = "text/plain; charset=UTF-8";
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=(?<charset>.+\\b)");

    /**
     * Parses a {@linkplain java.io.BufferedReader} into a {@linkplain retrofit.client.Response} object.
     * @param url URL this mock response is answering for
     * @param input {@link java.io.BufferedReader} to read from
     * @return {@link retrofit.client.Response} object filled with data from the {@linkplain java.io.BufferedReader}
     * @throws IOException
     */
    public static Response parse(String url, BufferedReader input) throws IOException {
        Status status = status(input);
        List<Header> headers = headers(input);
        TypedInput body = body(contentType(headers), input);

        return new Response(
                url,
                status.code(),
                status.reason(),
                headers,
                body
        );
    }

    /**
     * Parses a {@linkplain java.io.FileReader} into a {@linkplain retrofit.client.Response} object.
     * @param url URL this mock response is answering for
     * @param input {@link java.io.FileReader} to read from
     * @return {@link retrofit.client.Response} object filled with data from the {@linkplain java.io.FileReader}
     * @throws IOException
     */
    public static Response parse(String url, FileReader input) throws IOException {
        try (BufferedReader reader = new BufferedReader(input)) {
            return parse(url, reader);
        }
    }

    /**
     * Parses a {@linkplain java.io.File} into a {@linkplain retrofit.client.Response} object.
     * @param url URL this mock response is answering for
     * @param file {@link java.io.File} to read from
     * @return {@link retrofit.client.Response} object filled with data from the {@linkplain java.io.File}
     * @throws IOException
     */
    public static Response parse(String url, File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return parse(url, reader);
        }
    }

    /**
     * Parses a {@linkplain java.nio.file.Path} into a {@linkplain retrofit.client.Response} object.
     * @param url URL this mock response is answering for
     * @param path {@link java.nio.file.Path} to read from
     * @return {@link retrofit.client.Response} object filled with data from the {@linkplain java.nio.file.Path}
     * @throws IOException
     */
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

    private static String contentType(List<Header> headers) {
        String contentType = DEFAULT_CONTENT_TYPE;
        for (Header header : headers) {
            if ("Content-Type".equalsIgnoreCase(header.getName())) {
                contentType = header.getValue();
                break;
            }
        }
        return contentType;
    }

    private static TypedInput body(String mimeType, BufferedReader input) throws IOException {
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = input.readLine()) != null) {
            body.append(line).append('\n');
        }
        return new TypedByteArray(mimeType, body.toString().getBytes(charset(mimeType)));
    }

    private static Charset charset(String mimeType) {
        Matcher m = CHARSET_PATTERN.matcher(mimeType);
        if (m.find()) {
            return Charset.forName(m.group("charset"));
        } else {
            return Charset.defaultCharset();
        }
    }
}
