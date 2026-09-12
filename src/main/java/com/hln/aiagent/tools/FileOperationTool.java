package com.hln.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import static com.hln.aiagent.costant.FileConstant.BASE_FILE_PATH;

/**
 * 自定义 文件操作工具
 */
@Slf4j
public class FileOperationTool {
    public static final String FILE_PATH = BASE_FILE_PATH + "file";

    /**
     * 读取文件
     */
    @Tool(description = "读取文件内容")
    public String readFile(@ToolParam(description = "文件路径") String fileName) {
        String filePath = FILE_PATH + "/" + fileName;
        try {
            String content = FileUtil.readUtf8String(filePath);
            log.info("读取文件成功: {}", filePath);
            return content;
        } catch (Exception e) {
            log.error("读取文件失败: {}", filePath, e);
            return "读取文件失败: " + e.getMessage();
        }
    }

    /**
     * 写入文件
     */
    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Content to write to the file") String content, @ToolParam(description = "Name of the file to write") String fileName) {
        String filePath = FILE_PATH + "/" + fileName;
        try {
            // 确保父目录存在
            FileUtil.mkParentDirs(filePath);

            // 写入文件
            FileUtil.writeUtf8String(content, filePath);

            log.info("写入文件成功: {}", filePath);

            return String.format("文件%s写入成功！", fileName);
        } catch (Exception e) {
            log.error("写入文件失败: {}", filePath, e);
            return "写入文件失败: " + e.getMessage();
        }
    }

}
