package retromock.matchers;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import retrofit.client.Header;

import javax.xml.bind.DatatypeConverter;

import java.nio.charset.Charset;

import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

public class IsHeader {

    private static final Charset UTF_8;

    static {
        UTF_8 = Charset.forName("UTF-8");
    }

    public static Matcher<Header> header(String name, Matcher<String> value) {
        return allOf(new HeaderName(equalToIgnoringCase(name)), new HeaderValue(value));
    }

    public static Matcher<Header> basicAuth(String username, String password) {
        final String headerVal = DatatypeConverter.printBase64Binary((username + ":" + password).getBytes(UTF_8));
        return header("Authorization", equalTo("Basic " + headerVal));
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
            super(subMatcher, "a header with value", "value");
        }

        @Override
        protected String featureValueOf(Header header) {
            return header.getValue();
        }
    }
}
