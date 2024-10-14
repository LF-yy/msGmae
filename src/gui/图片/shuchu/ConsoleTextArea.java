//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui.图片.shuchu;

import gui.服务端输出信息;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;

public class ConsoleTextArea extends JTextArea {
    public ConsoleTextArea(InputStream[] inStreams) {
        for(int i = 0; i < inStreams.length; ++i) {
            this.startConsoleReaderThread(inStreams[i]);
        }

    }

    public ConsoleTextArea() throws IOException {
        LoopedStreams ls = new LoopedStreams();
        PrintStream ps = new PrintStream(ls.getOutputStream());
        System.setOut(ps);
        System.setErr(ps);
        this.startConsoleReaderThread(ls.getInputStream());
    }

    private void startConsoleReaderThread(InputStream inStream) {
        final BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
        (new Thread(new Runnable() {
            public void run() {
                StringBuffer sb = new StringBuffer();

                try {
                    Document doc = ConsoleTextArea.this.getDocument();

                    String s;
                    while((s = br.readLine()) != null) {
                        boolean caretAtEnd = false;
                        caretAtEnd = ConsoleTextArea.this.getCaretPosition() == doc.getLength();
                        sb.setLength(0);
                        ConsoleTextArea.this.append(sb.append(s).append('\n').toString());
                        if (caretAtEnd) {
                            ConsoleTextArea.this.setCaretPosition(doc.getLength());
                        }
                    }
                } catch (IOException var5) {
                    JOptionPane.showMessageDialog((Component)null, "从BufferedReader读取错误：" + var5);
                    System.exit(1);
                }

            }
        })).start();
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("ConsoleTextArea测试");
        ConsoleTextArea consoleTextArea = null;

        try {
            consoleTextArea = new ConsoleTextArea();
        } catch (IOException var4) {
            服务端输出信息.println_err("不能创建LoopedStreams：" + var4);
            System.exit(1);
        }

        consoleTextArea.setFont(Font.decode("monospaced"));
        f.getContentPane().add(new JScrollPane(consoleTextArea), "Center");
        f.setBounds(50, 50, 300, 300);
        f.setVisible(true);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });
        startWriterTestThread("写操作线程 #1", System.err, 920, 50);
        startWriterTestThread("写操作线程 #2", System.out, 500, 50);
        startWriterTestThread("写操作线程 #3", System.out, 200, 50);
        startWriterTestThread("写操作线程 #4", System.out, 1000, 50);
        startWriterTestThread("写操作线程 #5", System.err, 850, 50);
    }

    private static void startWriterTestThread(final String name, final PrintStream ps, final int delay, final int count) {
        (new Thread(new Runnable() {
            public void run() {
                for(int i = 1; i <= count; ++i) {
                    ps.println("***" + name + ", hello !, i=" + i);

                    try {
                        Thread.sleep((long)delay);
                    } catch (InterruptedException var3) {
                    }
                }

            }
        })).start();
    }
}
