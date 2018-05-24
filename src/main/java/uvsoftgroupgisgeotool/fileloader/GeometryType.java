/**
 * 
 */
package uvsoftgroupgisgeotool.fileloader;

import org.geotools.geometry.jts.JTSFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author planner
 *
 */
public class GeometryType {
	 public static void main(String[] args) throws Exception {
		 
		 GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory( null );
		 //Point
		 WKTReader reader = new WKTReader(geometryFactory);
		 Point pointwkt =(Point)reader.read("POINT (1 1)");
		
		 Coordinate coord = new Coordinate( 1, 1 );
		 Point point = geometryFactory.createPoint(coord );
		 // Line
		 WKTReader readerLine = new WKTReader(geometryFactory );
		 LineString line =(LineString) readerLine.read("LINESTRING(0 2, 2 0, 8 6)");
		 Coordinate[] coords  =new Coordinate[] {new Coordinate(0, 2), new Coordinate(2, 0), new Coordinate(8, 6) };
		 LineString lineCoords = geometryFactory.createLineString(coords);
		 // Polygon
		 WKTReader readerPolygon = new WKTReader( geometryFactory );
		 Polygon polygon =(Polygon)readerPolygon.read("POLYGON((20 10, 30 0, 40 10, 30 20, 20 10))");
		 
		 System.out.println("print point:"+point);

}
}