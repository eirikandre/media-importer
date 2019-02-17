package no.eidsa.importer;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ExifUtils {

    public static Optional<LocalDateTime> readDateFromExif(Path currentFile) throws Exception {

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(currentFile.toFile());

            for (Directory directory : metadata.getDirectories()) {
                if (directory instanceof ExifSubIFDDirectory) {

                    for (Tag tag : directory.getTags()) {
                        if (36867 == tag.getTagType()) {

                            DateTimeFormatter IMAGE_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

                            LocalDateTime pars = LocalDateTime.parse(tag.getDescription(), IMAGE_FORMATTER);
                            return Optional.of(pars);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Failed to find date from media file " + currentFile.toString() + " (" + e.getMessage() + ")");
        }
        return Optional.empty();
    }

    public static Optional<LocalDateTime> readDateFromFile(Path currentFile) {
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(currentFile, BasicFileAttributes.class);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }

        LocalDateTime imageCreated = attr.lastModifiedTime()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return Optional.of(imageCreated);
    }

}
