/**
 * 
 */
package uvsoftgroupgisgeotool.fileloader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.ecql.ECQL;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

/**
 * @author A.Riaydh
 *
 */
public class UvsoftgroupQueryGeoTools {
private FileDataStore fileDataStore;
private DataStore dataStore;
private SimpleFeatureSource simpleFeatureSource;

/*
public UvsoftgroupQueryGeoTools() {
	try{
		initDataStore();
		initFeatureSource();
	}
	catch(Exception ex){
		throw new IllegalStateException("Fail to find the data source!", ex);
	}
}
*/
private void initDataStore() throws Exception{
	ClassLoader classLoader=UvsoftgroupQueryGeoTools.class.getClassLoader();
	URL fileUrl= classLoader.getResource("C://gis_dataset/KA_OSM/landuse.shp");
	fileDataStore=FileDataStoreFinder.getDataStore(fileUrl);	
}
private void initFeatureSource() throws Exception{
	simpleFeatureSource=fileDataStore.getFeatureSource();
	
}

public SimpleFeatureSource getFeatureSource() throws Exception{
	return simpleFeatureSource;
	
}


public SimpleFeatureCollection findFeatureByName() throws Exception{
	Filter filter=(Filter) ECQL.toFilter("columnName=value");
	SimpleFeatureCollection result=simpleFeatureSource.getFeatures(filter);
	return result;
	
}

public SimpleFeatureCollection findFeatureByGreaterThandArea() throws Exception{
	Filter filter=(Filter) ECQL.toFilter("columnName>=value");
	SimpleFeatureCollection result=simpleFeatureSource.getFeatures(filter);
	return result;
}

public SimpleFeatureCollection findFeatureContainPoint() throws Exception{
	Filter filter=(Filter) ECQL.toFilter("CONTAINS(the_geom, POINT(90.22,23,66))");
	SimpleFeatureCollection result=simpleFeatureSource.getFeatures(filter);
	return result;
	
}

public SimpleFeatureCollection findFeatureBoundingBoxSection() throws Exception{
	Filter filter=(Filter) ECQL.toFilter("BBOX(the_geom, 90.22,23.66,90.88,23.88)");
	SimpleFeatureCollection result=simpleFeatureSource.getFeatures(filter);
	return result;
	
}



public void postgisDatabaseConnection() throws Exception {
  
    simpleFeatureSource=postGisDatabaseConnectionParameter().getFeatureSource("points"); 
    
    if(simpleFeatureSource!=null){
    	SimpleFeatureType type =simpleFeatureSource.getSchema();
        System.out.println("       typeName: " + type.getTypeName());
        System.out.println("           name: " + type.getName());
        System.out.println("attribute count: " + type.getAttributeCount());	
    }
    else{
    	System.out.println("Fail to connection database!");	
    }

 }


private DataStore postGisDatabaseConnectionParameter() throws IOException{
    DataStore dataStore=null;
    Map<String,Object> params = new HashMap<>();
    params.put("dbtype", "postgis");
    params.put("host", "localhost");
    params.put("port", 5432);
    params.put("schema", "public");
    params.put("database", "ka_osm");
    params.put("user", "postgres");
    params.put("passwd", "planner81");
    dataStore = DataStoreFinder.getDataStore(params);
    return dataStore;
}

}
