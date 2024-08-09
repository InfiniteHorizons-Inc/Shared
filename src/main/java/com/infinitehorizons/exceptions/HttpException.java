package com.infinitehorizons.exceptions;

import lombok.Getter;
import okhttp3.Headers;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when an unexpected non-2xx HTTP response is received.
 */
@Getter
public class HttpException extends RuntimeException {

    private final int code;
    private final String body;
    private final Headers headers;

    /**
     * Constructs a new HttpException with the specified HTTP status code, response body, and headers.
     *
     * @param code    The HTTP status code.
     * @param body    The body of the HTTP response.
     * @param headers The HTTP headers associated with the response.
     */
    public HttpException(int code, @NotNull String body, @NotNull Headers headers) {
        super("Request returned failure " + code + ": " + body);
        this.code = code;
        this.body = body;
        this.headers = headers;
    }
}
