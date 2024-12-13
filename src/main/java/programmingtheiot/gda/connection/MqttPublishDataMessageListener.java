package programmingtheiot.gda.connection;

import java.util.logging.Level;
import java.util.logging.Logger;
 
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.connection.IPubSubClient;
import programmingtheiot.gda.connection.MqttClientConnector;
 
 
public class MqttPublishDataMessageListener implements IDataMessageListener
{
   
   
    private static final Logger _Logger =
        Logger.getLogger(MqttPublishDataMessageListener.class.getName());
   
    public static final int DEFAULT_QOS = 0;
   
 
   
    private IPubSubClient mqttClient = null;
    private boolean pubOnMsg = true;
   
    private ResourceNameEnum pubResource = null;
   
    
 
    public MqttPublishDataMessageListener(ResourceNameEnum pubResource, boolean pubOnMsg)
    {
        super();
       
        this.pubResource = pubResource;
        this.pubOnMsg = pubOnMsg;
       
        mqttClient = new MqttClientConnector();
    }
   
   
    
    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data)
    {
        _Logger.log(Level.INFO, "Topic: {0}, Message: {1}", new Object[] {resourceName.getResourceName(), data});
       
        return true;
    }
 
   
    @Override
    public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data)
    {
        _Logger.log(Level.INFO, "Topic: {0}, Message: {1}", new Object[] {resourceName.getResourceName(), data});
       
        if (this.pubOnMsg) {
            String msg = DataUtil.getInstance().actuatorDataToJson(data);
           
            this.mqttClient.publishMessage(this.pubResource, msg, DEFAULT_QOS);
        }
       
        return true;
    }
 
    
    @Override
    public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg)
    {
        _Logger.log(Level.INFO, "Topic: {0}, Message: {1}", new Object[] {resourceName.getResourceName(), msg});
       
        if (this.pubOnMsg) {
            this.mqttClient.publishMessage(this.pubResource, msg, DEFAULT_QOS);
        }
       
        return true;
    }
 
    
    @Override
    public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data)
    {
        _Logger.log(Level.INFO, "Topic: {0}, Message: {1}", new Object[] {resourceName.getResourceName(), data});
       
        if (this.pubOnMsg) {
            String msg = DataUtil.getInstance().sensorDataToJson(data);
           
            this.mqttClient.publishMessage(this.pubResource, msg, DEFAULT_QOS);
        }
       
        return true;
    }
 
   
    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data)
    {
        _Logger.log(Level.INFO, "Topic: {0}, Message: {1}", new Object[] {resourceName.getResourceName(), data});
       
        if (this.pubOnMsg) {
            String msg = DataUtil.getInstance().systemPerformanceDataToJson(data);
           
            this.mqttClient.publishMessage(this.pubResource, msg, DEFAULT_QOS);
        }
       
        return true;
    }
   
   
    public void setActuatorDataListener(String name, IActuatorDataListener listener)
    {
        // ignore - nothing to do.
    }
   
}
 
 