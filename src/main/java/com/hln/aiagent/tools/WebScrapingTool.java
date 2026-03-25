package com.hln.aiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * 网页抓取工具
 */
public class WebScrapingTool {

    @Tool(description = "抓取网页内容")
    public String scrapeWebPage(@ToolParam(description = "网址") String url) {
        try {
            Document doc = Jsoup.connect(url).get();    // 获取网页内容
            return doc.html();
        } catch (IOException e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }
}
