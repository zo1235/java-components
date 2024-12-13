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

import programmingtheiot.gda.connection.ICloudClient;

import programmingtheiot.gda.connection.IPersistenceClient;

import programmingtheiot.gda.connection.IPubSubClient;

import programmingtheiot.gda.connection.IRequestResponseClient;

import programmingtheiot.gda.connection.MqttClientConnector;

import programmingtheiot.gda.connection.RedisPersistenceAdapter;

import programmingtheiot.gda.connection.SmtpClientConnector;

import programmingtheiot.gda.system.SystemPerformanceManager;

/**

* Shell representation of class for student implementation.

*

*/

public class DeviceDataManager implements IDataMessageListener

{

	// static

	private static final Logger _Logger =

		Logger.getLogger(DeviceDataManager.class.getName());

	// private var's

	private boolean enableMqttClient = true;

	private boolean enableCoapServer = false;

	private boolean enableCloudClient = true;

	private boolean enableSmtpClient = false;

	private boolean enablePersistenceClient = false;

	private boolean enableSystemPerf = true;

	private IActuatorDataListener actuatorDataListener = null;

	private IPubSubClient mqttClient = null;

	private ICloudClient cloudClient = null;

	private IPersistenceClient persistenceClient = null;

	private IRequestResponseClient smtpClient = null;

	private CoapServerGateway coapServer = null;

	private SystemPerformanceManager sysPerfMgr = null;

	private ActuatorData   latestHumidifierActuatorData = null;

	private ActuatorData   latestHumidifierActuatorResponse = null;

	private SensorData     latestHumiditySensorData = null;

	private OffsetDateTime latestHumiditySensorTimeStamp = null;

	private boolean handleHumidityChangeOnDevice = true; // optional

	private int     lastKnownHumidifierCommand   = ConfigConst.OFF_COMMAND;

	private String topicPrefix = "";

	//private MqttClientConnector mqttClientConn = null;

	private IDataMessageListener dataMsgListener = null;

	private int qosLevel = 1;

	// TODO: Load these from PiotConfig.props

	private long    humidityMaxTimePastThreshold = 300; // seconds

	private float   nominalHumiditySetting   = 40.0f;

	private float   triggerHumidifierFloor   = 30.0f;

	private float   triggerHumidifierCeiling = 50.0f;

	// constructors

	public DeviceDataManager()

	{

		super();
 
		ConfigUtil configUtil = ConfigUtil.getInstance();

		this.cloudClient = new CloudClientConnector();

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
 
		// parse config rules for local actuation events

		// TODO: add these to ConfigConst

		//gitstatus lab 12

		this.handleHumidityChangeOnDevice =

			configUtil.getBoolean(

				ConfigConst.GATEWAY_DEVICE, "handleHumidityChangeOnDevice");

		this.humidityMaxTimePastThreshold =

			configUtil.getInteger(

				ConfigConst.GATEWAY_DEVICE, "humidityMaxTimePastThreshold");

		this.nominalHumiditySetting =

			configUtil.getFloat(

				ConfigConst.GATEWAY_DEVICE, "nominalHumiditySetting");

		this.triggerHumidifierFloor =

			configUtil.getFloat(

				ConfigConst.GATEWAY_DEVICE, "triggerHumidifierFloor");

		this.triggerHumidifierCeiling =

			configUtil.getFloat(

				ConfigConst.GATEWAY_DEVICE, "triggerHumidifierCeiling");

		// TODO: basic validation for timing - add other validators for remaining values

		if (this.humidityMaxTimePastThreshold < 10 || this.humidityMaxTimePastThreshold > 7200) {

			this.humidityMaxTimePastThreshold = 300;

		}	

        initManager();

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

	// public methods

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
 
@Override

	public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg)

