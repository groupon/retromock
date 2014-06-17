package retromock.matchers;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import retrofit.client.Request;

import java.net.URI;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static retromock.matchers.IsRegex.matchesRegex;

public class IsRequestWithUrl extends FeatureMatcher<Request, URI> {

    public static Matcher<Request> withPath(final String path) {
        return withPath(equalTo(path));
    }

    public static Matcher<Request> withQuery(String k, String v) {
        Map.Entry<String, String> entry = new AbstractMap.SimpleImmutableEntry<>(k, v);
        return withQuery(hasItem(entry));
    }

    public static Matcher<Request> withPath(Matcher<String> pathMatcher) {
        return withUri(new UrlWithPath(pathMatcher));
    }

    public static Matcher<Request> withPathMatching(String regex) {
        return withPath(matchesRegex(regex));
    }

    public static Matcher<Request> pathWithUrlParameters(String path, Map<String, ?> urlParams) {
        for (Map.Entry entry : urlParams.entrySet()) {
            path = path.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return withPath(equalTo(path));
    }

    public static Matcher<Request> withQuery(Matcher<? super Iterable<Map.Entry<String,String>>> pathMatcher) {
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

    public static class UrlWithQuery extends FeatureMatcher<URI, Iterable<Map.Entry<String,String>>> {

        public UrlWithQuery(Matcher<? super Iterable<Map.Entry<String,String>>> subMatcher) {
            super(subMatcher, "a URI with query parameters", "query parameters");
        }

        @Override
        protected Iterable<Map.Entry<String,String>> featureValueOf(URI actual) {
            List<Map.Entry<String,String>> result = new LinkedList<>();
            String queryString = actual.getQuery();
            String[] queries = queryString.split("&");
            for (String query : queries) {
                if (query.isEmpty()) continue;
                String[] kv = query.split("=", 2);
                String key = kv[0];
                String val = kv.length == 2 ? kv[1] : null;
                result.add(new AbstractMap.SimpleImmutableEntry<>(key, val));
            }
            return result;
        }
    }
}
