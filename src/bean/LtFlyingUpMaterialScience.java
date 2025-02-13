package bean;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LtFlyingUpMaterialScience {

    private long id ;//` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    private  int max;//` int(11) NOT NULL DEFAULT '0' COMMENT '最大',
    private  int min;//` int(11) NOT NULL DEFAULT '0' COMMENT '最小',
    private  String item_list;//` varchar(500) NOT NULL  COMMENT '材料集合',
    private List<Integer> itemList;//` varchar(500) NOT NULL  COMMENT '材料集合',
    private  String item_count;//` varchar(500) NOT NULL  COMMENT '材料数量',
    private List<Integer> itemCount;//` varchar(500) NOT NULL  COMMENT '材料数量集合',
    public LtFlyingUpMaterialScience(long id, int max, int min, String item_list, String item_count) {
        this.id = id;
        this.max = max;
        this.min = min;
        this.item_list = item_list;
        setItemList(Arrays.stream(item_list.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList()));
        setItemCount(Arrays.stream(item_count.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList()));
    }

    public String getItem_count() {
        return item_count;
    }

    public void setItem_count(String item_count) {
        this.item_count = item_count;
    }

    public List<Integer> getItemCount() {
        return itemCount;
    }

    public void setItemCount(List<Integer> itemCount) {
        this.itemCount = itemCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public String getItem_list() {
        return item_list;
    }

    public void setItem_list(String item_list) {
        this.item_list = item_list;

        setItemList(Arrays.stream(item_list.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList()));
    }

    public List<Integer> getItemList() {
        return itemList;
    }

    public void setItemList(List<Integer> itemList) {
        this.itemList = itemList;
    }
}
