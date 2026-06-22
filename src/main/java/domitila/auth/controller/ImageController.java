package domitila.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;

import domitila.auth.service.ImageService;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @GetMapping("/**")
    public ResponseEntity<Resource> getImage(HttpServletRequest request) {
        String pathWithinMapping = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String imagePath = pathMatcher.extractPathWithinPattern(bestMatchPattern, pathWithinMapping);

        if (imagePath == null || imagePath.isBlank() || "health".equals(imagePath)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Imagen no encontrada");
        }

        Resource resource = imageService.loadAsResource(imagePath);
        String contentType = resolveContentType(resource);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Servicio de imágenes operativo. Directorio base: " + imageService.getUploadDir());
    }

    private String resolveContentType(Resource resource) {
        try {
            Path filePath = resource.getFile().toPath();
            String contentType = Files.probeContentType(filePath);
            return contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        } catch (Exception ex) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }
}