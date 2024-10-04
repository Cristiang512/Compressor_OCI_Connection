package com.zonafranca.compressor_services.servicios;

import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.*;
import com.oracle.bmc.objectstorage.responses.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ByteArrayInputStream;


@Service
public class ObjectStorageService {

    private ObjectStorageClient client;  // Removemos el modificador 'final'

    // Valores que serán leídos desde application.properties
    @Value("${oci.bucketName}")
    private String bucketName;

    @Value("${oci.namespace}")
    private String namespaceName;

    @Value("${oci.compartment}")
    private String compartmentId;  

    // Constructor que inicializa el cliente de Object Storage usando las credenciales de OCI
    public ObjectStorageService(@Value("${oci.tenancy}") String tenancy,
                                @Value("${oci.user}") String user,
                                @Value("${oci.fingerprint}") String fingerprint,
                                @Value("${oci.privateKeyPath}") String privateKeyPath,
                                @Value("${oci.passphrase}") String passphrase,
                                @Value("${oci.region}") String region) {

        SimpleAuthenticationDetailsProvider provider = null;
        try {
            // Proveedor de autenticación usando las credenciales de OCI
            provider = SimpleAuthenticationDetailsProvider.builder()
                    .tenantId(tenancy)
                    .userId(user)
                    .fingerprint(fingerprint)
                    .privateKeySupplier(() -> {
                        try {
                            return new FileInputStream(privateKeyPath);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException("Archivo de clave privada no encontrado: " + privateKeyPath, e);
                        }
                    })
                    .passPhrase(passphrase)
                    .build();

            // Inicializa el cliente de Object Storage solo si el provider fue creado correctamente
            if (provider != null) {
                this.client = ObjectStorageClient.builder().build(provider);
                this.client.setRegion(region);
            } else {
                this.client = null;  // Inicializa client como null en caso de error
            }

        } catch (Exception e) {
            System.err.println("Error al inicializar el cliente de Object Storage: " + e.getMessage());
            e.printStackTrace();
            this.client = null;  // Inicializa client como null en caso de error
        }
    }

    // Método para subir un archivo comprimido a Object Storage
    public PutObjectResponse uploadCompressedFile(String filePath, InputStream inputStream) {
        // Preparamos la solicitud para subir el archivo
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucketName(bucketName)
                .namespaceName(namespaceName)
                .objectName(filePath) // El nombre del archivo en Object Storage
                .putObjectBody(inputStream) // El contenido del archivo
                .build();

        // Subimos el archivo y devolvemos la respuesta
        return client.putObject(putObjectRequest);
    }

    // Método para verificar si un archivo existe en Object Storage
    public boolean verificarArchivoExiste(String rutaArchivo) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucketName(bucketName)
                    .namespaceName(namespaceName)
                    .objectName(rutaArchivo)
                    .build();

            client.getObject(getObjectRequest);  // Si no lanza excepción, el archivo existe
            return true;
        } catch (Exception e) {
            System.err.println("Archivo no encontrado en Object Storage: " + rutaArchivo);
            return false;
        }
    }

    // Método para verificar si una carpeta existe en Object Storage
    public boolean verificarCarpetaExiste(String carpeta) {
        try {
            // Intentamos acceder a la "carpeta", que en realidad es un prefijo
            ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                    .bucketName(bucketName)
                    .namespaceName(namespaceName)
                    .prefix(carpeta + "/")  // Las carpetas son representadas por un prefijo
                    .limit(1)
                    .build();

            ListObjectsResponse response = client.listObjects(listObjectsRequest);
            return !response.getListObjects().getObjects().isEmpty();
        } catch (Exception e) {
            System.err.println("Error al verificar carpeta: " + carpeta);
            return false;
        }
    }

    // Método para crear una carpeta en Object Storage
    public void crearCarpeta(String carpeta) {
        // Crear una carpeta implica subir un objeto vacío con el nombre de la carpeta y "/"
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucketName(bucketName)
                .namespaceName(namespaceName)
                .objectName(carpeta + "/")  // Las carpetas son representadas por un sufijo "/"
                .putObjectBody(new ByteArrayInputStream(new byte[0]))  // Archivo vacío
                .build();

        client.putObject(putObjectRequest);
        System.out.println("Carpeta creada: " + carpeta);
    }

    // Método para descargar un archivo desde Object Storage
    public InputStream descargarArchivo(String rutaArchivo) {
        // Crear la solicitud para obtener el archivo
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucketName(bucketName)  // Usamos el bucket ya definido
                .namespaceName(namespaceName)  // Namespace definido en la configuración
                .objectName(rutaArchivo)  // Ruta o nombre del archivo a descargar
                .build();

        // Realizamos la solicitud para obtener el archivo
        GetObjectResponse getObjectResponse = client.getObject(getObjectRequest);

        // Devolvemos el InputStream con el contenido del archivo
        return getObjectResponse.getInputStream();
    }


    public void copiarArchivo(String rutaArchivo, String carpetaDestino) {
        // Definimos la carpeta BackupCompressor
        String carpetaBackup = "BackupCompressor";
    
        // Verificamos si la carpeta BackupCompressor existe
        if (!verificarCarpetaExiste(carpetaBackup)) {
            // Si no existe, la creamos 
            crearCarpeta(carpetaBackup);
            System.out.println("Carpeta BackupCompressor creada.");
        } else {
            System.out.println("Carpeta BackupCompressor ya existe.");
        }
    
        // Creamos el nuevo nombre de archivo en la carpeta de BackupCompressor
        String nuevoNombreArchivo = carpetaBackup + "/" + rutaArchivo;
    
        // Descargamos el archivo original
        InputStream inputStream = descargarArchivo(rutaArchivo);
        
        // Subimos el archivo a la carpeta BackupCompressor
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucketName(bucketName)
                .namespaceName(namespaceName)
                .objectName(nuevoNombreArchivo)  // El archivo se copiará a la carpeta BackupCompressor
                .putObjectBody(inputStream)
                .build();
        
        client.putObject(putObjectRequest);
        
        System.out.println("Archivo copiado a la carpeta BackupCompressor: " + carpetaBackup);
    }    
}
