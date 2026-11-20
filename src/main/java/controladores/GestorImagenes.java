package controladores;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class GestorImagenes {

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("png", "jpg", "jpeg");

    public static Path obtenerCarpetaModulo(String subcarpeta) {
        return Paths.get(System.getProperty("user.home"), "Documents", "RedMedica", subcarpeta);
    }

    public static void asegurarDirectorio(String subcarpeta) throws IOException {
        Path directorio = obtenerCarpetaModulo(subcarpeta);
        if (!Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }
    }

    public static String guardarImagen(File archivoOrigen, String subcarpeta) {
        if (archivoOrigen == null || !archivoOrigen.exists() || !archivoOrigen.isFile()) {
            GestorExcepciones.manejarError("Imágenes (" + subcarpeta + ")", "Validar Archivo",
                    "La imagen seleccionada no existe o no es accesible.",
                    new IOException("Archivo inexistente o ilegible: " + archivoOrigen));
            return null;
        }

        String nombreOriginal = archivoOrigen.getName();
        int punto = nombreOriginal.lastIndexOf('.');
        String ext = (punto > 0) ? nombreOriginal.substring(punto + 1).toLowerCase() : "";

        if (!EXTENSIONES_PERMITIDAS.contains(ext)) {
            GestorExcepciones.manejarError("Imágenes (" + subcarpeta + ")", "Validar Formato",
                    "Formato de imagen no válido. Solo se permiten archivos PNG, JPG o JPEG.",
                    new IllegalArgumentException("Extensión no permitida: " + ext));
            return null;
        }

        try {
            asegurarDirectorio(subcarpeta);
            String nombreLimpio = nombreOriginal.replaceAll("[^a-zA-Z0-9._-]", "_");
            String nombreFinal = System.currentTimeMillis() + "_" + nombreLimpio;
            Path destino = obtenerCarpetaModulo(subcarpeta).resolve(nombreFinal);
            Files.copy(archivoOrigen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            return nombreFinal;
        } catch (IOException e) {
            GestorExcepciones.manejarError("Imágenes (" + subcarpeta + ")", "Copiar Fotografía",
                    "No se pudo guardar una copia de la imagen en Documentos.", e);
            return null;
        }
    }

    public static File recuperarImagen(String nombreArchivo, String subcarpeta) {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            return null;
        }
        File archivo = obtenerCarpetaModulo(subcarpeta).resolve(nombreArchivo).toFile();
        return (archivo.exists() && archivo.isFile()) ? archivo : null;
    }
}