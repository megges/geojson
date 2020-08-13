package geojson;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Crs implements Serializable {

    private String type = "name";
    private Map<String, Object> properties = new HashMap<>();

    public Crs() {
        properties = new HashMap<>();
        properties.put("name", "urn:ogc:def:crs:OGC:1.3:CRS84");
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}