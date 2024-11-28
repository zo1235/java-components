package programmingtheiot.gda.connection;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.common.SimpleCertManagementUtil;

/**
 * Implementation of the MQTT client connector for publishing and subscribing to topics.
 */
public class MqttClientConnector implements IPubSubClient, MqttCallbackExtended {
    // Static Logger
    private static final Logger _Logger = Logger.getLogger(MqttClientConnector.class.getName());

    // Parameters
    private MqttClient mqttClient;
    private MqttConnectOptions connOpts;
    private MemoryPersistence persistence;
    private IDataMessageListener dataMsgListener;
    private boolean useAsyncClient;
    private boolean enableEncryption;
    private boolean useCleanSession;
    private boolean enableAutoReconnect;
    private String clientID;
    private String brokerAddr;
    private String host;
    private String protocol;
    private int port;
    private int brokerKeepAlive;
    private String pemFileName;

    /**
     * Default constructor initializing default parameters and configurations.
     */
    public MqttClientConnector() {
        super();
        this.persistence = new MemoryPersistence();
        initClientParameters(ConfigConst.MQTT_GATEWAY_SERVICE);
    }

    @Override
    public boolean connectClient() {
        synchronized (this) {
            try {
                if (this.mqttClient == null) {
                    this.mqttClient = new MqttClient(this.brokerAddr, this.clientID, this.persistence);
                    this.mqttClient.setCallback(this);
                }
                if (!this.mqttClient.isConnected()) {
                    _Logger.info("Connecting MQTT client to broker: " + this.brokerAddr);
                    this.mqttClient.connect(this.connOpts);
                    return true;
                } else {
                    _Logger.warning("MQTT client already connected to broker: " + this.brokerAddr);
                }
            } catch (MqttException e) {
                _Logger.log(Level.SEVERE, "Failed to connect MQTT client to broker.", e);
            }
            return false;
        }
    }

    @Override
    public boolean disconnectClient() {
        synchronized (this) {
            try {
                if (this.mqttClient != null && this.mqttClient.isConnected()) {
                    _Logger.info("Disconnecting MQTT client from broker: " + this.brokerAddr);
                    this.mqttClient.disconnect();
                    return true;
                } else {
                    _Logger.warning("MQTT client is not connected to broker: " + this.brokerAddr);
                }
            } catch (MqttException e) {
                _Logger.log(Level.SEVERE, "Failed to disconnect MQTT client.", e);
            }
            return false;
        }
    }

    @Override
    public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos) {
        if (topicName == null || msg == null || msg.isEmpty()) {
            _Logger.warning("Invalid topic or message. Unable to publish.");
            return false;
        }
        if (qos < 0 || qos > 2) {
            qos = ConfigConst.DEFAULT_QOS;
        }
        try {
            byte[] payload = msg.getBytes();
            MqttMessage mqttMsg = new MqttMessage(payload);
            mqttMsg.setQos(qos);
            this.mqttClient.publish(topicName.getResourceName(), mqttMsg);
            return true;
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to publish message to topic: " + topicName, e);
        }
        return false;
    }

    @Override
    public void messageArrived(String topic, MqttMessage msg) {
        _Logger.info("MQTT message arrived on topic: '" + topic + "' with payload: " + new String(msg.getPayload()));
        if (this.dataMsgListener != null) {
            this.dataMsgListener.handleIncomingMessage(ResourceNameEnum.getEnumFromTopic(topic), new String(msg.getPayload()));
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        _Logger.info("MQTT connection successful (reconnect = " + reconnect + "). Broker: " + serverURI);
    }

    @Override
    public void connectionLost(Throwable t) {
        _Logger.log(Level.WARNING, "Connection lost to MQTT broker: " + this.brokerAddr, t);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        _Logger.info("Delivery complete for message with ID: " + token.getMessageId());
    }

    // Private helper methods
    private void initClientParameters(String configSectionName) {
        ConfigUtil configUtil = ConfigUtil.getInstance();
        this.host = configUtil.getProperty(configSectionName, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);
        this.port = configUtil.getInteger(configSectionName, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_MQTT_PORT);
        this.brokerKeepAlive = configUtil.getInteger(configSectionName, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);
        this.useCleanSession = configUtil.getBoolean(configSectionName, ConfigConst.CLEAN_SESSION_KEY, true);
        this.enableAutoReconnect = configUtil.getBoolean(configSectionName, ConfigConst.AUTO_RECONNECT_KEY, true);
        this.enableEncryption = configUtil.getBoolean(configSectionName, ConfigConst.ENABLE_CRYPT_KEY, false);
        this.clientID = configUtil.getProperty(configSectionName, ConfigConst.CLIENT_ID_KEY, MqttClient.generateClientId());
        this.pemFileName = configUtil.getProperty(configSectionName, ConfigConst.CERT_FILE_KEY, null);
        this.protocol = this.enableEncryption ? ConfigConst.DEFAULT_MQTT_SECURE_PROTOCOL : ConfigConst.DEFAULT_MQTT_PROTOCOL;
        this.brokerAddr = this.protocol + "://" + this.host + ":" + this.port;

        this.connOpts = new MqttConnectOptions();
        this.connOpts.setKeepAliveInterval(this.brokerKeepAlive);
        this.connOpts.setCleanSession(this.useCleanSession);
        this.connOpts.setAutomaticReconnect(this.enableAutoReconnect);

        if (this.enableEncryption && this.pemFileName != null) {
            initSecureConnectionParameters(configSectionName);
        }
    }

    private void initSecureConnectionParameters(String configSectionName) {
        try {
            File certFile = new File(this.pemFileName);
            if (certFile.exists()) {
                SSLSocketFactory sslFactory = SimpleCertManagementUtil.getInstance().loadCertificate(this.pemFileName);
                this.connOpts.setSocketFactory(sslFactory);
                _Logger.info("Using TLS encryption with certificate: " + this.pemFileName);
            } else {
                _Logger.warning("Certificate file not found. Using unencrypted connection.");
            }
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Failed to initialize secure MQTT connection.", e);
        }
    }
}
