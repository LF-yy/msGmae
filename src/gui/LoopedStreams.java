package gui;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PipedOutputStream;

public class LoopedStreams
{
    private PipedOutputStream pipedOS;
    private boolean keepRunning;
    private ByteArrayOutputStream byteArrayOS;
    private PipedInputStream pipedIS;
    
    public LoopedStreams() throws IOException {
        this.pipedOS = new PipedOutputStream();
        this.keepRunning = true;
        this.byteArrayOS = new ByteArrayOutputStream() {
            @Override
            public void close() {
                keepRunning = false;
                try {
                    super.close();
                    pipedOS.close();
                }
                catch (IOException e) {
                    System.exit(1);
                }
            }
        };
        this.pipedIS = new PipedInputStream() {
            @Override
            public void close() {
                keepRunning = false;
                try {
                    super.close();
                }
                catch (IOException e) {
                    System.exit(1);
                }
            }
        };
        this.pipedOS.connect(this.pipedIS);
        this.startByteArrayReaderThread();
    }
    
    public InputStream getInputStream() {
        return this.pipedIS;
    }
    
    public OutputStream getOutputStream() {
        return this.byteArrayOS;
    }
    
    /**
     * 启动一个独立的线程来处理字节数组的读取和传输
     * 该线程负责定期检查字节数组输出流（byteArrayOS）是否有数据
     * 如果有数据，则将其写入到管道输出流（pipedOS）中
     * 此机制用于在生产者和消费者之间异步传递数据，避免阻塞主线程
     */
    private void startByteArrayReaderThread() {
        // 创建并启动一个新的线程
        new Thread((Runnable)new Runnable() {
            @Override
            public void run() {
                // 线程运行的主循环，根据keepRunning标志决定是否继续运行
                while (keepRunning) {
                    // 检查是否有数据需要处理
                    if (byteArrayOS.size() > 0) {
                        byte[] buffer = null;
                        // 同步到byteArrayOS对象，以确保线程安全
                        synchronized (byteArrayOS) {
                            // 获取当前字节数组并重置输出流以准备下一次写入
                            buffer = byteArrayOS.toByteArray();
                            byteArrayOS.reset();
                        }
                        try {
                            // 将字节数组写入管道输出流中
                            pipedOS.write(buffer, 0, buffer.length);
                        }
                        catch (IOException e) {
                            // 如果发生IO异常，终止程序运行
                            System.exit(1);
                        }
                    }
                    else {
                        try {
                            // 如果没有数据，线程休眠1秒以避免忙等待
                            Thread.sleep(1000L);
                        }
                        catch (InterruptedException ex) {
                            // 如果线程被中断，不做任何处理
                        }
                    }
                }
            }
        }).start();
    }
}
