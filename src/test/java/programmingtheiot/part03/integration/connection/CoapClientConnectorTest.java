package programmingtheiot.part03.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import programmingtheiot.common.DefaultDataMessageListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SystemStateData;
import programmingtheiot.gda.connection.CoapClientConnector;

/**
 * This test case class contains basic integration tests for
 * CoapClientConnector. It should be considered a starting point for
 * further testing and development.
 *
 * NOTE: The CoAP server must be running before executing these tests.
 */
public class CoapClientConnectorTest
{
    // static
    public static final int DEFAULT_TIMEOUT = 5;
    public static final boolean USE_DEFAULT_RESOURCES = true;

    private static final Logger _Logger =
        Logger.getLogger(CoapClientConnectorTest.class.getName());

    // member variables
    private CoapClientConnector coapClient = null;
    private IDataMessageListener dataMsgListener = null;

    // test setup methods

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Initialize any shared resources here if needed
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // Clean up shared resources here if needed
    }

    @Before
    public void setUp() throws Exception {
        // Initialize test-specific resources
        this.coapClient = new CoapClientConnector();
        this.dataMsgListener = new DefaultDataMessageListener();

        // Ensure the client has a data message listener set
        this.coapClient.setDataMessageListener(this.dataMsgListener);
    }

    @After
    public void tearDown() throws Exception {
        // Clean up resources after each test
        this.coapClient = null;
        this.dataMsgListener = null;
    }

    // test methods

    @Test
    public void testConnectAndDiscover()
    {
    	assertTrue(this.coapClient.sendDiscoveryRequest(DEFAULT_TIMEOUT));

    	// NOTE: If you are using a custom asynchronous discovery, include a brief wait here
    	try {
    		Thread.sleep(2000L);
    	} catch (InterruptedException e) {
    		// ignore
    	}
    }

    @Test
    public void testGetRequestCon()
    {
    	assertTrue(this.coapClient.sendGetRequest(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, true, DEFAULT_TIMEOUT));
    }
    	
    @Test
    public void testGetRequestNon()
    {
    	assertTrue(this.coapClient.sendGetRequest(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, false, DEFAULT_TIMEOUT));
    }

    @Test
    public void testPostRequestCon()
    {
    	int actionCmd = 2;
    	
    	SystemStateData ssd = new SystemStateData();
    	ssd.setCommand(actionCmd);
    	
    	String ssdJson = DataUtil.getInstance().systemStateDataToJson(ssd);
    	assertTrue(this.coapClient.sendPostRequest(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, null, true, ssdJson, DEFAULT_TIMEOUT));
    }
    	
    @Test
    public void testPostRequestNon()
    {
    	int actionCmd = 2;
    	
    	SystemStateData ssd = new SystemStateData();
    	ssd.setCommand(actionCmd);
    	
    	String ssdJson = DataUtil.getInstance().systemStateDataToJson(ssd);
    	assertTrue(this.coapClient.sendPostRequest(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, null, false, ssdJson, DEFAULT_TIMEOUT));
    }

    @Test
    public void testPutRequestCon()
    {
    	int actionCmd = 2;
    	
    	SystemStateData ssd = new SystemStateData();
    	ssd.setCommand(actionCmd);
    	
    	String ssdJson = DataUtil.getInstance().systemStateDataToJson(ssd);
    	assertTrue(this.coapClient.sendPutRequest(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, null, true, ssdJson, DEFAULT_TIMEOUT));
    }
    	
    @Test
    public void testPutRequestNon()
    {
    	int actionCmd = 2;
    	
    	SystemStateData ssd = new SystemStateData();
    	ssd.setCommand(actionCmd);
    	
    	String ssdJson = DataUtil.getInstance().systemStateDataToJson(ssd);
    	assertTrue(this.coapClient.sendPutRequest(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, null, false, ssdJson, DEFAULT_TIMEOUT));
    }

    @Test
    public void testDeleteRequestCon()
    {
    	assertTrue(this.coapClient.sendDeleteRequest(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, null, true, DEFAULT_TIMEOUT));
    }
    	
    @Test
    public void testDeleteRequestNon()
    {
    	assertTrue(this.coapClient.sendDeleteRequest(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, null, false, DEFAULT_TIMEOUT));
    }
}
