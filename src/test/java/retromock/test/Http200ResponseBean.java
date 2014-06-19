package retromock.test;

import java.util.Map;

/**
 * Bean for the JSON body in {@code http-200-response.txt}.
 * <pre>
 * {
 *   "title" : "test",
 *   "properties" : {
 *     "keyA" : "valueA",
 *     "keyB" : "valueB",
 *     "keyC" : "valueC"
 *   },
 *   "foot" : "qwerty"
 * }
 * </pre>
 *
 *
 * @since 2014-06-18
 */
public class Http200ResponseBean {

    public String title;
    public Map<String, String> properties;
    public String foot;

}
