package com.hln.aiagent.tools;

import cn.hutool.http.HttpUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

import static com.hln.aiagent.costant.FileConstant.BASE_FILE_PATH;

/**
 * 资源下载工具
 */
public class ResourceDownloadTool {
    public static final String FILE_PATH = BASE_FILE_PATH + "download";

    @Tool(name = "download", description = "下载文件")
    public String download(@ToolParam(description = "资源URL") String url, @ToolParam(description = "保存文件名") String fileName) {
        String filePath = FILE_PATH + "/" + fileName;

        try {
            HttpUtil.downloadFile(url, new File(filePath));
            return fileName + " 下载成功";
        } catch (Exception e) {
            return "下载失败: " + e.getMessage();
        }
    }
}
