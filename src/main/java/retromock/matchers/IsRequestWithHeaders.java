package retromock.matchers;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import retrofit.client.Header;
import retrofit.client.Request;

import java.util.List;

import static org.hamcrest.core.IsCollectionContaining.hasItems;

public class IsRequestWithHeaders extends FeatureMatcher<Request, List<Header>> {
    public IsRequestWithHeaders(Matcher<? super List<Header>> subMatcher) {
        super(subMatcher, "a request with headers", "headers");
    }

    @SafeVarargs
    public static Matcher<Request> withHeaders(Matcher<Header>... headerMatchers) {
        return new IsRequestWithHeaders(hasItems(headerMatchers));
    }

    @Override
    protected List<Header> featureValueOf(Request request) {
        return request.getHeaders();
    }
}
