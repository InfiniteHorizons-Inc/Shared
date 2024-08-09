package com.infinitehorizons.models;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.JSONString;

/**
 * Represents the metadata of a readonly message attachment.
 * <p>This class does not contain the file itself but only metadata
 * useful for retrieving the actual attachment.</p>
 */
@Getter
@ToString
public class ReadonlyAttachment implements JSONString {
    private final String url;
    private final String proxyUrl;
    private final String fileName;
    private final int width;
    private final int height;
    private final int size;
    private final long id;

    /**
     * Constructs a new ReadonlyAttachment instance with the provided metadata.
     *
     * @param url       The URL for this attachment.
     * @param proxyUrl  The proxy URL for this attachment, used by the client to generate previews.
     * @param fileName  The name of this attachment file.
     * @param width     The width of the attachment (relevant to images/videos).
     * @param height    The height of the attachment (relevant to images/videos).
     * @param size      The approximated size of this attachment in bytes.
     * @param id        The ID of this attachment.
     */
    public ReadonlyAttachment(
            @NotNull String url, @NotNull String proxyUrl, @NotNull String fileName,
            int width, int height, int size, long id) {
        this.url = url;
        this.proxyUrl = proxyUrl;
        this.fileName = fileName;
        this.width = width;
        this.height = height;
        this.size = size;
        this.id = id;
    }

    /**
     * Provides a JSON representation of this attachment.
     *
     * @return The JSON representation.
     */
    @Override
    public String toJSONString() {
        return new JSONObject(this).toString();
    }
}
