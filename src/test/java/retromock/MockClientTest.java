package retromock;

import org.junit.Test;
import retrofit.RestAdapter;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.mime.TypedByteArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;


public class MockClientTest {

    static interface TestCase {
        @GET("/") @Headers("X-Foo: bar") String get();
        @POST("/") String post(@Body String body);
    }


    @Test
    public void testARequest() throws Exception {
        Response getResponse = new Response("", 200, "", Collections.EMPTY_LIST, new TypedByteArray("text/plain", "\"Hello, World\"".getBytes()));
        final MockClient.ResponseFactory responseFactory = new MockClient.ResponseFactory() {
            @Override
            public Response createFrom(Request request) throws IOException {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                request.getBody().writeTo(os);
                return new Response("", 200, "", Collections.EMPTY_LIST, new TypedByteArray("text/plain", os.toByteArray()));
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
                .setEndpoint("http://localhost/")
                .build();
        TestCase testCase = restAdapter.create(TestCase.class);
        assertEquals("Hello, World", testCase.get());
    }


}
