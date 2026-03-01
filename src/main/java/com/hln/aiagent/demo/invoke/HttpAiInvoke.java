package com.hln.aiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * httt 调用大模型
 */
public class HttpAiInvoke {
    public static void main(String[] args) {
        // API密钥，建议从环境变量获取
        String apiKey = TestApiKey.API_KEY;

        // 构建请求体
        JSONObject requestBody = new JSONObject();

        // 设置model
        requestBody.set("model", "qwen-plus");

        // 构建input中的messages
        JSONObject input = new JSONObject();
        JSONArray messages = new JSONArray();

        // system消息
        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", "You are a helpful assistant.");
        messages.add(systemMessage);

        // user消息
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", "你是谁？");
        messages.add(userMessage);

        input.set("messages", messages);
        requestBody.set("input", input);

        // 设置parameters
        JSONObject parameters = new JSONObject();
        parameters.set("result_format", "message");
        requestBody.set("parameters", parameters);

        // 发送POST请求
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        try (HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(requestBody))
                .execute()) {

            // 获取响应状态码和内容
            int status = response.getStatus();
            String responseBody = response.body();

            System.out.println("响应状态码: " + status);
            System.out.println("响应内容: " + responseBody);

            // 如果需要解析响应内容
            if (response.isOk()) {
                JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
                // 处理响应数据
                System.out.println("请求成功");
            } else {
                System.out.println("请求失败");
            }
        }
    }
}