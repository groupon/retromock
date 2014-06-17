package retromock.matchers;

import org.junit.Test;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.mime.TypedString;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static retromock.matchers.IsHeader.header;
import static retromock.matchers.IsRequestWithHeaders.withHeaders;

public class IsRequestWithHeadersTest {

    @Test
    public void testWithHeaders() throws Exception {
        final List<Header> headers = Arrays.asList(
                new Header("Content-Type", "application/json"),
                new Header("X-Header", "foo")
        );
        final Request request = request(headers);
        assertThat(request, withHeaders(
                header("CONTENT-TYPE", startsWith("application/")),
                header("x-header", is("foo"))
        ));

    }

    private Request request(final List<Header> headers) {
        return new Request("GET", "http://localhost/", headers, new TypedString(""));
    }
}