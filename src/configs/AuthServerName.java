package configs;

public enum AuthServerName
{
    无双冒险岛("00-16-3E-00-38-32");
    
    String mac;
    
    private AuthServerName(final String mac) {
        this.mac = mac;
    }
    
    public String getMac() {
        return this.mac;
    }
    
    public static AuthServerName getName(final String mac) {
        for (final AuthServerName n : values()) {
            if (n.getMac().equals((Object)mac)) {
                return n;
            }
        }
        return null;
    }
}
