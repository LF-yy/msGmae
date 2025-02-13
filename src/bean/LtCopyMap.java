package bean;

import java.io.Serializable;
import java.util.List;

public class LtCopyMap implements Serializable {
    private Long id;

    private Integer type;

    private String mapName;

    private Integer mapId;
    private List<LtCopyMapMonster> ltCopyMapMonster;

    public LtCopyMap(Long id, Integer type, String mapName, Integer mapId) {
        this.id = id;
        this.type = type;
        this.mapName = mapName;
        this.mapId = mapId;
    }

    public List<LtCopyMapMonster> getLtCopyMapMonster() {
        return ltCopyMapMonster;
    }

    public void setLtCopyMapMonster(List<LtCopyMapMonster> ltCopyMapMonster) {
        this.ltCopyMapMonster = ltCopyMapMonster;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public Integer getMapId() {
        return mapId;
    }

    public void setMapId(Integer mapId) {
        this.mapId = mapId;
    }
}
