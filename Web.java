import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InputStream;

public class Web {
	// By Sunny...
	// Main function which will interact with client browser and website requested by client
	
	public static void main(String[] args) {
		InputStream in;														// Inputstream to be used with client socket
		OutputStream out;													// Outputstream to be used with client socket
		int port = Integer.parseInt(args[0]);
		ServerSocket serverSocket = init(port);							
		try {
			while (true) {
				Socket socket = serverSocket.accept();						// This socket will be used to accept request and send response to client browser
				in = socket.getInputStream();
				out = socket.getOutputStream();
				char[] charUrl = null;

				Socket webSocket;											// This socket will be used to fetch webpage of requested site.
				byte[] request = new byte[65536];							// Will hold request sent by client browser
				byte[] response = new byte[29360128];						// Will hold response to be sent to client
				try {
					int n = in.read(request, 0, 65536);						// Reading request from browser
					if (n > 65535) {
						System.out.println("Too long request");
						continue;
					}

					charUrl = parse(request, n);
					if (charUrl == null) { 									// Rectifying null and "CONNECT" requests
						in.close();
						out.close();
						socket.close();
						continue;
					}
					InetAddress urlIP = null;
					urlIP = dnsLookUp(charUrl);
					if(urlIP == null){
						System.out.println("Url not found");						
						out.close();
						in.close();
						socket.close();
						continue;
					}
					boolean status = validate(urlIP);						// If host address can't be found status will be false
					if (status == false) {
						out.write("<HTML><h1>THIS WEBSITE IS BLOCKED BY PROXY<h1><HTML>".getBytes());
						out.close();
						in.close();
						socket.close();
						continue;
					}

					InetSocketAddress webSocketAddress = new InetSocketAddress(urlIP, 80);
					webSocket = new Socket(); 								// Socket to fetch webpage
					webSocket.connect(webSocketAddress);
					webSocket.setSoTimeout(2500);							// Setting maximum time it will take by a single read request of size (16KB)

					InputStream webIn = webSocket.getInputStream();			// Inputstream used to read response from website
					OutputStream webOut = webSocket.getOutputStream();		// Outputstream used to write client request on web socket
					webOut.write(request, 0, n);
					try {
						int no, i = 0, j = 0;
						byte[] temp = new byte[16000];
						try {
							while ((no = webIn.read(temp, 0, 16000)) > 0 && i < 29360128) {
								for (j = 0; j < no; j++) {					// Reading 16KB at a time and accummulating in response array
									response[i] = temp[j];
									i++;
								}
							}
						} catch (SocketTimeoutException toe) {
							System.out.println("Socket read timed out...Exiting loop...");
							toe.printStackTrace();
						}
						int length = i;
						out.write(response, 0, length);						// Sending response to client browser
						webSocket.close();
						System.out.print("LOG: a request for ");
						for(int k=0; k<charUrl.length; k++){
							if(charUrl[k] != ' ')
								System.out.print(charUrl[k]);
						}
						System.out.println(" serviced");
						out.close();
					} catch (SocketException se) {
						se.printStackTrace();
					} finally {
						webSocket.close();
					}
				} finally {
					in.close();
					out.close();
					socket.close();
				}
			}
		} catch (IOException ie) {
			System.out.println("Error while binding: ");
			ie.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("Can't close server socket");
				e.printStackTrace();
			}
		}
	}

	// By Deep...
	//This function creates and returns server socket object for specified port number
	public static ServerSocket init(int port) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket();											
			go(serverSocket, port);
			return serverSocket;
		} catch (IOException e) {
			System.out.println("Can't create socket");
			e.printStackTrace();
			System.exit(0);
		}
		return serverSocket;
	}

	// By Apoorva...
	// This function binds server socket with server and port number
	public static void go(ServerSocket serverSocket, int port) {
		InetSocketAddress socketAddress = new InetSocketAddress(dnsLookUp(new char[] { 'a','f','s','a','c','c','e','s','s','2','.','n','j','i','t','.','e','d','u'}), port);
		try {
			serverSocket.bind(socketAddress);
		} catch (IOException e) {
			System.out.print("Error in binding server socket: ");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("CS656 Project by Group M5 (afsaccess2@njit.edu)");
	}

	// By Amey...
	// This functions finds Ip address of passed url 
	public static InetAddress dnsLookUp(char[] charUrl) {
		InetAddress address = null;
		InetAddress[] addresses;
		try {
			addresses = InetAddress.getAllByName(new String(charUrl));				// Retrives all Ips associated with url
			if (addresses.length > 0)
				address = addresses[0]; 											// Assigns first address
		} catch (UnknownHostException e) {
			System.out.print("Can't find host: ");
			e.printStackTrace();
		}
		return address;
	}

	// By Kevin...
	// This function validates url against supplied list of blocked websites.
	public static boolean validate(InetAddress address) {
		boolean result = true;
		try {

			InetAddress[] addressesTorrentz = InetAddress.getAllByName("www.torrentz.eu");					// Blocked websites
			InetAddress[] addressesMakeMoney = InetAddress.getAllByName("www.makemoney.com");
			InetAddress[] addressesLottoForever = InetAddress.getAllByName("www.lottoforever.com");

			for (int i = 0; i < addressesTorrentz.length; i++) {
				if (address.equals(addressesTorrentz[i]))
					result = false;
			}

			if (result == true) {
				for (int i = 0; i < addressesMakeMoney.length; i++) {
					if (address.equals(addressesMakeMoney[i]))
						result = false;
				}
			}

			if (result == true) {
				for (int i = 0; i < addressesLottoForever.length; i++) {
					if (address.equals(addressesLottoForever[i]))
						result = false;
				}
			}
		} catch (UnknownHostException ue) {																	// If address is not found for site to be blocked
			System.out.println("Can't find address of sites to be blocked: ");
			ue.printStackTrace();
		} catch (NullPointerException ue) {																	// If address is not found for site to be blocked
			System.out.println("Can't find address of sites to be blocked: ");
			ue.printStackTrace();
		}
		return result;
	}

	// BY Yipeng...
	// This function extracts url from request
	public static char[] parse(byte[] request, int n) {
		char[] input = new char[65536]; 																	// Converting byte request into char
		for (int i = 0; i < n; i++) {
			input[i] = (char) request[i];
		}

		char[] url = null; 																					// Extracting URL
		for (int i = 0; i < input.length - 3; i++) {
			if (input[i] == 'G' && input[i + 1] == 'E' && input[i + 2] == 'T') {
				int j, k;
				for (j = i + 11; input[j] == ' '; j++)
					; 																						// locate the phrase after "GET and http://"
				for (k = j; input[k] != ' '; k++)
					;

				url = new char[k - j + 1];
				for (k = j; input[k] != '/'; k++) { 														// To remove last '/'
					url[k - j] = input[k];
				}				
				url[k - j] = '\0';
				break;
			}
		}
		return url;
	}
}