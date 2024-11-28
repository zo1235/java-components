package programmingtheiot.gda.app;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
 
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
 
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.BaseIotData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.data.SystemStateData;
import programmingtheiot.gda.connection.CloudClientConnector;
 
import programmingtheiot.gda.connection.CoapServerGateway;
import programmingtheiot.gda.connection.IPersistenceClient;
import programmingtheiot.gda.connection.IPubSubClient;
import programmingtheiot.gda.connection.IRequestResponseClient;
import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.gda.connection.RedisPersistenceAdapter;
import programmingtheiot.gda.connection.SmtpClientConnector;
import programmingtheiot.gda.system.SystemPerformanceManager;

public class DeviceDataManager implements IDataMessageListener
{
	// static
	private static final Logger _Logger =
		Logger.getLogger(DeviceDataManager.class.getName());
	// private var's
	private boolean enableMqttClient = false;
	private boolean enableCoapServer = true;
	private boolean enableCloudClient = true;
	private boolean enableSmtpClient = false;
	private boolean enablePersistenceClient = false;
	private boolean enableSystemPerf = true;
 
	private IActuatorDataListener actuatorDataListener = null;
	private IPubSubClient mqttClient = null;
	private IPubSubClient cloudClient = null;
	private IPersistenceClient persistenceClient = null;
	private IRequestResponseClient smtpClient = null;
	private CoapServerGateway coapServer = null;
	private SystemPerformanceManager sysPerfMgr = null;
 
	// constructors
	/**
     * Default constructor for DeviceDataManager.
     * Initializes the manager based on the configuration settings.
     */
	public DeviceDataManager()
	{
		super();
	ConfigUtil configUtil = ConfigUtil.getInstance();
	this.coapServer = new CoapServerGateway(this);
	this.enableMqttClient =
		configUtil.getBoolean(
			ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_MQTT_CLIENT_KEY);
	this.enableCoapServer =
		configUtil.getBoolean(
			ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_COAP_SERVER_KEY);
	this.enableCloudClient =
		configUtil.getBoolean(
			ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);
	this.enablePersistenceClient =
		configUtil.getBoolean(
			ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);
	this.handleHumidityChangeOnDevice =
		    configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, "handleHumidityChangeOnDevice");

		this.humidityMaxTimePastThreshold =
		    configUtil.getInteger(ConfigConst.GATEWAY_DEVICE, "humidityMaxTimePastThreshold");

		this.nominalHumiditySetting =
		    configUtil.getFloat(ConfigConst.GATEWAY_DEVICE, "nominalHumiditySetting");

		this.triggerHumidifierFloor =
		    configUtil.getFloat(ConfigConst.GATEWAY_DEVICE, "triggerHumidifierFloor");

		this.triggerHumidifierCeiling =
		    configUtil.getFloat(ConfigConst.GATEWAY_DEVICE, "triggerHumidifierCeiling");

	initManager();
	}
 
	 /**
     * Initializes the manager based on configuration settings.
     */
 
	private void initManager()
{
	ConfigUtil configUtil = ConfigUtil.getInstance();
	this.enableSystemPerf =
		configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE,  ConfigConst.ENABLE_SYSTEM_PERF_KEY);
	if (this.enableSystemPerf) {
		this.sysPerfMgr = new SystemPerformanceManager();
		this.sysPerfMgr.setDataMessageListener(this);
	}
	if (this.enableMqttClient) {
		// TODO: implement this in Lab Module 7
		this.mqttClient = new MqttClientConnector();
		// NOTE: The next line isn't technically needed until Lab Module 10
		this.mqttClient.setDataMessageListener(this);
	}
	if (this.enableCoapServer) {
		this.coapServer = new CoapServerGateway(this);
	}
	if (this.enableCloudClient) {
		// TODO: implement this in Lab Module 10
	}
	if (this.enablePersistenceClient) {
		// TODO: implement this as an optional exercise in Lab Module 5
	}
}
public DeviceDataManager(
	 	boolean enableMqttClient,
	 	boolean enableCoapClient,
	 	boolean enableCloudClient,
	 	boolean enableSmtpClient,
	 	boolean enablePersistenceClient)
	 {
	 	super();
	 	initManager();
	 }
private ActuatorData   latestHumidifierActuatorData = null;
private ActuatorData   latestHumidifierActuatorResponse = null;
private ActuatorData     latestHumiditySensorData = null;
private OffsetDateTime latestHumiditySensorTimeStamp = null;

private boolean handleHumidityChangeOnDevice = false; // optional
private int     lastKnownHumidifierCommand   = ConfigConst.OFF_COMMAND;

