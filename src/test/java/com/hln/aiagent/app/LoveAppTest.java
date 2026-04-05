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

    @Test
    void doChat() {
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithRag("我已经结婚了，但是婚后关系不太亲密，我应该怎么办？", chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doCallingTools() {
//        // 测试网页抓取：恋爱案例分析
//        testMessage("最近和对象吵架了，看看恋爱社区网站（https://bbs.hupu.com/love-19）的其他情侣是怎么解决矛盾的？");
//
//        // 测试资源下载：图片下载,联网搜索
//        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");
//
//        // 测试终端操作：执行代码
//        testMessage("执行 Python3.py 脚本来生成数据分析报告");
//
//        // 测试文件操作：保存用户档案
//        testMessage("我是已婚，今年的28岁，保存我的恋爱档案为文件");
//
//        // 测试 PDF 生成
        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doCallingTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        String message = "我的另一半居住在上海静安区，请帮我找到5公里内合适的约会地点。";
        String answer = loveApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }
}