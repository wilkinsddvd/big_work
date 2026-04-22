package com.uxsino.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 帮助文档下载（通过 Spring MVC 返回 classpath 中的 PDF，浏览器以附件形式保存）。
 * <p>
 * 安全说明：本接口未列入 Shiro 的 anon 列表，走全局默认的 auth 校验，
 * 请求头需携带与其它业务接口一致的 {@code token}，仅登录用户可下载。
 * </p>
 */
@RestController
@RequestMapping("/api/help")
public class HelpDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(HelpDocumentController.class);

    /**
     * PDF 在工程中的位置：{@code src/main/resources/docs/}，打包后位于 classpath 根路径下的 {@code docs/}。
     * 更新文档时替换该文件并重新打包即可随应用发布。
     */
    private static final String CLASSPATH_PDF = "docs/datamanager使用.pdf";

    /**
     * 供下载时使用的文件名（可与磁盘上的资源名一致，便于用户识别）。
     */
    private static final String DOWNLOAD_FILENAME = "datamanager使用.pdf";

    /**
     * 下载帮助文档（PDF）。
     * <p>
     * 设计要点：<br>
     * 1. 使用 GET：下载链接语义清晰，且可被浏览器/前端直接发起请求。<br>
     * 2. {@link MediaType#APPLICATION_PDF}：告诉客户端正文为 PDF，便于预览类插件处理。<br>
     * 3. {@code Content-Disposition: attachment}：提示浏览器「保存为文件」而非页内打开（如需内嵌预览可改为 inline）。<br>
     * 4. {@code filename*}（RFC 5987）：对中文等非 ASCII 文件名进行 UTF-8 百分号编码，避免乱码。<br>
     * 5. 返回 {@link org.springframework.core.io.Resource}：由 Spring 负责流式写出，避免一次性读入大文件到内存。
     * </p>
     *
     * @return 200 + PDF 流；资源缺失时 404
     */
    @GetMapping("/manual")
    public ResponseEntity<Resource> downloadManual() throws UnsupportedEncodingException {
        ClassPathResource pdf = new ClassPathResource(CLASSPATH_PDF);
        if (!pdf.exists()) {
            logger.warn("帮助文档不存在于 classpath: {}", CLASSPATH_PDF);
            return ResponseEntity.notFound().build();
        }

        // 同时提供 filename 与 filename*：兼顾旧浏览器与 RFC 5987 标准
        // Java 8 的 URLEncoder 仅支持 encode(String, String)，需传字符集名；Java 10+ 才有 Charset 重载
        String rfc5987 = URLEncoder.encode(DOWNLOAD_FILENAME, StandardCharsets.UTF_8.name()).replace("+", "%20");
        String contentDisposition =
            "attachment; filename=\"" + DOWNLOAD_FILENAME + "\"; filename*=UTF-8''" + rfc5987;

        Resource body = pdf;
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
            .body(body);
    }
}
