package retromock;

import org.junit.Test;
import retrofit.RestAdapter;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.client.Header;
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class MockClientTest {

    static interface TestCase {
        @GET("/") @Headers("X-Foo: bar") String get();
        @POST("/") String post(@Body String body);
    }

    static interface JsonTestCase {
        @GET("/") Http200ResponseBean get();
    }


        @Test
    public void testARequest() throws Exception {
        Response getResponse = new Response("", 200, "", Collections.<Header>emptyList(), new TypedByteArray("text/plain", "\"Hello, World\"".getBytes()));
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
                    .thenReturn(getResponse)
                .and().when()
                    .POST()
                    .thenReturn(responseFactory)
                ;
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(client)
                .setEndpoint("http://example.org/") // ignored, but we need to set some value
                .build();
        TestCase testCase = restAdapter.create(TestCase.class);
        assertEquals("Hello, World", testCase.get());
        assertEquals("post body", testCase.post("post body"));
    }

    @Test
    public void testResponseFromFile() throws Exception {
        Path http200file = FileLocator.findFirstInClasspath("http-200-response.txt");
        MockClient.Provider client = MockClient.when()
                .GET()
                .thenReturn(http200file);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(client)
                .setEndpoint("http://example.org/") // ignored, but we need to set some value
                .build();
        JsonTestCase testCase = restAdapter.create(JsonTestCase.class);
        Http200ResponseBean bean = testCase.get();
        assertEquals("test", bean.getTitle());
        assertFalse(bean.getProperties().isEmpty());
        assertEquals("qwerty", bean.getFoot());
    }

}
