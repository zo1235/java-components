package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.data.SystemStateData;
import programmingtheiot.gda.connection.CloudClientConnector;
import programmingtheiot.gda.connection.CoapServerGateway;
import programmingtheiot.gda.connection.IPersistenceClient;
import programmingtheiot.gda.connection.IPubSubClient;
import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.gda.connection.RedisPersistenceAdapter;
import programmingtheiot.gda.system.SystemPerformanceManager;

public class DeviceDataManager implements IDataMessageListener {

    // Logger definition
    private static final Logger _Logger = Logger.getLogger(DeviceDataManager.class.getName());

    // Configuration flags
    private boolean enableMqttClient;
    private boolean enableCoapServer;
    private boolean enableCloudClient;
    private boolean enablePersistenceClient;
    private boolean enableSystemPerf;

    // Connections and managers
    private IPubSubClient mqttClient;
    private CloudClientConnector cloudClient;
    private IPersistenceClient persistenceClient;
    private CoapServerGateway coapServer;
    private SystemPerformanceManager sysPerfMgr;

    // Constructor
    public DeviceDataManager() {
        super();
        _Logger.info("Initializing DeviceDataManager...");
        loadConfiguration();
        initManager();
    }

    // Load configuration
    private void loadConfiguration() {
        ConfigUtil configUtil = ConfigUtil.getInstance();
        this.enableMqttClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_MQTT_CLIENT_KEY);
        this.enableCoapServer = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_COAP_SERVER_KEY);
        this.enableCloudClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);
        this.enablePersistenceClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);
        this.enableSystemPerf = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_SYSTEM_PERF_KEY);
    }

    // Initialize components
    private void initManager() {
        if (this.enableSystemPerf) {
            this.sysPerfMgr = new SystemPerformanceManager();
            this.sysPerfMgr.setDataMessageListener(this);
        }

        if (this.enableMqttClient) {
            // Create MQTT client connector instance
            this.mqttClient = new MqttClientConnector();
        }

        if (this.enableCoapServer) {
            this.coapServer = new CoapServerGateway(this);
        }

        if (this.enableCloudClient) {
            this.cloudClient = new CloudClientConnector();
        }

        if (this.enablePersistenceClient) {
            this.persistenceClient = new RedisPersistenceAdapter();
        }
    }

    // Start the manager
    public void startManager() {
        _Logger.info("Starting DeviceDataManager...");

        if (this.mqttClient != null) {
            if (this.mqttClient.connectClient()) {
                _Logger.info("Successfully connected MQTT client to broker.");

                // Get QoS level from configuration (add this if not defined yet)
                int qos = ConfigConst.DEFAULT_QOS;

                // Subscribe to MQTT topics
                this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, qos);
                this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);
                this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);
                this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);
            } else {
                _Logger.severe("Failed to connect MQTT client to broker.");
            }
        }

        if (this.sysPerfMgr != null) {
            this.sysPerfMgr.startManager();
        }

        if (this.coapServer != null) {
            this.coapServer.startServer();
        }
    }

    // Stop the manager
    public void stopManager() {
        _Logger.info("Stopping DeviceDataManager...");

        if (this.sysPerfMgr != null) {
            this.sysPerfMgr.stopManager();
        }

        if (this.coapServer != null) {
            this.coapServer.stopServer();
        }

        if (this.mqttClient != null) {
            // Unsubscribe from topics
            this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE);
            this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE);
            this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
            this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE);

            // Disconnect MQTT client
            if (this.mqttClient.disconnectClient()) {
                _Logger.info("Successfully disconnected MQTT client from broker.");
            } else {
                _Logger.severe("Failed to disconnect MQTT client from broker.");
            }
        }
    }

    // Implement IDataMessageListener methods
    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data) {
        if (data != null) {
            _Logger.info("Handling actuator response: " + data.getName());
            if (data.hasError()) {
                _Logger.warning("Error flag set for ActuatorData instance.");
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg) {
        if (msg != null) {
            _Logger.info("Handling incoming generic message: " + msg);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data) {
        if (data != null) {
            _Logger.info("Handling sensor message: " + data.getName());
            if (data.hasError()) {
                _Logger.warning("Error flag set for SensorData instance.");
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data) {
        if (data != null) {
            _Logger.info("Handling system performance message: " + data.getName());
            if (data.hasError()) {
                _Logger.warning("Error flag set for SystemPerformanceData instance.");
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data) {
        return false; // TODO: Implement if needed
    }

    @Override
    public void setActuatorDataListener(String name, IActuatorDataListener listener) {
        // TODO: Implement if needed
    }
}
