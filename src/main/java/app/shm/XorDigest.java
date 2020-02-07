package app.shm;

import java.nio.ByteBuffer;

public class XorDigest {
  int buf_mask;
  byte[] code;
  int idx;
  public XorDigest(int log_buf_length) {
    if (log_buf_length < 0) throw new IllegalArgumentException("log_buf_length");
    code = new byte[1 << log_buf_length];
    buf_mask = code.length - 1;
    idx = 0;
  }
  public void update(byte b) {
    code[idx] = b;
    idx = (idx + 1) & buf_mask;
  }
  public void update(byte[] b_array) {
    for (byte b: b_array) {
      update(b);
    }
  }
  public void update(ByteBuffer b) {
    while (b.hasRemaining())
      update(b.get());
  }
  public byte[] digest() {
    return code.clone();
  }
}
