package practica6;

import java.net.*;

import javax.net.ssl.SSLSocket;

import java.io.*;

public class HttpRest extends Thread {
	
	InformationService iS;
	
	public HttpRest(InformationService iS) {
		this.iS = iS;
	}
	
	public void run() {
	
		ServerSocket s = null;
		Socket client = null;
		
		// Establecemos el servicio en el puerto 9999
		// No podemos elegir un puerto por debajo del 1023 si no somos
		// usuarios con los máximos privilegios (root)
		try {
			s = new ServerSocket( 10000 );
		} catch( IOException e ) {
			System.out.println( e );
		}
	 
		// Creamos el objeto desde el cual atenderemos y aceptaremos
		// las conexiones de los clientes y abrimos los canales de
		// comunicación de entrada y salida
		while (true) {
			try {
				client = s.accept();
				GestorHttpRest gestor = new GestorHttpRest(client, iS);
				gestor.start();
			} catch( IOException e ) {
			 System.out.println( e );
			}
		 }
	}
}
	
class GestorHttpRest extends Thread {
		
	Socket client;
	SSLSocket cliente;
	InformationService iS;
	
	public GestorHttpRest(Socket client, InformationService iS) {
		this.client = client;
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
			String url = this.getUrl(request);
			String response = this.processUrl(url);
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

	private String processUrl(String fullUrl) {
		String response = new String();
		String url = fullUrl.substring(1, fullUrl.length());
		TranslatorBroadcast tBroadcast = new TranslatorBroadcast();
		if (url.equals("index.html") || url.equals("") || url.equals("/")) {
			response = "HTTP/1.1 200 OK\r\n\r\n";
			response = response.concat("<html><head><title> SERVICIO DE INFORMACIÓN </title>");
			response = response.concat("<head><body>");
			response = response.concat("<a href=info> Obtener datos</a>");
			response = response.concat("<a href=stop> Detener servidorParar</a>");
			response = response.concat("<a href=speedup> Aumentar frecuencia de envío</a>");
			response = response.concat("<a href=speeddown> Reducir frecuencia de envío</a>");
			response = response.concat("</body></html>");
		}
		else if (url.equals("info")) {
				response = "HTTP/1.1 200 OK\r\n\r\n";
				response = response.concat("<html><head><title> SERVICIO DE INFORMACIÓN </title>");
				response = response.concat("</head><body>");
				response = response.concat("<h1>" + tBroadcast.javaToJson(iS.getLastMessage()) + "</h1>");
				response = response.concat("<a href=index.html> Volver a la página del índice</a>");
				response = response.concat("</body></json>");
		}
		else if (url.equals("stop")) {
			iS.processUnicast(iS.getServerId() + " " + url);
			response = "HTTP/1.1 200 OK\r\n\r\n";
			response = response.concat("<html><head><title> SERVICIO DE INFORMACIÓN </title>");
			response = response.concat("</head><body>");
			response = response.concat("<h1>" + iS.getControl() + "</h1>");
			response = response.concat("<a href=index.html> Volver a la página del índice</a>");
			response = response.concat("</body></json>");
		}
		else if (url.equals("speedup")) {
			iS.processUnicast(iS.getServerId() + " " + url);
			response = "HTTP/1.1 200 OK\r\n\r\n";
			response = response.concat("<html><head><title> SERVICIO DE INFORMACIÓN </title>");
			response = response.concat("</head><body>");
			response = response.concat("<h1>" + iS.getControl() + "</h1>");
			response = response.concat("<a href=index.html> Volver a la página del índice</a>");
			response = response.concat("</body></json>");
		}
		else if (url.equals("speeddown")) {
			iS.processUnicast(iS.getServerId() + " " + url);
			response = "HTTP/1.1 200 OK\r\n\r\n";
			response = response.concat("<html><head><title> SERVICIO DE INFORMACIÓN </title>");
			response = response.concat("</head><body>");
			response = response.concat("<h1>" + iS.getControl() + "</h1>");
			response = response.concat("<a href=index.html> Volver a la página del índice</a>");
			response = response.concat("</body></json>");
		}
		return response;
	}

	private String getUrl(String request) {
		String [] lines = request.split("\r\n");
		String [] lines2 = lines[0].split(" ");
		return lines2[1];
	}
} 

