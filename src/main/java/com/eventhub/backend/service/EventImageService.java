package com.eventhub.backend.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventImageService {
    private final RestClient client;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    @Autowired
    public EventImageService(@Value("${app.cloudinary.cloud-name:}") String cloudName,
            @Value("${app.cloudinary.api-key:}") String apiKey,
            @Value("${app.cloudinary.api-secret:}") String apiSecret) {
        this(createClient(), cloudName, apiKey, apiSecret);
    }

    EventImageService(RestClient client, String cloudName, String apiKey, String apiSecret) {
        this.client = client;
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    private static RestClient createClient() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30));
        return RestClient.builder().requestFactory(factory).build();
    }

    public PreparedImage prepare(MultipartFile file) {
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image must be nonempty and at most 5 MB");
        }
        byte[] bytes;
        String format;
        try {
            bytes = file.getBytes();
            // Use memory only; ImageIO's default cache may create temporary files.
            try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
                var readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only valid JPEG or PNG images are supported");
                }
                var reader = readers.next();
                try {
                    reader.setInput(input);
                    format = reader.getFormatName().toLowerCase(Locale.ROOT);
                    if (!format.equals("jpeg") && !format.equals("png")) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPEG or PNG images are supported");
                    }
                    if ((long) reader.getWidth(0) * reader.getHeight(0) > 20_000_000L) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image dimensions exceed 20 megapixels");
                    }
                    reader.read(0);
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image file");
        }
        return new PreparedImage(bytes, format);
    }

    public String upload(PreparedImage image, String imageId) {
        return upload(image, "eventhub/events", imageId);
    }

    public String upload(PreparedImage image, String folder, String imageId) {
        verifyConfiguration();
        var body = new LinkedMultiValueMap<String, Object>();
        String filename = image.format().equals("jpeg") ? "image.jpg" : "image.png";
        body.add("file", new ByteArrayResource(image.bytes()) {
            @Override
            public String getFilename() {
                return filename;
            }
        });
        body.add("folder", folder);
        body.add("public_id", imageId);
        body.add("overwrite", "false");
        CloudinaryResponse response;
        try {
            response = client.post().uri("https://api.cloudinary.com/v1_1/{cloudName}/image/upload", cloudName)
                    .headers(headers -> headers.setBasicAuth(apiKey, apiSecret))
                    .contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve().body(CloudinaryResponse.class);
        } catch (RestClientException exception) {
            // Do not expose provider responses, credentials or request headers to clients.
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Image could not be uploaded to Cloudinary");
        }
        if (response == null || response.secureUrl() == null
                || !response.secureUrl().startsWith("https://res.cloudinary.com/") || response.secureUrl().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary returned an invalid image URL");
        }
        return response.secureUrl();
    }

    public void verifyConfiguration() {
        if (!cloudName.matches("[A-Za-z0-9_-]+") || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Image upload is not configured");
        }
    }

    public void delete(String imageId) {
        delete("eventhub/events", imageId);
    }

    public void delete(String folder, String imageId) {
        verifyConfiguration();
        var body = new LinkedMultiValueMap<String, String>();
        body.add("public_id", folder + "/" + imageId);
        body.add("invalidate", "true");
        try {
            var response = client.post().uri("https://api.cloudinary.com/v1_1/{cloudName}/image/destroy", cloudName)
                    .headers(headers -> headers.setBasicAuth(apiKey, apiSecret))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(body).retrieve().body(DeleteResponse.class);
            if (response == null || !("ok".equals(response.result()) || "not found".equals(response.result()))) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Image cleanup failed");
            }
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Image cleanup failed");
        }
    }

    public record PreparedImage(byte[] bytes, String format) {
    }

    private record DeleteResponse(String result) {
    }

    private record CloudinaryResponse(@JsonProperty("secure_url") String secureUrl) {
    }
}
