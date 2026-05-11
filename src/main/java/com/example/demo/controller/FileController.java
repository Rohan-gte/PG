package com.example.demo.controller;

import com.example.demo.service.FileStorageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class FileController {

    private final FileStorageService storage;

    public FileController(FileStorageService storage) {
        this.storage = storage;
    }

    @PostMapping(value = "/api/files/upload", consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> upload(@RequestPart("file") MultipartFile file,
                                      @RequestParam(value = "type", defaultValue = "misc") String type) {
        String path = storage.save(file, type);
        Map<String, String> resp = new LinkedHashMap<>();
        resp.put("path", path);
        return resp;
    }

    @PostMapping(value = "/api/public/files/upload", consumes = "multipart/form-data")
    public Map<String, String> publicUpload(@RequestPart("file") MultipartFile file) {
        String path = storage.save(file, "id-proof");
        Map<String, String> resp = new LinkedHashMap<>();
        resp.put("path", path);
        return resp;
    }
}
