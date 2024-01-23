package server.custom.bankitem2;

public class BankItem2
{
    private long id;
    private int cid;
    private int itemid;
    private int count;
    private int type;

    public long getId() {
        return this.id;
    }
    
    public void setId(final long id) {
        this.id = id;
    }
    
    public int getCid() {
        return this.cid;
    }
    
    public void setCid(final int cid) {
        this.cid = cid;
    }
    
    public int getItemid() {
        return this.itemid;
    }
    
    public void setItemid(final int itemid) {
        this.itemid = itemid;
    }
    
    public int getCount() {
        return this.count;
    }
    
    public void setCount(final int count) {
        this.count = count;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
