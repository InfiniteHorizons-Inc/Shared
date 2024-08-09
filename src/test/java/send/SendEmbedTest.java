package send;

import com.infinitehorizons.components.WebHookEmbedComponent;
import com.infinitehorizons.models.send.WebHookEmbed;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class SendEmbedTest {
    private WebHookEmbedComponent builder;

    @Before
    public void setup() {
        builder = new WebHookEmbedComponent();
    }

    @Test
    public void checkEmptyAndReset() {
        Assert.assertTrue("Builder is supposed to be empty at the very start", builder.isEmpty());

        builder.setAuthor(new WebHookEmbed.EmbedAuthor("Author", null, null));
        Assert.assertFalse("Setting author doesn't change isEmpty to false", builder.isEmpty());
        builder.reset();
        Assert.assertTrue("Reset doesn't reset author", builder.isEmpty());

        builder.setDescription("");
        Assert.assertTrue("Empty description should still be empty", builder.isEmpty());
        builder.setDescription("desc");
        Assert.assertFalse("Setting description doesn't change isEmpty to false", builder.isEmpty());
        builder.reset();
        Assert.assertTrue("Reset doesn't reset description", builder.isEmpty());

        builder.setFooter(new WebHookEmbed.EmbedFooter("Text", null));
        Assert.assertFalse("Setting footer doesn't change isEmpty to false", builder.isEmpty());
        builder.reset();
        Assert.assertTrue("Reset doesn't reset footer", builder.isEmpty());

        builder.setImageUrl("");
        Assert.assertTrue("Empty image should still be empty", builder.isEmpty());
        builder.setImageUrl("imgurl");
        Assert.assertFalse("Setting image doesn't change isEmpty to false", builder.isEmpty());
        builder.reset();
        Assert.assertTrue("Reset doesn't reset image", builder.isEmpty());

        builder.setThumbnailUrl("");
        Assert.assertTrue("Empty thumbnail should still be empty", builder.isEmpty());
        builder.setThumbnailUrl("thumb");
        Assert.assertFalse("Setting thumbnail doesn't change isEmpty to false", builder.isEmpty());
        builder.reset();
        Assert.assertTrue("Reset doesn't reset thumbnail", builder.isEmpty());

        builder.setTimestamp(Instant.now());
        Assert.assertFalse("Setting timestamp doesn't change isEmpty to false", builder.isEmpty());
        builder.reset();
        Assert.assertTrue("Reset doesn't reset timestamp", builder.isEmpty());

        builder.setTitle(new WebHookEmbed.EmbedTitle("title", null));
        Assert.assertFalse("Setting title doesn't change isEmpty to false", builder.isEmpty());
        builder.reset();
        Assert.assertTrue("Reset doesn't reset title", builder.isEmpty());

        builder.addField(new WebHookEmbed.EmbedField(true, "FieldKey", "FieldValue"));
        Assert.assertFalse("Adding field doesn't change isEmpty to false", builder.isEmpty());
        builder.reset();
        Assert.assertTrue("Reset doesn't reset field(s)", builder.isEmpty());

        builder.setColor(0xFFFFFF);
        builder.setDescription("dummy");    //needed for build to work
        Assert.assertEquals("Color was not properly set", (Object) 0xFFFFFF, builder.build().getColor());
        builder.reset();
        builder.setDescription("dummy");
        Assert.assertNull("Reset doesn't reset color", builder.build().getColor());
    }

    @Test
    public void nonNullConstructors() {
        AtomicInteger numFailed = new AtomicInteger(0);

        try {
            new WebHookEmbed.EmbedAuthor(null, null, null);
        } catch(NullPointerException | IllegalArgumentException ex) { numFailed.incrementAndGet(); }

        try {
            new WebHookEmbed.EmbedTitle(null, null);
        } catch(NullPointerException | IllegalArgumentException ex) { numFailed.incrementAndGet(); }

        try {
            new WebHookEmbed.EmbedFooter(null, null);
        } catch(NullPointerException | IllegalArgumentException ex) { numFailed.incrementAndGet(); }

        try {
            new WebHookEmbed.EmbedField(true, "key", null);
        } catch(NullPointerException | IllegalArgumentException ex) { numFailed.incrementAndGet(); }

        try {
            new WebHookEmbed.EmbedField(true, null, "val");
        } catch(NullPointerException | IllegalArgumentException ex) { numFailed.incrementAndGet(); }

        Assert.assertEquals("Not all constructors with non-null field annotation threw errors", 5, numFailed.get());
    }

    @Test
    public void embedBuildsSuccessfully() {
        populateBuilder();
        builder.build();
    }

    @Test
    public void buildEmptyEmbed() {
        Assert.assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    public void checkJSON() {
        Map<String, Object> expected = new JSONObject()
                .put("title", "Title")
                .put("url", "titleUrl")
                .put("author", new JSONObject()
                        .put("name", "Minn")
                        .put("url", "authorUrl")
                        .put("icon", "authorIcon"))
                .put("color", 0xFF00FF)
                .put("description", "Hello World!")
                .put("image", new JSONObject().put("url", "imgUrl"))
                .put("thumbnail", new JSONObject().put("url", "thumbUrl"))
                .put("fields", new JSONArray().put(new JSONObject()
                        .put("inline", true)
                        .put("name", "key")
                        .put("value", "val")))
                .put("footer", new JSONObject()
                        .put("text", "Footer text")
                        .put("icon", "footerIcon"))
                .toMap();

        populateBuilder();
        Map<String, Object> provided = new JSONObject((builder.build().toJSONString())).toMap();

        Assert.assertTrue("Timestamp is not correctly set in json", provided.containsKey("timestamp")
                && Math.abs(Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse((String) provided.get("timestamp"))).until(Instant.now(), ChronoUnit.MILLIS)) < 50);

        provided.remove("timestamp");
        Assert.assertEquals("Json output is incorrect", expected, provided);

        //check if optional fields are (properly) omitted
        builder.reset();
        builder.setDescription("desc");
        expected = new JSONObject().put("description", "desc").toMap();
        provided = new JSONObject(builder.build().toJSONString()).toMap();
        Assert.assertEquals("Json output is adding extra (non-set) fields", expected, provided);

        builder.reset();
        builder.setTitle(new WebHookEmbed.EmbedTitle("title", null));
        builder.setAuthor(new WebHookEmbed.EmbedAuthor("author", null, null));
        builder.setFooter(new WebHookEmbed.EmbedFooter("footer", null));
        expected = new JSONObject()
                .put("title", "title")
                .put("author", new JSONObject().put("name", "author"))
                .put("footer", new JSONObject().put("text", "footer"))
                .toMap();
        provided = new JSONObject(builder.build().toJSONString()).toMap();
        Assert.assertEquals("Json output is adding extra (non-set) fields", expected, provided);
    }

    private void populateBuilder() {
        builder.setAuthor(new WebHookEmbed.EmbedAuthor("Minn", "https://avatars1.githubusercontent.com/u/18090140?s=460&v=4", Optional.of("https://github.com/InfiniteHorizons-Inc")));
        builder.setColor(0xff00ff);
        builder.setDescription("Hello World!");
        builder.setFooter(new WebHookEmbed.EmbedFooter("Footer text", "https://avatars1.githubusercontent.com/u/18090140?s=460&v=4"));
        builder.setImageUrl("imgUrl");
        builder.setThumbnailUrl("thumbUrl");
        builder.setTitle(new WebHookEmbed.EmbedTitle("Title", "https://github.com/InfiniteHorizons-Inc"));
        builder.setTimestamp(OffsetDateTime.now());
        builder.setTimestamp(Instant.now());
        builder.addField(new WebHookEmbed.EmbedField(true, "key", "val"));
    }
}
