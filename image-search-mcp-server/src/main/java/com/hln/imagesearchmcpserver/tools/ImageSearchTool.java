package com.hln.imagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 图片搜索工具
 */
@Component
public class ImageSearchTool {

    private static final String BASE_URL = "https://api.pexels.com/v1/search";
    private static final String apiKey = "xxx";

    /**
     * 根据关键词搜索图片，返回逗号分隔的图片链接
     *
     * @param query 搜索关键词（例如：风景、猫、狗、城市等）
     * @return 逗号分隔的图片链接字符串
     */
    @Tool(description = "根据关键词搜索Pexels图片库，返回逗号分隔的图片链接")
    public String searchImages(
            @ToolParam(description = "搜索关键词，例如：风景、猫、狗、城市、自然") String query) {

        try {
            String responseBody = HttpRequest.get(StrUtil.format("{}?query={}&per_page=5", BASE_URL, query))
                    .header("Authorization", apiKey)
                    .timeout(10000)
                    .execute()
                    .body();

            return JSONUtil.parseObj(responseBody)
                    .getJSONArray("photos")
                    .stream()
                    .map(obj -> (JSONObject) obj)
                    .map(photo -> photo.getJSONObject("src"))
                    .map(src -> src.getStr("large2x"))
                    .filter(StrUtil::isNotBlank)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("未找到相关图片");
        } catch (Exception e) {
            return "搜索失败：" + e.getMessage();
        }
    }
}
