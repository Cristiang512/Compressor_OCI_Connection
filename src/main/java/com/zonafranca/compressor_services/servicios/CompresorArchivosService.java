package com.zonafranca.compressor_services.servicios;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.aspose.pdf.Annotation;
import com.aspose.pdf.Artifact;
import com.aspose.pdf.ArtifactCollection;
import com.aspose.pdf.Document;
import com.aspose.pdf.FontRepository;
import com.aspose.pdf.Page;
import com.aspose.pdf.StampAnnotation;
import com.aspose.pdf.TextFragment;
import com.aspose.pdf.TextStamp;
import com.aspose.pdf.optimization.OptimizationOptions;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;

import java.util.List;

@Service
public class CompresorArchivosService {

    Logger logger = LoggerFactory.getLogger(CompresorArchivosService.class);

    /**
     * Comprime un archivo según su extensión
     * @param inputStream
     * @param rutaArchivo
     * @return
     */
    public byte[] comprimirArchivos(InputStream inputStream, String rutaArchivo) {
        byte[] compressedBytes = null;
        String extension = getFileExtension(rutaArchivo);
        if (rutaArchivo.toLowerCase().endsWith(".pdf")) {
            compressedBytes = comprimirPdf(inputStream);
        } else  {
            if (extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")) {
                compressedBytes = comprimirImagen(inputStream, extension, (float) 0.7);
            } else if (extension.equals("tif")) {
                compressedBytes = comprimirTiff(inputStream, "LZW", 0.7f);
            } else if (extension.equals("gif")) {
                compressedBytes = comprimirGif(inputStream, 50);
            } else {
                compressedBytes = null;
            }
        }
        return compressedBytes;
    }

    /**
     * Comprime un archivo PDF
     * @param inputStream
     * @return
     */
    public byte[] comprimirPdf(InputStream inputStream) {
        logger.info("Comprimiendo archivo PDF... {}", inputStream);
        byte[] compressedPdfBytes = null;
        try {
            if (pdfEsValido(inputStream)) {
                logger.info("El archivo es un PDF válido.");
                
                inputStream.reset();
    
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Document pdfDocument = new Document(inputStream);
                
                OptimizationOptions opt = new OptimizationOptions();
    
                opt.getImageCompressionOptions().setCompressImages(true);
                opt.getImageCompressionOptions().setImageQuality(50); // Calidad de compresión de imágenes (0-100), menos de 50 las imagenes se pixelan
                opt.getImageCompressionOptions().setMaxResolution(150);
                opt.getImageCompressionOptions().setResizeImages(false);
                pdfDocument.optimizeResources(opt);

                pdfDocument.save(outputStream);
                pdfDocument.close();
                compressedPdfBytes = outputStream.toByteArray();
            } else {
                logger.error("El archivo no es un PDF válido.");
            }
        } catch (Exception e) {
            logger.error("Error al comprimir el archivo PDF", e);
        }
        return compressedPdfBytes;
    }

    /**
     * Verifica si un archivo es un PDF válido
     * @param inputStream
     * @return
     */
    public boolean pdfEsValido(InputStream inputStream) {
        if (inputStream == null) {
            return false;
        }

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            // Leer los primeros 5 bytes del InputStream
            byte[] header = new byte[5];
            int bytesRead = bufferedInputStream.read(header, 0, header.length);
            
            // Verificar si se leyeron suficientes bytes
            if (bytesRead < header.length) {
                return false;
            }

            // Convertir los bytes leídos en una cadena
            String headerString = new String(header);
            // Verificar si el archivo comienza con el encabezado PDF
            return headerString.startsWith("%PDF-");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Comprime una imagen
     * @param inputStream
     * @param formatName
     * @param quality
     * @return
     */
    public byte[] comprimirImagen(InputStream inputStream, String formatName, float quality) {
        byte[] compressedImageBytes = null;
        try {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IOException("La imagen no es válida o el formato no es compatible.");
            }
    
            // Crear un ByteArrayOutputStream para almacenar la imagen comprimida
            ByteArrayOutputStream compressedImageStream = new ByteArrayOutputStream();
    
            // Obtener el escritor de imágenes adecuado según el formato
            ImageWriter writer = ImageIO.getImageWritersByFormatName(formatName).next();
            ImageWriteParam param = writer.getDefaultWriteParam();
    
            // Configurar la compresión si el formato soporta compresión
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality); // Calidad entre 0.0 y 1.0
            }
    
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(compressedImageStream)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, null), param);
            } finally {
                writer.dispose();
            }

            compressedImageBytes = compressedImageStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error al comprimir la imagen", e);
        }

        return compressedImageBytes;
    }

    /**
     * Comprime un archivo GIF
     * @param inputStream
     * @param quality
     * @return
     */
    public byte[] comprimirGif(InputStream inputStream, int quality) {
        byte [] compressedGifBytes = null;
        try {
            GifDecoder decoder = new GifDecoder();
            int status = decoder.read(inputStream);
            if (status != GifDecoder.STATUS_OK) {
                logger.error("Error al leer el archivo GIF: {}", status);
            }
    
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
    
            // Configuración de la calidad de compresión (0-100)
            encoder.setQuality(quality);
    
            encoder.setRepeat(decoder.getLoopCount());
            encoder.start(byteArrayOutputStream);
    
            for (int i = 0; i < decoder.getFrameCount(); i++) {
                BufferedImage frame = decoder.getFrame(i);
                encoder.setDelay(decoder.getDelay(i)); // Configurar el retraso entre frames
                encoder.addFrame(frame); // Añadir cada frame al GIF comprimido
            }
    
            encoder.finish(); // Finalizar el proceso de compresión
            compressedGifBytes = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error al comprimir el archivo GIF", e);
        }

        return compressedGifBytes;
    }

    /**
     * Comprime un archivo TIF
     * @param inputStream
     * @param compressionType
     * @param quality
     * @return
     */
    public byte[] comprimirTiff(InputStream inputStream, String compressionType, float quality) {
        byte[] compressedTiffBytes = null;
        try {
            BufferedImage image = ImageIO.read(inputStream);
    
            if (image == null) {
                throw new IOException("No se pudo leer la imagen TIF.");
            }
    
            ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream();
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(compressedOutputStream);
    
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("tif");
            if (!writers.hasNext()) {
                throw new IllegalStateException("No se encontró un escritor para el formato TIF.");
            }
    
            ImageWriter writer = writers.next();
            writer.setOutput(imageOutputStream);
    
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType(compressionType);
            param.setCompressionQuality(quality); // Calidad de compresión (0.0 - 1.0)
    
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();
            imageOutputStream.close();
            compressedTiffBytes = compressedOutputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error al comprimir la imagen TIFF", e);
        }
        return compressedTiffBytes;
    }

    /**
     * Obtiene la extensión de un archivo
     * @param filePath
     * @return
     */
    public static String getFileExtension(String filePath) {
        String fileName = Paths.get(filePath).getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        } else {
            return ""; // No extension found
        }
    }
}
