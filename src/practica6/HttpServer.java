package practica6;

import java.net.*;
import java.util.Scanner;

import javax.net.ssl.SSLSocket;

import java.io.*;

public class HttpServer extends Thread {
	
	InformationService iS;
	
	public HttpServer(InformationService iS) {
		this.iS = iS;
	}
	
	public void run() {
	
		ServerSocket s = null;
		Socket client = null;
		
		// Establecemos el servicio en el puerto 9999
		// No podemos elegir un puerto por debajo del 1023 si no somos
		// usuarios con los m�ximos privilegios (root)
		try {
			s = new ServerSocket( 9999 );
		} catch( IOException e ) {
			System.out.println( e );
		}
	 
		// Creamos el objeto desde el cual atenderemos y aceptaremos
		// las conexiones de los clientes y abrimos los canales de
		// comunicaci�n de entrada y salida
		while (true) {
			try {
				client = s.accept();
				GestorPeticionHttp gestor = new GestorPeticionHttp(client, iS);
				gestor.start();
			} catch( IOException e ) {
			 System.out.println( e );
			}
		 }
	}
}
	
class GestorPeticionHttp extends Thread {
		
	Socket client;
	SSLSocket cliente;
	InformationService iS;
	
	public GestorPeticionHttp(Socket client, InformationService iS) {
		this.client = client;
		this.iS = iS;
	}

	public GestorPeticionHttp(SSLSocket client, InformationService iS) {
		this.cliente = client;
		this.iS = iS;
	}
	
	public void run() {
		InputStream is;
		OutputStream os;

		try {
			
			is = client.getInputStream();
	        os = client.getOutputStream();

	        // Recibir datos
	        byte[] buffer = new byte[1000];
	        int leido;
	        String request = new String();
	        leido = is.read(buffer);
	        request = new String(buffer, 0, leido);
			// Procesamiento
			String response = this.getResponse();
			// Replicar datos
			byte[] bufferResponse = response.getBytes();
			os.write(bufferResponse);

			is.close();
			os.close();
			client.close();
		} catch (IOException e) {
			System.out.println("ERROR: " + e.getMessage() + ":" + e);
		}
	}
	private String getResponse() {
		// Concatenamos la respuesta
		String response = "HTTP/1.1 200 OK\r\n\r\n";
		response = response.concat("<html><head><title> SERVICIO DE INFORMACI�N </title>");
		response = response.concat("</head><body>");
		response = response.concat("<h1>" + iS.getLastData() + "</h1>");
		response = response.concat("</body></html>");
		return response;
	}

} 

