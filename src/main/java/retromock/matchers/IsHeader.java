package retromock.matchers;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import retrofit.client.Header;

import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

public class IsHeader {

    public static Matcher<Header> header(String name, Matcher<String> value) {
        return allOf(new HeaderName(equalToIgnoringCase(name)), new HeaderValue(value));
    }

    static class HeaderName extends FeatureMatcher<Header, String> {

        public HeaderName(Matcher<? super String> subMatcher) {
            super(subMatcher, "a header with name", "name");
        }

        @Override
        protected String featureValueOf(Header header) {
            return header.getName();
        }
    }

    static class HeaderValue extends FeatureMatcher<Header, String> {

        public HeaderValue(Matcher<? super String> subMatcher) {
            super(subMatcher, "a header with name", "name");
        }

        @Override
        protected String featureValueOf(Header header) {
            return header.getName();
        }
    }
}
