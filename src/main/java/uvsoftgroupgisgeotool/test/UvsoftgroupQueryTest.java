package uvsoftgroupgisgeotool.test;

import uvsoftgroupgisgeotool.fileloader.UvsoftgroupQueryGeoTools;

public class UvsoftgroupQueryTest {
	
public static void main(String[] args )throws Exception{
	//testGetFeaturesTestCase();
	postgisDatabaseConnectionTestCase();
}

	
	
private static void postgisDatabaseConnectionTestCase() throws Exception{
	UvsoftgroupQueryGeoTools uv=new UvsoftgroupQueryGeoTools();
	uv.postgisDatabaseConnection();
}

private static void testGetFeaturesTestCase() throws Exception{
	UvsoftgroupQueryGeoTools uv=new UvsoftgroupQueryGeoTools();
	//uv.testGetFeatures();
}

}
