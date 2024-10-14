//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.shuchu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class LoopedStreams {
    private PipedOutputStream pipedOS = new PipedOutputStream();
    private boolean keepRunning = true;
    private ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream() {
        public void close() {
            LoopedStreams.this.keepRunning = false;

            try {
                super.close();
                LoopedStreams.this.pipedOS.close();
            } catch (IOException var2) {
                System.exit(1);
            }

        }
    };
    private PipedInputStream pipedIS = new PipedInputStream() {
        public void close() {
            LoopedStreams.this.keepRunning = false;

            try {
                super.close();
            } catch (IOException var2) {
                System.exit(1);
            }

        }
    };

    public LoopedStreams() throws IOException {
        this.pipedOS.connect(this.pipedIS);
        this.startByteArrayReaderThread();
    }

    public InputStream getInputStream() {
        return this.pipedIS;
    }

    public OutputStream getOutputStream() {
        return this.byteArrayOS;
    }

    private void startByteArrayReaderThread() {
        (new Thread(new Runnable() {
            public void run() {
                while(LoopedStreams.this.keepRunning) {
                    if (LoopedStreams.this.byteArrayOS.size() > 0) {
                        byte[] buffer = null;
                        byte[] bufferx;
                        synchronized(LoopedStreams.this.byteArrayOS) {
                            bufferx = LoopedStreams.this.byteArrayOS.toByteArray();
                            LoopedStreams.this.byteArrayOS.reset();
                        }

                        try {
                            LoopedStreams.this.pipedOS.write(bufferx, 0, bufferx.length);
                        } catch (IOException var4) {
                            System.exit(1);
                        }
                    } else {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException var6) {
                        }
                    }
                }

            }
        })).start();
    }
}
