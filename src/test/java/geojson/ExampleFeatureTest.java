package geojson;

import com.fasterxml.jackson.core.type.TypeReference;
import geojson.example.ExampleFeature;
import geojson.example.ExampleFeatureCollection;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class ExampleFeatureTest {

    private ExampleFeatureCollection collection;

    @Before
    public void setUp() throws Exception {
        ExampleFeature e1 = new ExampleFeature(GeoUtil.asWgs84(48, 9), "e1");
        ExampleFeature e2 = new ExampleFeature(GeoUtil.asWgs84(46, 9), "e2");

        collection = new ExampleFeatureCollection();
        collection.add(e1);
        collection.add(e2);
    }

    @Test
    public void serializeCollectionTest() throws Exception {
        // serialize to json string
        String collectionJson = Mapper.get().writeValueAsString(collection);

        // deserialize from json string
        ExampleFeatureCollection collection_ = Mapper.get().readValue(collectionJson, ExampleFeatureCollection.class);

        System.out.println(Mapper.get().writeValueAsString(collection_));

        // compare first initial FeatureCollection with deserialized FeatureCollection
        assertEquals(collection.getName(), collection_.getName());
        assertEquals(collection.getFeatures().size(), collection_.getFeatures().size());
    }


    @Test
    public void deserializeCollectionTest() throws Exception {
        // test with some GTFS stops in Portland
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/featurecollection.json");
        FeatureCollection<Feature> featureCollection = Mapper.get().readValue(resourceAsStream,
                new TypeReference<FeatureCollection<Feature>>() {
                });
        assertEquals(featureCollection.getFeatures().size(), 50);

        // all feature are points
        for (Feature feature : featureCollection) {
            assertEquals(feature.getGeometry().getGeometryType(), "Point");
        }
    }
}
