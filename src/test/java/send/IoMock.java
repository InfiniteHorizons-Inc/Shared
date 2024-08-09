package send;

import com.infinitehorizons.SharedWebHook;
import com.infinitehorizons.components.WebHookComponent;
import com.infinitehorizons.components.WebHookEmbedComponent;
import com.infinitehorizons.components.WebHookMessageComponent;
import com.infinitehorizons.constants.SharedConstants;
import com.infinitehorizons.models.send.WebhookMessage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class IoMock {

    @Captor
    private ArgumentCaptor<Request> requestCaptor;

    @Mock
    private OkHttpClient httpClient;

    private SharedWebHook client;

    private AutoCloseable mocks;

    @Before
    public void init() {
        mocks = MockitoAnnotations.openMocks(this);
        when(httpClient.newCall(any())).thenReturn(null);   //will make WebhookClient code throw NPE internally, which we don't care about
        client = new WebHookComponent(1234, "token").waitForMessage(false).httpClient(httpClient).build();
    }

    @After
    public void cleanup() throws Exception {
        client.close();
        mocks.close();
    }

    @Test
    public void testUrl() {
        client.send("Hello World");

        verify(httpClient, timeout(1000).only()).newCall(requestCaptor.capture());
        Request req = requestCaptor.getValue();
        Assert.assertEquals("POST", req.method());
        String expectedUrl = String.format("https://discord.com/api/v%d/webhooks/%d/%s", SharedConstants.DISCORD_API_VERSION, 1234, "token");
        Assert.assertEquals(expectedUrl, req.url().toString());
    }

    @Test
    public void messageBodyUsed() {
        //implicitly checks JSON sent due to JSON (requester) being checked in MessageTest test class
        RequestBody body = new WebHookMessageComponent()
                .setContent("CONTENT!")
                .setUsername("MrWebhook")
                .setAvatarUrl("linkToImage")
                .setTTS(true)
                .addEmbeds(new WebHookEmbedComponent().setDescription("embed").build())
                .build().getBody();

        WebhookMessage mock = mock(WebhookMessage.class);
        when(mock.getBody()).thenReturn(body);

        client.send(mock);

        verify(httpClient, timeout(1000).only()).newCall(requestCaptor.capture());
        Request req = requestCaptor.getValue();
        Assert.assertSame(body, req.body());
    }
}