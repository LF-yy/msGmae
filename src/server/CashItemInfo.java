package server;

public class CashItemInfo
{
    private int itemId;
    private int count;
    private int price;
    private int sn;
    private int expire;
    private int gender;
    private boolean onSale;
    private int mod;
    
    public CashItemInfo(final int itemId, final int count, final int price, final int sn, final int expire, final int gender, final boolean sale, final int mod) {
        this.itemId = itemId;
        this.count = count;
        this.price = price;
        this.sn = sn;
        this.expire = expire;
        this.gender = gender;
        this.onSale = sale;
        this.mod = mod;
    }
    
    public CashItemInfo(final int itemId, final int count, final int price, final int sn, final int expire, final int gender, final boolean sale, final String name) {
        this.itemId = itemId;
        this.count = count;
        this.price = price;
        this.sn = sn;
        this.expire = expire;
        this.gender = gender;
        this.onSale = sale;
    }
    
    public CashItemInfo(final int sn, final int sale) {
        this.sn = sn;
        this.onSale = (sale > 0);
    }
    
    public int getId() {
        return this.itemId;
    }
    
    public int getCount() {
        return this.count;
    }
    
    public int getPrice() {
        return this.price;
    }
    
    public int getSN() {
        return this.sn;
    }
    
    public int getPeriod() {
        return this.expire;
    }
    
    public int getGender() {
        return this.gender;
    }
    
    public int getCsMod() {
        return this.mod;
    }
    
    public boolean onSale() {
        return this.onSale || (CashItemFactory.getInstance().getModInfo(this.sn) != null && CashItemFactory.getInstance().getModInfo(this.sn).showUp);
    }
    
    public boolean genderEquals(final int g) {
        return g == this.gender || this.gender == 2;
    }
    
    public int getItemId() {
        return this.itemId;
    }
    
    public int getOnSale() {
        if (this.onSale) {
            return 1;
        }
        return 0;
    }
    
    public static class CashModInfo
    {
        public int discountPrice;
        public int mark;
        public int priority;
        public int sn;
        public int itemid;
        public int flags;
        public int period;
        public int gender;
        public int count;
        public int meso;
        public int unk_1;
        public int unk_2;
        public int unk_3;
        public int extra_flags;
        public int mod;
        public boolean showUp;
        public boolean packagez;
        private CashItemInfo cii;
        private byte buyMode = 0;
        private int storage = -1;
        private int daily_storage = -1;

        public byte getBuyMode() {
            return this.buyMode;
        }

        public void setBuyMode(byte buyMode) {
            this.buyMode = buyMode;
        }

        public int getStorage() {
            return this.storage;
        }

        public void setStorage(int storage) {
            this.storage = storage;
        }
        public void setDaily_storage(int daily_storage) {
            this.daily_storage = daily_storage;
        }

        public int getDaily_storage() {
            return this.daily_storage;
        }

        public CashModInfo(int sn, int discount, int mark, boolean show, int itemid, int priority, boolean packagez, int period, int gender, int count, int meso, int unk_1, int unk_2, int unk_3, int extra_flags, int mod) {
            this.sn = sn;
            this.itemid = itemid;
            this.discountPrice = discount;
            this.mark = mark;
            this.showUp = show;
            this.priority = priority;
            this.packagez = packagez;
            this.period = period;
            this.gender = gender;
            this.count = count == 0 ? 1 : count;
            this.meso = meso;
            this.flags = 0;
            this.mod = mod;
            if (this.itemid > 0) {
                this.flags |= 1;
            }

            if (this.count > 0) {
                this.flags |= 2;
            }

            if (this.discountPrice > 0) {
                this.flags |= 4;
            }

            if (this.meso > 0) {
                this.flags |= 128;
            }

            if (this.gender >= 0) {
                this.flags |= 512;
            }

            if (this.showUp) {
                this.flags |= 1024;
            }

            if (this.mark > 0) {
                this.flags |= 2048;
            }

        }

        public CashItemInfo toCItem(CashItemInfo backup) {
            if (this.cii != null) {
                return this.cii;
            } else {
                int item;
                if (this.itemid <= 0) {
                    item = backup == null ? 0 : backup.getId();
                } else {
                    item = this.itemid;
                }

                int c;
                if (this.count <= 0) {
                    c = backup == null ? 0 : backup.getCount();
                } else {
                    c = this.count;
                }

                int price;
                if (this.meso <= 0) {
                    if (this.discountPrice <= 0) {
                        price = backup == null ? 0 : backup.getPrice();
                    } else {
                        price = this.discountPrice;
                    }
                } else {
                    price = this.meso;
                }

                int expire;
                if (this.period <= 0) {
                    expire = backup == null ? 0 : backup.getPeriod();
                } else {
                    expire = this.period;
                }

                int gen;
                if (this.gender < 0) {
                    gen = backup == null ? 0 : backup.getGender();
                } else {
                    gen = this.gender;
                }

                boolean onSale;
                if (!this.showUp) {
                    onSale = backup == null ? false : backup.onSale();
                } else {
                    onSale = this.showUp;
                }

                this.cii = new CashItemInfo(item, c, price, this.sn, expire, gen, onSale, this.mod);
                return this.cii;
            }
        }
    }
}