	{

		if (resourceName != null && msg != null) {

			try {

				if (resourceName == ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE) {

					_Logger.info("Handling incoming ActuatorData message: " + msg);

					// NOTE: it may seem wasteful to convert to ActuatorData and back while

					// the JSON data is already available; however, this provides a validation

					// scheme to ensure the data is actually an 'ActuatorData' instance

					// prior to sending off to the CDA

					ActuatorData ad = DataUtil.getInstance().jsonToActuatorData(msg);

					String jsonData = DataUtil.getInstance().actuatorDataToJson(ad);

					if (this.mqttClient != null) {

						// TODO: retrieve the QoS level from the configuration file

						_Logger.fine("Publishing data to MQTT broker: " + jsonData);

						return this.mqttClient.publishMessage(resourceName, jsonData, 0);

					}

					// TODO: If the GDA is hosting a CoAP server (or a CoAP client that

					// will connect to the CDA's CoAP server), you can add that logic here

					// in place of the MQTT client or in addition

				} else {

					_Logger.warning("Failed to parse incoming message. Unknown type: " + msg);

					return false;

				}

			} catch (Exception e) {

				_Logger.log(Level.WARNING, "Failed to process incoming message for resource: " + resourceName, e);

			}

		} else {

			_Logger.warning("Incoming message has no data. Ignoring for resource: " + resourceName);

		}

		if (msg != null) {

			_Logger.info("Handling incoming generic message: " + msg);

			return true;

		} else {

			return false;

		}

	}

	@Override

