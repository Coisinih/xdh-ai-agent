package com.hln.aiagent.controller;


import com.hln.aiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AIConreoller {

    @Resource
    private LoveApp loveApp;

    /**
     * 同步调用
     */
    @GetMapping("/chat/sync")
    public String doChat(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    /**
     * 流式调用
     */
    @GetMapping("/chat/sse")
    public Flux<String> doChatWithStream(String message, String chatId) {
        return loveApp.doChatWithSse(message, chatId);
    }

    /**
     * 流式调用
     */
    @GetMapping(value = "/chat/server_sent_event", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> doChatWithServerSentEvent(String message, String chatId) {
        return loveApp.doChatWithSse(message, chatId).map(chunk -> ServerSentEvent.builder(chunk).build());
    }

    /**
     * 流式调用
     */
    @GetMapping("/chat/sse_emitter")
    public SseEmitter doChatWithSseEmitter(String message, String chatId) {
        SseEmitter emitter = new SseEmitter(180000L);   // 设置三分钟超时时间
        loveApp.doChatWithSse(message, chatId)
                .subscribe(
                        // 处理每条信息
                        chunk -> {
                            try {
                                emitter.send(chunk);    // 发送信息到前端
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError, // 处理错误
                        emitter::complete); // 处理完成
        return emitter;
    }
}
