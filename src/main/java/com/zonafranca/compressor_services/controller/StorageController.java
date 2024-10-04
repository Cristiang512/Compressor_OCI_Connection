package com.zonafranca.compressor_services.controller;

import com.zonafranca.compressor_services.servicios.ObjectStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@RestController
@RequestMapping("/api/storage")
public class StorageController {

    private final ObjectStorageService objectStorageService;

    @Autowired
    public StorageController(ObjectStorageService objectStorageService) {
        this.objectStorageService = objectStorageService;
    }

    // Método de prueba para verificar la conexión con OCI Object Storage
    @GetMapping("/test-upload")
    public ResponseEntity<String> testUpload() {
        try {
            // Simulamos la subida de un archivo de prueba
            String fileName = "test-upload.txt";
            InputStream fileStream = new ByteArrayInputStream("Contenido de prueba".getBytes());

            // Usamos el servicio ObjectStorageService para subir el archivo
            objectStorageService.uploadCompressedFile(fileName, fileStream);

            return ResponseEntity.ok("Archivo subido correctamente: " + fileName);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error subiendo archivo: " + e.getMessage());
        }
    }
}
