//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package server.bean;

import client.MapleCharacter;
import client.inventory.IEquip;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import gui.LtMS;
import server.Start;
import tools.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Potential {
    public static int buffItemId = 2430793;
    public static int duration = 36000000;

    public Potential() {
    }

    public static int setPotentialLevel(MapleCharacter chr, short equipmentPosition, short level) {
        if (chr == null) {
            return 0;
        } else if (equipmentPosition >= 0) {
            return -1;
        } else {
            IEquip equip = (IEquip)chr.getInventory(MapleInventoryType.EQUIPPED).getItem(equipmentPosition);
            if (equip == null) {
                return -2;
            } else if (level >= 0 && level <= 4) {
                equip.setHpRR(level);
                return 1;
            } else {
                return -3;
            }
        }
    }

    public static int setPotentialLevel(IItem item, short level) {
        if (item == null) {
            return -2;
        } else {
            IEquip equip = (IEquip)item;
            if (equip == null) {
                return -2;
            } else if (level >= 0 && level <= 4) {
                equip.setHpRR(level);
                return 1;
            } else {
                return -3;
            }
        }
    }

    public static int getPotentialLevel(MapleCharacter chr, short equipmentPosition) {
        if (chr == null) {
            return 0;
        } else if (equipmentPosition >= 0) {
            return 0;
        } else {
            IEquip equip = (IEquip)chr.getInventory(MapleInventoryType.EQUIPPED).getItem(equipmentPosition);
            return equip == null ? 0 : equip.getHpRR();
        }
    }

    public static int getPotentialLevel(IItem item) {
        if (item == null) {
            return 0;
        } else {
            IEquip equip = (IEquip)item;
            return equip == null ? 0 : equip.getHpRR();
        }
    }

    public static int setPotential(MapleCharacter chr, short equipmentPosition, short potentialPosition, int potentialType, int potentialValue) {
        if (chr == null) {
            return 0;
        } else if (equipmentPosition >= 0) {
            return -1;
        } else {
            IItem item = chr.getInventory(MapleInventoryType.EQUIPPED).getItem(equipmentPosition);
            if (item == null) {
                return -2;
            } else {
                String potentials = item.getPotentials();
                int potentialQuantity = getPotentialQuantity(chr, equipmentPosition);
                int count;
                if (potentialPosition > potentialQuantity) {
                    int numToAdd = potentialPosition - potentialQuantity;
                    if (potentialQuantity == 0) {
                        potentials = "";
                    }

                    for(count = 0; count < numToAdd - 1; ++count) {
                        potentials = potentials + "0:0,";
                    }

                    potentials = potentials + potentialType + ":" + potentialValue + ",";
                } else {
                    String[] potentialList = potentials.split(",");
                    count = 0;

                    int i;
                    for(i = 0; i < potentialList.length; ++i) {
                        if (potentialList[i].length() > 0) {
                            ++count;
                            if (count == potentialPosition) {
                                potentialList[i] = potentialType + ":" + potentialValue;
                                break;
                            }
                        }
                    }

                    potentials = "";

                    for(i = 0; i < potentialList.length; ++i) {
                        if (potentialList[i].length() > 0) {
                            potentials = potentials + potentialList[i] + ",";
                        }
                    }
                }

                item.setPotentials(potentials);
                chr.道具存档();
                return 1;
            }
        }
    }

    public static int setPotential(IItem item, short potentialPosition, int potentialType, int potentialValue) {
        if (item == null) {
            return -2;
        } else {
            String potentials = item.getPotentials();
            int potentialQuantity = potentials.equals("") ? potentials.split(",").length - 1 : potentials.split(",").length;
            int count;
            if (potentialPosition > potentialQuantity) {
                int numToAdd = potentialPosition - potentialQuantity;
                if (potentialQuantity == 0) {
                    potentials = "";
                }

                for(count = 0; count < numToAdd - 1; ++count) {
                    potentials = potentials + "0:0,";
                }

                potentials = potentials + potentialType + ":" + potentialValue + ",";
            } else {
                String[] potentialList = potentials.split(",");
                count = 0;

                int i;
                for(i = 0; i < potentialList.length; ++i) {
                    if (potentialList[i].length() > 0) {
                        ++count;
                        if (count == potentialPosition) {
                            potentialList[i] = potentialType + ":" + potentialValue;
                            break;
                        }
                    }
                }

                potentials = "";

                for(i = 0; i < potentialList.length; ++i) {
                    if (potentialList[i].length() > 0) {
                        potentials = potentials + potentialList[i] + ",";
                    }
                }
            }

            item.setPotentials(potentials);
            return 1;
        }
    }

    public static int getPotentialQuantity(MapleCharacter chr, short equipmentPosition) {
        if (chr == null) {
            return 0;
        } else {
            IItem item = chr.getInventory(MapleInventoryType.EQUIPPED).getItem(equipmentPosition);
            if (item == null) {
                return 0;
            } else {
                String potentials = item.getPotentials();
                if (potentials != null && potentials.length() != 0) {
                    String[] potentialList = potentials.split(",");
                    int count = 0;
                    int realCount = 0;
                    String[] var7 = potentialList;
                    int var8 = potentialList.length;

                    for(int var9 = 0; var9 < var8; ++var9) {
                        String potential = var7[var9];
                        if (potential.length() > 0) {
                            ++count;
                            if (Integer.parseInt(potential.split(":")[0]) > 0) {
                                ++realCount;
                            }

                            if (count >= 10) {
                                break;
                            }
                        }
                    }

                    return realCount;
                } else {
                    return 0;
                }
            }
        }
    }

    public static int getPotentialQuantity(IItem item) {
        if (item == null) {
            return 0;
        } else {
            String potentials = item.getPotentials();
            if (potentials != null && potentials.length() != 0) {
                String[] potentialList = potentials.split(",");
                int count = 0;
                int realCount = 0;
                String[] var5 = potentialList;
                int var6 = potentialList.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    String potential = var5[var7];
                    if (potential.length() > 0) {
                        ++count;
                        if (Integer.parseInt(potential.split(":")[0]) > 0) {
                            ++realCount;
                        }

                        if (count >= 10) {
                            break;
                        }
                    }
                }

                return realCount;
            } else {
                return 0;
            }
        }
    }

    public static Map<Integer, Integer> getPotentialMap(MapleCharacter chr, short equipmentPosition) {
        if (chr == null) {
            return null;
        } else if (equipmentPosition >= 0) {
            return null;
        } else {
            IItem item = chr.getInventory(MapleInventoryType.EQUIPPED).getItem(equipmentPosition);
            if (item == null) {
                return null;
            } else {
                Map potentialMap = new HashMap();
                String potentials = item.getPotentials();
                String[] potentialList = potentials.split(",");

                for(int i = 0; i < potentialList.length; ++i) {
                    if (potentialList[i].length() > 0) {
                        String[] potentialString = potentialList[i].split(":");
                        if (potentialString.length >= 2 && Integer.parseInt(potentialString[0]) > 0) {
                            potentialMap.put(Integer.parseInt(potentialString[0]), Integer.parseInt(potentialString[1]));
                        }
                    }
                }

                return potentialMap;
            }
        }
    }

    public static Map<Integer, Integer> getPotentialMap(IItem item) {
        if (item == null) {
            return null;
        } else {
            Map potentialMap = new HashMap();
            String potentials = item.getPotentials();
            String[] potentialList = potentials.split(",");

            for(int i = 0; i < potentialList.length; ++i) {
                if (potentialList[i].length() > 0) {
                    String[] potentialString = potentialList[i].split(":");
                    if (potentialString.length >= 2 && Integer.parseInt(potentialString[0]) > 0) {
                        potentialMap.put(Integer.parseInt(potentialString[0]), Integer.parseInt(potentialString[1]));
                    }
                }
            }

            return potentialMap;
        }
    }

    public static ArrayList<Pair<Integer, Integer>> getPotentialList(MapleCharacter chr, short equipmentPosition) {
        if (chr == null) {
            return null;
        } else if (equipmentPosition >= 0) {
            return null;
        } else {
            IItem item = chr.getInventory(MapleInventoryType.EQUIPPED).getItem(equipmentPosition);
            if (item == null) {
                return null;
            } else {
                ArrayList<Pair<Integer, Integer>> list = new ArrayList();
                String potentials = item.getPotentials();
                String[] potentialList = potentials.split(",");

                for(int i = 0; i < potentialList.length; ++i) {
                    if (potentialList[i].length() > 0) {
                        String[] potentialString = potentialList[i].split(":");
                        if (potentialString.length >= 2 && Integer.parseInt(potentialString[0]) != 0 && Integer.parseInt(potentialString[1]) != 0) {
                            list.add(new Pair(Integer.parseInt(potentialString[0]), Integer.parseInt(potentialString[1])));
                        }
                    }
                }

                return list;
            }
        }
    }

    public static ArrayList<Pair<Integer, Integer>> getPotentialList(IItem item) {
        if (item == null) {
            return null;
        } else {
            ArrayList<Pair<Integer, Integer>> list = new ArrayList();
            String potentials = item.getPotentials();
            String[] potentialList = potentials.split(",");

            for(int i = 0; i < potentialList.length; ++i) {
                if (potentialList[i].length() > 0) {
                    String[] potentialString = potentialList[i].split(":");
                    if (potentialString.length >= 2 && Integer.parseInt(potentialString[0]) != 0 && Integer.parseInt(potentialString[1]) != 0) {
                        list.add(new Pair(Integer.parseInt(potentialString[0]), Integer.parseInt(potentialString[1])));
                    }
                }
            }

            return list;
        }
    }

    public static Map<Integer, Integer> getPotentialMap(MapleCharacter chr) {
        return chr == null ? null : chr.getPotentialMap();
    }

    public static String getPotentialName(int potentialType) {
        String[] potentialString = (String[]) Start.potentialListMap.get(potentialType);
        return potentialString != null && potentialString.length >= 2 ? potentialString[0] : "";
    }

    public static String getPotentialInfo(int potentialType) {
        return getPotentialInfo(potentialType, -1);
    }

    public static int getPotentialValue(MapleCharacter chr, int potentialType, short equipmentPosition) {
        if (chr == null) {
            return 0;
        } else {
            Map<Integer, Integer> potentialMap = getPotentialMap(chr, equipmentPosition);
            return potentialMap != null ? (Integer)potentialMap.get(potentialType) : 0;
        }
    }

    public static int getPotentialValue(IItem item, int potentialType) {
        Map<Integer, Integer> potentialMap = getPotentialMap(item);
        return potentialMap != null ? (Integer)potentialMap.get(potentialType) : 0;
    }

    public static int getPotentialValue(MapleCharacter chr, int potentialType) {
        if (chr == null) {
            return 0;
        } else {
            Map<Integer, Integer> potentialMap = getPotentialMap(chr);
            return potentialMap != null ? (Integer)potentialMap.get(potentialType) : 0;
        }
    }

    public static String getPotentialInfo(int potentialType, int potentialValue) {
        String[] potentialString = (String[])Start.potentialListMap.get(potentialType);
        if (potentialString != null && potentialString.length >= 2) {
            String info = potentialString[1];
            if (potentialValue < 0) {
                info = info.replace("%s", "?");
            } else {
                info = info.replace("%s", potentialValue + "");
            }

            return info;
        } else {
            return "";
        }
    }

    public static boolean isDamageSkill(int skillId) {
        int[] ids = new int[]{1001004, 1001005, 1111002, 1111003, 1111005, 1120003, 1121008, 1211002, 1221007, 1221009, 1221011, 1311001, 1311003, 1311006, 1321003, 2001005, 2101004, 2111002, 2111006, 2121003, 2121006, 2121007, 2121001, 2201004, 2201005, 2211002, 2211003, 2211006, 2221001, 2221003, 2221006, 2221007, 2301005, 2311004, 2321001, 2321007, 2321008, 3001004, 3001005, 3101005, 3110001, 3111003, 3111004, 3111006, 3121003, 3121004, 3221003, 3121007, 3201005, 3210001, 3211003, 3211004, 3211006, 3221001, 3221007, 4001002, 4001334, 4001344, 4111004, 4111005, 4121008, 4121007, 4201005, 4211002, 4211004, 4211006, 4221001, 4221007, 5001001, 5001002, 5001003, 5101002, 5101003, 5101004, 5201001, 5201002, 5201004, 5201006, 5111002, 5111006, 5210000, 5211002, 5211004, 5211005, 5211006, 5121001, 5121002, 5121004, 5121005, 5121007, 5220002, 5221003, 5221004, 5221007, 5221008, 21000002, 21100001, 21100002, 21100004, 21110002, 21110003, 21110004, 21110006, 21120005, 21120006, 1111008, 11111006, 11101002, 11121066, 11121056, 14001002, 11121072, 11111004, 11121058, 11121065, 13101005, 13101002, 11121061, 13111002, 13111000, 11121060, 14111005, 14101006, 11121064, 11001003, 11121057, 11121062, 15111007, 15101003, 15101005, 13001004, 15001002, 15001001, 11121054, 11001002, 11121059, 12101002, 12101006, 12001003, 12111004, 12101004, 12111006, 12111003};
        int[] var2 = ids;
        int var3 = ids.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            int id = var2[var4];
            if (id == skillId) {
                return true;
            }
        }

        return false;
    }

    public static boolean isCoolSkill(int skillId) {
        int[] ids = new int[]{1121010, 1121011, 1221012, 1321010, 2121008, 2221008, 2321005, 2321006, 2321009, 3121009, 3221008, 4221006, 4221008, 5101007, 5111005, 5121003, 5121008, 5121010, 5221010, 21121008};
        int[] var2 = ids;
        int var3 = ids.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            int id = var2[var4];
            if (id == skillId) {
                return true;
            }
        }

        return false;
    }

    public static boolean isPotentialExist(IItem item) {
        String potentials = item.getPotentials();
        String[] potentialList = potentials.split(",");
        boolean ret = false;

        for(int i = 0; i < potentialList.length; ++i) {
            if (potentialList[i].length() > 0) {
                String[] potentialString = potentialList[i].split(":");
                if (potentialString.length >= 2 && Integer.parseInt(potentialString[0]) > 0) {
                    ret = true;
                }
            }
        }

        return ret;
    }

    public static boolean isPotentialExist(IItem item, int beginPosition) {
        String potentials = item.getPotentials();
        String[] potentialList = potentials.split(",");
        boolean ret = false;
        if (potentialList.length >= beginPosition) {
            for(int i = beginPosition - 1; i < potentialList.length; ++i) {
                if (potentialList[i].length() > 0) {
                    String[] potentialString = potentialList[i].split(":");
                    if (potentialString.length >= 2 && Integer.parseInt(potentialString[0]) > 0) {
                        ret = true;
                    }
                }
            }
        }

        return ret;
    }
}
