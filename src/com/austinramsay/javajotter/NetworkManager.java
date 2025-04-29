package com.austinramsay.javajotter;

import java.net.Socket;
import java.net.InetSocketAddress;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.EOFException;

public class NetworkManager {

	private JavaJotter controller;
	private Socket clientConn;
	private ObjectOutputStream out;
	private ConnectionState state;

	public NetworkManager(JavaJotter controller) {
		this.controller = controller;
		state = new ConnectionState(false);
	}

	public boolean establishConnection(String serverAddress, int port) {
		try {
			clientConn = new Socket();
			InetSocketAddress endpoint = new InetSocketAddress(serverAddress, port);
			clientConn.connect(endpoint, 5000);

			// Connection is established, let's begin thread to receive from server
			new Thread(new Listener(controller, state, clientConn)).start();

			// Create an output stream for when client wants to send objects
			out = new ObjectOutputStream(clientConn.getOutputStream());

			state.setConnectionState(true);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void send(Object obj) {
		try {
			out.writeObject(obj);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// TODO: program should call this on exit
	public void closeConnection() {
		try {
			out.close();
			clientConn.close();
			state.setConnectionState(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}

class ConnectionState {

	private boolean connectionActive;

	public ConnectionState(boolean active) {
		this.connectionActive = active;
	}

	public void setConnectionState(boolean active) {
		connectionActive = active;
	}

	public boolean isConnectionActive() {
		return connectionActive;
	}
}

class Listener implements Runnable {

	private JavaJotter controller;
	private ConnectionState state;
	private Socket connection;

	public Listener(JavaJotter controller, ConnectionState state, Socket connection) {
		this.controller = controller;
		this.state = state;
		this.connection = connection;
	}

	@Override
	public void run() {
		receive();
	}

	private void receive() {
		ObjectInputStream in = null;

		try {
			in = new ObjectInputStream(connection.getInputStream());

			while (true) {
				Object recvObj = in.readObject();
				controller.process(recvObj);
			}
		} catch (EOFException e) {
			// Server may have forced close connection
			controller.lostConnection();
			state.setConnectionState(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}