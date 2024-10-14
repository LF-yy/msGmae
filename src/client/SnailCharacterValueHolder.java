//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package client;

import client.inventory.IItem;

import java.util.ArrayList;

public class SnailCharacterValueHolder {
    public boolean 是否开店;
    public boolean 是否储备经验;
    public boolean 是否防滑;
    public ArrayList<IItem> 临时防滑鞋子 = new ArrayList();

    public SnailCharacterValueHolder(boolean 是否开店, boolean 是否储备经验, boolean 是否防滑, ArrayList<IItem> 临时防滑鞋子) {
        this.是否开店 = 是否开店;
        this.是否储备经验 = 是否储备经验;
        this.是否防滑 = 是否防滑;
        if (!this.临时防滑鞋子.isEmpty()) {
            this.临时防滑鞋子.clear();
        }

        this.临时防滑鞋子 = new ArrayList(临时防滑鞋子);
    }
}
