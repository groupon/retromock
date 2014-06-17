package retromock.matchers;

import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class IsRegexTest {

    static final String PATTERN_STR = "[a-zA-Z]+";
    static final IsRegex isRegex = IsRegex.matchesRegex(PATTERN_STR);

    @Test
    public void testRegExMatching() throws Exception {
        assertTrue(isRegex.matches("abcdefg"));
        Description description = new StringDescription();
        isRegex.describeTo(description);
        String desc = description.toString();
        assertTrue(desc.contains("a string matching the pattern"));
        assertTrue(desc.contains(PATTERN_STR));
    }

    @Test
    public void testRegExNotMatching() throws Exception {
        Description description = new StringDescription();
        assertFalse(isRegex.matchesSafely("123456", description));
        String desc = description.toString();
        assertTrue(desc.contains("did not match pattern"));
        assertTrue(desc.contains("123456"));
        assertTrue(desc.contains(PATTERN_STR));
    }

}
