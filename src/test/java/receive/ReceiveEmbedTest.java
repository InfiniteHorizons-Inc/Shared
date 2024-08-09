package receive;

import com.infinitehorizons.models.EntityFactory;
import com.infinitehorizons.models.ReadonlyEmbed;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Iterator;

public class ReceiveEmbedTest {
    public static final JSONObject MOCK_IMAGE_JSON =
            new JSONObject()
                    .put("width", 100)
                    .put("url", "https://avatars1.githubusercontent.com/u/18090140?s=460&v=4")
                    .put("proxyUrl", "https://images-ext-2.discordapp.net/external/szbDUqX0oTT0E460Vn14767s7VMpWig23v9vPJczatE/%3Fs%3D460%26v%3D4/https/avatars1.githubusercontent.com/u/18090140?width=135&height=135")
                    .put("height", 200);

    public static final JSONObject MOCK_PROVIDER_JSON =
            new JSONObject()
                    .put("name", "github")
                    .put("url", "https://github.com");

    public static final JSONObject MOCK_VIDEO_JSON =
            new JSONObject()
                    .put("url", "https://youtube.com")
                    .put("width", 100)
                    .put("height", 200);

    public static final JSONObject MOCK_FOOTER_JSON =
            new JSONObject()
                    .put("text", "this is a footer")
                    .put("icon", "https://avatars1.githubusercontent.com/u/18090140?s=460&v=4");

    public static final JSONObject MOCK_AUTHOR_JSON =
            new JSONObject()
                    .put("name", "InfiniteHorizons")
                    .put("url", "https://github.com/InfiniteHorizons-Inc")
                    .put("icon", "https://avatars1.githubusercontent.com/u/18090140?s=460&v=4");

    public static final JSONObject MOCK_FIELD_JSON =
            new JSONObject()
                    .put("name", "this is a field")
                    .put("value", "this is a value of a field")
                    .put("inline", false);

    public static final JSONObject MOCK_EMBED_JSON =
            new JSONObject()
                    .put("title", "this is a title")
                    .put("description", "this is a description")
                    .put("url", "https://github.com/InfiniteHorizons-Inc/Shared")
                    .put("color", 0xff00ff)
                    .put("timestamp", OffsetDateTime.now(ZoneId.of("UTC")).toString())
                    .put("image", MOCK_IMAGE_JSON)
                    .put("thumbnail", MOCK_IMAGE_JSON)
                    .put("provider", MOCK_PROVIDER_JSON)
                    .put("footer", MOCK_FOOTER_JSON)
                    .put("video", MOCK_VIDEO_JSON)
                    .put("author", MOCK_AUTHOR_JSON)
                    .put("fields", new JSONArray().put(MOCK_FIELD_JSON));


    @Test
    public void parseEmbed() {
        ReadonlyEmbed embed = EntityFactory.makeEmbed(MOCK_EMBED_JSON);
        Assert.assertEquals(MOCK_EMBED_JSON.get("description"), embed.getDescription());

        // Compara específicamente el texto del título
        Assert.assertEquals(MOCK_EMBED_JSON.get("title"), embed.getTitle().getText());

        // Asegúrate de comparar la URL del título
        Assert.assertEquals(MOCK_EMBED_JSON.get("url"), embed.getTitle().getUrl());

        Assert.assertEquals(MOCK_EMBED_JSON.getInt("color"), (int) embed.getColor());
        Assert.assertEquals(MOCK_IMAGE_JSON.toString(), new JSONObject(embed.getImage()).toString());
        Assert.assertEquals(MOCK_IMAGE_JSON.toString(), new JSONObject(embed.getThumbnail()).toString());
        Assert.assertEquals(MOCK_PROVIDER_JSON.toString(), new JSONObject(embed.getProvider()).toString());
        Assert.assertEquals(MOCK_FOOTER_JSON.toString(), new JSONObject(embed.getFooter()).toString());
        Assert.assertEquals(MOCK_AUTHOR_JSON.toString(), new JSONObject(embed.getAuthor()).toString());
        Assert.assertEquals(MOCK_VIDEO_JSON.toString(), new JSONObject(embed.getVideo()).toString());
        Assert.assertEquals(new JSONArray().put(MOCK_FIELD_JSON).toString(), new JSONArray(embed.getFields()).toString());

        // Asegúrate de que el JSON serializado tenga la estructura esperada
        JSONObject parsedJson = new JSONObject(embed.toJSONString());
        for (Iterator<String> it = parsedJson.keys(); it.hasNext(); ) {
            String key = it.next();
            String value1 = String.valueOf(MOCK_EMBED_JSON.opt(key));
            String value2 = String.valueOf(parsedJson.opt(key));
            Assert.assertEquals("Not matching values for key " + key, value1, value2);
        }
    }
}
