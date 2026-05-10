package com.optcg.deckbuilder.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * Proxy para imágenes externas.
 * Permite que el frontend cargue imágenes de dominios externos en un canvas/jsPDF
 * sin restricciones CORS, ya que la petición la hace el servidor (no el navegador).
 * Endpoint: GET /api/proxy/image?url={encodedUrl}
 */
@RestController
@RequestMapping("/api/proxy")
public class ImageProxyController {

    private static final int MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB max
    private static final int TIMEOUT_MS = 8000;

    @GetMapping("/image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {

        // Sólo permitimos URLs de los dominios conocidos de OPTCG
        if (!isAllowedDomain(url)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            URI uri = URI.create(url);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "LogPose/1.0");

            int status = conn.getResponseCode();
            if (status != 200) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }

            String contentType = conn.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = "image/jpeg";
            }

            // Limit download size
            try (InputStream in = conn.getInputStream();
                 ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
                byte[] chunk = new byte[8192];
                int read;
                int total = 0;
                while ((read = in.read(chunk)) != -1) {
                    total += read;
                    if (total > MAX_SIZE_BYTES) {
                        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
                    }
                    buf.write(chunk, 0, read);
                }
                byte[] imageBytes = buf.toByteArray();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.setCacheControl("public, max-age=86400"); // cache 24h
                return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    /**
     * Whitelist de dominios permitidos para el proxy.
     * Añadir aquí cualquier CDN adicional que use la API externa de cartas.
     */
    private boolean isAllowedDomain(String url) {
        if (url == null || url.isBlank()) return false;
        String lower = url.toLowerCase();
        return lower.startsWith("https://") && (
            lower.contains("onepiece-cardgame.com") ||
            lower.contains("optcgdb.com")           ||
            lower.contains("op-tcg.com")            ||
            lower.contains("optcgapi.com")          ||
            lower.contains("cdnjs")                 ||
            lower.contains("githubusercontent.com") ||
            lower.contains("api.tcgdex.net")        ||
            lower.contains("storage.googleapis.com")
        );
    }
}
