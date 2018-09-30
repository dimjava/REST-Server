package com.reshigo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.reshigo.model.entity.*;
import com.reshigo.model.entity.permanent.OrderStatusEnum;
import com.reshigo.notifications.NotificationMessagingTemplate;
import com.reshigo.payment.PaymentVerifier;
import com.reshigo.service.scheduled.ScheduledTasks;
import com.reshigo.service.scheduled.ScheduledTasksImpl;
import com.reshigo.sms.SmsAeroTemplate;
import com.reshigo.sms.SmsTemplate;
import com.twilio.Twilio;
import com.twilio.http.TwilioRestClient;
import com.twilio.type.PhoneNumber;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.SessionFactory;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.socket.WebSocketSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan(value = {"com.reshigo.dao.**", "com.reshigo.service.**"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ScheduledTasksImpl.class)})
@EnableTransactionManagement
public class ServiceTestContext {
    @Bean
    public PhoneNumber phoneNumber() {
        return new PhoneNumber("");
    }

    @Bean
    public TwilioRestClient restClient() {
        Twilio.init("", "");

        return Twilio.getRestClient();
    }

    @Bean
    public Properties commonProperties() throws IOException {
        Properties properties = new Properties();

        InputStream is = getClass().getClassLoader().getResourceAsStream("common.properties");
        properties.load(is);

        return properties;
    }

    @Bean(name = "twilioMessageSender")
    public SmsTemplate twilioMessageSender() {
        return mock(SmsTemplate.class);
    }

    @Bean(name = "smsAero")
    public SmsTemplate smsAero() {
        return mock(SmsAeroTemplate.class);
    }

    @Bean
    public PaymentVerifier paymentVerifier() {
        return new PaymentVerifier() {
            @Override
            public boolean verify(BigDecimal outSum, Long invId, String signatureValue) throws NoSuchAlgorithmException {
                return true;
            }

            @Override
            public String createHtmlPage(Payment payment) throws NoSuchAlgorithmException {
                return "";
            }

            @Override
            public void setKeys(String publicKey, String privateKey, int isTest) {
            }
        };
    }

    @Bean
    public BasicDataSource dataSource() {
        BasicDataSource bs = new BasicDataSource();

        bs.setUrl("jdbc:sqlserver://reshigo.database.windows.net:1433;database=reshigo-release-db_2017-06-08T18-15Z");
        bs.setUsername("dimjava");
        bs.setPassword("Dim12345");

        bs.setDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());

        return bs;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();

        sfb.setDataSource(dataSource);
        sfb.setPackagesToScan("com.reshigo.model.entity.**");

        Properties properties = new Properties();
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServer2008Dialect");

        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        sfb.setHibernateProperties(properties);

        return sfb;
    }

    @Bean
    @Autowired
    public HibernateTransactionManager hibernateTransactionManager(DataSource dataSource, SessionFactory sessionFactory) {
        HibernateTransactionManager tm = new HibernateTransactionManager();
        tm.setDataSource(dataSource);
        tm.setSessionFactory(sessionFactory);

        return tm;
    }

    @Bean(name = "userAntony")
    public User userAntony() {
        User user = new User();
        user.setName("antony");
        user.setEmail("antony@email.ru");
        user.setEnabled(true);
        user.setPassword("some");
        user.setPhone("+79260000000");

        return user;
    }

    @Bean(name = "userDmitry")
    public User userDmitry() {
        User user = new User();
        user.setName("dmitry");
        user.setEmail("dmitry@email.ru");
        user.setEnabled(true);
        user.setPhone("+79260000001");
        user.setPassword("some");

        return user;
    }

    @Bean
    public Order getOrder() {
        Order order = new Order();
        order.setStatus(OrderStatusEnum.DRAFT);
        order.setDate(new Timestamp(new Date(2016, 1, 1).getTime()));
        order.setMaturityDate(new Timestamp(new Date(2025, 1, 1).getTime()));

        return order;
    }

    @Bean
    public Subject subject() {
        Subject s = new Subject();
        s.setId(0L);
        s.setSubject("Other");
        s.setSubjectRU(new byte[] {});
        return s;
    }

    @Bean
    @Autowired
    public Theme theme(Subject subject) {
        Theme th = new Theme();
        th.setSubject(subject);
        th.setId(1L);
        th.setTheme("Other");
        th.setThemeRU(new byte[] {});

        return th;
    }

    @Bean
    public ScheduledTasks scheduledTasks() {
        return Mockito.mock(ScheduledTasks.class);
    }

    @Bean @Qualifier("FCMNotificationMessagingTemplate")
    public NotificationMessagingTemplate notificationsMessagingTemplate() {
        return Mockito.mock(NotificationMessagingTemplate.class);
    }

    @Bean @Qualifier("websocketMessagingTemplate")
    public NotificationMessagingTemplate websocketMessagingTemplate() {
        return Mockito.mock(NotificationMessagingTemplate.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(MapperFeature.USE_ANNOTATIONS, false);

        return om;
    }

    @Bean
    public ThreadPoolTaskScheduler defaultSockJsTaskScheduler() {
        return Mockito.mock(ThreadPoolTaskScheduler.class);
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
