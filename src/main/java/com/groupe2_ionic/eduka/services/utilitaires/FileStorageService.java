package com.groupe2_ionic.eduka.services.utilitaires;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageRoot;

    private static final Set<String> ALLOWED_EXT = Set.of(
            "png","jpg","jpeg","pdf","doc","docx","ppt","pptx","zip","txt","md"
    );

    public FileStorageService(@Value("${file.upload-dir:uploads}") String uploadDir) throws IOException {
        this.storageRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(storageRoot);
    }

    public StoredFile store(MultipartFile file, String subFolder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Fichier vide");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String ext = getExtension(original);
        if (!ALLOWED_EXT.contains(ext.toLowerCase())) {
            throw new IOException("Type de fichier non supporté: " + ext);
        }

        String uniqueName = UUID.randomUUID().toString() + "." + ext;
        Path folder = storageRoot.resolve(subFolder).normalize();
        Files.createDirectories(folder);

        Path destination = folder.resolve(uniqueName).normalize().toAbsolutePath();
        file.transferTo(destination);

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(subFolder + "/")
                .path(uniqueName)
                .toUriString();

        return new StoredFile(uniqueName, url, destination.toString(), file.getSize(), file.getContentType());
    }

    /**
     * Supprime un fichier.
     * Accepte :
     *  - une URL complète (ex: https://host/uploads/contributions/uuid.ext),
     *  - un chemin relatif (ex: contributions/uuid.ext),
     *  - juste le nom de fichier (ex: uuid.ext),
     *  - ou un chemin absolu.
     *
     * Lève IOException si le fichier n'existe pas ou si accès refusé.
     */
    public void delete(String pathOrUrl) throws IOException {
        if (pathOrUrl == null || pathOrUrl.isBlank()) {
            throw new IllegalArgumentException("Le chemin fourni est vide.");
        }

        String relative = extractRelativePath(pathOrUrl);
        Path target = storageRoot.resolve(relative).normalize().toAbsolutePath();

        // Sécurité : vérifier que la cible est bien dans le dossier de stockage
        if (!target.startsWith(storageRoot)) {
            throw new IOException("Accès refusé au chemin demandé : " + pathOrUrl);
        }

        if (!Files.exists(target)) {
            throw new NoSuchFileException("Fichier non trouvé: " + target);
        }

        Files.delete(target);

        // Optionnel : supprimer les dossiers parents vides jusqu'à storageRoot (mais pas storageRoot)
        Path parent = target.getParent();
        while (parent != null && !parent.equals(storageRoot) && isDirectoryEmpty(parent)) {
            Files.delete(parent);
            parent = parent.getParent();
        }
    }

    /**
     * Supprime plusieurs fichiers et renvoie une liste de messages (succès / erreur).
     */
    public List<String> deleteMultiple(List<String> paths) {
        List<String> results = new ArrayList<>();
        for (String p : paths) {
            try {
                delete(p);
                results.add("Supprimé : " + p);
            } catch (IOException e) {
                results.add("Erreur : " + p + " -> " + e.getMessage());
            }
        }
        return results;
    }

    private boolean isDirectoryEmpty(Path dir) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            return !ds.iterator().hasNext();
        }
    }

    private String extractRelativePath(String pathOrUrl) {
        // 1) Si c'est une URL, extraire la portion après "/uploads/"
        try {
            URI uri = URI.create(pathOrUrl);
            String scheme = uri.getScheme();
            if (scheme != null && (scheme.equals("http") || scheme.equals("https"))) {
                String p = uri.getPath(); // ex: /uploads/contributions/uuid.ext
                int idx = p.indexOf("/uploads/");
                if (idx >= 0) {
                    return p.substring(idx + "/uploads/".length());
                }
                // sinon retirer le slash initial
                if (p.startsWith("/")) return p.substring(1);
                return p;
            }
        } catch (Exception ignored) {
            // pas une URL -> continuer
        }

        // 2) Si c'est un chemin absolu à l'extérieur, tenter de le relativiser
        Path given = Paths.get(pathOrUrl);
        if (given.isAbsolute()) {
            Path normalized = given.toAbsolutePath().normalize();
            if (normalized.startsWith(storageRoot)) {
                return storageRoot.relativize(normalized).toString().replace("\\", "/");
            } else {
                // chemin absolu hors storageRoot -> on considère le nom du fichier pour tenter la suppression
                return normalized.getFileName().toString();
            }
        }

        // 3) chemin relatif ou nom de fichier -> nettoyer slashs initiaux
        return pathOrUrl.replaceFirst("^/+", "");
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return (idx >= 0 && idx < filename.length() - 1) ? filename.substring(idx + 1) : "";
    }

    public record StoredFile(String fileName, String url, String absolutePath, long size, String contentType) {}
}
