package com.github.mangila.pokedex.shared.webp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for converting images to WebP format using webplib via subprocess.
 * This class requires the webplib command-line tool to be installed and available in the system PATH.
 */
public class WebpConverter {

    private static final String WEBPLIB_COMMAND = "cwebp"; // Assuming the command is 'cwebp'
    private static final int TIMEOUT_SECONDS = 30;
    private static final int VERSION_CHECK_TIMEOUT_SECONDS = 5;

    /**
     * Supported input image formats for conversion to WebP.
     */
    public enum ImageFormat {
        PNG(".png"),
        JPEG(".jpg"),
        TIFF(".tiff"),
        WEBP(".webp"),
        UNKNOWN(".bin");

        private final String extension;

        ImageFormat(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }
    }

    /**
     * Checks if the webplib command is available on the system.
     *
     * @return true if the webplib command is available, false otherwise
     */
    public boolean isWebplibAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(WEBPLIB_COMMAND, "-version");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read and discard output
            try (InputStream is = process.getInputStream()) {
                byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    // Discard output
                }
            }

            boolean completed = process.waitFor(VERSION_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                return false;
            }

            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Converts an image byte array to WebP format.
     * This method assumes the input format is unknown and uses a generic binary extension.
     *
     * @param imageData the byte array containing the image data to convert
     * @return the byte array containing the converted WebP image data
     * @throws IOException if an I/O error occurs during conversion
     * @throws InterruptedException if the conversion process is interrupted
     * @throws IllegalStateException if the conversion process fails or times out
     */
    public byte[] convertToWebp(byte[] imageData) throws IOException, InterruptedException {
        return convertToWebp(imageData, ImageFormat.UNKNOWN);
    }

    /**
     * Converts an image byte array to WebP format with the specified input format.
     *
     * @param imageData the byte array containing the image data to convert
     * @param format the format of the input image data
     * @return the byte array containing the converted WebP image data
     * @throws IOException if an I/O error occurs during conversion
     * @throws InterruptedException if the conversion process is interrupted
     * @throws IllegalStateException if the conversion process fails or times out
     */
    public byte[] convertToWebp(byte[] imageData, ImageFormat format) throws IOException, InterruptedException {
        return convertToWebp(imageData, format, 75); // Default quality is 75
    }

    /**
     * Converts an image byte array to WebP format with the specified input format and quality.
     *
     * @param imageData the byte array containing the image data to convert
     * @param format the format of the input image data
     * @param quality the quality of the output WebP image (0-100, where 100 is the best quality)
     * @return the byte array containing the converted WebP image data
     * @throws IOException if an I/O error occurs during conversion
     * @throws InterruptedException if the conversion process is interrupted
     * @throws IllegalStateException if the conversion process fails or times out
     * @throws IllegalArgumentException if quality is not between 0 and 100
     */
    public byte[] convertToWebp(byte[] imageData, ImageFormat format, int quality) throws IOException, InterruptedException {
        if (quality < 0 || quality > 100) {
            throw new IllegalArgumentException("Quality must be between 0 and 100");
        }
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("Image data cannot be null or empty");
        }

        if (!isWebplibAvailable()) {
            throw new IllegalStateException("WebP library (cwebp) is not available. Please ensure it is installed and in the system PATH.");
        }

        // Create temporary files for input and output
        Path tempInputPath = Files.createTempFile("input_image", format.getExtension());
        Path tempOutputPath = Files.createTempFile("output_image", ".webp");

        try {
            // Write input data to temporary file
            try (FileOutputStream fos = new FileOutputStream(tempInputPath.toFile())) {
                fos.write(imageData);
            }

            // Build the process to convert the image
            ProcessBuilder processBuilder = new ProcessBuilder(
                    WEBPLIB_COMMAND,
                    "-q", String.valueOf(quality),
                    tempInputPath.toString(),
                    "-o", tempOutputPath.toString()
            );

            // Redirect error stream to standard output
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Capture process output
            StringBuilder output = new StringBuilder();
            try (InputStream is = process.getInputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    output.append(new String(buffer, 0, bytesRead));
                }
            }

            // Wait for the process to complete with timeout
            boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                throw new IllegalStateException("Conversion process timed out after " + TIMEOUT_SECONDS + " seconds");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new IllegalStateException("Conversion process failed with exit code: " + exitCode + 
                    (output.length() > 0 ? ". Process output: " + output.toString() : ""));
            }

            // Read the output file into byte array
            File outputFile = tempOutputPath.toFile();
            byte[] outputData = new byte[(int) outputFile.length()];

            try (FileInputStream fis = new FileInputStream(outputFile)) {
                int bytesRead = fis.read(outputData);
                if (bytesRead != outputData.length) {
                    throw new IOException("Failed to read entire output file");
                }
            }

            return outputData;
        } finally {
            // Clean up temporary files
            try {
                Files.deleteIfExists(tempInputPath);
                Files.deleteIfExists(tempOutputPath);
            } catch (IOException e) {
                // Log but don't throw as this is cleanup code
                System.err.println("Warning: Failed to delete temporary files: " + e.getMessage());
            }
        }
    }
}
