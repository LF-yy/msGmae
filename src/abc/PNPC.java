//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package abc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Properties;

public class PNPC {
    private static PNPC instance = null;
    private static boolean CANLOG;
    private Properties itempb_cfg = new Properties();
    private String PM;
    private static Logger log = LoggerFactory.getLogger(PNPC.class);

    public PNPC() {
        try {
            InputStreamReader is = new FileReader("脚本\\pnpc\\pnpc.ini");
            this.itempb_cfg.load(is);
            is.close();
            this.PM = this.itempb_cfg.getProperty("pnpc");
        } catch (Exception var2) {
            log.error("Could not configuration", var2);
        }

    }

    public String getPM() {
        return this.PM;
    }

    public boolean isCANLOG() {
        return CANLOG;
    }

    public void setCANLOG(boolean CANLOG) {
        PNPC.CANLOG = CANLOG;
    }

    public static PNPC getInstance() {
        if (instance == null) {
            instance = new PNPC();
        }

        return instance;
    }
}
