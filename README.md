# geojson
Library to work with GeoJSON objects and perform spatial operations.
It can serialize and deserialize GeoJSON data to Features and FeatureCollections with [JTS](http://tsusiatsoftware.net/jts/main.html) geometries
so all JTS operations can be used with them. Like:
* Geometry Operations (merge, buffer, etc.)
* Triangulation
* STR-Trees
* ...

See: http://tsusiatsoftware.net/jts/jts-features.html for more info.

For de/serialization [Jackson](https://github.com/FasterXML/jackson) is used.

## Usage

### Example

```java
Feature myFeature = new Feature();
myFeature.set("attribute1", "bla");
myFeature.set("attribute2", "blub");
myFeature.setGeometry(GeoUtil.asWgs84(48, 9));
        
FeatureCollection<Feature> myFeatureCollection = new FeatureCollection<>();
myFeatureCollection.add(myFeature);    
```

you can also extend Feature and FeatureCollection like in [ExampleFeature.java](src/test/java/geojson/example/ExampleFeature.java)

##### Convert feature objects to String
```java
String json1 = Mapper.get().writeValueAsString(myFeatureCollection)
String json2 = Mapper.get().writeValueAsString(myFeature)
```
##### Write feature objects to File
```java
Mapper.get().writeValue(new FileOutputStream("path"), myFeatureCollection);
```

##### Read feature objects from File
```java
FeatureCollection fc = Mapper.get().readValue(new FileInputStream(
    "path"), FeatureCollection.class);
```
