package programmingtheiot.part03.integration.connection;

import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.WebLink;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.DefaultDataMessageListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.CoapServerGateway;

/**
 * This test case class contains basic integration tests for
 * CoapServerGateway. It serves as a starting point for testing
 * CoAP server integration.
 */
public class CoapServerGatewayTest
{
    // Static constants for the test
    public static final int DEFAULT_TIMEOUT = 300 * 1000;  // 5 minutes
    public static final boolean USE_DEFAULT_RESOURCES = true;
    
    private static final Logger _Logger = Logger.getLogger(CoapServerGatewayTest.class.getName());
    
    // Member variables for CoAP server and listener
    private CoapServerGateway csg = null;
    private IDataMessageListener dml = null;
    
    // Test setup method
    @Before
    public void setUp() throws Exception
    {
        // Initialize the default listener
        dml = new DefaultDataMessageListener();
    }
    
    // Test cleanup method
    @After
    public void tearDown() throws Exception
    {
        // Cleanup resources
        if (this.csg != null) {
            this.csg.stopServer();
        }
    }
    
    // Integration test method for running a simple CoAP server test
    @Test
    public void testRunSimpleCoapServerGatewayIntegration()
    {
        try {
            // CoAP URL to connect to the server
            String url = ConfigConst.DEFAULT_COAP_PROTOCOL + "://" + ConfigConst.DEFAULT_HOST + ":" + ConfigConst.DEFAULT_COAP_PORT;
            
            // Initialize and start the CoAP server with the listener
            this.csg = new CoapServerGateway(dml);
            this.csg.startServer();
            
            // Wait for the server to initialize before trying the client
            Thread.sleep(5000);
            
            // Create a CoAP client and perform resource discovery
            CoapClient clientConn = new CoapClient(url);
            
            // Perform resource discovery
            Set<WebLink> wlSet = clientConn.discover();
            
            if (wlSet != null) {
                for (WebLink wl : wlSet) {
                    _Logger.info(" --> WebLink: " + wl.getURI() + ". Attributes: " + wl.getAttributes());
                }
            }
            
            // Execute simple GET requests for predefined resources
            // NOTE: Update these with actual resource names in your environment
            clientConn.setURI(url + "/" + ConfigConst.PRODUCT_NAME);
            clientConn.get();
            
            clientConn.setURI(url + "/" + ConfigConst.PRODUCT_NAME + "/" + ConfigConst.CONSTRAINED_DEVICE);
            clientConn.get();
            
            clientConn.setURI(url + "/" + ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE.getResourceName());
            clientConn.get();
            
            // Wait for a while (2 minutes) to simulate some time for other app tests
            Thread.sleep(120000L);
            
            // Stop the server after the test
            this.csg.stopServer();
        } catch (Exception e) {
            _Logger.severe("Exception during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
