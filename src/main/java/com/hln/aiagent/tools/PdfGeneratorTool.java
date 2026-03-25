package com.hln.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import static com.hln.aiagent.costant.FileConstant.BASE_FILE_PATH;

@Slf4j
public class PdfGeneratorTool {
    public static final String FILE_PATH = BASE_FILE_PATH + "pdf";


    /**
     * 生成简单 PDF（仅文本）
     */
    @Tool(name = "generate_pdf", description = "生成包含文本内容的PDF文件")
    public String generateSimplePdf(
            @ToolParam(description = "文件名") String fileName,
            @ToolParam(description = "PDF内容文本") String content,
            @ToolParam(description = "标题（可选）", required = false) String title) {
        String outputPath = FILE_PATH + "/" + fileName;

        try {
            // 确保目录存在
            FileUtil.mkParentDirs(outputPath);

            // 创建 PDF 写入器
            PdfWriter writer = new PdfWriter(outputPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 添加标题
            if (title != null && !title.isEmpty()) {
                Paragraph titlePara = new Paragraph(title)
                        .setFontSize(18);
                document.add(titlePara);
                document.add(new Paragraph(" ")); // 空行
            }

            // 添加内容
            Paragraph contentPara = new Paragraph(content)
                    .setFontSize(12);
            document.add(contentPara);

            // 关闭文档
            document.close();
            return "PDF 生成成功: {}" + outputPath;

        } catch (Exception e) {
            return "PDF 生成失败" + e.getMessage();
        }
    }
}
