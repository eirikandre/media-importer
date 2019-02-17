package no.eidsa.importer;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilesService {

    private final Reporter reporter;
    private final Method currentMethod;

    public FilesService(Reporter reporter, Method method) {
        this.reporter = reporter;
        this.currentMethod = method;
    }

    public void startWork(String path, String target) {
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            List<Path> collect = paths.filter(this::isMediaFile).collect(Collectors.toList());

            int size = collect.size();

            int currentProcentage = 0;
            for (int i = 0; i < collect.size(); i++) {

                int currentProgres = i * 100 / size;

                if (currentProgres > currentProcentage) {
                    System.out.print("-");
                    currentProcentage = currentProgres;
                }

                try {
                    handleFile(collect.get(i), target);
                } catch (Exception e) {
                    System.out.println("Error while handeling file " + collect.get(i));
                    e.printStackTrace();
                }
            }
            System.out.println("|");

        } catch (IOException e) {
            System.out.println("Filed to walk files");
            e.printStackTrace();
        }
    }

    private boolean isMediaFile(Path file) {
        if (Files.isRegularFile(file)
                && !file.toFile().getName().startsWith(".")) {
            boolean recognized = file.toFile().getName().toUpperCase().endsWith(".NEF") ||
                    file.toFile().getName().toUpperCase().endsWith(".RAF") ||
                    file.toFile().getName().toUpperCase().endsWith(".JPG") ||
                    file.toFile().getName().toUpperCase().endsWith(".JPEG") ||
                    file.toFile().getName().toUpperCase().endsWith(".TIFF") ||
                    file.toFile().getName().toUpperCase().endsWith(".MOV") ||
                    file.toFile().getName().toUpperCase().endsWith(".PNG") ||
                    file.toFile().getName().toUpperCase().endsWith(".AAE") ||
                    file.toFile().getName().toUpperCase().endsWith(".M4V") ||
                    file.toFile().getName().toUpperCase().endsWith(".MP4");

            if (!recognized) {
                reporter.skippted("Not recognized file type: (" + file.toAbsolutePath() + ")");
            }

            return recognized;
        } else {
            return false;
        }
    }

    private void handleFile(Path currentFile, String target) throws Exception {
        LocalDateTime imageCreated = ExifUtils.readDateFromExif(currentFile)
                .orElse((ExifUtils.readDateFromFile(currentFile)
                        .orElseThrow()));

        Path newPathForImage = Paths.get(target, String.valueOf(imageCreated.getYear()),
                String.format("%02d", imageCreated.getMonthValue()),
                String.format("%02d", imageCreated.getDayOfMonth()),
                currentFile.toFile().getName());

        File newImageFile = newPathForImage.toFile();

        if (!Method.dry.equals(currentMethod)) {
            newImageFile.getParentFile().mkdirs();
        }

        if (newImageFile.exists()) {
            checkHash(currentFile, newImageFile);
        } else {
            if (!Method.dry.equals(currentMethod)) {
                doFileTransfer(currentFile, newPathForImage);
            }
            reporter.copied(currentFile.toString() + " -> " + newImageFile.getAbsolutePath());
        }
    }

    private void doFileTransfer(Path currentFile, Path newPathForImage) {
        try {
            if (Method.copy.equals(currentMethod)) {
                Files.copy(Paths.get(currentFile.toString()), newPathForImage, StandardCopyOption.COPY_ATTRIBUTES);
            } else if (Method.move.equals(currentMethod)) {
                Files.move(Paths.get(currentFile.toString()), newPathForImage, StandardCopyOption.ATOMIC_MOVE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            reporter.skippted("Failed to currentMethod file " + currentFile.toString());
        }
    }

    private void checkHash(Path currentFile, File newImageFile) {
        String currentFileHash = generateHash(currentFile.toFile());
        String newImageFileHash = generateHash(newImageFile);

        if (currentFileHash.equals(newImageFileHash)) {
            reporter.skippted(currentFile.toString() + " == " + newImageFile.getAbsolutePath());
        } else {
            if (!Method.dry.equals(currentMethod)) {
                Path conflictPath = Paths.get(newImageFile.getParent(), "conflict");
                conflictPath.toFile().mkdirs();
                Path newConflictedFile = Paths.get(conflictPath.toString(), newImageFile.getName());

                doFileTransfer(currentFile, newConflictedFile);
                reporter.skippted(currentFile.toString() + " != " + newImageFile.getAbsolutePath() + ". Moved to " + newConflictedFile.toString());
            } else {
                reporter.skippted(currentFile.toString() + " != " + newImageFile.getAbsolutePath());
            }

        }
    }

    private String generateHash(File file) {
        try (FileInputStream fin = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fin); //used to get MD5
        } catch (Exception e) {
            throw new RuntimeException("Failed to create hash for " + file.getAbsolutePath());
        }
    }

    public void printReport() {
        reporter.printReport();
    }

}
