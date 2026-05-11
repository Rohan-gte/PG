package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Uploads uploads = new Uploads();
    private Cors cors = new Cors();

    public Uploads getUploads() { return uploads; }
    public void setUploads(Uploads uploads) { this.uploads = uploads; }
    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }

    public static class Uploads {
        private String dir = "./uploads";
        public String getDir() { return dir; }
        public void setDir(String dir) { this.dir = dir; }
    }

    public static class Cors {
        private String allowedOrigins = "*";
        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }
}
