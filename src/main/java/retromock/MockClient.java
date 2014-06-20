package retromock;

import org.hamcrest.*;
import org.hamcrest.core.AnyOf;
import retrofit.client.Client;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedString;
import retromock.matchers.IsRequestWithMethod;
import retromock.matchers.IsRequestWithUrl;
import retromock.parser.HttpParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.core.AllOf.allOf;
import static retromock.matchers.IsHeader.header;
import static retromock.matchers.IsRequestWithHeaders.withHeaders;

public class MockClient implements Client {
    
    private final List<Route> routes;

    private MockClient(List<Route> routes) {
        this.routes = routes;
    }

    @Override
    public Response execute(Request request) throws IOException {
        List<Matcher<? super Request>> unmatchedRoutes = new LinkedList<>();
        for (Route route : routes) {
            if (route.requestMatcher.matches(request)) return route.response.createFrom(request);
            unmatchedRoutes.add(route.requestMatcher);
        }
        StringDescription description = new StringDescription();
        AnyOf.anyOf(unmatchedRoutes).describeTo(description);
        return new Response(
                request.getUrl(),
                404,
                "No route matched",
                Collections.<Header>emptyList(), 
                new TypedString("No matching route found. expected:\n" + description.toString())
        );
    }

    public static Provider when() { return new Provider(); }
    
    public static class Provider implements Client.Provider {

        final List<Route> routes = new LinkedList<>();

        public RouteBuilder aRequest() { return new RouteBuilder(); }

        public RouteBuilder GET() { return aRequest().withMethod("GET"); }
        public RouteBuilder GET(final String path) { return GET().withPath(path); }

        public RouteBuilder POST() { return aRequest().withMethod("POST"); }
        public RouteBuilder POST(final String path) { return POST().withPath(path); }

        public RouteBuilder PUT() { return aRequest().withMethod("PUT"); }
        public RouteBuilder PUT(final String path) { return PUT().withPath(path); }

        public RouteBuilder DELETE() { return aRequest().withMethod("DELETE"); }
        public RouteBuilder DELETE(final String path) { return DELETE().withPath(path); }

        /* syntax sugar */

        public Provider and() { return this; }

        public Provider when() { return this; }

        @Override public MockClient get() {
            return new MockClient(routes);
        }

        public class RouteBuilder {
            final List<Matcher<? super Request>> matchers = new LinkedList<>();

            public RouteBuilder matching(Matcher<? super Request> requestMatcher) {
                matchers.add(requestMatcher);
                return this;
            }

            public RouteBuilder withMethod(String method) {
                return matching(IsRequestWithMethod.withMethod(method));
            }

            public RouteBuilder withHeader(String headerName, Matcher<String> headerValue) {
                return matching(withHeaders(header(headerName, headerValue)));
            }

            public RouteBuilder withPath(String url) {
                return matching(IsRequestWithUrl.withPath(url));
            }

            public Provider thenReturn(Response response) {
                return thenReturn(ResponseFactory.always(response));
            }

            public Provider thenReturn(File file) {
                return thenReturn(ResponseFactory.fromFile(file));
            }

            public Provider thenReturn(Path path) {
                return thenReturn(ResponseFactory.fromFile(path));
            }

            public Provider thenReturn(ResponseFactory response) {
                Matcher<Request> requestMatcher = allOf(matchers);
                routes.add(Route.of(requestMatcher, response));
                return Provider.this;
            }
        }

    }
    
    private static class Route {
        Matcher<Request> requestMatcher;
        ResponseFactory response;
        private static Route of(Matcher<Request> requestMatcher, ResponseFactory response) {
            Route res = new Route();
            res.requestMatcher = requestMatcher;
            res.response = response;
            return res;
        }
    }

    public static abstract class ResponseFactory {
        public static ResponseFactory always(final Response response) {
            return new ResponseFactory() {
                @Override
                public Response createFrom(Request request) {
                    return response;
                }
            };
        }

        public static ResponseFactory fromFile(final File file) {
            return new ResponseFactory() {
                @Override
                public Response createFrom(Request request) throws IOException {
                    return HttpParser.parse(request.getUrl(), file);
                }
            };
        }

        public static ResponseFactory fromFile(final Path path) {
            return new ResponseFactory() {
                @Override
                public Response createFrom(Request request) throws IOException {
                    return HttpParser.parse(request.getUrl(), path);
                }
            };
        }

        public abstract Response createFrom(Request request) throws IOException;
    }

}
