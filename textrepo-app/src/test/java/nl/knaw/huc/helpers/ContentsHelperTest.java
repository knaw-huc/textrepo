package nl.knaw.huc.helpers;

import nl.knaw.huc.core.Contents;
import nl.knaw.huc.resources.rest.ContentsHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static javax.ws.rs.core.HttpHeaders.CONTENT_ENCODING;
import static org.assertj.core.api.Assertions.assertThat;

class ContentsHelperTest {
  // $ echo lorem | xxd
  // 00000000: 6c6f 7265 6d0a                           lorem.
  private static final byte[] LOREM_RAW = {
      0x6c, 0x6f, 0x72, 0x65, 0x6d, 0x0a
  };

  // $ echo 'lorem' | gzip -c - | xxd
  // 00000000: 1f8b 0800 02ba 865f 0003 cbc9 2f4a cde5  ......._..../J..
  // 00000010: 0200 3613 3a6a 0600 0000                 ..6.:j....
  private static final byte[] LOREM_GZ = {
      0x1f, (byte) 0x8b, 0x08, 0x00, 0x02, (byte) 0xba, (byte) 0x86, 0x5f,
      0x00, 0x03, (byte) 0xcb, (byte) 0xc9, 0x2f, 0x4a, (byte) 0xcd, (byte) 0xe5,
      0x02, 0x00, 0x36, 0x13, 0x3a, 0x6a, 0x06, 0x00, 0x00, 0x00
  };

  private static final String GZIP_ENCODING = "gzip";

  private static final Contents CONTENTS = new Contents("just-a-test-not-a-real-sha224", LOREM_GZ);
  private static final int SUFFICIENT_SPACE_TO_DECOMPRESS = LOREM_GZ.length * 2;
  private static final int INSUFFICIENT_SPACE_TO_DECOMPRESS = LOREM_GZ.length / 2;

  @Test
  void asAttachment_shouldNotCompress_whenSufficientSpaceToDecompress_AndNoCompressionRequested() {
    var sut = new ContentsHelper(SUFFICIENT_SPACE_TO_DECOMPRESS);
    final var responseBuilder = sut.asAttachment(CONTENTS, null);
    final var response = responseBuilder.build();
    assertThat(response.getHeaderString(CONTENT_ENCODING)).isNull();
    assertThat((byte[]) response.getEntity()).containsExactly(LOREM_RAW);
  }

  @Test
  void asAttachment_shouldCompress_whenInsufficientSpaceToDecompress_AndNoCompressionRequested() {
    var sut = new ContentsHelper(INSUFFICIENT_SPACE_TO_DECOMPRESS);
    final var responseBuilder = sut.asAttachment(CONTENTS, null);
    final var response = responseBuilder.build();
    assertThat(response.getHeaderString(CONTENT_ENCODING)).isEqualTo(GZIP_ENCODING);
    assertThat((byte[]) response.getEntity()).containsExactly(LOREM_GZ);
  }

  @ParameterizedTest
  @ValueSource(strings = {"gzip", "x-gzip", "deflate, gzip", "x-gzip, compress"})
  void asAttachment_shouldCompress_whenSupportedCompressionRequested(String acceptEncoding) {
    var sut = new ContentsHelper(SUFFICIENT_SPACE_TO_DECOMPRESS);
    final var responseBuilder = sut.asAttachment(CONTENTS, acceptEncoding);
    final var response = responseBuilder.build();
    assertThat(response.getHeaderString(CONTENT_ENCODING)).isEqualTo(GZIP_ENCODING);
    assertThat((byte[]) response.getEntity()).containsExactly(LOREM_GZ);
  }

  @ParameterizedTest
  @ValueSource(strings = {"br", "compress", "deflate"})
  void asAttachment_shouldNotCompress_whenUnsupportedCompressionRequested(String acceptEncoding) {
    var sut = new ContentsHelper(SUFFICIENT_SPACE_TO_DECOMPRESS);
    final var responseBuilder = sut.asAttachment(CONTENTS, acceptEncoding);
    final var response = responseBuilder.build();
    assertThat(response.getHeaderString(CONTENT_ENCODING)).isNull();
    assertThat((byte[]) response.getEntity()).containsExactly(LOREM_RAW);
  }
}
