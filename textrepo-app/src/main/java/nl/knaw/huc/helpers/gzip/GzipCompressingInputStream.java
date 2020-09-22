package nl.knaw.huc.helpers.gzip;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompressingInputStream extends InputStream {
  private final InputStream in;
  private final GZIPOutputStream gz;

  private byte[] buf = new byte[8192];
  private final byte[] readBuf = new byte[8192];
  private int read = 0;
  private int write = 0;

  public GzipCompressingInputStream(InputStream in) throws IOException {
    this.in = in;

    final OutputStream delegate = new OutputStream() {
      private void growBufferIfNeeded(int len) {
        if ((write + len) >= buf.length) {
          // grow the array if we don't have enough space to fulfill the incoming data
          byte[] enlarged = new byte[(buf.length + len) * 2];
          System.arraycopy(buf, 0, enlarged, 0, buf.length);
          buf = enlarged;
        }
      }

      @Override
      public void write(@Nonnull byte[] data, int off, int len) {
        growBufferIfNeeded(len);
        System.arraycopy(data, off, buf, write, len);
        write += len;
      }

      @Override
      public void write(int data) {
        growBufferIfNeeded(1);
        buf[write++] = (byte) data;
      }
    };
    this.gz = new GZIPOutputStream(delegate);
  }

  @Override
  public int read(@Nonnull byte[] data, int off, int len) throws IOException {
    compressStream();
    int numBytes = Math.min(len, write - read);
    if (numBytes > 0) {
      System.arraycopy(buf, read, data, off, numBytes);
      read += numBytes;
    } else if (len > 0) {
      // if bytes were requested, but we have none, then we're at the end of the stream
      return -1;
    }
    return numBytes;
  }

  @Override
  public int read() throws IOException {
    compressStream();
    if (write == 0) {
      // write should not be 0 if we were able to get data from compress stream, must mean we're at the end
      return -1;
    } else {
      // reading a single byte
      return buf[read++] & 0xFF;
    }
  }

  private void compressStream() throws IOException {
    // if the reader has caught up with the writer, then zero the positions out
    if (read == write) {
      read = 0;
      write = 0;
    }

    while (write == 0) {
      // feed the gzip stream data until it spits out a block
      int val = in.read(readBuf);
      if (val == -1) {
        // nothing left to do, we've hit the end of the stream. finalize and break out
        gz.close();
        break;
      } else if (val > 0) {
        gz.write(readBuf, 0, val);
      }
    }
  }
}
