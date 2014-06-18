package retromock.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.regex.Pattern;

public class IsRegex extends TypeSafeDiagnosingMatcher<String> {

    private final Pattern pattern;

    public static IsRegex matchesRegex(String regex) {
        return new IsRegex(Pattern.compile(regex));
    }

    public IsRegex(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    protected boolean matchesSafely(String item, Description mismatchDescription) {
        final boolean matches = pattern.matcher(item).matches();
        if (!matches) {
            mismatchDescription
                    .appendText("Item ")
                    .appendText(item)
                    .appendText(" did not match pattern ")
                    .appendText(pattern.pattern());
        }
        return matches;
    }

    @Override
    public void describeTo(Description description) {
        description
                .appendText("a string matching the pattern ")
                .appendText(pattern.pattern());
    }
}
