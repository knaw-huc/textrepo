package nl.knaw.huc.resources;

import nl.knaw.huc.api.FormContents;
import nl.knaw.huc.api.ResultContents;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.apache.commons.io.FilenameUtils.getExtension;

class ZipHandling {
  private static final Logger logger = LoggerFactory.getLogger(ZipHandling.class);

  private ZipHandling() {
  }

  static boolean isZip(
    FormDataBodyPart bodyPart,
    FormDataContentDisposition fileDetail
  ) {
    return "application/zip".equals(bodyPart.getMediaType().toString()) ||
        "zip".equals(getExtension(fileDetail.getFileName()));
  }

  static ArrayList<ResultContents> handleZipFile(
      InputStream uploadedInputStream,
      Function<FormContents, ResultContents> handleFile
  ) {
    var results = new ArrayList<ResultContents>();

    var inputStream = new ZipInputStream(uploadedInputStream);

    ZipEntry entry;
    var buffer = new ByteArrayOutputStream();
    try {
      while ((entry = inputStream.getNextEntry()) != null) {
        if (skipEntry(entry)) {
          continue;
        }
        var filename = entry.getName();
        logger.info("handle zipped [{}]", filename);

        inputStream.transferTo(buffer);
        var content = buffer.toByteArray();
        buffer.reset();
        var formFile = new FormContents(filename, content);
        var resultFile = handleFile.apply(formFile);
        results.add(resultFile);
      }
    } catch (IllegalArgumentException | IOException ex) {
      throw new BadRequestException("Zip could not be processed", ex);
    }

    return results;
  }

  private static boolean skipEntry(ZipEntry entry) {
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

  private static boolean isHiddenFile(ZipEntry entry) {
    var name = entry.getName();
    // Zip always uses / as the dir separator:
    var base = 1 + name.lastIndexOf('/');
    return base < name.length() && name.charAt(base) == '.';
  }

}
