package retromock.matchers;

import com.jayway.restassured.path.json.JsonPath;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import retrofit.client.Request;
import retrofit.mime.TypedOutput;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.allOf;

public class IsRequestWithBody extends FeatureMatcher<Request, TypedOutput> {

    @SafeVarargs
    public static Matcher<Request> body(final Matcher<JsonPath>... pathMatchers) {
        return new IsRequestWithBody(jsonBody(allOf(pathMatchers)));
    }

    public static Matcher<TypedOutput> contentType(final Matcher<String> mimeType) {
        return new FeatureMatcher<TypedOutput, String>(mimeType, "a request body with mime type", "mime type") {
            @Override
            protected String featureValueOf(TypedOutput actual) {
                return actual.mimeType();
            }
        };
    }

    public static Matcher<TypedOutput> jsonBody(final Matcher<JsonPath> mimeType) {
        return new FeatureMatcher<TypedOutput, JsonPath>(mimeType, "a request body with mime type", "mime type") {
            @Override
            protected JsonPath featureValueOf(TypedOutput actual) {
                final int capacity = (int) Math.max(actual.length(), 32);
                final ByteArrayOutputStream os = new ByteArrayOutputStream(capacity);
                try {
                    actual.writeTo(os);
                    return JsonPath.from(new String(os.toByteArray(), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }

    public static<T> Matcher<JsonPath> jsonPath(final String jsonPath, final Matcher<T> matcher) {
        return new FeatureMatcher<JsonPath, T>(matcher, "a json matching", "jsonPath") {
            @Override
            protected T featureValueOf(JsonPath actual) {
                return actual.get(jsonPath);
            }
        };
    }

    public IsRequestWithBody(Matcher<? super TypedOutput> subMatcher) {
        super(subMatcher, "a request with body", "body");
    }

    @Override
    protected TypedOutput featureValueOf(Request actual) {
        final TypedOutput body = actual.getBody();
        return body;
    }
}
