package com.reshigo.service.chats;

import com.reshigo.dao.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope("prototype")
public class ChatsCheckerService implements Runnable {
    @Autowired
    private ChatsService chatsService;

    private Long id;

    @Override
    public void run() {
        chatsService.notifyParticipantsIfNoActivity(id);
    }

    public void setId(Long id) {
        this.id = id;
    }
}
