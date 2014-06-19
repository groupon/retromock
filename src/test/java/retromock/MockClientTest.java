package retromock;

import org.junit.Test;
import retrofit.RestAdapter;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.mime.TypedByteArray;
import retromock.test.FileLocator;
import retromock.test.Http200ResponseBean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static retromock.matchers.IsRequestWithBody.body;
import static retromock.matchers.IsRequestWithBody.jsonPath;

public class MockClientTest {

    private final Response helloWorld = new Response(
            "/response", 200, "OK",
            Collections.<Header>emptyList(),
            new TypedByteArray("text/plain", "\"Hello, World\"".getBytes())
    );

    static interface TestCase {
        @GET("/") @Headers("X-Foo: bar") String get();
        @POST("/") String post(@Body String body);
    }

    static interface JsonTestCase {
        @GET("/") Http200ResponseBean get();
        @POST("/post") String post(@Body Http200ResponseBean body, @retrofit.http.Header("Content-Type") String contentType);
    }


    @Test
    public void testARequest() throws Exception {

        final MockClient.ResponseFactory responseFactory = new MockClient.ResponseFactory() {
            @Override
            public Response createFrom(Request request) throws IOException {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                request.getBody().writeTo(os);
                return new Response("", 200, "", Collections.<Header>emptyList(), new TypedByteArray("text/plain", os.toByteArray()));
            }
        };

        MockClient.Provider client = MockClient.when()
                    .aRequest()
                    .withMethod("GET")
                    .withHeader("x-foo", is("bar"))
                    .thenReturn(helloWorld)
                .and().when()
                    .POST()
                    .thenReturn(responseFactory)
                ;

        TestCase testCase = restAdapter(client).create(TestCase.class);

        assertEquals("Hello, World", testCase.get());
        assertEquals("post body", testCase.post("post body"));

    }

    @Test
    public void testJsonPost() {
        MockClient.Provider client = MockClient.when()
            .POST("/post")
            .matching(body(
                    jsonPath("title", is("test")),
                    jsonPath("properties.foo", is("bar"))
            ))
            .thenReturn(helloWorld);

        final JsonTestCase jsonTestCase = restAdapter(client).create(JsonTestCase.class);

        final Http200ResponseBean body = new Http200ResponseBean();
        body.title = "test";
        body.properties = new HashMap<String, String>() {{
            put("foo", "bar");
        }};

        assertEquals("Hello, World", jsonTestCase.post(body, "application/json"));
    }

    @Test
    public void testResponseFromFile() throws Exception {
        Path http200file = FileLocator.findFirstInClasspath("http-200-response.txt");
        MockClient.Provider client = MockClient.when()
                .GET()
                .thenReturn(http200file);

        JsonTestCase testCase = restAdapter(client).create(JsonTestCase.class);

        Http200ResponseBean bean = testCase.get();

        assertEquals("test", bean.title);
        assertFalse(bean.properties.isEmpty());
        assertEquals("qwerty", bean.foot);
    }

    private RestAdapter restAdapter(MockClient.Provider client) {
        return new RestAdapter.Builder()
                .setClient(client)
                .setEndpoint("http://example.org/") // ignored, but we need to set some value
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
    }

}
