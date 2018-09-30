package com.reshigo.service.chats;

import com.microsoft.azure.storage.StorageException;
import com.reshigo.ServiceTestContext;
import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotFound;
import com.reshigo.model.entity.Message;
import com.reshigo.model.entity.MessageType;
import com.reshigo.service.DBUnitConfig;
import org.dbunit.Assertion;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by dmitry103 on 24/07/16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = {ServiceTestContext.class})
@WebAppConfiguration
public class ChatsServiceTest extends DBUnitConfig {
    @Autowired
    private ChatsService chatsService;

    @Before
    public void setUp() throws Exception {
        //dataSet = new FlatXmlDataSet(Thread.currentThread().getContextClassLoader().getResourceAsStream("com/reshigo/service/chats/chats-dataset.xml"));

        //super.setUp();
    }

    @Test
    public void addMessageTest() throws NotFound, NotAllowed, Exception {
        Message message = new Message();
        MessageType messageType = new MessageType();

        messageType.setId(0L);
        message.setMessageType(messageType);
        message.setData("привет".getBytes());

        chatsService.addMessage(1L, message, "antony");

        IDataSet actual = getConnection().createDataSet();

        assertEquals(actual.getTable("message").getValue(6, "id").toString(), message.getId().toString());
        Assert.assertArrayEquals((byte[]) actual.getTable("message").getValue(6, "data"), message.getData());
    }

    @Test(expected = NotAllowed.class)
    public void addMessageNotAllowed() throws NotFound, NotAllowed, IOException, IllegalAccessException, URISyntaxException, StorageException {
        Message message = new Message();
        MessageType messageType = new MessageType();

        messageType.setId(0L);
        message.setMessageType(messageType);
        message.setData("привет".getBytes());

        chatsService.addMessage(2L, message, "antony");
    }

    @Test
    public void getLastRead() {
        Message message = chatsService.getLastRead(3L, "antony");
        assertEquals(message.getId().intValue(), 2);

        message = chatsService.getLastRead(3L, "dimjavas");
        assertEquals(message.getId().intValue(), 1);
    }

    @Test
    public void setRead() throws Exception, NotFound {
        chatsService.setRead(3L, 4L, "antony");

        IDataSet actual = getConnection().createDataSet();
        IDataSet expected = new FlatXmlDataSet(Thread.currentThread().getContextClassLoader().getResourceAsStream("com/reshigo/service/chats/chats-dataset-setread.xml"));

        Assertion.assertEquals(expected.getTable("message"), actual.getTable("message"));
    }

    @Test
    public void fraudActivityCheckTest() {
        assertTrue(chatsService.checkFraudActivity("вот мой номер телеофна 89261119222"));
        assertTrue(chatsService.checkFraudActivity("89774903984"));
        assertTrue(chatsService.checkFraudActivity("+79152796021"));
        assertTrue(chatsService.checkFraudActivity("https://vk.com/r_kozhevnikov"));
        assertTrue(chatsService.checkFraudActivity("кинь id свой"));
        assertTrue(chatsService.checkFraudActivity("Вк"));
        assertTrue(chatsService.checkFraudActivity("Давай я тут подтвержу, а ты мне на карту 500 скинешь?"));
        assertTrue(chatsService.checkFraudActivity("напиши мне вк"));
        assertTrue(chatsService.checkFraudActivity("5469 4000 2347 7966"));

        assertFalse(chatsService.checkFraudActivity("в номере 1 как делать ?"));
        assertFalse(chatsService.checkFraudActivity("в ответе получается включительно 500 ?"));
    }
}
