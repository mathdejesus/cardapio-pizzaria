package com.pizzaria.controller;

import com.pizzaria.config.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class UploadControllerTest {

    @Autowired
    MockMvc mockMvc;

    private Path uploadDir;
    private static final byte[] TINY_PNG = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
            (byte) 0x89, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x44, 0x41,
            0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
            0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00,
            0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
            0x42, 0x60, (byte) 0x82
    };

    @BeforeEach
    void setUp() throws IOException {
        uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
    }

    @AfterEach
    void cleanup() throws IOException {
        if (Files.exists(uploadDir)) {
            try (var stream = Files.list(uploadDir)) {
                stream.forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
            }
        }
    }

    @Test
    @WithMockUser(username = "admin@pizzaria.com", roles = "ADMIN")
    void upload_emptyFile_shouldReturn400() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        mockMvc.perform(multipart("/api/upload/imagem").file(empty))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Arquivo vazio"));
    }

    @Test
    @WithMockUser(username = "admin@pizzaria.com", roles = "ADMIN")
    void upload_nonImageMimeType_shouldReturn400() throws Exception {
        MockMultipartFile text = new MockMultipartFile(
                "file", "doc.txt", "text/plain", "hello world".getBytes());

        mockMvc.perform(multipart("/api/upload/imagem").file(text))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Apenas arquivos de imagem são permitidos"));
    }

    @Test
    @WithMockUser(username = "admin@pizzaria.com", roles = "ADMIN")
    void upload_validImage_shouldReturn200AndUrl() throws Exception {
        MockMultipartFile image = new MockMultipartFile("file", "pizza.png", "image/png", TINY_PNG);

        mockMvc.perform(multipart("/api/upload/imagem").file(image))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.startsWith("/api/upload/imagem/")));
    }

    @Test
    void serve_existingImage_shouldReturn200() throws Exception {
        String filename = "test-serve.png";
        Files.write(uploadDir.resolve(filename), TINY_PNG);

        mockMvc.perform(get("/api/upload/imagem/" + filename))
                .andExpect(status().isOk())
                .andExpect(content().bytes(TINY_PNG));
    }

    @Test
    void serve_nonExistingImage_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/upload/imagem/does-not-exist.png"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@pizzaria.com", roles = "ADMIN")
    void serve_pathTraversal_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/upload/imagem/..secret"))
                .andExpect(status().isNotFound());
    }
}
