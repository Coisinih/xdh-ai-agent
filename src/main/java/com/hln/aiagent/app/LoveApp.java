package com.hln.aiagent.app;

import com.hln.aiagent.advisor.MyLoggerAdvisor;
import com.hln.aiagent.demo.rag.LoveAppRagCustomAdvisorFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {
    private final ChatClient chatClient;
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    /**
     * 初始化 ChatClient
     *
     * @param dashscopeChatModel 根据名称自动注入
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        // 基于文件的会话记忆
//        ChatMemory chatMemory = new FileBasedChatMemory(System.getProperty("user.dir") + "/tmp/chat_memory");
        // 初始化基于内存的会话记忆
        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
//                        new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI 基础对话，支持多轮对话记忆
     *
     * @param message 用户prompt
     * @param chatId  会话id 用于隔绝会话
     * @return 大模型输出的结果
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)   // 会话id，用来隔绝会话
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))  //  每次会话关联上下文的数量，经过验证，这里的n是指最新的n条（不包含当前条）
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions) {
    }

    /**
     * 恋爱报告生产
     */
    public LoveReport doChatWhitReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)   // 会话id，用来隔绝会话
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1))  //  每次会话关联上下文的数量，经过验证，这里的n是指最新的n条（不包含当前条）
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    @Resource
    private VectorStore loveAppVectorStore;
    @Resource
    private Advisor loveAppRagCloudAdvisor;
    @Resource
    private VectorStore pgVectorVectorStore;

    /**
     * 和 rag 知识库进行会话
     */
    public String doChatWithRag(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)   // 会话id，用来隔绝会话
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))  //  每次会话关联上下文的数量，经过验证，这里的n是指最新的n条（不包含当前条）
//                .advisors(new MyLoggerAdvisor()    // 输出的日志，方便跟踪
//                        ,new QuestionAnswerAdvisor(loveAppVectorStore) // 应用 RAG 知识库问答
//                        loveAppRagCloudAdvisor // 应用 RAG 检索增强服务 （基于云知识库）
//                        new QuestionAnswerAdvisor(pgVectorVectorStore)  // 应用 RAG 检索增强服务 （基于 PgVector 向量检索知识库）
//                )
                .advisors(LoveAppRagCustomAdvisorFactory.createAdvisor(loveAppVectorStore, "单身"))
                .advisors()
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallback[] callingTools;

    /**
     * 调用工具
     */
    public String doCallingTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)   // 会话id，用来隔绝会话
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1))  //  每次会话关联上下文的数量，经过验证，这里的n是指最新的n条（不包含当前条）
                .tools(callingTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallbackProvider toolCallbackProvider;
    /**
     * 调用 MCP
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)   // 会话id，用来隔绝会话
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))  //  每次会话关联上下文的数量，经过验证，这里的n是指最新的n条（不包含当前条）
                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI 基础对话，支持多轮对话记忆，支持 sse 流式返回
     *
     * @param message 用户prompt
     * @param chatId  会话id 用于隔绝会话
     * @return 大模型输出的结果
     */
    public Flux<String> doChatWithSse(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)   // 会话id，用来隔绝会话
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))  //  每次会话关联上下文的数量，经过验证，这里的n是指最新的n条（不包含当前条）
                .stream().content();
    }
}
