# Retromock

like [Wiremock][wiremock] for [Retrofit][retrofit], but faster.

## What is it?

Retromock is an easy way to mock external APIs if you use Square's HTTP client, [retrofit].
It allows you to specify expected requests and corresponding responses using a DSL reminiscent of [wiremock].

Request matching is performed with [hamcrest] matchers, so it is easy to extend the DSL with your own matchers, e.g.
for custom authentication schemes. Retromock comes with a wide variety of matchers that allow you to match on many
parts of an HTTP request, including the path, query parameters or headers. You can also use
[JSON Path](https://code.google.com/p/json-path/) expressions to match on POST or PUT request bodies.

## How do I use Retromock?

tl;dr: look at [MockClientTest](src/test/java/retromock/MockClientTest.java).

To use Retormock, you perform these steps:

1.  use the DSL to setup routes

    ```java
    MockClient.Provider mockClient = MockClient.when()
     .GET("/some/path")
     .withHeader("x-foo", is("headerVal"))
     .matching(body(
       jsonPath("title", startsWith("test")),
       jsonPath("properties.foo", is(200))
     ))
     .thenReturn(helloWorld);
    ```

   many of the methods are overloaded or have alternative ways of specifying the same functionality.
   For example, if you want to specify the request method yourself, you can use
   `MockClient.when().aRequest().withMethod("PATCH").andPath("/whatever")`.

1.  Tell Retrofit to use your mock client instead of the HTTP client library you usually use

    ```java
    RestAdapter restAdapter = new RestAdapter.Builder()
       .setClient(mockClient)
       .setEndpoint("http://example.org/") // ignored, but you must set some value
       // more config, e.g. for request interceptors
       .build();
    ```

1.  Use Retrofit as usual

    ```java
    YourApiClient yac = restAdapter.create(YourApiClient.class);

    assertEquals("Hello, World", yac.someMethod());
    ```

## Alternatives to Retromock

Depending of how much "infrastructure" you want to test, there are other approaches you can use.

* In the simplest case, if you use retrofit, your API clients are simple Java interfaces, so you can provide mock
  implementations of the interfaces or use a mocking library like [Mockito](https://code.google.com/p/mockito/).

* If you want to simulate network conditions like delays / connection failures, Square provides
  [retrofit-mock](https://github.com/square/retrofit/tree/master/retrofit-mock).

* Or you can go the whole hog and ramp up a server to serve your mock responses with various libraries like
  [wiremock], et al.


[wiremock]: http://wiremock.org/
[retrofit]: http://square.github.io/retrofit/
[hamcrest]: https://code.google.com/p/hamcrest/
