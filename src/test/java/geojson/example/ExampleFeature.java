package geojson.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import geojson.Feature;
import com.vividsolutions.jts.geom.Geometry;

import java.util.List;

public class ExampleFeature extends Feature {

    public ExampleFeature() {
    }

    public ExampleFeature(Geometry geometry, String name) {
        setGeometry(geometry);
        setName(name);
    }

    @JsonIgnore
    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name", name);
    }


    @JsonIgnore
    public List<String> getImageURLs() {
        return getAsList("images", String.class);
    }

    public void setImageURLs(List<String> images) {
        set("images", images);
    }
}
