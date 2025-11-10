package com.fhtw.ocrworker.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class OcrProcessorService {
    private static final Logger log = LoggerFactory.getLogger(OcrProcessorService.class);

    private final MinioClient minioClient;

    public OcrProcessorService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void processPdf(String bucket, String objectName) throws Exception {
        File workDir = Files.createTempDirectory("ocr-work-").toFile();
        File pdfFile = new File(workDir, "input.pdf");
        try {
            // 1) Download PDF from MinIO
            downloadFromMinio(bucket, objectName, pdfFile);

            // 2) Convert PDF to PNG images using Ghostscript
            List<File> images = convertPdfToImages(pdfFile, workDir);
            if (images.isEmpty()) {
                throw new RuntimeException("No images were produced from the PDF conversion");
            }

            // 3) Run Tesseract on each image and collect text
            StringBuilder combined = new StringBuilder();
            for (File img : images) {
                String text = runTesseract(img);
                combined.append(text).append("\n");
            }

            // 4) Log the combined text
            log.info("[OCR RESULT] bucket={} object={} text=\n{}", bucket, objectName, combined.toString());
        } catch (Exception e) {
            log.error("OCR processing failed for bucket={} object={}", bucket, objectName, e);
            throw e;
        } finally {
            // Cleanup temp directory
            deleteRecursive(workDir);
        }
    }

    private void downloadFromMinio(String bucket, String objectName, File target) throws Exception {
        log.info("Downloading from MinIO: bucket={}, object={}", bucket, objectName);
        try (InputStream in = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build());
             FileOutputStream out = new FileOutputStream(target)) {
            in.transferTo(out);
        }
        if (!target.exists() || target.length() == 0) {
            throw new IOException("Downloaded file is empty or missing: " + target.getAbsolutePath());
        }
    }

    private List<File> convertPdfToImages(File pdf, File workDir) throws IOException, InterruptedException {
        // Ghostscript command: gs -dNOPAUSE -sDEVICE=png16m -r300 -o page-%03d.png input.pdf
        List<String> cmd = Arrays.asList(
                "gs", "-dNOPAUSE", "-dBATCH",
                "-sDEVICE=png16m",
                "-r300",
                "-o", "page-%03d.png",
                pdf.getAbsolutePath()
        );
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(workDir);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exit = p.waitFor();
        if (exit != 0) {
            log.error("Ghostscript failed (exit {}) output:\n{}", exit, output);
            throw new IOException("Ghostscript conversion failed with exit code: " + exit);
        }
        File[] files = workDir.listFiles((dir, name) -> name.startsWith("page-") && name.endsWith(".png"));
        List<File> list = new ArrayList<>();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            list.addAll(Arrays.asList(files));
        }
        return list;
    }

    private String runTesseract(File image) throws IOException, InterruptedException {
        // tesseract image stdout
        ProcessBuilder pb = new ProcessBuilder("tesseract", image.getAbsolutePath(), "stdout");
        pb.redirectErrorStream(true);
        Process p = pb.start();
        byte[] bytes = p.getInputStream().readAllBytes();
        int exit = p.waitFor();
        String output = new String(bytes, StandardCharsets.UTF_8);
        if (exit != 0) {
            log.error("Tesseract failed on {} (exit {}) output:\n{}", image.getName(), exit, output);
            throw new IOException("Tesseract failed with exit code: " + exit);
        }
        return output;
    }

    private void deleteRecursive(File f) {
        if (f == null || !f.exists()) return;
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File c : children) deleteRecursive(c);
            }
        }
        if (!f.delete()) {
            // try to mark for deletion on exit as a fallback
            f.deleteOnExit();
        }
    }
}
