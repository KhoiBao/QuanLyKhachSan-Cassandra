package com.qlkhachsan.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Ket noi Astra DB - NGUON DUY NHAT la config.json trong resources
 * (token, keyspace, secureBundle). application.properties chi la fallback
 * khi config.json khong ghi keyspace/bundle. Khong dung bien moi truong,
 * khong hardcode credential trong code.
 */
@Configuration
public class CassandraConfig {

    @Value("${astra.db.keyspace:khachsan}")
    private String defaultKeyspace;

    @Value("${astra.db.secure-bundle:secure-connect-hotel-management.zip}")
    private String defaultBundle;

    @Bean(destroyMethod = "close")
    public CqlSession cqlSession() throws Exception {

        // Đọc config.json (hỗ trợ cả 2 định dạng: phẳng và lồng trong "astra")
        ObjectMapper mapper = new ObjectMapper();

        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("config.json");

        if (inputStream == null) {
            throw new RuntimeException("Không tìm thấy config.json trong classpath (src/main/resources/config.json)");
        }

        JsonNode root;
        try (inputStream) {
            root = mapper.readTree(inputStream);
        }

        // config.json thực tế là phẳng: {"clientId":..., "secret":..., "token":...}
        // nhưng code cũ đọc config.get("astra").get("token") -> NPE.
        // Hỗ trợ cả 2 dạng để không vỡ khi đổi format.
        JsonNode config = (root.has("astra") && root.get("astra").isObject())
                ? root.get("astra")
                : root;

        String token = textOrNull(config, "token");
        String keyspace = firstNonBlank(
                textOrNull(config, "keyspace"),
                defaultKeyspace
        );
        String secureBundle = firstNonBlank(
                firstNonBlank(textOrNull(config, "secureBundle"), textOrNull(config, "secure-bundle")),
                defaultBundle
        );

        if (token == null || token.isBlank()) {
            throw new RuntimeException(
                    "Thiếu 'token' trong config.json. Cần có dạng "
                            + "{\"token\": \"AstraCS:...\"} hoặc {\"astra\": {\"token\": \"...\"}}"
            );
        }
        if (keyspace == null || keyspace.isBlank()) {
            throw new RuntimeException("Thiếu keyspace. Kiểm tra config.json hoặc astra.db.keyspace trong application.properties");
        }

        // Load Secure Connect Bundle từ classpath (chạy được cả khi đóng jar),
        // copy ra file tạm vì driver yêu cầu Path.
        InputStream bundleStream = getClass()
                .getClassLoader()
                .getResourceAsStream(secureBundle);

        if (bundleStream == null) {
            throw new RuntimeException(
                    "Không tìm thấy Secure Connect Bundle trong classpath: "
                            + secureBundle
                            + " (kiểm tra file có nằm trong src/main/resources không)"
            );
        }

        Path bundlePath = Files.createTempFile("secure-connect-", ".zip");
        try (bundleStream) {
            Files.copy(bundleStream, bundlePath, StandardCopyOption.REPLACE_EXISTING);
        }
        bundlePath.toFile().deleteOnExit();

        System.out.println("Connecting to Astra DB...");
        System.out.println("Keyspace: " + keyspace);
        System.out.println("Bundle: " + secureBundle);

        try {
            return CqlSession.builder()
                    .withCloudSecureConnectBundle(bundlePath)
                    .withAuthCredentials("token", token)
                    .withKeyspace(keyspace)
                    .build();
        } catch (com.datastax.oss.driver.api.core.InvalidKeyspaceException e) {
            // Keyspace sai -> vẫn cho app chạy để không sập web UI,
            // đồng thời liệt kê keyspace hiện có để người dùng sửa config.
            System.out.println("WARN: Keyspace '" + keyspace + "' không tồn tại. "
                    + "Kết nối tạm thời KHÔNG gắn keyspace để web UI vẫn chạy được.");
            CqlSession tmp = CqlSession.builder()
                    .withCloudSecureConnectBundle(bundlePath)
                    .withAuthCredentials("token", token)
                    .build();
            try {
                System.out.println("Các keyspace hiện có trên Astra DB:");
                tmp.execute("SELECT keyspace_name FROM system_schema.keyspaces")
                        .forEach(r -> System.out.println(" - " + r.getString("keyspace_name")));
                System.out.println("=> Hãy sửa 'astra.db.keyspace' trong application.properties "
                        + "hoặc 'keyspace' trong config.json cho đúng.");
            } catch (Exception ex) {
                System.out.println("Không liệt kê được keyspace: " + ex.getMessage());
            }
            return tmp;
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        String value = node.get(field).asText(null);
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private static String firstNonBlank(String primary, String fallback) {
        return (primary != null && !primary.isBlank()) ? primary : fallback;
    }
}