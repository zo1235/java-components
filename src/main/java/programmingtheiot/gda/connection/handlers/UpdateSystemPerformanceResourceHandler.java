package programmingtheiot.gda.connection.handlers;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.data.SensorData;
import programmingtheiot.common.ResourceNameEnum;

public class UpdateSystemPerformanceResourceHandler extends CoapResource {
    private IDataMessageListener listener;

    public UpdateSystemPerformanceResourceHandler(String name) {
        super(name); // Call CoapResource constructor
    }

    public void setDataMessageListener(IDataMessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        // Implement the handling logic for GET requests
        exchange.respond("System performance data");
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        // Handle incoming POST (data) request
        String data = exchange.getRequestText();
        SystemPerformanceData performanceData = parsePerformanceData(data); // Assume parsing logic exists
        if (this.listener != null) {
            this.listener.handleSystemPerformanceMessage(ResourceNameEnum.SYSTEM_PERFORMANCE, performanceData);
        }
        exchange.respond("POST request received.");
    }

    private SystemPerformanceData parsePerformanceData(String data) {
        // Parsing logic here
        return new SystemPerformanceData(); // Return parsed data
    }
}
