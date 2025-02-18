//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package snail;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MonsterKillQuest {
    HashMap<Integer, MonsterKill> monsterKillMap = new HashMap<>();
    int questId;
    int chrId;
    Timestamp beginTime;
    Timestamp finishTime;

    public MonsterKillQuest(int chrId, int questId) {
        this.questId = questId;
        this.chrId = chrId;
        this.beginTime = new Timestamp(System.currentTimeMillis());
    }

    public MonsterKillQuest(int chrId, int questId, HashMap<Integer, MonsterKill> monsterKillMap) {
        this.monsterKillMap = monsterKillMap;
        this.questId = questId;
        this.chrId = chrId;
        this.beginTime = new Timestamp(System.currentTimeMillis());
    }

    public HashMap<Integer, MonsterKill> getMonsterKillMap() {
        return this.monsterKillMap;
    }

    public void clearMonsterKillMap() {
        this.monsterKillMap.clear();
    }

    public void addMonsterKill(int monsterId, int nowQuantity, int finishQuantity) {
        if (this.monsterKillMap.containsKey(monsterId)) {
            ((MonsterKill)this.monsterKillMap.get(monsterId)).setFinishQuantity(finishQuantity);
            ((MonsterKill)this.monsterKillMap.get(monsterId)).setNowQuantity(nowQuantity);
        } else {
            this.monsterKillMap.put(monsterId, new MonsterKill(monsterId, nowQuantity, finishQuantity));
        }

    }

    public boolean deleteMonsterKill(int monsterId) {
        if (this.monsterKillMap.containsKey(monsterId)) {
            this.monsterKillMap.remove(monsterId);
            return true;
        } else {
            return false;
        }
    }

    public boolean addCount(int monsterId, int count) {
        if (this.monsterKillMap.containsKey(monsterId)) {
            ((MonsterKill)this.monsterKillMap.get(monsterId)).gainNowQuantity(count);
            if (this.getFinishTime() == null && this.isFinished()) {
                this.setFinishTime(new Timestamp(System.currentTimeMillis()));
            }

            return true;
        } else {
            return false;
        }
    }

    public int getQuestId() {
        return this.questId;
    }

    public void setQuestId(int questId) {
        this.questId = questId;
    }

    public int getChrId() {
        return this.chrId;
    }

    public void setChrId(int chrId) {
        this.chrId = chrId;
    }

    public boolean hasMonster(int monsterId) {
        return this.monsterKillMap.containsKey(monsterId);
    }

    public int getNowQuantity(int monsterId) {
        return this.monsterKillMap.containsKey(monsterId) ? ((MonsterKill)this.monsterKillMap.get(monsterId)).getNowQuantity() : 0;
    }

    public int getFinishQuantity(int monsterId) {
        return this.monsterKillMap.containsKey(monsterId) ? ((MonsterKill)this.monsterKillMap.get(monsterId)).getFinishQuantity() : 0;
    }

    public boolean isFinished(int monsterId) {
        int finishQuantity = this.getFinishQuantity(monsterId);
        int nowQuantity = this.getNowQuantity(monsterId);
        return finishQuantity > 0 && nowQuantity >= finishQuantity;
    }

    public boolean isFinished() {
        boolean isFinished = true;
        Iterator var2 = this.monsterKillMap.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<Integer, MonsterKill> entry = (Map.Entry)var2.next();
            if (!this.isFinished((Integer)entry.getKey())) {
                isFinished = false;
                break;
            }
        }

        return isFinished;
    }

    public Timestamp getFinishTime() {
        return this.finishTime;
    }

    public void setFinishTime(Timestamp finishTime) {
        this.finishTime = finishTime;
    }

    public Timestamp getBeginTime() {
        return this.beginTime;
    }

    public void setBeginTime(Timestamp beginTime) {
        this.beginTime = beginTime;
    }

    public class MonsterKill {
        int monsterId;
        int finishQuantity;
        int nowQuantity;

        public MonsterKill(int monsterId, int nowQuantity, int finishQuantity) {
            this.monsterId = monsterId;
            this.finishQuantity = finishQuantity;
            this.nowQuantity = nowQuantity;
        }

        public int getMonsterId() {
            return this.monsterId;
        }

        public void setMonsterId(int monsterId) {
            this.monsterId = monsterId;
        }

        public int getFinishQuantity() {
            return this.finishQuantity;
        }

        public void setFinishQuantity(int finishQuantity) {
            this.finishQuantity = finishQuantity;
        }

        public int getNowQuantity() {
            return this.nowQuantity;
        }

        public void gainNowQuantity(int count) {
            this.nowQuantity += count;
        }

        public void setNowQuantity(int nowQuantity) {
            this.nowQuantity = nowQuantity;
        }
    }
}
