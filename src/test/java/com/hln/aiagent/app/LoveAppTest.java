package com.hln.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {
    @Resource
    private LoveApp loveApp;

    @Test
    void testDoChat() {
        // 第一轮对话
        String chatId = UUID.randomUUID().toString();
        String anwser = loveApp.doChat("你好，我是咸蛋黄！", chatId);
        // 第二轮对话
        anwser = loveApp.doChat("我想让我的另一半（林小狗）更爱我", chatId);
        Assertions.assertNotNull(anwser);
        // 第三轮对话
        anwser = loveApp.doChat("你还记得我叫什么吗？", chatId);
        Assertions.assertNotNull(anwser);
    }

    @Test
    void doChat1() {
        // 第一轮对话
        String chatId = UUID.randomUUID().toString();
        loveApp.doChat("你好，我是咸蛋黄！", chatId);
    }

    @Test
    void doChatWhitReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "我想让另一半更爱我，但是我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWhitReport(message, chatId);
        assertNotNull(loveReport);
    }
}