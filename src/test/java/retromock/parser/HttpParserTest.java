package retromock.parser;

import org.junit.Test;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retromock.test.FileLocator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class HttpParserTest {

    static final List<Path> HTTP_FILES = FileLocator.findAllInClasspath("http-*-response.txt");
    static final String LOCALHOST = "http://localhost";
    static final String NAME_VALUE = "\"\\w+?\"\\s*:\\s*\".+?\",?\\s*";
    static final String LIST = "\\[\\s*(" + NAME_VALUE +  ")+\\],?\\s*";
    static final String MULTI_LIST = "\\[\\s*(" + NAME_VALUE + "|" + LIST + ")+\\],?\\s*";
    static final Pattern JSON_PATTERN = Pattern.compile("\\{\\s*(" + NAME_VALUE + "|" + MULTI_LIST + ")+\\s*\\}");

    @Test
    public void testParse200ResponseFromFile() throws Exception {
        Response response = HttpParser.parse(LOCALHOST, getFile("http-200-response.txt"));
        assertNotNull(response);
        assertEquals(LOCALHOST, response.getUrl());
        assertEquals(200, response.getStatus());
        assertEquals("OK", response.getReason());
        Map<String, String> headers = headerMap(response.getHeaders());
        assertEquals("Flat-File", headers.get("X-Powered-By"));
        assertTrue(response.getBody() instanceof TypedByteArray);
        TypedByteArray body = (TypedByteArray) response.getBody();
        assertEquals(headers.get("Content-Type"), body.mimeType());
        assertEquals(headers.get("Content-Length"), String.valueOf(body.length()));
        String bodyAsStr = new String(body.getBytes());
        assertTrue(JSON_PATTERN.matcher(bodyAsStr.replaceAll("\n", "")).matches());
    }

    @Test
    public void testParse404ResponseFromFile() throws Exception {
        Response response = HttpParser.parse(LOCALHOST, getFile("http-404-response.txt"));
        assertNotNull(response);
        assertEquals(LOCALHOST, response.getUrl());
        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getReason());
        Map<String, String> headers = headerMap(response.getHeaders());
        assertEquals("Flat-File", headers.get("X-Powered-By"));
        assertEquals(headers.get("Content-Length"), "0");
    }

    private Map<String, String> headerMap(List<Header> headers) {
        Map<String, String> headerMap = new HashMap<>();
        for (Header header : headers) {
            headerMap.put(header.getName(), header.getValue());
        }
        return headerMap;
    }

    private static Path getFile(String name) {
        Path fileName = Paths.get(name);
        for (Path path : HTTP_FILES) {
            if (path.endsWith(fileName)) {
                return path;
            }
        }
        return null;
    }
}
