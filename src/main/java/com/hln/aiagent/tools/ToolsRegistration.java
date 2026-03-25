package com.hln.aiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具注册类
 */
@Configuration
public class ToolsRegistration {

    @Bean
    public ToolCallback[] callingTools(){
        FileOperationTool fileOperationTool = new FileOperationTool();
        PdfGeneratorTool pdfGeneratorTool = new PdfGeneratorTool();
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        BaiduWebSearchTool baiduWebSearchTool = new BaiduWebSearchTool();
        return ToolCallbacks.from(fileOperationTool, pdfGeneratorTool, webScrapingTool, terminalOperationTool, resourceDownloadTool, baiduWebSearchTool);
    }
}
