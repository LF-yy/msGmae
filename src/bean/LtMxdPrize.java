package bean;

public class LtMxdPrize {
    private long id;
    /**
     * 类型
     */
    private int type;

    /**
     * '物品编码'
     */
    private int itemid;
    /**
     * '价格
     */
    private int price;
    /**
     * '价格类型 0 = 金币 1= 点卷 2= 抵用 3= 元宝 4= 积分'
     */
    private int price_type;
    /**
     * '概率'
     */
    private int prob;
    /**
     * '数量'
     */
    private int counts;
    /**
     * '是否公告 0 不公告  1 公告'
     */
    private int notice;
    /**
     * '备注'
     */
    private String remark;

    public LtMxdPrize(long id, int type, int itemid, int price, int price_type, int prob, int counts, int notice, String remark) {
        this.id = id;
        this.type = type;
        this.itemid = itemid;
        this.price = price;
        this.price_type = price_type;
        this.prob = prob;
        this.counts = counts;
        this.notice = notice;
        this.remark = remark;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getItemid() {
        return itemid;
    }

    public void setItemid(int itemid) {
        this.itemid = itemid;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPrice_type() {
        return price_type;
    }

    public void setPrice_type(int price_type) {
        this.price_type = price_type;
    }

    public int getProb() {
        return prob;
    }

    public void setProb(int prob) {
        this.prob = prob;
    }

    public int getCounts() {
        return counts;
    }

    public void setCounts(int counts) {
        this.counts = counts;
    }

    public int getNotice() {
        return notice;
    }

    public void setNotice(int notice) {
        this.notice = notice;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
