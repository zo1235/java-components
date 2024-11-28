package programmingtheiot.gda.connection.handlers;
 
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
 
 
/**
* Generic CoAP resource handler implementation.
* 
*/
public class SensorDataObserverHandler implements CoapHandler
{
	// static
	private static final Logger _Logger =
			Logger.getLogger(SensorDataObserverHandler.class.getName());
		public SensorDataObserverHandler ()
		{
			super();
		}
		/* (non-Javadoc)
		 * @see org.eclipse.californium.core.CoapHandler#onError()
		 */
		public void onError()
		{
			_Logger.warning("Handling CoAP error...");
		}
 
		/* (non-Javadoc)
		 * @see org.eclipse.californium.core.CoapHandler#onLoad(org.eclipse.californium.core.CoapResponse)
		 */
		public void onLoad(CoapResponse response)
		{
			_Logger.info("Received CoAP response (payload should be SensorData in JSON): " + response.getResponseText());
		}
 
		public void setDataMessageListener(IDataMessageListener listener) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onLoad(CoapResponse response) {
			// TODO Auto-generated method stub
			
		}
}