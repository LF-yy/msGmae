package bean;


public class FiveTurn {

     private int charactersid;// int(11) NOT NULL   COMMENT '角色编码',
     private String occupationName;// VARCHAR(50)  DEFAULT NULL  COMMENT '5转职业名称',
     private int occupationId;// int(11)  DEFAULT NULL  COMMENT '5转编码',

    public int getCharactersid() {
        return charactersid;
    }

    public void setCharactersid(int charactersid) {
        this.charactersid = charactersid;
    }

    public String getOccupationName() {
        return occupationName;
    }

    public void setOccupationName(String occupationName) {
        this.occupationName = occupationName;
    }

    public int getOccupationId() {
        return occupationId;
    }

    public void setOccupationId(int occupationId) {
        this.occupationId = occupationId;
    }
}
