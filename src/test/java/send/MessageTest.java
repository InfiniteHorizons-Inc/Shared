package send;

import com.infinitehorizons.components.WebHookEmbedComponent;
import com.infinitehorizons.components.WebHookMessageComponent;
import com.infinitehorizons.models.send.WebHookEmbed;
import com.infinitehorizons.models.send.WebhookMessage;
import com.infinitehorizons.utils.IoUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import root.IoTestUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Answers.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageTest {
    private WebHookMessageComponent builder;

    @Before
    public void setupBuilder() {
        builder = new WebHookMessageComponent();
    }

    @Test
    public void setAndReset() {
        //checking isEmpty and reset of those fields
        Assert.assertTrue("Builder should be empty at start", builder.isEmpty());

        builder.setContent("CONTENT!");
        Assert.assertFalse("Setting content doesn't change isEmpty to false", builder.isEmpty());
        builder.reset();
        Assert.assertTrue("Reset doesn't reset content", builder.isEmpty());

        builder.addEmbeds(new WebHookEmbedComponent().setDescription("test").build());
        Assert.assertFalse("Adding embed doesn't change isEmpty to false", builder.isEmpty());
        builder.reset();
        Assert.assertTrue("Reset doesn't reset embed(s)", builder.isEmpty());

        Assert.assertEquals("File count of empty builder mismatches", 0, builder.getFileAmount());
        builder.addFile("notARealFile", new byte[0]);
        Assert.assertEquals("File count of builder mismatches", 1, builder.getFileAmount());
        Assert.assertFalse("Adding file doesn't change isEmpty to false", builder.isEmpty());
        builder.reset();
        Assert.assertEquals("File count of empty builder mismatches", 0, builder.getFileAmount());
        Assert.assertTrue("Reset doesn't reset file(s)", builder.isEmpty());

        //checking remaining setters + reset on those
        builder.setUsername("NotAWebhook");
        builder.setAvatarUrl("avatarUrl");
        builder.setTTS(true);
        Assert.assertTrue("Some extra field set isEmpty to false", builder.isEmpty());
        builder.setContent("dummy"); //needed for building
        WebhookMessage msg = builder.build();
        Assert.assertEquals("Username mismatches", "NotAWebhook", msg.getUsername());
        Assert.assertEquals("AvatarUrl mismatches", "avatarUrl", msg.getAvatarUrl());
        Assert.assertTrue("TTS mismatches", msg.isTTS());

        builder.reset();
        builder.setContent("dummy"); //needed for building
        msg = builder.build();
        Assert.assertNull("Username not reset by reset()", msg.getUsername());
        Assert.assertNull("AvatarUrl not reset by reset()", msg.getAvatarUrl());
        Assert.assertFalse("TTS not reset by reset()", msg.isTTS());
    }

    @Test
    public void messageBuilds() {
        builder.setContent("Hello World");
        builder.setUsername("Minn");
        builder.build().getBody();
    }

    @Test
    public void buildMessageWithEmbed() {
        List<WebHookEmbed> embedList = Arrays.asList(
                new WebHookEmbedComponent()
                        .setDescription("Hello World")
                        .build(),
                new WebHookEmbedComponent()
                        .setDescription("World")
                        .build()
        );
        builder.addEmbeds(embedList.getFirst());
        builder.addEmbeds(embedList.subList(1, 2));
        WebhookMessage message = builder.build();
        for (int i = 0; i < 2; i++) {
            Assert.assertEquals(embedList.get(i), message.getEmbeds().get(i));
        }
    }

    @Test
    public void buildMessageWithFiles() throws IOException {
        File tmp = File.createTempFile("message-test", "cat.png");
        builder.addFile(tmp);
        builder.addFile("dog.png", new FileInputStream(tmp));
        builder.addFile("bird.png", IoUtils.readAllBytes(new FileInputStream(tmp)));
        tmp.delete();
        WebhookMessage message = builder.build();
        Assert.assertNotNull(message.getAttachments());
        Assert.assertEquals(3, message.getAttachments().length);
        Assert.assertEquals(tmp.getName(), message.getAttachments()[0].getName());
        Assert.assertEquals("dog.png", message.getAttachments()[1].getName());
        Assert.assertEquals("bird.png", message.getAttachments()[2].getName());
    }

    @Test
    public void buildMessageWithDataMessage() {
        MessageEmbed jdaEmbed = new EmbedBuilder()
                .setTitle("myEmbed")
                .build();

        MessageCreateData jdaMessage = new MessageCreateBuilder()
                .setTTS(true)
                .setContent("myContent")
                .setEmbeds(jdaEmbed)
                .build();

        WebhookMessage webhookMessage = WebHookMessageComponent.fromJDA(jdaMessage).build();
        List<WebHookEmbed> webhookEmbeds = webhookMessage.getEmbeds();

        Assert.assertEquals(webhookEmbeds.size(), 1);

        WebHookEmbed webhookEmbed = webhookEmbeds.getFirst();

        Assert.assertTrue(webhookMessage.isTTS());
        Assert.assertEquals(webhookMessage.getContent(), "myContent");
        Assert.assertEquals(webhookEmbed.getTitle().getText(), "myEmbed");
    }

    @Test
    public void buildMessageWithReceivedMessage() {
        MessageEmbed jdaEmbed = new EmbedBuilder()
                .setTitle("myEmbed")
                .build();

        Message jdaMessage = mock(ReceivedMessage.class);
        Mentions mentions = mock(Mentions.class, RETURNS_DEFAULTS);
        when(jdaMessage.isTTS()).thenReturn(true);
        when(jdaMessage.getContentRaw()).thenReturn("myContent");
        when(jdaMessage.getEmbeds()).thenReturn(List.of(jdaEmbed));
        when(jdaMessage.getMentions()).thenReturn(mentions);
        when(jdaMessage.getType()).thenReturn(MessageType.DEFAULT);

        WebhookMessage webhookMessage = WebHookMessageComponent.fromJDA(jdaMessage).build();
        List<WebHookEmbed> webhookEmbeds = webhookMessage.getEmbeds();

        Assert.assertEquals(webhookEmbeds.size(), 1);

        WebHookEmbed webhookEmbed = webhookEmbeds.getFirst();

        Assert.assertTrue(true);
        Assert.assertTrue(webhookMessage.isTTS());
        Assert.assertEquals(webhookMessage.getContent(), "myContent");
        Assert.assertEquals(webhookEmbed.getTitle().getText(), "myEmbed");
    }

    @Test
    public void factoryEmbeds() {
        WebHookEmbed embed1 = new WebHookEmbedComponent()
                .setDescription("Hello").build();
        WebHookEmbed embed2 = new WebHookEmbedComponent()
                .setDescription("World").build();
        WebhookMessage.embeds(embed1, embed2).getBody();
        WebhookMessage.embeds(Arrays.asList(embed1, embed2)).getBody();
    }

    @Test
    public void factoryFiles() throws IOException {
        File tmp = File.createTempFile("message-test", "cat.png");
        WebhookMessage.files(
                "cat.png", tmp,
                "dog.png", new FileInputStream(tmp),
                "bird.png", IoUtils.readAllBytes(new FileInputStream(tmp))).getBody();
        Map<String, Object> files = new HashMap<>();
        files.put("cat.png", tmp);
        files.put("dog.png", new FileInputStream(tmp));
        files.put("bird.png", IoUtils.readAllBytes(new FileInputStream(tmp)));
        WebhookMessage.files(files).getBody();
        tmp.delete();
    }

    @Test
    public void buildEmptyMessage() {
        Assert.assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    public void checkJSONNonFile() throws IOException {
        JSONObject allowedMentions = new JSONObject()
                .put("parse", new JSONArray()
                        .put("users")
                        .put("roles")
                        .put("everyone"));

        Map<String, Object> expected = new JSONObject()
                .put("content", "CONTENT!")
                .put("username", "MrWebhook")
                .put("avatar_url", "linkToImage")
                .put("tts", true)
                .put("embeds", new JSONArray().put(new JSONObject().put("description", "embed")))
                .put("allowed_mentions", allowedMentions)
                .put("flags", 0)
                .toMap();

        WebhookMessage msg = builder
                .setContent("CONTENT!")
                .setUsername("MrWebhook")
                .setAvatarUrl("linkToImage")
                .setTTS(true)
                .addEmbeds(new WebHookEmbedComponent().setDescription("embed").build())
                .build();
        Assert.assertFalse("Message should not be of type file", msg.isFile());
        RequestBody body = msg.getBody();
        Assert.assertEquals("Request type mismatch", IoUtils.JSON, body.contentType());

        String bodyContent = IoTestUtil.readRequestBody(body);

        Map<String, Object> provided = new JSONObject(bodyContent).toMap();

        Assert.assertEquals("Json output is incorrect", expected, provided);

        // This is no longer expected behavior, we intentionally include optional fields due to the PATCH endpoint behavior
//        //check if optional fields are omitted if not used (tts is always sent)
//        expected = new JSONObject()
//                .put("content", "...")
//                .put("tts", false)
//                .put("allowed_mentions", allowedMentions)
//                .toMap();
//
//        msg = builder
//                .reset()
//                .setContent("...")
//                .build();
//
//        bodyContent = IOTestUtil.readRequestBody(msg.getBody());
//        provided = new JSONObject(bodyContent).toMap();
//
//        Assert.assertEquals("Json output has additional fields", expected, provided);
    }

    @Test
    public void checkMultipart() throws IOException {
        JSONObject allowedMentions = new JSONObject()
                .put("parse", new JSONArray()
                        .put("users")
                        .put("roles")
                        .put("everyone"));

        String fileContent = "Hello World!\nNext line...\r\nAnother line";
        WebhookMessage msg = builder
                .setContent("CONTENT!")
                .addFile("myFile.txt", fileContent.getBytes(StandardCharsets.UTF_8))
                .build();
        Assert.assertTrue("Message should be of type file", msg.isFile());

        RequestBody body = msg.getBody();
        Assert.assertTrue("Request type mismatch", IoTestUtil.isMultiPart(body));

        Map<String, Object> multiPart = IoTestUtil.parseMultipart(body);

        Assert.assertTrue("Multipart doesn't contain payload json", multiPart.containsKey("payload_json"));
        Assert.assertTrue("Multipart json is not of correct type", multiPart.get("payload_json") instanceof String);
        Assert.assertEquals("Multipart json mismatches",
                new JSONObject()
                        .put("allowed_mentions", allowedMentions)
                        .put("content", "CONTENT!")
                        .put("embeds", new JSONArray())
                        .put("tts", false)
                        .put("flags", 0)
                        .toMap(),
                new JSONObject((String) multiPart.get("payload_json")).toMap()
        );

        Assert.assertTrue("Multipart doesn't contain file", multiPart.containsKey("file0"));
        Assert.assertTrue("Multipart file is not of correct type", multiPart.get("file0") instanceof IoTestUtil.MultiPartFile);
        Assert.assertEquals("Multipart file mismatches",
                fileContent,
                new String(((IoTestUtil.MultiPartFile) multiPart.get("file0")).content, StandardCharsets.UTF_8)
        );
    }

}