	public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data)

	{

		if (data != null) {

			_Logger.fine("Handling sensor message: " + data.getName());

			if (data.hasError()) {

				_Logger.warning("Error flag set for SensorData instance.");

			}

			if (this.cloudClient != null) {

				// TODO: handle any failures

				if (this.cloudClient.sendEdgeDataToCloud(resourceName, data)) {

					_Logger.fine("Sent SensorData upstream to CSP.");

				}

			}

			String jsonData = DataUtil.getInstance().sensorDataToJson(data);

			_Logger.fine("JSON [SensorData] -> " + jsonData);

			// TODO: retrieve this from config file

			int qos = ConfigConst.DEFAULT_QOS;

			if (this.enablePersistenceClient && this.persistenceClient != null) {

				this.persistenceClient.storeData(resourceName.getResourceName(), qos, data);

			}

			this.handleIncomingDataAnalysis(resourceName, data);

			this.handleUpstreamTransmission(resourceName, jsonData, qos);

			return true;

		} else {

			return false;

		}

	}

	@Override

	public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data)

	{

		if (data != null) {

			_Logger.info("Handling system performance message: " + data.getName());

			if (data.hasError()) {

				_Logger.warning("Error flag set for SystemPerformanceData instance.");

			}

			if (this.cloudClient != null) {

				// TODO: handle any failures

				if (this.cloudClient.sendEdgeDataToCloud(resourceName, data)) {

					_Logger.fine("Sent SystemPerformanceData upstream to CSP.");

				}

			}

			return true;

		} else {

			return false;

		}

	}

	public void setActuatorDataListener(String name, IActuatorDataListener listener)

	{

		if (listener != null) {

			// for now, just ignore 'name' - if you need more than one listener,

			// you can use 'name' to create a map of listener instances

			this.actuatorDataListener = listener;

		}

	}

	public void startManager()

    {

        if (this.sysPerfMgr != null) {

            _Logger.info("Starting DeviceDataManager...");

            this.sysPerfMgr.startManager();

        }

        if (this.mqttClient != null) {

            if (this.mqttClient.connectClient()) {

            	_Logger.info("***************I AM HERE****");

                _Logger.info("Successfully connected MQTT client to broker.");

                // add necessary subscriptions

                // TODO: read this from the configuration file

                // int qos = ConfigConst.DEFAULT_QOS;

                // TODO: check the return value for each and take appropriate action

                // this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, qos);

                //this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);

                //this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);

                //this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);

            } else {

                _Logger.severe("Failed to connect MQTT client to broker.");

                // TODO: take appropriate action

            }

        }

        if (this.cloudClient != null) {

            if (this.cloudClient.connectClient()) {

                _Logger.info("Successfully connected MQTT client to broker.");

                // add necessary subscriptions

                // TODO: read this from the configuration file

                // int qos = ConfigConst.DEFAULT_QOS;

                // // TODO: check the return value for each and take appropriate action

                // try {

                //  // sleep for half a minute or so...

                //  Thread.sleep(30000L);

                // } catch (Exception e) {

                //  // ignore

                // }

                // this.cloudClient.subscribeToCloudEvents(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE);

                // try {

                //  // sleep for half a minute or so...

                //  Thread.sleep(30000L);

                // } catch (Exception e) {

                //  // ignore

                // }

                //this.mqttClient.ToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);

                //this.mqttClient.ToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);

                //this.mqttClient.ToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);

            } else {

                _Logger.severe("Failed to connect cloud client to broker.");

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

    }
 
public void stopManager()

	{

		if (this.sysPerfMgr != null) {

			_Logger.info("Stopping DeviceDataManager...");

			this.sysPerfMgr.stopManager();

		}

		if (this.mqttClient != null) {

			// add necessary un-subscribes

			// TODO: check the return value for each and take appropriate action

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

		if (this.cloudClient != null) {

			// add necessary subscriptions

			// TODO: read this from the configuration file

			//int qos = ConfigConst.DEFAULT_QOS;

			// TODO: check the return value for each and take appropriate action

			try {

				// sleep for half a minute or so...

				Thread.sleep(30000L);

			} catch (Exception e) {

				// ignore

			}

			this.cloudClient.unsubscribeFromCloudEvents(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE);

			try {

				// sleep for half a minute or so...

				Thread.sleep(30000L);

			} catch (Exception e) {

				// ignore

			}

			//this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);

			//this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);

			//this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);

		} else {

			_Logger.severe("Failed to disconnect from cloud client to broker.");

			// TODO: take appropriate action

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

	private void handleIncomingDataAnalysis(ResourceNameEnum resource, SensorData data)

	{

		// check either resource or SensorData for type

		if (data.getTypeID() == ConfigConst.HUMIDITY_SENSOR_TYPE) {

			handleHumiditySensorAnalysis(resource, data);

		}

	}

	private void handleHumiditySensorAnalysis(ResourceNameEnum resource, SensorData data)

	{

		//

		// NOTE: INCOMPLETE and VERY BASIC CODE SAMPLE. Not intended to provide a solution.

		//

		_Logger.fine("Analyzing humidity data from CDA: " + data.getLocationID() + ". Value: " + data.getValue());

		boolean isLow  = data.getValue() < this.triggerHumidifierFloor;

		boolean isHigh = data.getValue() > this.triggerHumidifierCeiling;

		if (isLow || isHigh) {

			_Logger.info("Humidity data from CDA exceeds nominal range.");

			if (this.latestHumiditySensorData == null) {

				// set properties then exit - nothing more to do until the next sample

				this.latestHumiditySensorData = data;

				this.latestHumiditySensorTimeStamp = getDateTimeFromData(data);

				_Logger.info(

					"Starting humidity nominal exception timer. Waiting for seconds: " +

					this.humidityMaxTimePastThreshold);

				return;

			} else {

				OffsetDateTime curHumiditySensorTimeStamp = getDateTimeFromData(data);

				long diffSeconds =

					ChronoUnit.SECONDS.between(

						this.latestHumiditySensorTimeStamp, curHumiditySensorTimeStamp);

				_Logger.info("Checking Humidity value exception time delta: " + diffSeconds);

				if (diffSeconds >= this.humidityMaxTimePastThreshold) {

					ActuatorData ad = new ActuatorData();

					ad.setName(ConfigConst.HUMIDIFIER_ACTUATOR_NAME);

					ad.setLocationID(data.getLocationID());

					ad.setTypeID(ConfigConst.HUMIDIFIER_ACTUATOR_TYPE);

					ad.setValue(this.nominalHumiditySetting);

					if (isLow) {

						ad.setCommand(ConfigConst.ON_COMMAND);

					} else if (isHigh) {

						ad.setCommand(ConfigConst.OFF_COMMAND);

					}

					_Logger.info(

						"Humidity exceptional value reached. Sending actuation event to CDA: " +

						ad);

					this.lastKnownHumidifierCommand = ad.getCommand();

					sendActuatorCommandtoCda(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, ad);

					// set ActuatorData and reset SensorData (and timestamp)

					this.latestHumidifierActuatorData = ad;

					this.latestHumiditySensorData = null;

					this.latestHumiditySensorTimeStamp = null;

				}

			}

		} else if (this.lastKnownHumidifierCommand == ConfigConst.ON_COMMAND) {

			// check if we need to turn off the humidifier

			if (this.latestHumidifierActuatorData != null) {

				// check the value - if the humidifier is on, but not yet at nominal, keep it on

				if (this.latestHumidifierActuatorData.getValue() >= this.nominalHumiditySetting) {

					this.latestHumidifierActuatorData.setCommand(ConfigConst.OFF_COMMAND);

					_Logger.info(

						"Humidity nominal value reached. Sending OFF actuation event to CDA: " +

						this.latestHumidifierActuatorData);

					sendActuatorCommandtoCda(

						ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, this.latestHumidifierActuatorData);

					// reset ActuatorData and SensorData (and timestamp)

					this.lastKnownHumidifierCommand = this.latestHumidifierActuatorData.getCommand();

					this.latestHumidifierActuatorData = null;

					this.latestHumiditySensorData = null;

					this.latestHumiditySensorTimeStamp = null;

				} else {

					_Logger.fine("Humidifier is still on. Not yet at nominal levels (OK).");

				}

			} else {

				// shouldn't happen, unless some other logic

				// nullifies the class-scoped ActuatorData instance

				_Logger.warning(

					"ERROR: ActuatorData for humidifier is null (shouldn't be). Can't send command.");

			}

		}

	}

	private void sendActuatorCommandtoCda(ResourceNameEnum resource, ActuatorData data)

	{

		if (this.actuatorDataListener != null) {

			this.actuatorDataListener.onActuatorDataUpdate(data);

		}

		if (this.enableMqttClient && this.mqttClient != null) {

			String jsonData = DataUtil.getInstance().actuatorDataToJson(data);

			if (this.mqttClient.publishMessage(resource, jsonData, ConfigConst.DEFAULT_QOS)) {

				_Logger.info(

					"Published ActuatorData humidifier command from GDA to CDA: " + data.getCommand());

			} else {

				_Logger.warning(

					"Failed to publish ActuatorData humidifier command from GDA to CDA: " + data.getCommand());

			}

		}

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

			this.mqttClient = new MqttClientConnector();

			// NOTE: The next line isn't technically needed until Lab Module 10

			this.mqttClient.setDataMessageListener(this);

		}

		if (this.enableCoapServer) {

			// TODO: implement this in Lab Module 8

			this.coapServer = new CoapServerGateway(this);

		}

		if (this.enableCloudClient) {

			// TODO: implement this in Lab Module 10

			this.cloudClient = new CloudClientConnector();

			this.cloudClient.setDataMessageListener(this);

		}

		if (this.enablePersistenceClient) {

			// TODO: implement this as an optional exercise in Lab Module 5

		}

	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, ActuatorData data)

	{

		_Logger.fine("handling incoming actuator data analysis");

		_Logger.info("Analyzing incoming actuator data: " + data.getName());

		if (data.isResponseFlagEnabled()) {

			// TODO: implement this

		} else {

			if (this.actuatorDataListener != null) {

				this.actuatorDataListener.onActuatorDataUpdate(data);

			}

		}

	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, SystemStateData data)

	{

		_Logger.fine("handling incoming system state data analysis");

	}

	private boolean handleUpstreamTransmission(ResourceNameEnum resourceName, String jsonData, int qos)

	{

		_Logger.fine("handling upstream transmission");

		return true;

	}

	@Override

	public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data)

	{

		if (data != null) {

			// NOTE: Feel free to update this log message for debugging and monitoring

			_Logger.log(

				Level.FINE,

				"Actuator request received: {0}. Message: {1}",

				new Object[] {resourceName.getResourceName(), Integer.valueOf((data.getCommand()))});

			if (data.hasError()) {

				_Logger.warning("Error flag set for ActuatorData instance.");

			}

			// TODO: retrieve this from config file

			int qos = ConfigConst.DEFAULT_QOS;

			// TODO: you may want to implement some analysis logic here or

			// in a separate method to determine how best to handle incoming

			// ActuatorData before calling this.sendActuatorCommandtoCda()

			// Recall that this private method was implement in Lab Module 10

			// See PIOT-GDA-10-003 for details

			this.sendActuatorCommandtoCda(resourceName, data);

			return true;

		} else {

			return false;

		}

	}

	public void CloudClientConnector()

	{

		ConfigUtil configUtil = ConfigUtil.getInstance();

		this.topicPrefix =

			configUtil.getProperty(ConfigConst.CLOUD_GATEWAY_SERVICE, ConfigConst.BASE_TOPIC_KEY);

		// Depending on the cloud service, the topic names may or may not begin with a "/", so this code

		// should be updated according to the cloud service provider's topic naming conventions

		if (topicPrefix == null) {

			topicPrefix = "/";

		} else {

			if (! topicPrefix.endsWith("/")) {

				topicPrefix += "/";

			}

		}

	}

}
 