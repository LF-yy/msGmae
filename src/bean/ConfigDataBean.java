package bean;

import tools.Pair;

import java.util.List;

/**
 * This class represents the configuration data of the game.
 * 配置
 * @author <NAME>
 *         <p>
 *         This class contains the following fields:
 *         <ul>
 *         <li>setBonusTable: A list of pairs, where each pair contains a set number and a pair of strings and integers.
 *         The first string is the name of the set, the second string is the name of the bonus, and the integer is the
 *         bonus amount.
 *         </ul>
 */
public class ConfigDataBean implements java.io.Serializable {

    private  ConfigDataBean instance;
    /**
     * A list of pairs, where each pair contains a set number and a pair of strings and integers.
     * The first string is the name of the set, the second string is the name of the bonus, and the integer is the
     * bonus amount.
     * 套装配置
     */
    private List<Pair<Integer, Pair<String, Pair<String, Integer>>>> setBonusTable;


    public ConfigDataBean() {
    }

    public  ConfigDataBean getInstance(){
        if(instance==null){
            instance=new ConfigDataBean();
        }
        return instance;
    }


    public List<Pair<Integer, Pair<String, Pair<String, Integer>>>> getSetBonusTable() {
        return setBonusTable;
    }

    public void setSetBonusTable(List<Pair<Integer, Pair<String, Pair<String, Integer>>>> setBonusTable) {
        this.setBonusTable = setBonusTable;
    }

    @Override
    public String toString() {
        return "ConfigDataBean{" +
                "instance=" + instance +
                ", setBonusTable=" + setBonusTable +
                '}';
    }
}
