package programmingtheiot.gda.connection;

import programmingtheiot.common.IDataMessageListener;

public class MqttPublishDataMessageListener implements IDataMessageListener {
    
    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data) {
        // Implement your logic here
        return false;
    }

    @Override
    public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg) {
        // Implement your logic here
        return false;
    }

    @Override
    public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data) {
        // Implement your logic here
        return false;
    }

    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data) {
        // Implement your logic here
        return false;
    }

    @Override
    public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data) {
        // Implement your logic here
        return false;
    }

    @Override
    public void setActuatorDataListener(String name, IActuatorDataListener listener) {
        // Implement if needed
    }
}
