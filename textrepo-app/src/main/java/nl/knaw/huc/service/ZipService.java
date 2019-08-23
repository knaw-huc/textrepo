package nl.knaw.huc.service;

import nl.knaw.huc.api.FormFile;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.apache.commons.io.FilenameUtils.getExtension;

public class ZipService {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public static boolean isZip(
      FormDataBodyPart bodyPart,
      FormDataContentDisposition fileDetail
  ) {
    return "application/zip".equals(bodyPart.getMediaType().toString()) ||
        "zip".equals(getExtension(fileDetail.getFileName()));
  }

  public <T> List<T> handleZipFiles(
      InputStream uploadedInputStream,
      CheckedFunction<FormFile, T> handleFile
  ) {
    var results = new ArrayList<T>();

    var inputStream = new ZipInputStream(uploadedInputStream);
    var buffer = new byte[2048];

    ZipEntry entry;
    try {
      while ((entry = inputStream.getNextEntry()) != null) {
        if (skipEntry(entry)) {
          continue;
        }
        logger.info("handle zipped file [{}]", entry.getName());

        // add result only when new:
        try {
          results.add(handleEntry(inputStream, buffer, entry.getName(), handleFile));
        } catch (ExistsException e) {
          logger.info("skip existing [{}]", entry.getName());
        }

      }
    } catch (IllegalArgumentException | IOException ex) {
      throw new BadRequestException("Zip could not be processed", ex);
    }

    return results;
  }

  private <T> T handleEntry(
      ZipInputStream zis,
      byte[] buffer,
      String name,
      CheckedFunction<FormFile, T> handleContent
  ) throws IOException, ExistsException {
    try (var output = new ByteArrayOutputStream()) {
      int len;
      while ((len = zis.read(buffer)) > 0) {
        output.write(buffer, 0, len);
      }
      var content = output.toByteArray();
      return handleContent.apply(new FormFile(name, content));
    }
  }

  private boolean skipEntry(ZipEntry entry) {
    if (isHiddenFile(entry)) {
      logger.info("skip hidden file [{}]", entry.getName());
      return true;
    }
    if (entry.isDirectory()) {
      logger.info("skip directory [{}]", entry.getName());
      return true;
    }
    return false;
  }

  private boolean isHiddenFile(ZipEntry entry) {
    var name = entry.getName();
    // Zip always uses / as the dir separator:
    var base = 1 + name.lastIndexOf('/');
    return base < name.length() && name.charAt(base) == '.';
  }


}
