/**
 * 
 */
package uvsoftgroupgisgeotool.fileloader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JDataStoreWizard;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.swing.table.FeatureCollectionTableModel;
import org.geotools.swing.wizard.JWizard;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;


/**
 * @author planner
 *
 */
public class QueryLab extends JFrame {
	private static final long serialVersionUID = 1L;
	private DataStore dataStore;
    private JComboBox featureTypeCBox;
    private JTable table;
    private JTextField text;
    
    public QueryLab() {
    	setTitle("Java based GIS Data Processes and Visualizations");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        text = new JTextField(80);
        text.setText("include"); // include selects everything!
        getContentPane().add(text, BorderLayout.NORTH);

        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setModel(new DefaultTableModel(50, 50));
        table.setPreferredScrollableViewportSize(new Dimension(500,500));

        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JMenuBar menubar = new JMenuBar();
        menubar.setBackground(Color.getHSBColor(225f, 25f, 45f));
        setJMenuBar(menubar);

        JMenu fileMenu = new JMenu("Data Load Selection");
        JMenu mapMenu = new JMenu("Map View");
        JMenu toolsMenu = new JMenu("Coversion Tools");
        JMenu helpMenu = new JMenu("Help");
        JMenu dataMenu = new JMenu("Data View Selection");
        JMenu exitMenu = new JMenu("Exit");
 
        menubar.add(fileMenu);
        featureTypeCBox = new JComboBox();
        menubar.add(featureTypeCBox);
        menubar.add(dataMenu);
        menubar.add(mapMenu);
        menubar.add(toolsMenu);
        menubar.add(helpMenu);
        menubar.add(exitMenu);
        pack();
        
        exitMenu.add(new SafeAction("Exit") {
			private static final long serialVersionUID = 1L;
			public void action(ActionEvent e) throws Throwable {
                System.exit(0);
            }
        });
        
        mapMenu.add(new SafeAction("Map View") {
			private static final long serialVersionUID = 1L;
			public void action(ActionEvent e) throws Throwable {
				SelectionLab selectionLab = new SelectionLab();
		        File file = JFileDataStoreChooser.showOpenFile("shp", null);
		        if (file == null) {
		            return;
		        }
		        selectionLab.displayShapefile(file);
            }
        });
        
        
        fileMenu.add(new SafeAction("Open Shapefile") {
			private static final long serialVersionUID = 1L;
			public void action(ActionEvent e) throws Throwable {
                connect(new ShapefileDataStoreFactory());
            }
        });
        fileMenu.add(new SafeAction("Connect to PostGIS Database") {
			private static final long serialVersionUID = 1L;
			public void action(ActionEvent e) throws Throwable {
                connect(new PostgisNGDataStoreFactory());
            }
        });
        fileMenu.add(new SafeAction("Connect to DataStore") {
			private static final long serialVersionUID = 1L;
			public void action(ActionEvent e) throws Throwable {
                connect(null);
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(new SafeAction("Exit") {
			private static final long serialVersionUID = 1L;
			public void action(ActionEvent e) throws Throwable {
                System.exit(0);
            }
        });

        dataMenu.add(new SafeAction("Tabular Feature View") {
			private static final long serialVersionUID = 1L;
			public void action(ActionEvent e) throws Throwable {
                filterFeatures();
            }
        });
        dataMenu.add(new SafeAction("Feature Counter") {
			private static final long serialVersionUID = 1L;
			public void action(ActionEvent e) throws Throwable {
                countFeatures();
            }
        });
        dataMenu.add(new SafeAction("Feature Geometry") {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void action(ActionEvent e) throws Throwable {
                queryFeatures();
            }
        });
        
        toolsMenu.add(new SafeAction("SHP to JSON") {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void action(ActionEvent e) throws Throwable {
                connect(new ShapefileDataStoreFactory());
            }
        });
        
        toolsMenu.add(new SafeAction("SHP to KML") {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void action(ActionEvent e) throws Throwable {
                connect(new ShapefileDataStoreFactory());
            }
        });
        
        toolsMenu.add(new SafeAction("SHP to CSV") {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void action(ActionEvent e) throws Throwable {
                connect(new ShapefileDataStoreFactory());
            }
        });
        
        toolsMenu.add(new SafeAction("PostGIS Table to KML") {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void action(ActionEvent e) throws Throwable {
                connect(new ShapefileDataStoreFactory());
            }
        });
        
        toolsMenu.add(new SafeAction("PostGIS Table to CSV") {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void action(ActionEvent e) throws Throwable {
                connect(new ShapefileDataStoreFactory());
            }
        });
        
    }
    
    private void connect(DataStoreFactorySpi format) throws Exception {
        JDataStoreWizard wizard = new JDataStoreWizard(format);
        int result = wizard.showModalDialog();
        if (result == JWizard.FINISH) {
            Map<String, Object> connectionParameters = wizard.getConnectionParameters();
            dataStore = DataStoreFinder.getDataStore(connectionParameters);
            if (dataStore == null) {
                JOptionPane.showMessageDialog(null, "Could not connect - check parameters");
            }
            else{
            	JOptionPane.showMessageDialog(null, "Connect successful- check parameters!");	
            }
            updateUI();
        }
    }

    private void updateUI() throws Exception {
        ComboBoxModel cbm = new DefaultComboBoxModel(dataStore.getTypeNames());
        featureTypeCBox.setModel(cbm);
        table.setModel(new DefaultTableModel(50,50));
    }

    private void filterFeatures() throws Exception {
        String typeName = (String) featureTypeCBox.getSelectedItem();
        SimpleFeatureSource source = dataStore.getFeatureSource(typeName);
        Filter filter = CQL.toFilter(text.getText());
        SimpleFeatureCollection features = source.getFeatures(filter);
        FeatureCollectionTableModel model = new FeatureCollectionTableModel(features);
        table.setModel(model);
    }

    private void countFeatures() throws Exception {
        String typeName = (String) featureTypeCBox.getSelectedItem();
        SimpleFeatureSource source = dataStore.getFeatureSource(typeName);
        Filter filter = CQL.toFilter(text.getText());
        SimpleFeatureCollection features = source.getFeatures(filter);
        int count = features.size();
        if(count!=0){
        	JOptionPane.showMessageDialog(text, "Number of selected features:" + count);
        }
        else{
        	JOptionPane.showMessageDialog(text, "No Feature Found!:" + count);
        }  
    }

    private void queryFeatures() throws Exception {
        String typeName = (String) featureTypeCBox.getSelectedItem();
        SimpleFeatureSource source = dataStore.getFeatureSource(typeName);
        FeatureType schema = source.getSchema();
        String name = schema.getGeometryDescriptor().getLocalName();
        Filter filter = CQL.toFilter(text.getText());
        Query query = new Query(typeName, filter, new String[] { name });
        SimpleFeatureCollection features = source.getFeatures(query);
        FeatureCollectionTableModel model = new FeatureCollectionTableModel(features);
        table.setModel(model);
    }


   
    public static void main(String[] args) throws Exception {
        JFrame frame = new QueryLab();
        frame.setVisible(true);
    }
    
    
}
