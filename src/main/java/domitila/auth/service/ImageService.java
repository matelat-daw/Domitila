package domitila.auth.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class ImageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }

    public Path resolveUploadBasePath() {
        Path basePath = Paths.get(uploadDir);
        if (!basePath.isAbsolute()) {
            basePath = Paths.get(System.getProperty("user.dir")).resolve(basePath);
        }
        return basePath.normalize();
    }

    public boolean isValidImagePath(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return false;
        }

        String normalized = imagePath.replace("\\", "/").trim();
        if (normalized.startsWith("/") || normalized.contains("..")) {
            return false;
        }

        return normalized.matches("^[a-zA-Z0-9/_\\.-]+$");
    }

    public String saveProfileImage(MultipartFile file, Integer personalId) {
        validateImageFile(file);
        String extension = resolveExtension(file);

        try {
            Path personalDir = ensurePersonalImageDirectory(personalId);
            String fileName = "profile." + extension;
            Path targetPath = personalDir.resolve(fileName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return personalId + "/" + fileName;
        } catch (IOException ex) {
            log.error("Error al guardar la imagen de perfil para el usuario {}", personalId, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la imagen de perfil.");
        }
    }

    public void deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return;
        }

        try {
            Path imagePathResolved = resolvePathUnderUploadBase(imagePath);
            Files.deleteIfExists(imagePathResolved);
            deleteParentDirectoryIfEmpty(imagePathResolved.getParent());
        } catch (IOException ex) {
            log.warn("No se pudo eliminar la imagen {}", imagePath, ex);
        }
    }

    public void deletePersonalDirectory(Integer personalId) {
        if (personalId == null) {
            return;
        }

        Path personalDirectory = resolveUploadBasePath().resolve(String.valueOf(personalId)).normalize();
        if (!personalDirectory.startsWith(resolveUploadBasePath()) || !Files.exists(personalDirectory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(personalDirectory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof IOException ioEx) {
                log.warn("No se pudo eliminar la carpeta de imágenes del usuario {}", personalId, ioEx);
                return;
            }
            throw ex;
        } catch (IOException ex) {
            log.warn("No se pudo eliminar la carpeta de imágenes del usuario {}", personalId, ex);
        }
    }

    public Resource loadAsResource(String imagePath) {
        try {
            Path path = resolvePathUnderUploadBase(imagePath);
            if (!Files.exists(path) || !Files.isReadable(path)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen no encontrada");
            }

            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen no encontrada");
            }

            return resource;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error al cargar la imagen {}", imagePath, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo obtener la imagen.");
        }
    }

    private Path ensurePersonalImageDirectory(Integer personalId) throws IOException {
        Path basePath = resolveUploadBasePath();
        Files.createDirectories(basePath);

        Path personalPath = basePath.resolve(String.valueOf(personalId));
        Files.createDirectories(personalPath);
        return personalPath;
    }

    private Path resolvePathUnderUploadBase(String relativePath) {
        if (!isValidImagePath(relativePath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ruta de imagen no es válida.");
        }

        Path uploadPath = resolveUploadBasePath();
        Path resolved = uploadPath.resolve(relativePath.replace("\\", "/")).normalize();
        if (!resolved.startsWith(uploadPath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ruta de imagen no es válida.");
        }
        return resolved;
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes seleccionar una imagen.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo debe ser una imagen válida.");
        }

        String extension = resolveExtension(file);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagen no soportado. Usa JPG, PNG, GIF o WEBP.");
        }
    }

    private String resolveExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            int lastDotIndex = originalFilename.lastIndexOf('.');
            if (lastDotIndex >= 0 && lastDotIndex < originalFilename.length() - 1) {
                String extension = originalFilename.substring(lastDotIndex + 1).toLowerCase();
                if (ALLOWED_EXTENSIONS.contains(extension)) {
                    return extension;
                }
            }
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return "jpg";
        }

        return switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/jpeg", "image/jpg" -> "jpg";
            default -> "jpg";
        };
    }

    private void deleteParentDirectoryIfEmpty(Path directory) throws IOException {
        Path uploadBase = resolveUploadBasePath();
        if (directory == null || directory.equals(uploadBase) || !directory.startsWith(uploadBase)) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            if (!stream.iterator().hasNext()) {
                Files.deleteIfExists(directory);
            }
        }
    }
}
