package com.iView.client.roomba;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.iView.network.roomba.ConnectionRoombaObject;

public class RoombaTCP extends ARoomba {

	private Socket clientSocket;
	private DataOutputStream outToRoomba;


	@Override
	public boolean connect(ConnectionRoombaObject roombaConnection) {
		try {
			clientSocket = new Socket(roombaConnection.getHost(),
					roombaConnection.getPortRoomba());
			outToRoomba = new DataOutputStream(clientSocket.getOutputStream());
			System.out.println(
					"Connection to Roomba: " + roombaConnection.getHost()
							+ " Port: " + roombaConnection.getPortRoomba());
			connected = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return connected;

	}

	@Override
	public void disconnect() {
		send(START);
		this.send(DOCK);

		try {
			if (outToRoomba != null)
				outToRoomba.close();
			connected = false;
		} catch (Exception e) {
			System.out.print("exception in disconnect");
			e.printStackTrace();
		}
		outToRoomba = null;

		try {
			System.out.println("Trenne Verbindung zu Roomba: ");
			if (clientSocket != null)
				clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		clientSocket = null;

	}

	@Override
	public boolean send(byte[] bytes) {

		try {
			outToRoomba.write(bytes, 0, bytes.length);
			outToRoomba.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean send(int b) {
		logmsg(Integer.toString(b));
		try {
			outToRoomba.write(b & 0xff);
			outToRoomba.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


}
