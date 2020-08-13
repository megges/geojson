package geojson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CoordinateJsonDeserializerTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        objectMapper = Mapper.get();
    }

    @Test
    public void testSerializeDeserialize2D() throws Exception {
        Coordinate coord = new Coordinate(1.1, 2.2);
        String coordJson = objectMapper.writeValueAsString(coord);
        assertEquals("[1.1,2.2]", coordJson);
        Coordinate coord_ = objectMapper.readValue(coordJson, Coordinate.class);
        assertEquals(coord, coord_);
        assertEquals(1.1, coord_.x, 0);
        assertEquals(2.2, coord_.y, 0);
        assertEquals(coord_.z, Coordinate.NULL_ORDINATE, 0);
    }

//    @Test
//    public void testSerializeDeserialize3D() throws Exception {
//        Coordinate coord = new Coordinate(1.1, 2.2, 3.3);
//        String coordJson = objectMapper.writeValueAsString(coord);
//        assertEquals("[1.1,2.2,3.3]", coordJson);
//        Coordinate coord_ = objectMapper.readValue(coordJson, Coordinate.class);
//        assertTrue(coord.equals3D(coord_));
//        assertEquals(1.1, coord_.x, 0);
//        assertEquals(2.2, coord_.y, 0);
//        assertEquals(3.3, coord_.z, 0);
//    }
}
