package receive;

import root.IoTestUtil;
import com.infinitehorizons.SharedWebHook;
import com.infinitehorizons.components.WebHookComponent;
import com.infinitehorizons.models.EntityFactory;
import com.infinitehorizons.models.ReadonlyMessage;
import com.infinitehorizons.models.ReadonlyUser;
import okhttp3.OkHttpClient;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReceiveMock {
    @Captor
    private ArgumentCaptor<JSONObject> jsonCaptor;

    @Mock
    private OkHttpClient httpClient;

    @Mock
    private EntityFactory messageFactory;

    private SharedWebHook client;

    private AutoCloseable mocks;

    @Before
    public void init() {
        mocks = MockitoAnnotations.openMocks(this);
        client = new WebHookComponent(1234, "token")
                .waitForMessage(true)
                .httpClient(httpClient)
                .build();
    }

    @After
    public void cleanup() throws Exception {
        if (client != null) {
            client.close();
        }
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void testPassedEntity() throws InterruptedException, ExecutionException, TimeoutException {
        ReadonlyMessage mockMessage = setupFakeResponse(ReceiveMessageTest.getMockMessageJson().toString(), false);
        ReadonlyMessage readMessage = client.send("dummy").get(5, TimeUnit.SECONDS);

        assertNotNull("Returned message is null", readMessage);
        assertSame("Returned message not same as result of EntityFactory.makeMessage", mockMessage, readMessage);
    }

    @Test
    public void testNonGzip() throws InterruptedException, ExecutionException, TimeoutException {
        JSONObject json = ReceiveMessageTest.getMockMessageJson();
        setupFakeResponse(json.toString(), false);
        client.send("dummy").get(5, TimeUnit.SECONDS);

        verify(messageFactory, times(1)).makeMessage(jsonCaptor.capture());
        JSONObject value = jsonCaptor.getValue();
        assertNotNull("Null json passed to EntityFactory", value);
        assertEquals("Json passed to EntityFactory is not 1:1 http response", json.toMap(), value.toMap());
    }

    @Test
    public void testGzip() throws InterruptedException, ExecutionException, TimeoutException {
        JSONObject json = ReceiveMessageTest.getMockMessageJson();
        setupFakeResponse(json.toString(), true);
        client.send("dummy").get(5, TimeUnit.SECONDS);

        verify(messageFactory, times(1)).makeMessage(jsonCaptor.capture());
        JSONObject value = jsonCaptor.getValue();
        assertNotNull("Null json passed to EntityFactory", value);
        assertEquals("Json passed to EntityFactory is not 1:1 http response", json.toMap(), value.toMap());
    }

    private ReadonlyMessage setupFakeResponse(String json, boolean useGzip) {
        when(httpClient.newCall(any())).thenAnswer(info -> IoTestUtil.forgeCall(info.getArgument(0), json, useGzip));
        ReadonlyMessage msg = new ReadonlyMessage(1, 2, false, false, 0,
                new ReadonlyUser(3, (short)4, false, "wh", null),
                "content", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()
        );
        when(messageFactory.makeMessage(jsonCaptor.capture())).thenReturn(msg);
        return msg;
    }
}
