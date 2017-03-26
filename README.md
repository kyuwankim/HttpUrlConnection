# HttpUrlConnection
--
[ANDROID 공식문서](https://developer.android.com/reference/java/net/HttpURLConnection.html)

A URLConnection with support for HTTP-specific features. See the spec for details.


Uses of this class follow a pattern:

1. Obtain a new HttpURLConnection by calling URL.openConnection() and casting the result to HttpURLConnection.

2. Prepare the request. The primary property of a request is its URI. Request headers may also include metadata such as credentials, preferred content types, and session cookies.
3. Optionally upload a request body. Instances must be configured with setDoOutput(true) if they include a request body. Transmit data by writing to the stream returned by getOutputStream().
4. Read the response. Response headers typically include metadata such as the response body's content type and length, modified dates and session cookies. The response body may be read from the stream returned by getInputStream(). If the response has no body, that method returns an empty stream.
5. Disconnect. Once the response body has been read, the HttpURLConnection should be closed by calling disconnect(). Disconnecting releases the resources held by a connection so they may be closed or reused.

For example, to retrieve the webpage at http://www.android.com/:

```java
  URL url = new URL("http://www.android.com/");
   HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
   try {
     InputStream in = new BufferedInputStream(urlConnection.getInputStream());
     readStream(in);
   } finally {
     urlConnection.disconnect();
   }
```

###Secure Communication with HTTPS
Calling openConnection() on a URL with the "https" scheme will return an HttpsURLConnection, which allows for overriding the default HostnameVerifier and SSLSocketFactory. An application-supplied SSLSocketFactory created from an SSLContext can provide a custom X509TrustManager for verifying certificate chains and a custom X509KeyManager for supplying client certificates. See HttpsURLConnection for more details.

###Response Handling
HttpURLConnection will follow up to five HTTP redirects. It will follow redirects from one origin server to another. This implementation doesn't follow redirects from HTTPS to HTTP or vice versa.
If the HTTP response indicates that an error occurred, getInputStream() will throw an IOException. Use getErrorStream() to read the error response. The headers can be read in the normal way using getHeaderFields(),

###Posting Content
To upload data to a web server, configure the connection for output using setDoOutput(true).
For best performance, you should call either setFixedLengthStreamingMode(int) when the body length is known in advance, or setChunkedStreamingMode(int) when it is not. Otherwise HttpURLConnection will be forced to buffer the complete request body in memory before it is transmitted, wasting (and possibly exhausting) heap and increasing latency.

For example, to perform an upload:
```java
   HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
   try {
     urlConnection.setDoOutput(true);
     urlConnection.setChunkedStreamingMode(0);

     OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
     writeStream(out);

     InputStream in = new BufferedInputStream(urlConnection.getInputStream());
     readStream(in);
   } finally {
     urlConnection.disconnect();
   }
```

###Performance
The input and output streams returned by this class are not buffered. Most callers should wrap the returned streams with BufferedInputStream or BufferedOutputStream. Callers that do only bulk reads or writes may omit buffering.
When transferring large amounts of data to or from a server, use streams to limit how much data is in memory at once. Unless you need the entire body to be in memory at once, process it as a stream (rather than storing the complete body as a single byte array or string).

To reduce latency, this class may reuse the same underlying Socket for multiple request/response pairs. As a result, HTTP connections may be held open longer than necessary. Calls to disconnect() may return the socket to a pool of connected sockets.

By default, this implementation of HttpURLConnection requests that servers use gzip compression and it automatically decompresses the data for callers of getInputStream(). The Content-Encoding and Content-Length response headers are cleared in this case. Gzip compression can be disabled by setting the acceptable encodings in the request header:

```java
   urlConnection.setRequestProperty("Accept-Encoding", "identity");
```

Setting the Accept-Encoding request header explicitly disables automatic decompression and leaves the response headers intact; callers must handle decompression as needed, according to the Content-Encoding header of the response.

getContentLength() returns the number of bytes transmitted and cannot be used to predict how many bytes can be read from getInputStream() for compressed streams. Instead, read that stream until it is exhausted, i.e. when read() returns -1.

###Handling Network Sign-On

Some Wi-Fi networks block Internet access until the user clicks through a sign-on page. Such sign-on pages are typically presented by using HTTP redirects. You can use getURL() to test if your connection has been unexpectedly redirected. This check is not valid until after the response headers have been received, which you can trigger by calling getHeaderFields() or getInputStream(). For example, to check that a response was not redirected to an unexpected host:

```java
HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
   try {
     InputStream in = new BufferedInputStream(urlConnection.getInputStream());
     if (!url.getHost().equals(urlConnection.getURL().getHost())) {
       // we were redirected! Kick the user out to the browser to sign on?
     }
     ...
   } finally {
     urlConnection.disconnect();
   }
```

###HTTP Authentication
HttpURLConnection supports HTTP basic authentication. Use Authenticator to set the VM-wide authentication handler:
```java
  Authenticator.setDefault(new Authenticator() {
     protected PasswordAuthentication getPasswordAuthentication() {
       return new PasswordAuthentication(username, password.toCharArray());
     }
   });
```