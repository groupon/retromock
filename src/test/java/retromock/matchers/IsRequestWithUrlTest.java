package retromock.matchers;

import org.junit.Test;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.mime.TypedString;

import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static retromock.matchers.IsRequestWithUrl.*;

public class IsRequestWithUrlTest {

    @Test
    public void testWithPath() throws Exception {
        final Request request = request("http://localhost/foo");
        assertThat(request, withPath("/foo"));
        assertThat(request, withPath(startsWith("/f")));
        assertThat(request, withPathMatching("/fo+"));
    }

    @Test
    public void testWithQuery() throws Exception {
        final Request request = request("http://localhost/foo?bar=baz");
        assertThat(request, withQuery("bar", "baz"));
        assertThat(request, not(withQuery("bar", "something")));
        assertThat(request, not(withQuery("whatever", "baz")));
    }

    @Test
    public void testPathWithUrlParameters() throws Exception {
        final Request request = request("http://localhost/users/1");
        assertThat(request, pathWithUrlParameters("/users/{userId}", new HashMap<String, Object>() {{
            put("userId", 1);
            put("somethingElse", "bar");
        }}));

    }

    private Request request(final String url) {
        return new Request("GET", url, Collections.<Header>emptyList(), new TypedString(""));
    }
}