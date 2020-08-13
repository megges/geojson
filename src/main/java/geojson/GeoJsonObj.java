package geojson;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.vividsolutions.jts.geom.Envelope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "unused"})
@JsonTypeInfo(property = "type", use = Id.NAME)
@JsonSubTypes({@Type(Feature.class), @Type(FeatureCollection.class)})
@JsonSerialize(include = Inclusion.NON_NULL)
public class GeoJsonObj implements Serializable {

    private String id;
    private Crs crs;
    private Envelope bbox;

    @JsonSerialize(include = Inclusion.NON_NULL)
    private HashMap<String, Object> properties = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Crs getCrs() {
        return crs;
    }

    public void setCrs(Crs crs) {
        this.crs = crs;
    }


    @JsonIgnore // do not expect the client to calculate the Bbox so ignore when serializing
    public Envelope getBbox() {
        return bbox;
    }

    @JsonProperty // but use for deserialization
    public void setBbox(Envelope bbox) {
        this.bbox = bbox;
    }

    public void set(String key, Object value) {
        properties.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) properties.get(key);
    }

    /**
     * More robust version of get(key)
     */
    public <T> T get(String key, Class<T> type) {
        return Mapper.get().convertValue(properties.get(key), type);
    }

    /**
     * More robust version of get(key)
     */
    public <T> T get(String key, TypeReference<T> typeReference) {
        return Mapper.get().convertValue(properties.get(key), typeReference);
    }

    /**
     * Property as typed list (instead of LinkedHashMap)
     */
    public <T> List<T> getAsList(String key, Class<T> type) {
        JavaType javaType = Mapper.get().getTypeFactory().constructCollectionType(List.class, type);
        List<T> list = Mapper.get().convertValue(properties.get(key), javaType);
        if (list == null) {
            list = new ArrayList<>();
            set(key, list);
        }
        return list;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public Map<String, Object> getPropertiesCopy() { return (HashMap) properties.clone(); }

    public void setProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
    }

}
