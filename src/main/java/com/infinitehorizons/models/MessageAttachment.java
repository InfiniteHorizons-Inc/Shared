package com.infinitehorizons.models;

import com.infinitehorizons.utils.IoUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents an attachment for outgoing messages.
 * Provides constructors for creating attachments from byte arrays, input streams, and files.
 */
@Getter
public class MessageAttachment {
    private final String name;
    private final byte[] data;

    /**
     * Constructs a MessageAttachment using a byte array.
     *
     * @param name the name of the attachment.
     * @param data the data of the attachment in byte array form.
     */
    public MessageAttachment(@NotNull String name, @NotNull byte[] data) {
        this.name = name;
        this.data = data;
    }

    /**
     * Constructs a MessageAttachment using an input stream.
     *
     * @param name   the name of the attachment.
     * @param stream the input stream from which to read the data.
     * @throws IOException if an I/O error occurs while reading the stream.
     */
    public MessageAttachment(@NotNull String name, @NotNull InputStream stream) throws IOException {
        this.name = name;
        this.data = IoUtils.readAllBytes(stream);
    }

    /**
     * Constructs a MessageAttachment using a file.
     *
     * @param name the name of the attachment.
     * @param file the file from which to read the data.
     * @throws IOException if an I/O error occurs while reading the file.
     */
    public MessageAttachment(@NotNull String name, @NotNull File file) throws IOException {
        this(name, new FileInputStream(file));
    }
}
