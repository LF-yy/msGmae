//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package abc;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class 任务修复 {
    private static 任务修复 instance = null;
    private static boolean CANLOG;
    private Properties itempb_cfg = new Properties();
    private String PM;
    private String PMM;
    private static Logger log = LoggerFactory.getLogger(任务修复.class);

    public 任务修复() {
        try {
            InputStreamReader is = new FileReader("scripts\\zevms\\QUEST.ini");
            this.itempb_cfg.load(is);
            is.close();
            this.PM = this.itempb_cfg.getProperty("PM");
            this.PMM = this.itempb_cfg.getProperty("PMM");
        } catch (Exception var2) {
            log.error("Could not configuration", var2);
        }

    }

    public String getPM() {
        return this.PM;
    }

    public String getPMM() {
        return this.PMM;
    }

    public boolean isCANLOG() {
        return CANLOG;
    }

    public void setCANLOG(boolean CANLOG) {
        任务修复.CANLOG = CANLOG;
    }

    public static 任务修复 getInstance() {
        if (instance == null) {
            instance = new 任务修复();
        }

        return instance;
    }
}
