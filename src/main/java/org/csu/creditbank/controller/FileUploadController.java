package org.csu.creditbank.controller;

import org.csu.creditbank.common.ApiResult;
import org.csu.creditbank.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx");

    private final Path uploadRoot;

    public FileUploadController(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @PostMapping("/proofs")
    public ApiResult<Map<String, String>> uploadProof(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的证明文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("证明文件不能超过10MB");
        }

        String original = file.getOriginalFilename() == null ? "proof" : file.getOriginalFilename();
        String ext = extensionOf(original);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException("仅支持图片、PDF、Word 文件");
        }

        try {
            Path proofDir = uploadRoot.resolve("proofs");
            Files.createDirectories(proofDir);
            String fileName = UUID.randomUUID() + "." + ext;
            Path target = proofDir.resolve(fileName).normalize();
            if (!target.startsWith(proofDir)) {
                throw new BusinessException("文件名非法");
            }
            file.transferTo(target);
            String url = "/uploads/proofs/" + fileName;
            return ApiResult.ok(Map.of("url", url, "fileName", original));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("证明文件上传失败");
        }
    }

    private String extensionOf(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) return "";
        return fileName.substring(idx + 1).toLowerCase(Locale.ROOT);
    }
}
