package retromock.parser;

import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

/**
 * Parses a {@linkplain java.io.Reader}, e.g. from a flat file, into a {@linkplain retrofit.client.Response} object.
 *
 * For example you could prepare a mocked {@linkplain retrofit.client.Response} in a plain text file like this:
 * <pre>
 * HTTP/1.1 200 OK
 * Date: ${DATE}
 * Server: RetroMock/1.2.3.4
 * Content-Type: text/plain; charset=UTF-8
 * Content-Length: ${LENGTH}
 *
 * Hello World!
 * </pre>
 *
 * The placeholder {@code ${DATE}} will be replaced with the current date.
 * The placeholder {@code ${LENGTH}} will be replaced with the actual length of the body.
 * The {@code charset} parameter of {@code Content-Type} is considered while parsing the body.
 *
 * @since 2014-06-17
 */
public class HttpParser {

    private static final Pattern STATUS_LINE_PATTERN = Pattern.compile("HTTP/1\\.1 (?<statusCode>\\d{3}) (?<statusReason>.+)");
    private static final Pattern HEADER_PATTERN = Pattern.compile("(?<name>[a-zA-Z-]+): (?<value>.+)");
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=(?<charset>.+\\b)");
    private static final String DEFAULT_CONTENT_TYPE = "text/plain; charset=UTF-8";

    /**
     * Parses a {@linkplain java.io.BufferedReader} into a {@linkplain retrofit.client.Response} object.
     *
     * @param url URL this mock response is answering for
     * @param input {@link java.io.BufferedReader} to read from
     * @return {@link retrofit.client.Response} object filled with data from the {@linkplain java.io.BufferedReader}
     * @throws IOException If an I/O error occurs while parsing
     */
    public static Response parse(String url, BufferedReader input) throws IOException {
        Status status = status(input);
        List<Header> headers = headers(input);
        TypedInput body = body(contentType(headers), input);
        headers = new PlaceholderReplacer(headers)
                .withDate(new Date())
                .withLength(body)
                .build();

        return new Response(
                url,
                status.code(),
                status.reason(),
                headers,
                body
        );
    }

    /**
     * Parses a {@linkplain java.io.Reader} into a {@linkplain retrofit.client.Response} object.
     *
     * @param url URL this mock response is answering for
     * @param input {@link java.io.Reader} to read from
     * @return {@link retrofit.client.Response} object filled with data from the {@linkplain java.io.Reader}
     * @throws IOException If an I/O error occurs while parsing
     */
    public static Response parse(String url, Reader input) throws IOException {
        try (BufferedReader reader = new BufferedReader(input)) {
            return parse(url, reader);
        }
    }

    /**
     * Parses an {@linkplain java.io.InputStream} into a {@linkplain retrofit.client.Response} object.
     *
     * @param url URL this mock response is answering for
     * @param is {@link java.io.InputStream} to read from
     * @return {@link retrofit.client.Response} object filled with data from the {@linkplain java.io.InputStream}
     * @throws IOException If an I/O error occurs while parsing
     */
    public static Response parse(String url, InputStream is) throws IOException {
        try (Reader reader = new InputStreamReader(is)) {
            return parse(url, reader);
        }
    }

    /**
     * Parses a {@linkplain java.io.File} into a {@linkplain retrofit.client.Response} object.
     *
     * @param url URL this mock response is answering for
     * @param file {@link java.io.File} to read from
     * @return {@link retrofit.client.Response} object filled with data from the {@linkplain java.io.File}
     * @throws IOException If an I/O error occurs while parsing
     */
    public static Response parse(String url, File file) throws IOException {
        try (Reader reader = new FileReader(file)) {
            return parse(url, reader);
        }
    }

    /**
     * Parses a {@linkplain java.nio.file.Path} into a {@linkplain retrofit.client.Response} object.
     *
     * @param url URL this mock response is answering for
     * @param path {@link java.nio.file.Path} to read from
     * @return {@link retrofit.client.Response} object filled with data from the {@linkplain java.nio.file.Path}
     * @throws IOException If an I/O error occurs while parsing
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
        while (isNotEmptyOrNull(line = input.readLine())) {
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

    private static class PlaceholderReplacer {

        static final TimeZone GMT = TimeZone.getTimeZone("GMT");
        final DateFormat dateFormat;
        final List<Header> headers;
        String length;
        String date;

        PlaceholderReplacer(List<Header> headers) {
            this.headers = headers;
            this.dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            this.dateFormat.setTimeZone(GMT);
        }

        public PlaceholderReplacer withLength(long length) {
            this.length = String.valueOf(length);
            return this;
        }

        public PlaceholderReplacer withLength(TypedByteArray byteArray) {
            return withLength(byteArray.length());
        }

        public PlaceholderReplacer withLength(TypedInput input) {
            return withLength((TypedByteArray)input);
        }

        public PlaceholderReplacer withDate(Date date) {
            this.date = dateFormat.format(date);
            return this;
        }

        public List<Header> build() {
            List<Header> result = new ArrayList<>(headers.size());
            for (Header header : headers) {
                switch (header.getValue()) {
                    case "${LENGTH}" :
                        result.add(new Header(header.getName(), length));
                        break;
                    case "${DATE}" :
                        result.add(new Header(header.getName(), date));
                        break;
                    default:
                        result.add(header);
                }
            }
            return result;
        }

    }

}
