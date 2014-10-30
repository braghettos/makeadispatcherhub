package com.makeacoffee.backend.api;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.makeacoffee.backend.enddevice.EndDevice;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import entity.User;

@Path("/dh")
public class DispatcherHubApi {
	@GET
	@Path("/ed/{device}/{event}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String fireEvent(@PathParam("device") String deviceId,
			@PathParam("event") String eventId) {
		System.out.println("[ed] Invocazione di un evento verso l'end device");
		System.out.println("[ed] Device da aggiornare: " + deviceId);
		System.out.println("[ed] Evento invocato sul device: " + eventId);

		// State toState = MakeACoffeeService.fireEvent(deviceId, eventId);

		// Mockup per l'invocazione del servizio di notifica
		EndDevice endDeviceIO = EndDevice.getInstance();
		if(endDeviceIO.serialWrite(deviceId, eventId))
			System.out.println("[ed] Invocazione andata a buon fine");
		else
			System.out.println("[ed] Invocazione in kul!!!");

		return "fired!";
	}

	@GET
	@Path("/gae/{device}/{event}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String notifyEvent(@PathParam("device") String deviceId,
			@PathParam("event") String eventId) {
		System.out
				.println("[gae] Notifica al cloud del risultato di un'azione");
		System.out.println("[gae] Device da aggiornare: " + deviceId);
		System.out.println("[gae] Evento invocato sul device: " + eventId);

		// Device d = MakeACoffeeService.getDevice(id);

		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		// URI baseURI =
		// UriBuilder.fromUri("http://localhost:8888/gae/dh").build(); // Test
		// purpose
		URI baseURI = UriBuilder.fromUri(
				"https://make-a-cloud.appspot.com/gae/dh").build();
		WebResource service = client.resource(baseURI);
		/*System.out.println(service.path(deviceId.toString()).path(eventId)
				.accept(MediaType.APPLICATION_JSON).get(String.class));*/

		System.out.println("[gae] Notifica andata a buon fine.");

		return "notified!";
	}

	/**
	 * Classe rappresentante un semplicissimo Mockup dell'End Device.
	 * 
	 * @author massimo.cattai
	 *
	 */
	/*public static class Mockup {
		private Long deviceId;

		public Mockup(Long deviceId) {
			this.deviceId = deviceId;
		}

		@Override
		public void run() {
			System.out.println("[mk] Mockup for [" + deviceId + ", OK]");
			// Attesa per 'millis' millisecondi
			try {

				String hex = "7e0004080153485b";

				// create an instance of the serial communications class
				final Serial serial = SerialFactory.createInstance();
				serial.open(Serial.DEFAULT_COM_PORT, 9600);         

				for (int i = 0; i < hex.length() - 1; i += 2) {
					// grab the hex in pairs
					String output = hex.substring(i, (i + 2));
					System.out.print("output: " + output + " ");
					// convert hex to decimal
					int decimal = Integer.parseInt(output, 16);
					//System.out.println("decimal: " + decimal);
					serial.write((byte) decimal);
					// convert the decimal to character
					//System.out.println("char: " + ((char) decimal) + ";");
				}
				
				System.out.println("\nFrame spedito!");
				
			} catch (IllegalStateException e) {
				e.printStackTrace();
				System.out.println("Mockup in kul!!");
			}

			// Invocazione del WS di notifica
			ClientConfig config = new DefaultClientConfig();
			Client client = Client.create(config);
			// URI baseURI =
			// UriBuilder.fromUri("http://localhost:18080/HelloWebServices").build();
			// // Test purpose
			URI baseURI = UriBuilder.fromUri(
					"http://localhost:8080/MakeADispacherHub").build();
			WebResource service = client.resource(baseURI);
			String result = service.path("dh").path("gae")
					.path(deviceId.toString()).path("OK")
					.accept(MediaType.APPLICATION_JSON).get(String.class);

			System.out.println("[mk] Result from GAE for [" + deviceId
					+ ", OK]: " + result);
		}

	}*/
}
