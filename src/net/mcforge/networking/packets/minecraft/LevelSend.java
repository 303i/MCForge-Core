/*******************************************************************************
 * Copyright (c) 2012 MCForge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package net.mcforge.networking.packets.minecraft;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import net.mcforge.API.io.PacketPrepareEvent;
import net.mcforge.iomodel.Player;
import net.mcforge.networking.IOClient;
import net.mcforge.networking.packets.Packet;
import net.mcforge.networking.packets.PacketManager;
import net.mcforge.networking.packets.PacketType;
import net.mcforge.server.Server;

public class LevelSend extends Packet {

	public LevelSend(String name, byte ID, PacketManager parent,
			PacketType packetType) {
		super(name, ID, parent, packetType);
	}

	public LevelSend(PacketManager pm) {
		super("Level Send", (byte)0x03, pm, PacketType.Server_to_Client);
	}

	@Override
	public void Write(IOClient p, Server server) {
		PacketPrepareEvent event = new PacketPrepareEvent(p, this, server);
		server.getEventSystem().callEvent(event);
		if (event.isCancelled())
			return;
		Player player;
		if (p instanceof Player) {
			player = (Player)p;
		}
		else
			return;
		try {
			byte[] levelbuff = new byte[player.getLevel().getLength() + 4];
			intToNetworkByteOrder(player.getLevel().getLength(), levelbuff, 0, 4);
			for (int i = 0; i < player.getLevel().getLength(); i++)
				levelbuff[i + 4] = player.getLevel().getTile(i).getVisableBlock();
			byte[] gzip = compressBytes(levelbuff);
			levelbuff = null;
			if (gzip == null)
				return;
			int number = (int)Math.ceil(((double)(gzip.length)) / 1024);
			byte[] send;
			byte[] tempbuffer;
			for (int i = 1; gzip.length > 0; ++i) {
				short lenght = (short)Math.min(gzip.length, 1024);
				send = new byte[1028];
				send[0] = ID;
				System.arraycopy(HTNO(lenght), 0, send, 1, 2);
				System.arraycopy(gzip, 0, send, 3, lenght);
				tempbuffer = new byte[gzip.length - lenght];
				System.arraycopy(gzip, lenght, tempbuffer, 0, gzip.length - lenght);
				gzip = tempbuffer;
				int percent = (int)((double)((double)i * (double)100 / (double)number));
				send[1027] = (byte)percent; 
				player.WriteData(send);
				Thread.sleep(10);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void Handle(byte[] message, Server server, IOClient player) {
		// TODO Auto-generated method stub

	}
	private byte[] HTNO(short x) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeShort(x);
		dos.flush();
		return baos.toByteArray();
	}
	private byte[] compressBytes(byte[] orginal) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try {
			GZIPOutputStream gzip = new GZIPOutputStream(byteStream);
			gzip.write(orginal);
			gzip.close();
		} catch (IOException e) {
			this.parent.server.Log("Error compressing level!");
			e.printStackTrace();
			return null;
		}
		return byteStream.toByteArray();
	}

	private static void intToNetworkByteOrder(int num, byte[] buf, int start, int count) {
		if (count > 4) {
			throw new IllegalArgumentException(
					"Cannot handle more than 4 bytes");
		}

		for (int i = count - 1; i >= 0; i--) {
			buf[start + i] = (byte) (num & 0xff);
			num >>>= 8;
		}
	}

}
