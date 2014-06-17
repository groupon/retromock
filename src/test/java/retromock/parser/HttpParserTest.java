package retromock.parser;

import org.junit.Test;
import retrofit.client.Response;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

public class HttpParserTest {

    private static final InputStream istream = HttpParserTest.class.getClassLoader().getResourceAsStream("http-200-response.txt");

    @Test
    public void testParseResponseFromFile() throws Exception {
        Response response;
        try (InputStreamReader ireader = new InputStreamReader(istream);
             BufferedReader reader = new BufferedReader(ireader)) {
            response = HttpParser.parse("http://localhost", reader);
        }
        assertNotNull(response);
    }

}
