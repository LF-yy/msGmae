package bean;


import java.io.Serializable;

/**
* 段伤物品加成
* @TableName ltt_item_additional_damage
*/
public class LttItemAdditionalDamage implements Serializable {

    /**
    * 
    */

    private Long id;
    /**
    * 物品id
    */
    private Integer itemId;
    /**
    * 物品名称
    */
    private String itemName;
    /**
    * 类型
    */
    private Long type;
    /**
    * 描述
    */
    private Integer remark;

    /**
    * 
    */
    public void setId(Long id){
    this.id = id;
    }

    /**
    * 物品id
    */
    public void setItemId(Integer itemId){
    this.itemId = itemId;
    }

    /**
    * 物品名称
    */
    public void setItemName(String itemName){
    this.itemName = itemName;
    }

    /**
    * 类型
    */
    public void setType(Long type){
    this.type = type;
    }

    /**
    * 描述
    */
    public void setRemark(Integer remark){
    this.remark = remark;
    }


    /**
    * 
    */
    public Long getId(){
    return this.id;
    }

    /**
    * 物品id
    */
    public Integer getItemId(){
    return this.itemId;
    }

    /**
    * 物品名称
    */
    public String getItemName(){
    return this.itemName;
    }

    /**
    * 类型
    */
    public Long getType(){
    return this.type;
    }

    /**
    * 描述
    */
    public Integer getRemark(){
    return this.remark;
    }


    public LttItemAdditionalDamage(Long id, Integer itemId, String itemName, Long type, Integer remark) {
        this.id = id;
        this.itemId = itemId;
        this.itemName = itemName;
        this.type = type;
        this.remark = remark;
    }
}
