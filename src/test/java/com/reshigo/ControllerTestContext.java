package com.reshigo;

import com.reshigo.controller.UserOrdersController;
import com.reshigo.dao.FileUtil;
import com.reshigo.dao.OrderDao;
import com.reshigo.dao.PictureDao;
import com.reshigo.dao.UserDao;
import com.reshigo.dao.permanent.ThemesDao;
import com.reshigo.notifications.NotificationMessagingTemplate;
import com.reshigo.service.UserOrdersService;
import com.reshigo.service.scheduled.ScheduledTasks;
import org.hibernate.SessionFactory;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Configuration
public class ControllerTestContext {

    @Bean @Qualifier("FCMNotificationMessagingTemplate")
    public NotificationMessagingTemplate notificationsMessagingTemplate() {
        return Mockito.mock(NotificationMessagingTemplate.class);
    }

    @Bean @Qualifier("websocketMessagingTemplate")
    public NotificationMessagingTemplate websocketMessagingTemplate() {
        return Mockito.mock(NotificationMessagingTemplate.class);
    }

    @Bean
    public UserOrdersService userOrdersService() {
        return Mockito.mock(UserOrdersService.class);
    }

    @Bean @Autowired
    public UserOrdersController userOrdersController(UserOrdersService userOrdersService) {
        return new UserOrdersController(userOrdersService);
    }

    @Bean
    public UserDao userDao() {
        return Mockito.mock(UserDao.class);
    }

    @Bean
    public OrderDao orderDao() {
        return Mockito.mock(OrderDao.class);
    }

    @Bean
    public PictureDao pictureDao() {
        return Mockito.mock(PictureDao.class);
    }

    @Bean
    public ThemesDao themesDao() {
        return Mockito.mock(ThemesDao.class);
    }

    @Bean
    public FileUtil fileUtil() {
        return Mockito.mock(FileUtil.class);
    }

    @Bean
    public Properties commonProperties() throws IOException {
        Properties properties = new Properties();

        InputStream is = getClass().getClassLoader().getResourceAsStream("common.properties");
        properties.load(is);

        return properties;
    }

    @Bean
    public ScheduledTasks scheduledTasks() {
        return Mockito.mock(ScheduledTasks.class);
    }

    @Bean
    public SessionFactory sessionFactory() {
        return Mockito.mock(SessionFactory.class);
    }

    @Bean
    public ConcurrentHashMap<String, WebSocketSession> sessions() {
        return Mockito.mock(ConcurrentHashMap.class);
    }

    @Bean
    public ConcurrentSkipListSet<String> solvers() {
        return Mockito.mock(ConcurrentSkipListSet.class);
    }
}
