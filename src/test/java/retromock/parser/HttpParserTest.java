package retromock.parser;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retromock.test.FileLocator;
import retromock.test.Http200ResponseBean;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static retromock.matchers.IsRegex.matchesRegex;

public class HttpParserTest {

    private List<Path> HTTP_FILES;
    static final String LOCALHOST = "http://localhost";
    static final Gson GSON = new Gson();

    @Before
    public void setup() throws IOException {
        HTTP_FILES = FileLocator.findAllInClasspath("http-*-response.txt");
    }

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
        Http200ResponseBean bodyAsBean = GSON.fromJson(new String(body.getBytes()), Http200ResponseBean.class);
        assertEquals("test", bodyAsBean.title);
        assertEquals("qwerty", bodyAsBean.foot);
    }

    @Test
    public void testParse302ResponseFromFile() throws Exception {
        Response response = HttpParser.parse(LOCALHOST, getFile("http-302-response.txt"));
        assertNotNull(response);
        assertEquals(LOCALHOST, response.getUrl());
        assertEquals(302, response.getStatus());
        assertEquals("Found", response.getReason());
        Map<String, String> headers = headerMap(response.getHeaders());
        assertEquals("http://otherhost/", headers.get("Location"));
        assertEquals("Flat-File", headers.get("X-Powered-By"));
        assertEquals(headers.get("Content-Length"), "0");
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
        assertTrue(response.getBody() instanceof TypedByteArray);
        TypedByteArray body = (TypedByteArray) response.getBody();
        assertEquals(headers.get("Content-Length"), String.valueOf(body.length()));
        String html = new String(body.getBytes());
        assertThat(html.replaceAll("\n", ""), matchesRegex(".+(<\\w+>404 .+</\\w+>.*)+"));
    }

    private Map<String, String> headerMap(List<Header> headers) {
        Map<String, String> headerMap = new HashMap<>();
        for (Header header : headers) {
            headerMap.put(header.getName(), header.getValue());
        }
        return headerMap;
    }

    private Path getFile(String name) {
        Path fileName = Paths.get(name);
        for (Path path : HTTP_FILES) {
            if (path.endsWith(fileName)) {
                return path;
            }
        }
        return null;
    }
}