// TODO: Load these from PiotConfig.props
private long    humidityMaxTimePastThreshold = 300; // seconds
private float   nominalHumiditySetting   = 40.0f;
private float   triggerHumidifierFloor   = 30.0f;
private float   triggerHumidifierCeiling = 50.0f;
	// public methods
	 /**
     * Handles the response from an actuator command.
     *
     * @param resourceName The resource name associated with the actuator.
     * @param data          The actuator data containing the response.
     * @return True if the response was successfully handled, false otherwise.
     */
	@Override
	public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data)
	{
		if (data != null) {
			_Logger.info("Handling actuator response: " + data.getName());
			// this next call is optional for now
			//this.handleIncomingDataAnalysis(resourceName, data);
			if (data.hasError()) {
				_Logger.warning("Error flag set for ActuatorData instance.");
			}
			return true;
		} else {
			return false;
		}
	}
private void handleIncomingDataAnalysis(ResourceNameEnum resource, ActuatorData data)
	{
		_Logger.info("Analyzing incoming actuator data: " + data.getName());
		if (data.isResponseFlagEnabled()) {
			// TODO: implement this
		} else {
			if (this.actuatorDataListener != null) {
				this.actuatorDataListener.onActuatorDataUpdate(data);
			}
		}
		if (data.getTypeID() == ConfigConst.HUMIDITY_SENSOR_TYPE) {
	        handleHumiditySensorAnalysis(resource, data);
	    }
	}
