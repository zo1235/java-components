package programmingtheiot.gda.connection.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import programmingtheiot.data.DataUtil;
//import programmingtheiot.data.TelemetryData;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import com.google.gson.Gson;

/**
 * Resource handler for handling telemetry data updates in the CoAP server.
 */
public class UpdateTelemetryResourceHandler extends CoapResource {

    private static final Logger _Logger = Logger.getLogger(UpdateTelemetryResourceHandler.class.getName());
    private IDataMessageListener dataMsgListener = null;

    // Constructor to initialize the resource with the given name
    public UpdateTelemetryResourceHandler(String resourceName) {
        super(resourceName);
    }

    // Set the data message listener for processing incoming telemetry data
    public void setDataMessageListener(IDataMessageListener listener) {
        if (listener != null) {
            this.dataMsgListener = listener;
        }
    }

    /**
     * Handle PUT request to update telemetry data.
     * 
     * @param context The CoapExchange context containing the request and response.
     */
    @Override
    public void handlePUT(CoapExchange context) {
        ResponseCode code = ResponseCode.NOT_ACCEPTABLE;  // Default to NOT_ACCEPTABLE

        context.accept();  // Accept the request

        if (this.dataMsgListener != null) {
            try {
                // Get the JSON payload from the PUT request
                String jsonData = new String(context.getRequestPayload());

                // Use Gson to parse the JSON payload into a TelemetryData object
                Gson gson = new Gson();
                //TelemetryData telemetryData = gson.fromJson(jsonData, TelemetryData.class);

                // Log the incoming telemetry data (for debugging or monitoring purposes)
                //_Logger.info("Received Telemetry Data: " + telemetryData.toString());

                // Pass the telemetry data to the listener for further processing
                //this.dataMsgListener.handleTelemetryMessage(ResourceNameEnum.CDA_TELEMETRY_MSG_RESOURCE, telemetryData);

                code = ResponseCode.CHANGED;  // Successful response
            } catch (Exception e) {
                _Logger.warning("Failed to handle PUT request. Exception: " + e.getMessage());
                code = ResponseCode.BAD_REQUEST;  // Respond with BAD_REQUEST if parsing fails
            }
        } else {
            _Logger.warning("No callback listener set for telemetry update request.");
            code = ResponseCode.CONTINUE;  // Continue if no listener is set
        }

        // Respond to the request with the appropriate code and message
        String msg = "Update telemetry data request handled: " + super.getName();
        context.respond(code, msg);  // Respond with the status code and message
    }

    /**
     * Handle GET request (currently for telemetry resource).
     * 
     * @param context The CoapExchange context containing the request and response.
     */
    @Override
    public void handleGET(CoapExchange context) {
        _Logger.info("GET request received for " + getName());
        context.respond(ResponseCode.CONTENT, "Telemetry data.");
    }

    /**
     * Handle POST request (currently for creating the resource).
     * 
     * @param context The CoapExchange context containing the request and response.
     */
    @Override
    public void handlePOST(CoapExchange context) {
        _Logger.info("POST request received for " + getName());
        context.respond(ResponseCode.CREATED, "Resource created.");
    }

    /**
     * Handle DELETE request (currently for deleting the resource).
     * 
     * @param context The CoapExchange context containing the request and response.
     */
    @Override
    public void handleDELETE(CoapExchange context) {
        _Logger.info("DELETE request received for " + getName());
        context.respond(ResponseCode.DELETED, "Resource deleted.");
    }
}
