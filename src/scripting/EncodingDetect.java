

package scripting;


import tools.BytesEncodingDetect;

import java.io.File;

public class EncodingDetect {
    public EncodingDetect() {
    }

    public static String getJavaEncode(String filePath) {
        return getJavaEncode(new File(filePath));
    }

    public static String getJavaEncode(File file) {
        BytesEncodingDetect s = new BytesEncodingDetect();
        String fileCode = BytesEncodingDetect.javaname[s.detectEncoding(file)];
        return fileCode;
    }
}
