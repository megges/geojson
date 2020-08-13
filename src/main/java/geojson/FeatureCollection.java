package geojson;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.vividsolutions.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NONE)
public class FeatureCollection<T extends Feature> extends GeoJsonObj implements Iterable<T> {

    // useful for subclasses that override the JsonTypInfo with NONE
    public String getType() {
        return "FeatureCollection";
    }

    private List<T> features = new ArrayList<>();

    public List<T> getFeatures() {
        return features;
    }

    public void setFeatures(List<T> features) {
        this.features = features;
    }

    public FeatureCollection<T> add(T feature) {
        features.add(feature);
        return this;
    }

    public FeatureCollection<T> addAll(Collection<T> features) {
        this.features.addAll(features);
        return this;
    }

    @Override
    public Envelope getBbox() {
        if (super.getBbox() == null && features != null && features.size() > 0) {
            Envelope e = new Envelope();
            for (T feature : features) {
                if (feature.getBbox() != null)
                    e.expandToInclude(feature.getBbox());
            }
            super.setBbox(e);
        }
        return super.getBbox();
    }


    @Override
    public Iterator<T> iterator() {
        return features.iterator();
    }
}