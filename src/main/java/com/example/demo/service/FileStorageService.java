package com.example.demo.service;

import com.example.demo.config.AppProperties;
import com.example.demo.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "pdf"
    );

    private final AppProperties appProps;
    private Path rootDir;

    public FileStorageService(AppProperties appProps) {
        this.appProps = appProps;
    }

    @PostConstruct
    public void init() throws IOException {
        rootDir = Paths.get(appProps.getUploads().getDir()).toAbsolutePath().normalize();
        Files.createDirectories(rootDir);
    }

    public String save(MultipartFile file, String subdir) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Empty file");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot > 0) ext = original.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BadRequestException("File type not allowed. Allowed: " + ALLOWED_EXT);
        }
        try {
            String safeSub = (subdir == null || subdir.isBlank()) ? "misc" : subdir.replaceAll("[^a-zA-Z0-9-_]", "");
            Path dir = rootDir.resolve(safeSub);
            Files.createDirectories(dir);
            String fname = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(fname);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + safeSub + "/" + fname;
        } catch (IOException ex) {
            throw new BadRequestException("Failed to save file: " + ex.getMessage());
        }
    }
}
