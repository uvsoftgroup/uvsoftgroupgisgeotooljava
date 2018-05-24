/**
 * 
 */
package uvsoftgroupgisgeotool.fileloader;

import java.io.File;

import org.geotools.data.CachingFeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;

/**
 * @author planner
 *
 */
public class ShapFileImport {

	 public static void main(String[] args) throws Exception {
	        // display a data store file chooser dialog for shapefiles
	        File file = JFileDataStoreChooser.showOpenFile("shp", null);
	        if (file == null) {
	            return;
	        }

	        FileDataStore store = FileDataStoreFinder.getDataStore(file);
	        SimpleFeatureSource featureSource = store.getFeatureSource();
	        CachingFeatureSource cache = new CachingFeatureSource(featureSource);
	        // Create a map content and add our shapefile to it
	        MapContent map = new MapContent();
	        map.setTitle("Using cached features");
	        Style style = SLD.createSimpleStyle(featureSource.getSchema());
	        FeatureLayer layer = new FeatureLayer(cache, style);
	        map.addLayer(layer);
	        // Now display the map
	        JMapFrame.showMap(map);
	    }

}