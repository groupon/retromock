package retromock.matchers;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import retrofit.client.Request;

import static org.hamcrest.core.IsEqual.equalTo;

public class IsRequestWithMethod extends FeatureMatcher<Request, String> {
    public IsRequestWithMethod(Matcher<? super String> subMatcher) {
        super(subMatcher, "a request with method", "method");
    }

    public static Matcher<Request> withMethod(final String requestMethod) {
        return new IsRequestWithMethod(equalTo(requestMethod));
    }

    @Override
    protected String featureValueOf(Request request) {
        return request.getMethod();
    }
}
