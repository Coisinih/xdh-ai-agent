package com.hln.aiagent.memory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 基于文件持久化的对话记忆
 */
public class FileBasedChatMemory implements ChatMemory {
    private final String BASE_DIR;
    private static final Kryo kryo = new Kryo();

    static {
        // 关闭注册
        kryo.setRegistrationRequired(false);
        // 设置实例化策略为标准的实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public FileBasedChatMemory(String baseDir) {
        this.BASE_DIR = baseDir;
        File file = new File(baseDir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 追加单条会话记录
     */
    @Override
    public void add(String conversationId, Message message) {
        // 获取之前的会话记录
        List<Message> conversationMessage = getOrCreateConversation(conversationId);
        // 添加新的会话记录
        conversationMessage.add(message);
        // 重新保存会话记录
        saveConversation(conversationId, conversationMessage);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        // 获取之前的会话记录
        List<Message> conversationMessage = getOrCreateConversation(conversationId);
        // 添加新的会话记录
        conversationMessage.addAll(messages);
        // 重新保存会话记录
        saveConversation(conversationId, conversationMessage);

    }

    /**
     * 获取倒数 lastN 条会话记录
     */
    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> messageList = getOrCreateConversation(conversationId);
        return messageList.stream()
                .skip(Math.max(0, messageList.size() - lastN)) // 跳过 messageList.size() - lastN 条
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        File conversationFile = getConversationFile(conversationId);
        if (conversationFile.exists()) {
            conversationFile.delete();
        }
    }

    /**
     * 获取历史会话记录
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        File conversationFile = getConversationFile(conversationId);
        List<Message> messegeList = new ArrayList<>();
        if (conversationFile.exists()) {
            try (Input input = new Input(new FileInputStream(conversationFile))) {
                messegeList = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messegeList;
    }

    /**
     * 保存对话记忆
     */
    private void saveConversation(String conversationId, List<Message> messages) {
        try (Output output = new Output(new FileOutputStream(getConversationFile(conversationId)))) {
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前会话的持久化文件
     */
    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }
}
