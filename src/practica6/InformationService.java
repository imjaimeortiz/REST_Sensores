package practica6;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Scanner;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class InformationService extends Thread {
	private static final int PORT_DEFAULT = 6666;
	private DatagramSocket socket;
	private InetAddress serverAddress;
	private boolean running;
	private HashMap<Integer, Integer> servidores;
	private SchemaValidation schValidation;
	private Translator tBroadcast;
	private Translator tControl;
	private Msg controlMsg;
	private String format;
	private BufferedWriter bw;
	private String lastData;
	private Msg lastMessage;
	private int serverId;
	private String control;
	
	public InformationService(String format) {
		try {
			this.socket = new DatagramSocket(PORT_DEFAULT);
		//	new ReadCommands(this).start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.servidores = new HashMap<Integer, Integer>();
		this.schValidation = new SchemaValidation();
		this.tBroadcast = new TranslatorBroadcast();
		this.tControl = new TranslatorControl();
		this.format = format;
		this.lastData = new String();
		this.control = new String();
	}

	// Se recibe lo que el servidor envía y se guardan los datos de los servidores
	// para el posterior envío
	// Se recibe lo que el servidor envía y se guardan los datos de los servidores para el posterior envío
		@Override
		public void run() {
			running = true;
			while (running) {
				byte[] buf = new byte[256];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				try {
					socket.receive(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String received = new String(packet.getData(), 0, packet.getLength());
				serverAddress = packet.getAddress();
				// Deserialización
				if (received.contains("<")) {
					if (schValidation.validateBroadcastXML(received)) {
						lastMessage = (BroadcastMessage) tBroadcast.XMLtoJava(received, "XML");
						lastData = ((BroadcastMessage) lastMessage).getMessage("XML");
						lastMessage.showMessage("XML");
						serverId = ((BroadcastMessage) lastMessage).getId();
						this.servidores.put(serverId, packet.getPort());
					}
				} else {
					lastMessage = (BroadcastMessage) tBroadcast.JsonToJava(received, "JSON");
					lastData = ((BroadcastMessage) lastMessage).getMessage("JSON");
					lastMessage.showMessage("JSON");
					serverId = ((BroadcastMessage) lastMessage).getId();
					this.servidores.put(serverId, packet.getPort());
				}
			}
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			socket.close();
		}

	// Procesamiento anterior al envío de la trama unicast en función de los
	// distintos mensajes que puede enviar el emisor
	public void processUnicast(String line) {
		String[] lines = line.split(" ");
		if (lines[0].equals("client")) {
			if (lines[1].equals("XML") || lines[1].equals("JSON")) {
				this.format = lines[1];
				System.out.println("Cambiando el formato de envío del cliente a " + lines[1]);	
			}
			else {
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				running = false;
			}
		}
		else {
			//System.out.println("recibido : " + line);
			int id = 0;
			try {
				id = Integer.parseInt(lines[0]);
			} catch (Exception e) {
				System.out.println("<Cliente> Operación no soportada");
			}
			String op = lines[1];
			// Esto no tiene gran funcionalidad, es para dar información al usuario sobre la
			// aplicación
			if (servidores.keySet().contains(id)) {
				if (op.equals("stop"))
					System.out.println("<Cliente><" + "Deteniendo el servidor " + id + ">");
				else if (op.equals("speedup"))
					System.out.println("<Cliente><" + "Cambiando a " + op + "Hz la frecuencia de envío del servidor " + id + ">");
				else if (op.equals("speeddown"))
					System.out.println("<Cliente><" + "Cambiando a " + op + "Hz la frecuencia de envío del servidor " + id + ">");
				this.controlMsg = new ControlMessage(op);
				// Serialización
				if (format.equals("XML"))
					control = tControl.javaToXML(controlMsg);
				else 
					control = tControl.javaToJson(controlMsg);
				this.send(id, control.getBytes());
			} else
				System.out.println("<Cliente>No se pudo conectar con el ServidorId=" + id);
		}
	}

	// Envío de la trama unicast
	private void send(int id, byte[] buf) {
		DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, servidores.get(id));
		try {
			socket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getLastData() {
		return this.lastData;
	}
	
	public BroadcastMessage getLastMessage() {
		return (BroadcastMessage) this.lastMessage;
	}
	
	public String getFormat() {
		return this.format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getControl() {
		return this.control;
	}
	
	public static void main(String[] args) {

		File file = new File("./data/broadcastMessages1.txt");
		Server s1 = new Server("JSON", file);
		InformationService iS = new InformationService("JSON");
		
		s1.start();
		iS.start();
		
		new HttpServer(iS).start();
		new HttpsServer(iS).start();
		new Smtp(iS).start();
		new HttpRest(iS).start();
		new HttpsRest(iS).start();
	}

	public int getServerId() {
		return this.serverId;
	}
}
