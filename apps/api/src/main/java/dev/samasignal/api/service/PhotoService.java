package dev.samasignal.api.service;

import dev.samasignal.api.exception.ExceptionMetier;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

@Service
public class PhotoService {
    public record PhotoValidee(byte[] contenu, String type) {}

    public PhotoValidee valider(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) return null;
        if (photo.getSize() > 5 * 1024 * 1024)
            throw new ExceptionMetier(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "PHOTO_TROP_VOLUMINEUSE",
                    "La photo ne doit pas dépasser 5 Mo.");
        try {
            byte[] b = photo.getBytes();
            String type = null;
            if (b.length >= 3 && (b[0] & 255) == 255 && (b[1] & 255) == 216 && (b[2] & 255) == 255)
                type = "image/jpeg";
            if (b.length >= 8
                    && Arrays.equals(
                            Arrays.copyOf(b, 8),
                            new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10}))
                type = "image/png";
            if (b.length >= 12
                    && new String(b, 0, 4, java.nio.charset.StandardCharsets.US_ASCII)
                            .equals("RIFF")
                    && new String(b, 8, 4, java.nio.charset.StandardCharsets.US_ASCII)
                            .equals("WEBP")) type = "image/webp";
            if (type == null || !type.equals(photo.getContentType()))
                throw new ExceptionMetier(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "PHOTO_INVALIDE",
                        "Utilisez une photo JPEG, PNG ou WebP valide.");
            return new PhotoValidee(b, type);
        } catch (IOException e) {
            throw new ExceptionMetier(
                    HttpStatus.BAD_REQUEST, "PHOTO_ILLISIBLE", "Impossible de lire la photo.");
        }
    }
}
