package com.infinitehorizons.handler;

import com.infinitehorizons.SharedWebHook;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface WebHookErrorHandler {

    WebHookErrorHandler DEFAULT = (client, message, throwable) -> LoggerFactory.getLogger(SharedWebHook.class).error(message, throwable);

    void handle(@NotNull SharedWebHook client, @NotNull String message, @NotNull Throwable throwable);
}
