package com.hln.aiagent.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;

/**
 * 自定义 百度搜索工具
 */
@Slf4j
public class BaiduWebSearchTool {
    private static final String API_URL = "https://qianfan.baidubce.com/v2/ai_search/web_search";

    @Value("${spring.ai.qianfan.api-key}")
    private String API_KEY;

    /**
     * 执行联网搜索
     *
     * @param query 搜索关键词
     * @return 搜索结果 JSON 字符串
     */
    @Tool(name = "web_search", description = "百度联网搜索，获取实时信息")
    public String search(@ToolParam(description = "搜索关键词") String query) {
        try {
            // 构建请求体
            JSONObject requestBody = new JSONObject();

            // 构建 messages
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.set("content", query);
            message.set("role", "user");
            messages.add(message);
            requestBody.set("messages", messages);

            // 设置搜索配置
            requestBody.set("edition", "standard");
            requestBody.set("search_source", "baidu_search_v2");

            // 设置返回结果类型和数量
            JSONArray resourceTypeFilter = new JSONArray();
            JSONObject webFilter = new JSONObject();
            webFilter.set("type", "web");    // 网页搜索
            webFilter.set("top_k", 3);    // 返回前3个结果
            resourceTypeFilter.add(webFilter);
            requestBody.set("resource_type_filter", resourceTypeFilter);

            log.info("执行百度搜索: {}", query);

            // 发送请求
            HttpResponse response = HttpRequest.post(API_URL)
                    .header("X-Appbuilder-Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(30000)
                    .execute();

            if (response.isOk()) {
                String result = response.body();
                log.info("搜索成功，查询词: {}", query);
                return result;
            } else {
                return String.format("{\"error\": true, \"message\": \"搜索失败，状态码: %d\"}",
                        response.getStatus());
            }

        } catch (Exception e) {
            log.error("搜索异常", e);
            return String.format("{\"error\": true, \"message\": \"搜索异常: %s\"}", e.getMessage());
        }
    }
}
