package com.hln.aiagent.tools;

import cn.hutool.json.JSONObject;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class TerminalOperationTool {


    /**
     * 执行命令
     */
    @Tool(name = "run_command", description = "Execute a command in the terminal")
    public String runCommand(@ToolParam(description = "Command to execute in the terminal") String command) {

        try {
            // 执行命令,默认当前目录下执行
            Process process = Runtime.getRuntime().exec("cmd.exe /c " + command);

            // 读取输出（Windows中文系统使用GBK编码）
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("GBK")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待执行完成
            process.waitFor();
            return output.toString();

        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.set("success", false);
            error.set("error", e.getMessage());
            return error.toString();
        }
    }
}
