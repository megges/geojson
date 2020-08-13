package geojson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

@SuppressWarnings({"WeakerAccess", "unused"})
@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NONE)
public class Feature extends GeoJsonObj {

    private Geometry geometry;

    // useful for subclasses that override the JsonTypInfo with NONE
    public String getType() {
        return "Feature";
    }

    @Override
    @JsonIgnore
    public Envelope getBbox() {
        if (super.getBbox() == null && getGeometry() != null)
            super.setBbox(getGeometry().getEnvelopeInternal());
        return super.getBbox();
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
}