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

import xBeeJPacketBuilder_lib.*;

import java.net.URI;
import java.util.ArrayList;

public class EndDevice {
	
	private static EndDevice instance;
	final Serial serial;
	private String deviceId;
	
	private EndDevice(){
		
		// create an instance of the serial communications class
		serial = SerialFactory.createInstance();
		serial.open(Serial.DEFAULT_COM_PORT, 9600);
		
		// create and register the serial data listener
        serial.addListener(new SerialDataListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {
                // print out the data received to the console
                String eventString = event.getData();
                ArrayList<Byte> receivedBytes = new ArrayList<Byte>(eventString.length());

                for(int i = 0; i < eventString.length(); i++)
                {
                        System.out.println("Byte["+ i + "] = " + String.format("0x%02X", (byte)eventString.charAt(i)));
                        receivedBytes.add((byte)(eventString.charAt(i)));
                }
                       
				// Invocazione del WS di notifica
				ClientConfig config = new DefaultClientConfig();
				Client client = Client.create(config);
				
				URI baseURI = UriBuilder.fromUri(
									"http://localhost:8080/MakeADispatcherHub").build();
							WebResource service = client.resource(baseURI);
							String result = service.path("dh").path("gae")
									.path(deviceId).path(receivedBytes.toString())
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
	
	public boolean serialWrite(String deviceId, String msg){

		this.deviceId = deviceId;
		
		System.out.println("Fin qui ci sono arrivato per il device " + deviceId + " ad eseguire l'azione " + msg + "!");
		
		Frame0x10TransmitRequestInput in = new Frame0x10TransmitRequestInput (0x10, 0, (byte)3, (byte)0x01, deviceId, "FFFE", (byte)0, (byte)0, msg);
		FrameOutput out = xBeeJPacketBuilder.make0x10Frame(in);
		
		/*Frame0x17RemoteATCommandInput in = new Frame0x17RemoteATCommandInput(0x17, 0, (byte)3, (byte)0x01, deviceId, "FFFE", (byte)0, msg, "");
		FrameOutput out = xBeeJPacketBuilder.make0x17Frame(in);*/
		
		try {
		
			for( int i = 0; i < out.packet.size(); i++ )
			{
				System.out.println("output: " + String.format("0x%02X", out.packet.get(i)));
				//System.out.println("output: " + out.packet.get(i));
			    serial.write( out.packet.get(i));   //invio via seriale il contenuto del pacchetto, byte per byte
			}

			/*for (int i = 0; i < msg.length() - 1; i += 2) {
				// grab the hex in pairs
				String output = msg.substring(i, (i + 2));
				System.out.print("output: " + output + " ");
				// convert hex to decimal
				int decimal = Integer.parseInt(output, 16);
				//System.out.println("decimal: " + decimal);
				serial.write((byte) decimal);
				// convert the decimal to character
				//System.out.println("char: " + ((char) decimal) + ";");
			}*/
			
			System.out.println("\nFrame spedito!");
			
		} catch (IllegalStateException e) {
			e.printStackTrace();
			System.out.println("Mockup in kul!!");
			
			return false;
		}
		
		return true;
	}

}
