//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class 消息窗口监听 implements DocumentListener {
    服务端输出信息 view;

    public 消息窗口监听() {
    }

    public void setView(服务端输出信息 view) {
        this.view = view;
    }

    public void insertUpdate(DocumentEvent documentEvent) {
        final int maxline = 10000;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int end;
                if (服务端输出信息.标准输出.getLineCount() >= maxline) {
                    end = 0;

                    try {
                        end = 服务端输出信息.标准输出.getLineEndOffset(0);
                    } catch (Exception var4) {
                    }

                    服务端输出信息.标准输出.replaceRange("", 0, end);
                }

                if (服务端输出信息.错误输出.getLineCount() >= maxline) {
                    end = 0;

                    try {
                        end = 服务端输出信息.错误输出.getLineEndOffset(0);
                    } catch (Exception var3) {
                    }
                    服务端输出信息.错误输出.replaceRange("", 0, end);
                }

            }
        });
    }

    public void removeUpdate(DocumentEvent documentEvent) {
        this.changedUpdate(documentEvent);
    }

    public void changedUpdate(DocumentEvent documentEvent) {
    }
}
