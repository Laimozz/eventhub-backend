package com.eventhub.backend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class EventImageServiceTest {
    private static final String URL = "https://res.cloudinary.com/test-cloud/image/upload/v1/eventhub/events/poster.png";
    private MockRestServiceServer server;
    private RestClient client;
    private EventImageService images;

    @BeforeEach
    void prepareCloudinaryClient() {
        var builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = builder.build();
        images = new EventImageService(client, "test-cloud", "test-only-key", "test-only-secret");
    }

    @ParameterizedTest
    @ValueSource(strings = {"png", "jpeg"})
    void shouldUploadActualImageBytesAndReturnSecureUrl(String format) throws Exception {
        var file = image(format);
        server.expect(requestTo("https://api.cloudinary.com/v1_1/test-cloud/image/upload"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", "Basic " + Base64.getEncoder().encodeToString(
                        "test-only-key:test-only-secret".getBytes(StandardCharsets.ISO_8859_1))))
                .andExpect(request -> {
                    assertThat(request.getHeaders().getContentType().isCompatibleWith(MediaType.MULTIPART_FORM_DATA)).isTrue();
                    byte[] body = ((org.springframework.mock.http.client.MockClientHttpRequest) request).getBodyAsBytes();
                    String multipart = new String(body, StandardCharsets.ISO_8859_1);
                    assertThat(multipart).contains("name=\"file\"", "name=\"folder\"", "eventhub/events");
                    assertThat(multipart).contains(new String(file.getBytes(), StandardCharsets.ISO_8859_1));
                    assertThat(multipart).doesNotContain("../../", "test-only-secret");
                })
                .andRespond(withSuccess("{\"secure_url\":\"" + URL + "\",\"public_id\":\"eventhub/events/poster\",\"width\":2}", MediaType.APPLICATION_JSON));
        assertThat(images.upload(images.prepare(file), "test-id")).isEqualTo(URL);
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 429, 500})
    void shouldReturnSanitizedErrorWhenCloudinaryRejectsUpload(int status) throws Exception {
        server.expect(requestTo("https://api.cloudinary.com/v1_1/test-cloud/image/upload"))
                .andRespond(withStatus(HttpStatus.valueOf(status)).body("provider-private-error"));
        var file = image("png");
        assertThatThrownBy(() -> images.upload(images.prepare(file), "test-id")).isInstanceOfSatisfying(ResponseStatusException.class, error -> {
            assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
            assertThat(error.getMessage()).doesNotContain("provider-private-error", "test-only-key", "test-only-secret");
        });
        server.verify();
    }

    @Test
    void shouldHandleConnectionFailureWithoutRetryingUpload() throws Exception {
        server.expect(requestTo("https://api.cloudinary.com/v1_1/test-cloud/image/upload"))
                .andRespond(withException(new java.net.SocketTimeoutException("timeout")));
        var file = image("png");
        assertThatThrownBy(() -> images.upload(images.prepare(file), "test-id")).isInstanceOfSatisfying(ResponseStatusException.class,
                error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY));
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"secure_url\":null}", "{\"secure_url\":\"http://res.cloudinary.com/unsafe.png\"}", "{\"secure_url\":\"https://example.invalid/other.png\"}"})
    void shouldRejectMissingOrInvalidProviderUrl(String response) throws Exception {
        server.expect(requestTo("https://api.cloudinary.com/v1_1/test-cloud/image/upload"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
        var file = image("png");
        assertThatThrownBy(() -> images.upload(images.prepare(file), "test-id")).isInstanceOfSatisfying(ResponseStatusException.class,
                error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY));
        server.verify();
    }

    @Test
    void shouldRejectUrlThatExceedsExistingDatabaseColumn() throws Exception {
        server.expect(requestTo("https://api.cloudinary.com/v1_1/test-cloud/image/upload"))
                .andRespond(withSuccess("{\"secure_url\":\"" + URL + "x".repeat(255) + "\"}", MediaType.APPLICATION_JSON));
        var file = image("png");
        assertThatThrownBy(() -> images.upload(images.prepare(file), "test-id")).isInstanceOfSatisfying(ResponseStatusException.class,
                error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY));
        server.verify();
    }

    @Test
    void shouldRequireBackendCredentialsWithoutMakingAnExternalRequest() throws Exception {
        var file = image("png");
        for (String[] credentials : new String[][] {{"", "key", "secret"}, {"test-cloud", "", "secret"}, {"test-cloud", "key", ""}, {"../cloud", "key", "secret"}}) {
            var unconfigured = new EventImageService(client, credentials[0], credentials[1], credentials[2]);
            assertThatThrownBy(() -> unconfigured.upload(unconfigured.prepare(file), "test-id")).isInstanceOfSatisfying(ResponseStatusException.class,
                    error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        }
        server.verify();
    }

    @Test
    void shouldRejectEmptyOversizedFakeAndUnsupportedImagesBeforeCallingCloudinary() throws Exception {
        for (var file : new MockMultipartFile[] {
                new MockMultipartFile("file", new byte[0]),
                new MockMultipartFile("file", new byte[5 * 1024 * 1024 + 1]),
                new MockMultipartFile("file", "fake.png", "image/png", "not an image".getBytes(StandardCharsets.UTF_8)),
                image("gif")}) {
            assertThatThrownBy(() -> images.upload(images.prepare(file), "test-id")).isInstanceOfSatisfying(ResponseStatusException.class,
                    error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        }
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ok", "not found"})
    void shouldDeleteOnlyTheImageIdCreatedByThisAttempt(String result) {
        server.expect(requestTo("https://api.cloudinary.com/v1_1/test-cloud/image/destroy"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", "Basic " + Base64.getEncoder().encodeToString(
                        "test-only-key:test-only-secret".getBytes(StandardCharsets.ISO_8859_1))))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.content().string(
                        "public_id=eventhub%2Fevents%2Ftest-id&invalidate=true"))
                .andRespond(withSuccess("{\"result\":\"" + result + "\"}", MediaType.APPLICATION_JSON));
        images.delete("test-id");
        server.verify();
    }

    @Test
    void shouldReportCleanupFailureWithoutExposingProviderDetails() {
        server.expect(requestTo("https://api.cloudinary.com/v1_1/test-cloud/image/destroy"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY).body("private provider error"));
        assertThatThrownBy(() -> images.delete("test-id")).isInstanceOfSatisfying(ResponseStatusException.class,
                error -> assertThat(error.getMessage()).doesNotContain("private provider error", "test-only-secret"));
        server.verify();
    }

    private MockMultipartFile image(String format) throws Exception {
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), format, bytes);
        return new MockMultipartFile("file", "../../original." + format, "image/" + format, bytes.toByteArray());
    }
}
