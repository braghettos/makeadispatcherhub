package com.makeacoffee.backend.enddevice;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.MediaType;

import java.net.URI;

public class EndDevice {
	
	private static EndDevice instance;
	final Serial serial;
	private Long deviceId;
	
	private EndDevice(){
		
		// create an instance of the serial communications class
		serial = SerialFactory.createInstance();
		serial.open(Serial.DEFAULT_COM_PORT, 9600);
		
		// create and register the serial data listener
        serial.addListener(new SerialDataListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {
                // print out the data received to the console

            	//byte[] b = event.getData().getBytes();
                //System.out.println("Lunghezza array letto:" + b.length);

				String eventString = event.getData();
				System.out.println("eventString=" + eventString);

				byte[] eventBytes = eventString.getBytes();
				System.out.println("eventBytes=" + eventBytes);

				StringBuilder sb = new StringBuilder();
				for (byte b : eventBytes) {
					System.out.println("Reading byte: " + b);
					System.out.println("Reading char: "
							+ String.format("%02X ", b));
					if (b != -62)
						sb.append(String.format("%02X", b));

				}
				System.out.println("eventHex=" + sb.toString());
				
				// Invocazione del WS di notifica
				ClientConfig config = new DefaultClientConfig();
				Client client = Client.create(config);
				
				URI baseURI = UriBuilder.fromUri(
									"http://localhost:8080/MakeADispatcherHub").build();
							WebResource service = client.resource(baseURI);
							String result = service.path("dh").path("gae")
									.path(deviceId.toString()).path(sb.toString())
									.accept(MediaType.APPLICATION_JSON).get(String.class);

							System.out.println("[mk] Result from GAE for [" + deviceId
									+ ", OK]: " + result);

				// for(int i=0; i<b.length; i++){
				// System.out.println("Reading " + String.format("%05X", b[i] &
				// 0xFFFFF));
				// }
			}

        });
	}
	
	public static EndDevice getInstance(){
		
		if(instance == null)
			instance = new EndDevice();
		
		return instance;
	}
	
	public boolean serialWrite(Long deviceId, String msg){
		
		this.deviceId = deviceId;
		
		try {

			for (int i = 0; i < msg.length() - 1; i += 2) {
				// grab the hex in pairs
				String output = msg.substring(i, (i + 2));
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
			
			return false;
		}
		
		return true;
	}

}