private void handleHumiditySensorAnalysis(ResourceNameEnum resource, ActuatorData actuatorData2) {
    _Logger.fine("Analyzing humidity data: " + actuatorData2.getValue());

    boolean isLow = actuatorData2.getValue() < this.triggerHumidifierFloor;
    boolean isHigh = actuatorData2.getValue() > this.triggerHumidifierCeiling;

    if (isLow || isHigh) {
        if (this.latestHumiditySensorData == null) {
            this.latestHumiditySensorData = actuatorData2;
            this.latestHumiditySensorTimeStamp = OffsetDateTime.now();
            return;
        }

        long timeElapsed = ChronoUnit.SECONDS.between(
            this.latestHumiditySensorTimeStamp, OffsetDateTime.now());

        if (timeElapsed >= this.humidityMaxTimePastThreshold) {
            int command = isLow ? ConfigConst.ON_COMMAND : ConfigConst.OFF_COMMAND;

            ActuatorData actuatorData = new ActuatorData();
            actuatorData.setName(ConfigConst.HUMIDIFIER_ACTUATOR_NAME);
            actuatorData.setCommand(command);
            actuatorData.setValue(this.nominalHumiditySetting);

            sendActuatorCommandtoCda(resource, actuatorData);

            this.latestHumidifierActuatorData = actuatorData;
            this.latestHumiditySensorData = null; // Reset after actuation
        }
    }
}
private void sendActuatorCommandtoCda(ResourceNameEnum resource, ActuatorData data) {
    if (this.mqttClient != null) {
        String jsonData = DataUtil.getInstance().actuatorDataToJson(data);
        this.mqttClient.publishMessage(resource.getResourceName(), jsonData, ConfigConst.DEFAULT_QOS);
        _Logger.info("Published ActuatorData: " + data.getCommand());
    }
}

	/**
     * Handles a request for an actuator command.
     *
     * @param resourceName The resource name associated with the actuator.
     * @param data          The actuator data containing the request.
     * @return Always returns false as actuator command requests are not handled in this version.
     */
	@Override
	public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data)
	{
		return false;
	}
	/**
     * Handles an incoming generic message.
     *
     * @param resourceName The resource name associated with the incoming message.
     * @param msg           The incoming generic message.
     * @return True if the message was successfully handled, false otherwise.
     */
	@Override
	public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg)
	{
		if (msg != null) {
			_Logger.info("Handling incoming generic message: " + msg);
			return true;
		} else {
			return false;
		}
	}
	private void handleUpstreamTransmission(ResourceNameEnum resource, String jsonData, int qos)
	{
		// NOTE: This will be implemented in Part 04
		_Logger.info("TODO: Send JSON data to cloud service: " + resource);
	}
	private OffsetDateTime getDateTimeFromData(BaseIotData data)
	{
		OffsetDateTime odt = null;
		
		try {
			odt = OffsetDateTime.parse(data.getTimeStamp());
		} catch (Exception e) {
			_Logger.warning(
				"Failed to extract ISO 8601 timestamp from IoT data. Using local current time.");
			
			// TODO: this won't be accurate, but should be reasonably close, as the CDA will
			// most likely have recently sent the data to the GDA
			odt = OffsetDateTime.now();
		}
		
		return odt;
	}
	 /**
     * Handles a sensor message.
     *
     * @param resourceName The resource name associated with the sensor.
     * @param data          The sensor data.
     * @return True if the sensor message was successfully handled, false otherwise.
     */
	@Override
	public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data)
	{
		if (data != null) {
			_Logger.info("Handling sensor message: " + data.getName());
			if (data.hasError()) {
				_Logger.warning("Error flag set for SensorData instance.");
			}
			return true;
		} else {
			return false;
		}
	}
	/**
     * Handles a system performance message.
     *
     * @param resourceName The resource name associated with the system performance data.
     * @param data          The system performance data.
     * @return True if the system performance message was successfully handled, false otherwise.
     */
 
	@Override
	public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data)
	{
		if (data != null) {
			_Logger.info("Handling system performance message: " + data.getName());
			if (data.hasError()) {
				_Logger.warning("Error flag set for SystemPerformanceData instance.");
			}
			return true;
		} else {
			return false;
		}
	}
	/**
     * Sets the actuator data listener for a specific actuator.
     *
     * @param name     The name of the actuator.
     * @param listener The actuator data listener.
     */
	public void setActuatorDataListener(String name, IActuatorDataListener listener)
	{
		if (listener != null) {
			// for now, just ignore 'name' - if you need more than one listener,
			// you can use 'name' to create a map of listener instances
			this.actuatorDataListener = listener;
		}
	}
	/**
     * Starts the manager, initiating necessary actions based on the enabled features.
     */
	public void startManager()
	{
		if (this.sysPerfMgr != null) {
			this.sysPerfMgr.startManager();
		}
 
	if (this.mqttClient != null) {
		if (this.mqttClient.connectClient()) {
			_Logger.info("Successfully connected MQTT client to broker.");
			// add necessary subscriptions
			// TODO: read this from the configuration file
			int qos = ConfigConst.DEFAULT_QOS;
			// TODO: check the return value for each and take appropriate action
			// IMPORTANT NOTE: The 'subscribeToTopic()' method calls shown
			// below will be moved to MqttClientConnector.connectComplete()
			// in Lab Module 10. For now, they can remain here.
			this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, qos);
			this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);
			this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);
			this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);
		} else {
			_Logger.severe("Failed to connect MQTT client to broker.");
			// TODO: take appropriate action
		}
	}
	if (this.enableCoapServer && this.coapServer != null) {
		if (this.coapServer.startServer()) {
			_Logger.info("CoAP server started.");
		} else {
			_Logger.severe("Failed to start CoAP server. Check log file for details.");
		}
	}
 
	if (this.sysPerfMgr != null) {
		this.sysPerfMgr.startManager();
	}
	if (this.mqttClient != null) {
        if (this.mqttClient.connectClient()) {
            _Logger.info("Successfully connected MQTT client to broker.");
            // Subscriptions now handled in MqttClientConnector's connectComplete()
        } else {
            _Logger.severe("Failed to connect MQTT client to broker.");
        }
    }

    if (this.enableCoapServer && this.coapServer != null) {
        if (this.coapServer.startServer()) {
            _Logger.info("CoAP server started.");
        } else {
            _Logger.severe("Failed to start CoAP server. Check log file for details.");
        }
    }

    if (this.sysPerfMgr != null) {
        this.sysPerfMgr.startManager();
    }
}

	 /**
     * Stops the manager, terminating ongoing processes based on the enabled features.
     */
 
	public void stopManager()
	{
		if (this.sysPerfMgr != null) {
			this.sysPerfMgr.stopManager();
		}
		if (this.mqttClient != null) {
			// add necessary un-subscribes
			// TODO: check the return value for each and take appropriate action
			// NOTE: The unsubscribeFromTopic() method calls below should match with
			// the subscribeToTopic() method calls from startManager(). Also, the
			// unsubscribe logic below can be moved to MqttClientConnector's
			// disconnectClient() call PRIOR to actually disconnecting from
			// the MQTT broker.
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
			this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE);
			if (this.mqttClient.disconnectClient()) {
				_Logger.info("Successfully disconnected MQTT client from broker.");
			} else {
				_Logger.severe("Failed to disconnect MQTT client from broker.");
				// TODO: take appropriate action
			}
		}
 
		if (this.enableCoapServer && this.coapServer != null) {
			if (this.coapServer.stopServer()) {
				_Logger.info("CoAP server stopped.");
			} else {
				_Logger.severe("Failed to stop CoAP server. Check log file for details.");
			}
		}
	}
 
	
	// private methods
	/**
	 * Initializes the enabled connections. This will NOT start them, but only create the
	 * instances that will be used in the {@link #startManager() and #stopManager()) methods.
	 * 
	 */


}