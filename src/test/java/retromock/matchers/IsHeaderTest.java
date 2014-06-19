package retromock.matchers;

import org.hamcrest.Matcher;
import org.junit.Test;
import retrofit.client.Header;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;

import static org.junit.Assert.*;

public class IsHeaderTest {

    static final Matcher<Header> isHeader = IsHeader.header("name", not(isEmptyOrNullString()));

    @Test
    public void testBasicAuth() throws Exception {
        Header header = new Header("Authorization", "Basic dXNlcjpwYXNzd29yZA==");
        assertThat(header, IsHeader.basicAuth("user", "password"));
    }

    @Test
    public void testIsHeaderTrue() throws Exception {
        Header header = new Header("name", "value");
        assertTrue(isHeader.matches(header));
    }

    @Test
    public void testIsHeaderFalse() throws Exception {
        Header header = new Header("name", "");
        assertFalse(isHeader.matches(header));
    }

}
