package geojson.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import geojson.FeatureCollection;

public class ExampleFeatureCollection extends FeatureCollection<ExampleFeature> {

    @JsonIgnore
    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name", name);
    }


}
