package programmingtheiot.gda.connection.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
 
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
 
public class UpdateSystemPerformanceResourceHandler extends CoapResource {
 
    private static final Logger _Logger = Logger.getLogger(UpdateSystemPerformanceResourceHandler.class.getName());
    private IDataMessageListener dataMsgListener =null;
 
    public UpdateSystemPerformanceResourceHandler(String resourceName) {
        super(resourceName);
    }
 
    public void setDataMessageListener(IDataMessageListener listener) {
        this.dataMsgListener = listener;
    }
    @Override
    public void handleGET(CoapExchange context)
    {
    }
    @Override
    public void handleDELETE(CoapExchange context)
    {
    }
    @Override
    public void handlePOST(CoapExchange context)
    {
    }
    @Override
    public void handlePUT(CoapExchange context) {
        ResponseCode responseCode = ResponseCode.NOT_ACCEPTABLE;
        context.accept();
 
        // Log the incoming request payload for debugging purposes
        String requestPayload = new String(context.getRequestPayload());
        _Logger.info("Received PUT request with payload: " + requestPayload);
 
        if (this.dataMsgListener != null) {
            try {
                // Convert the JSON payload to SystemPerformanceData object
                SystemPerformanceData sysPerfData = DataUtil.getInstance().jsonToSystemPerformanceData(requestPayload);
                // Pass the data to the listener for further processing
                this.dataMsgListener.handleSystemPerformanceMessage(
                        ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, sysPerfData);
 
                // Set response code indicating success
                responseCode = ResponseCode.CHANGED;
                _Logger.info("System performance data updated successfully.");
 
            } catch (Exception e) {
                _Logger.log(Level.WARNING, "Failed to handle PUT request. Error: " + e.getMessage(), e);
                responseCode = ResponseCode.BAD_REQUEST; // Use BAD_REQUEST for malformed or invalid data
            }
        } else {
            _Logger.warning("No data message listener available. Ignoring PUT request.");
            responseCode = ResponseCode.CONTINUE;
        }
 
        // Respond to the client with an appropriate status and message
        context.respond(responseCode, "System performance update handled for resource: " + super.getName());
    }
}