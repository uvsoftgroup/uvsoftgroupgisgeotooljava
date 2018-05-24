package uvsoftgroupgisgeotool.fileloader;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JToolBar;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.styling.JSimpleStyleDialog;
import org.geotools.swing.tool.CursorTool;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
/**
 * 
 * @author A.Riaydh
 *
 */
public class SelectionLab {
    // Factories that we will use to create style and filter objects
    private StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    
    static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
    static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);

    //Convenient constants for the type of feature geometry in the shapefile
    private enum GeomType {POINT,LINE,POLYGON};
    //Some default style variables
    private static final Color LINE_COLOUR = Color.GRAY;
    private static final Color FILL_COLOUR = Color.GRAY;
    private static final Color SELECTED_COLOUR = Color.RED;
    private static final float OPACITY = 1.0f;
    private static final float LINE_WIDTH = 2.0f;
    private static final float POINT_SIZE = 10.0f;
    private JMapFrame mapFrame;
    private SimpleFeatureSource featureSource;
    private String geometryAttributeName;
    private GeomType geometryType;

    
    /**
     * This method connects to the shapefile; retrieves information about its features; creates a
     * map frame to display the shapefile and adds a custom feature selection tool to the toolbar of
     * the map frame.
     */
    public void displayShapefile(File file) throws Exception {
        FileDataStore fileDataStore = FileDataStoreFinder.getDataStore(file);
        featureSource =fileDataStore.getFeatureSource();
        setGeometry();
        MapContent map = new MapContent();
        map.setTitle("Map View and Feature Selection:"+file);
        
        
        //Style style = createDefaultStyle();
        
        Style style = createStyle(file, featureSource);
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);
        
        mapFrame = new JMapFrame(map);
        mapFrame.setSize(10,50);
       
        mapFrame.enableToolBar(true);
        mapFrame.enableStatusBar(true);
        mapFrame.setSize(1000, 500);

        /*
         * Before making the map frame visible we add a new button to its
         * toolbar for our custom feature selection tool
         */
        JToolBar toolBar = mapFrame.getToolBar();
        JButton btn = new JButton("Select Feature");
        toolBar.addSeparator();
        toolBar.add(btn);

        /*
         * When the user clicks the button we want to enable
         * our custom feature selection tool. Since the only
         * mouse action we are intersted in is 'clicked', and
         * we are not creating control icons or cursors here,
         * we can just create our tool as an anonymous sub-class
         * of CursorTool.
         */
        btn.addActionListener(e-> mapFrame.getMapPane()
                     .setCursorTool(
                      new CursorTool() {
                       @Override
                       public void onMouseClicked(MapMouseEvent ev) {
                       selectFeatures(ev);
                        }
                      }));
        mapFrame.setSize(600, 600);
        mapFrame.setVisible(true);
    }
    /**
     * This method is called by our feature selection tool when the user has clicked on the map.
     * @param ev the mouse event being handled
     */
    void selectFeatures(MapMouseEvent ev) {
        System.out.println("Mouse click Position: " + ev.getMapPosition());
        //Construct a 5x5 pixel rectangle centred on the mouse click position
        Point screenPos = ev.getPoint();
        Rectangle screenRect = new Rectangle(screenPos.x - 2, screenPos.y - 2, 5, 5);
        /*
         * Transform the screen rectangle into bounding box in the coordinate
         * reference system of our map context. Note: we are using a naive method
         * here but GeoTools also offers other, more accurate methods.
         */
        AffineTransform screenToWorld = mapFrame.getMapPane().getScreenToWorldTransform();
        Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
        ReferencedEnvelope bbox =new ReferencedEnvelope(worldRect, mapFrame.getMapContent().getCoordinateReferenceSystem());
        //Create a Filter to select features that intersect with the bounding box
        Filter filter = ff.intersects(ff.property(geometryAttributeName), ff.literal(bbox));
        // Use the filter to identify the selected features
        try {
            SimpleFeatureCollection selectedFeatures = featureSource.getFeatures(filter);
            Set<FeatureId> IDs = new HashSet<>();
            SimpleFeatureIterator iter = selectedFeatures.features();
              while (iter.hasNext()) {
                    SimpleFeature feature = iter.next();
                    IDs.add(feature.getIdentifier());
                    System.out.println("   " + feature.getIdentifier());
                }
            if (IDs.isEmpty()) {
                System.out.println("No Feature Selected!");
            }
            else{
            	 displaySelectedFeatures(IDs);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //Sets the display to paint selected features yellow and unselected features in the default style.@param IDs identifiers of currently selected features
    public void displaySelectedFeatures(Set<FeatureId> ids) {
        Style style;
        if (ids.isEmpty()) {
            style = createDefaultStyle();
        } else {
            style = createSelectedStyle(ids);
        }
        Layer layer = mapFrame.getMapContent().layers().get(0);
        ((FeatureLayer) layer).setStyle(style);
        mapFrame.getMapPane().repaint();
    }
    // Create a default Style for feature display
    private Style createDefaultStyle() {
        Rule rule = createRule(LINE_COLOUR, FILL_COLOUR);
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(rule);
        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }
    /**
     * Create a Style where features with given IDs are painted yellow, while others are painted
     * with the default colors.
     */
    private Style createSelectedStyle(Set<FeatureId> IDs) {
        Rule selectedRule = createRule(SELECTED_COLOUR, SELECTED_COLOUR);
        selectedRule.setFilter(ff.id(IDs));
        Rule otherRule = createRule(LINE_COLOUR, FILL_COLOUR);
        otherRule.setElseFilter(true);
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(selectedRule);
        fts.rules().add(otherRule);
        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }
    /**
     * Helper for createXXXStyle methods. Creates a new Rule containing a Symbolizer tailored to the
     * geometry type of the features that we are displaying.
     */
    private Rule createRule(Color outlineColor, Color fillColor) {
        Symbolizer symbolizer = null;
        Fill fill = null;
        Stroke stroke = sf.createStroke(ff.literal(outlineColor), ff.literal(LINE_WIDTH));
        switch (geometryType) {
            case POLYGON:
                fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));
                symbolizer = sf.createPolygonSymbolizer(stroke, fill, geometryAttributeName);
                break;
            case LINE:
                symbolizer = sf.createLineSymbolizer(stroke, geometryAttributeName);
                break;
            case POINT:
                fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));
                Mark mark = sf.getCircleMark();
                mark.setFill(fill);
                mark.setStroke(stroke);
                Graphic graphic = sf.createDefaultGraphic();
                graphic.graphicalSymbols().clear();
                graphic.graphicalSymbols().add(mark);
                graphic.setSize(ff.literal(POINT_SIZE));
                symbolizer = sf.createPointSymbolizer(graphic, geometryAttributeName);
        }
        Rule rule = sf.createRule();
        rule.symbolizers().add(symbolizer);
        return rule;
    }
   
    /** Retrieve information about the feature geometry */
    private void setGeometry() {
        GeometryDescriptor geomDesc = featureSource.getSchema().getGeometryDescriptor();
        geometryAttributeName = geomDesc.getLocalName();
        Class<?> clazz = geomDesc.getType().getBinding();
        if (Polygon.class.isAssignableFrom(clazz) || MultiPolygon.class.isAssignableFrom(clazz)) {
            geometryType = GeomType.POLYGON;
        } else if (LineString.class.isAssignableFrom(clazz) || MultiLineString.class.isAssignableFrom(clazz)) {
            geometryType = GeomType.LINE;
        } else {
            geometryType = GeomType.POINT;
        }
    }
    
    /**
     * Create a Style to display the features. If an SLD file is in the same
     * directory as the shapefile then we will create the Style by processing
     * this. Otherwise we display a JSimpleStyleDialog to prompt the user for
     * preferences.
     */
    private Style createStyle(File file, FeatureSource featureSource) {
        File sld = toSLDFile(file);
        if (sld != null) {
            return createFromSLD(sld);
        }

        SimpleFeatureType schema = (SimpleFeatureType)featureSource.getSchema();
        return JSimpleStyleDialog.showDialog(null, schema);
    }

    // docs end create style

    // docs start sld
    /**
     * Figure out if a valid SLD file is available.
     */
    public File toSLDFile(File file)  {
        String path = file.getAbsolutePath();
        String base = path.substring(0,path.length()-4);
        String newPath = base + ".sld";
        File sld = new File( newPath );
        if( sld.exists() ){
            return sld;
        }
        newPath = base + ".SLD";
        sld = new File( newPath );
        if( sld.exists() ){
            return sld;
        }
        return null;
    }

    /**
     * Create a Style object from a definition in a SLD document
     */
    private Style createFromSLD(File sld) {
        try {
            SLDParser stylereader = new SLDParser(styleFactory, sld.toURI().toURL());
            Style[] style = stylereader.readXML();
            return style[0];
            
        } catch (Exception e) {
            
        }
        return null;
    }
    
    /**
     * Here is a programmatic alternative to using JSimpleStyleDialog to
     * get a Style. This methods works out what sort of feature geometry
     * we have in the shapefile and then delegates to an appropriate style
     * creating method.
     */
    private Style createStyle2(FeatureSource featureSource) {
        SimpleFeatureType schema = (SimpleFeatureType)featureSource.getSchema();
        Class geomType = schema.getGeometryDescriptor().getType().getBinding();

        if (Polygon.class.isAssignableFrom(geomType)
                || MultiPolygon.class.isAssignableFrom(geomType)) {
            return createPolygonStyle();

        } else if (LineString.class.isAssignableFrom(geomType)
                || MultiLineString.class.isAssignableFrom(geomType)) {
            return createLineStyle();

        } else {
            return createPointStyle();
        }
    }

    /**
     * Create a Style to draw polygon features with a thin blue outline and
     * a cyan fill
     */
    private Style createPolygonStyle() {

        // create a partially opaque outline stroke
        Stroke stroke = styleFactory.createStroke(
                filterFactory.literal(Color.BLUE),
                filterFactory.literal(1),
                filterFactory.literal(0.5));

        // create a partial opaque fill
        Fill fill = styleFactory.createFill(
                filterFactory.literal(Color.CYAN),
                filterFactory.literal(0.5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
        PolygonSymbolizer sym = styleFactory.createPolygonSymbolizer(stroke, fill, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }
    
    /**
     * Create a Style to draw line features as thin blue lines
     */
    private Style createLineStyle() {
        Stroke stroke = styleFactory.createStroke(
                filterFactory.literal(Color.BLUE),
                filterFactory.literal(1));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
        LineSymbolizer sym = styleFactory.createLineSymbolizer(stroke, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

    /**
     * Create a Style to draw point features as circles with blue outlines
     * and cyan fill
     */
    private Style createPointStyle() {
        Graphic gr = styleFactory.createDefaultGraphic();

        Mark mark = styleFactory.getCircleMark();

        mark.setStroke(styleFactory.createStroke(
                filterFactory.literal(Color.BLUE), filterFactory.literal(1)));

        mark.setFill(styleFactory.createFill(filterFactory.literal(Color.CYAN)));

        gr.graphicalSymbols().clear();
        gr.graphicalSymbols().add(mark);
        gr.setSize(filterFactory.literal(5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
        PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }
   

}
