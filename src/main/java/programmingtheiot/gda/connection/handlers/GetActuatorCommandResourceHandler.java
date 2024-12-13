package programmingtheiot.gda.connection.handlers;
 
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
 
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
 
import java.util.logging.Logger;
 
public class GetActuatorCommandResourceHandler extends CoapResource implements IActuatorDataListener {
 
    private static final Logger _Logger = Logger.getLogger(GetActuatorCommandResourceHandler.class.getName());
 
    private ActuatorData actuatorData = null;
 
    public GetActuatorCommandResourceHandler(String resourceName) {
        super(resourceName);
 
        // set the resource to be observable
        super.setObservable(true);
    }
 
    @Override
    public boolean onActuatorDataUpdate(ActuatorData data) {
        if (data != null) {
            if (this.actuatorData == null) {
                this.actuatorData = data;
            } else {
                this.actuatorData.updateData(data);
            }
            
            // notify all connected clients about the data change
            super.changed();
 
            _Logger.fine("Actuator data updated for URI: " + super.getURI() +
                         ": Data value = " + this.actuatorData.getValue());
 
            return true;
        }
        
        return false;
    }
 
    @Override
    public void handleGET(CoapExchange context) {
        // Log the GET request and accept it
        _Logger.info("GET request received for " + super.getName());
        context.accept();
 
        // Convert actuator data to JSON if available
        String jsonData = (this.actuatorData != null)
                          ? DataUtil.getInstance().actuatorDataToJson(this.actuatorData)
                          : "{}";
 
        // Send response with the JSON data as payload
        context.respond(ResponseCode.CONTENT, jsonData);
    }
}