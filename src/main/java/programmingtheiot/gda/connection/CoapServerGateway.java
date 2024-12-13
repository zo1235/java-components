package programmingtheiot.gda.connection;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
 
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.interceptors.MessageTracer;
import org.eclipse.californium.core.server.resources.Resource;
 
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.Resource;
//import org.eclipse.californium.tools.GUIClientFX;
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.handlers.GenericCoapResourceHandler;
import programmingtheiot.gda.connection.handlers.GetActuatorCommandResourceHandler;
import programmingtheiot.gda.connection.handlers.UpdateSystemPerformanceResourceHandler;
import programmingtheiot.gda.connection.handlers.UpdateTelemetryResourceHandler;
 
/**
* Shell representation of class for student implementation.
* 
*/
public class CoapServerGateway {
	// static
 
	private static final Logger _Logger = Logger.getLogger(CoapServerGateway.class.getName());
 
	// params
 
	private CoapServer coapServer = null;
 
	private IDataMessageListener dataMsgListener = null;
 
	
 
	// constructors
    /**
     * Constructor for initializing a CoapServerGateway.
     * 
     * @param dataMsgListener Listener for handling data messages.
     */
	public CoapServerGateway(IDataMessageListener dataMsgListener) {
		super();
 
		/*
		 * Basic constructor implementation provided. Change as needed.
		 */
 
		this.dataMsgListener = dataMsgListener;
 
		initServer();
	}
 
	
	// public methods
    /**
     * Adds a CoAP server resource to the gateway.
     * 
     * @param resourceType Type of the resource.
     * @param endName      End name of the resource.
     * @param resource     CoAP resource to be added.
     */
	public void addResource(ResourceNameEnum resourceType, String endName, Resource resource) {
		// TODO: while not needed for this exercise, you may want to include
		// the endName parameter as part of this resource chain creation process
 
		if (resourceType != null && resource != null) {
			// break out the hierarchy of names and build the resource
			// handler generation(s) as needed, checking if any parent already
			// exists - and if so, add to the existing resource
			createAndAddResourceChain(resourceType, resource);
		}
	}
 
 
	/**
     * Creates and adds a resource chain to the CoAP server.
     * 
     * @param resourceType Type of the resource.
     * @param resource     CoAP resource to be added.
     */
	private void createAndAddResourceChain(ResourceNameEnum resourceType, Resource resource) {
		_Logger.info("Adding server resource handler chain: " + resourceType.getResourceName());
 
		List<String> resourceNames = resourceType.getResourceNameChain();
		Queue<String> queue = new ArrayBlockingQueue<>(resourceNames.size());
 
		queue.addAll(resourceNames);
 
		// check if we have a parent resource
		Resource parentResource = this.coapServer.getRoot();
 
		// if no parent resource, add it in now (should be named "PIOT")
		if (parentResource == null) {
			parentResource = new CoapResource(queue.poll());
			this.coapServer.add(parentResource);
		}
 
		while (!queue.isEmpty()) {
			// get the next resource name
			String resourceName = queue.poll();
			Resource nextResource = parentResource.getChild(resourceName);
 
			if (nextResource == null) {
				if (queue.isEmpty()) {
					nextResource = resource;
					nextResource.setName(resourceName);
				} else {
					nextResource = new CoapResource(resourceName);
				}
 
				parentResource.add(nextResource);
			}
 
			parentResource = nextResource;
		}
	}
 
	/**
     * Checks if a resource with the given name exists in the server.
     * 
     * @param name Name of the resource to check.
     * @return True if the resource exists, otherwise false.
     */
	public boolean hasResource(String name) {
		return false;
	}
 
	/**
     * Sets the data message listener for the gateway.
     * 
     * @param listener Data message listener.
     */
	public void setDataMessageListener(IDataMessageListener listener) {
		if (listener != null) {
			this.dataMsgListener = listener;
		}
	}
 
	/**
     * Starts the CoAP server.
     * 
     * @return True if the server started successfully, otherwise false.
     */
	public boolean startServer() {
		try {
			if (this.coapServer != null) {
				this.coapServer.start();
 
				// for message logging
				for (Endpoint ep : this.coapServer.getEndpoints()) {
					ep.addInterceptor(new MessageTracer());
				}
 
				return true;
			} else {
				_Logger.warning("CoAP server START failed. Not yet initialized.");
			}
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to start CoAP server.", e);
		}
		return false;
	}
 
	/**
     * Stops the CoAP server.
     * 
     * @return True if the server stopped successfully, otherwise false.
     */
	public boolean stopServer()
 
	{
		try {
			if (this.coapServer != null) {
				this.coapServer.stop();
 
				return true;
			} else {
				_Logger.warning("CoAP server STOP failed. Not yet initialized.");
			}
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to stop CoAP server.", e);
		}
 
		return false;
	}
 
	// private methods
	/**
     * Creates a resource chain for the given resource name enumeration.
     * 
     * @param resource Type of the resource to create a chain for.
     * @return The created resource chain.
     */
 
	private Resource createResourceChain(ResourceNameEnum resource) {
		return null;
	}
 
	// private methods
    /**
     * Initializes the CoAP server with default resources.
     * 
     * @param resources Array of default resource names.
     */
	private void initServer(ResourceNameEnum... resources) {
		this.coapServer = new CoapServer();
 
		initDefaultResources();
	}
 
	/**
     * Initializes default resources for the CoAP server.
     */
	private void initDefaultResources() {
		// initialize pre-defined resources
		GetActuatorCommandResourceHandler getActuatorCmdResourceHandler = new GetActuatorCommandResourceHandler(
				ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE.getResourceType());
 
		if (this.dataMsgListener != null) {
			this.dataMsgListener.setActuatorDataListener(null, getActuatorCmdResourceHandler);
		}
 
		addResource(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, null, getActuatorCmdResourceHandler);
 
		UpdateTelemetryResourceHandler updateTelemetryResourceHandler = new UpdateTelemetryResourceHandler(
				ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE.getResourceType());
 
		updateTelemetryResourceHandler.setDataMessageListener(this.dataMsgListener);
 
		addResource(
				ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, null, updateTelemetryResourceHandler);
 
		UpdateSystemPerformanceResourceHandler updateSystemPerformanceResourceHandler = new UpdateSystemPerformanceResourceHandler(
				ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE.getResourceType());
 
		updateSystemPerformanceResourceHandler.setDataMessageListener(this.dataMsgListener);
 
		addResource(
				ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, null, updateSystemPerformanceResourceHandler);
	}
}