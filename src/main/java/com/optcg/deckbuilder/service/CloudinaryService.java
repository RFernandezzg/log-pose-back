package com.optcg.deckbuilder.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folderName) throws IOException {
        String publicId = folderName + "/" + UUID.randomUUID().toString();
        
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folderName
        ));
        
        return uploadResult.get("secure_url").toString();
    }

    public String uploadFileWithHash(MultipartFile file, String folderName) throws IOException {
        try {
            byte[] fileBytes = file.getBytes();
            String hash = calculateHash(fileBytes);
            
            // Usamos el hash como public_id para evitar duplicados
            // overwrite: false asegura que si el ID existe, no se vuelva a subir/procesar
            Map uploadResult = cloudinary.uploader().upload(fileBytes, ObjectUtils.asMap(
                    "public_id", hash,
                    "folder", folderName,
                    "overwrite", false
            ));
            
            log.info("Archivo subido a Cloudinary en carpeta '{}' con public_id '{}'", folderName, hash);
            return uploadResult.get("secure_url").toString();
            
        } catch (NoSuchAlgorithmException e) {
            log.error("Error al generar hash para la imagen", e);
            throw new RuntimeException("Error interno al procesar la imagen", e);
        }
    }

    private String calculateHash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(data);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
