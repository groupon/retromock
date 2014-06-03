package retromock.matchers;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsMapContaining;
import retrofit.client.Request;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;

public class IsRequestWithUrl extends FeatureMatcher<Request, URI> {

    public static Matcher<Request> withPath(final String path) {
        return withPath(equalTo(path));
    }

    public static Matcher<Request> withQuery(String k, String v) {
        return withQuery(hasEntry(k, v));
    }

    public static Matcher<Request> withPath(Matcher<String> pathMatcher) {
        return withUri(new UrlWithPath(pathMatcher));
    }

    public static Matcher<Request> withQuery(Matcher<Map<? extends String, ? extends String>> pathMatcher) {
        return withUri(new UrlWithQuery(pathMatcher));
    }

    private static Matcher<Request> withUri(Matcher<URI> uriMatcher) {
        return new IsRequestWithUrl(uriMatcher);
    }

    public IsRequestWithUrl(Matcher<? super URI> subMatcher) {
        super(subMatcher, "a request with URI", "URI");
    }

    @Override
    protected URI featureValueOf(Request request) {
        return URI.create(request.getUrl());
    }

    public static class UrlWithPath extends FeatureMatcher<URI, String> {

        public UrlWithPath(Matcher<? super String> subMatcher) {
            super(subMatcher, "a URI with path", "path");
        }

        @Override
        protected String featureValueOf(URI actual) {
            return actual.getPath();
        }
    }

    public static class UrlWithQuery extends FeatureMatcher<URI, Map<String,String>> {

        public UrlWithQuery(Matcher<? super Map<String,String>> subMatcher) {
            super(subMatcher, "a URI with path", "path");
        }

        @Override
        protected Map<String,String> featureValueOf(URI actual) {
            LinkedHashMap<String, String> result = new LinkedHashMap<>();
            String queryString = actual.getQuery();
            String[] queries = queryString.split("&");
            for (String query : queries) {
                if (query.isEmpty()) continue;
                String[] kv = query.split("=", 2);
                result.put(kv[0], kv.length == 2 ? kv[1] : null);
            }
            return result;
        }
    }
}
