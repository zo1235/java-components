package programmingtheiot.gda.connection;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.elements.exception.ConnectorException;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

/**
 * Shell representation of class for student implementation.
 *
 */
public class CoapClientConnector implements IRequestResponseClient {
    // static
    private static final Logger _Logger = Logger.getLogger(CoapClientConnector.class.getName());

    // params
    private String protocol;
    private String host;
    private int port;
    private String serverAddr;
    private CoapClient clientConn;
    private IDataMessageListener dataMsgListener;

    // constructors

    /**
     * Default.
     * 
     * All config data will be loaded from the config file.
     */
    public CoapClientConnector() {
        ConfigUtil config = ConfigUtil.getInstance();
        this.host = config.getProperty(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);

        if (config.getBoolean(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.ENABLE_CRYPT_KEY)) {
            this.protocol = ConfigConst.DEFAULT_COAP_SECURE_PROTOCOL;
            this.port = config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.SECURE_PORT_KEY, ConfigConst.DEFAULT_COAP_SECURE_PORT);
        } else {
            this.protocol = ConfigConst.DEFAULT_COAP_PROTOCOL;
            this.port = config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_COAP_PORT);
        }

        this.serverAddr = this.protocol + "://" + this.host + ":" + this.port;
        initClient();

        _Logger.info("Using URL for server conn: " + this.serverAddr);
    }

    /**
     * Constructor.
     * 
     * @param host
     * @param isSecure
     * @param enableConfirmedMsgs
     */
    public CoapClientConnector(String host, boolean isSecure, boolean enableConfirmedMsgs) {
        // Optionally implement if necessary.
    }

    // public methods

    public boolean sendDiscoveryRequest1(int timeout) {
        _Logger.info("sendDiscoveryRequest() called with timeout = " + timeout);
        Set<WebLink> wlSet = null;
        try {
            wlSet = this.clientConn.discover();
        } catch (ConnectorException e) {
            _Logger.log(Level.SEVERE, "Connector Exception occurred during discovery", e);
        } catch (IOException e) {
            _Logger.log(Level.SEVERE, "IOException occurred during discovery", e);
        }

        if (wlSet != null) {
            for (WebLink wl : wlSet) {
                _Logger.info(" --> URI: " + wl.getURI() + ". Attributes: " + wl.getAttributes());
            }
        }
        return false;
    }

    @Override
    public boolean sendDeleteRequest(ResourceNameEnum resource, String name, boolean enableCON, int timeout) {
        return false;
    }
    
    public boolean sendGetRequest(ResourceNameEnum resource, boolean enableCON, int timeout) {
        CoapResponse response = null;

        try {
            if (enableCON) {
                this.clientConn.useCONs();
            } else {
                this.clientConn.useNONs();
            }

            this.clientConn.setURI(this.serverAddr + "/" + resource.getResourceName());
            response = this.clientConn.get();

            if (response != null) {
                // Log response details
                _Logger.info("Handling GET. Response: " + response.isSuccess() + " - " +
                        response.getOptions() + " - " + response.getCode() + " - " + response.getResponseText());

                // Notify the data message listener if applicable
                if (this.dataMsgListener != null) {
                    // Implement the listener logic here
                }

                return true;
            } else {
                _Logger.warning("Handling GET. No response received.");
            }

        } catch (IOException e) {
            _Logger.log(Level.SEVERE, "IOException occurred during GET request", e);
        } catch (ConnectorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return false;
    }

    @Override
    public boolean sendPostRequest(ResourceNameEnum resource, String name, boolean enableCON, String payload, int timeout) {
        return false;
    }

    @Override
    public boolean sendPutRequest(ResourceNameEnum resource, String name, boolean enableCON, String payload, int timeout) {
        return false;
    }

    @Override
    public boolean setDataMessageListener(IDataMessageListener listener) {
        this.dataMsgListener = listener;
        return true;
    }

    public void clearEndpointPath() {
    }

    public void setEndpointPath(ResourceNameEnum resource) {
    }

    @Override
    public boolean startObserver(ResourceNameEnum resource, String name, int ttl) {
        return false;
    }

    @Override
    public boolean stopObserver(ResourceNameEnum resourceType, String name, int timeout) {
        return false;
    }

    // private methods
    private void initClient() {
        try {
            this.clientConn = new CoapClient(this.serverAddr);
            _Logger.info("Created client connection to server / resource: " + this.serverAddr);
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to connect to broker: " + (this.clientConn != null ? this.clientConn.getURI() : this.serverAddr), e);
        }
    }

    @Override
    public boolean sendDiscoveryRequest(int timeout) {
        // TODO Auto-generated method stub
        return false;
    }

	@Override
	public boolean sendGetRequest(ResourceNameEnum resource, String name, boolean enableCON, int timeout) {
		// TODO Auto-generated method stub
		return false;
	}
}
