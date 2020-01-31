package userApplication2;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.io.PrintWriter;

public class userApplication2 {
	public static void main(String[] args) {
		int hostPort, clientPort;
		Scanner input = new Scanner(System.in);
		System.out.println("Hello !!");

		for (;;) {

			System.out.println("for sending echo packets press 1");
			System.out.println("for getting images press 2");
			System.out.println("for getting DPCM sound press 3");
			System.out.println("for getting  AQDPCM sound press 4");
			System.out.println("for ithakicopter telemetry  press 5");
			System.out.println("for getting vehicle measurments press 6");

			try {
				int choice = input.nextInt();
				if (choice == 5) {
					hostPort = 38038;
					clientPort = 48038;
				} else {
					System.out.println("Please give host port number");
					hostPort = input.nextInt();
					System.out.println("Please give client port number");
					clientPort = input.nextInt();
				}
				switch (choice) {
				case 1:
					new userApplication2().echo(hostPort, clientPort);
					continue;
				case 2:
					new userApplication2().image(hostPort, clientPort);
					continue;
				case 3:
					new userApplication2().DPCM_sound(hostPort, clientPort);
					continue;
				case 4:
					new userApplication2().AQDPCM_sound(hostPort, clientPort);
					continue;
				case 5:
					new userApplication2().ithakiCopter(hostPort, clientPort);
					continue;
				case 6:
					new userApplication2().vehicle(hostPort, clientPort);
					continue;
				}
			} catch (Exception e) {
				System.out.println("sorry,you didnt give a right choice,please try again");
			}
		}
	}

	public void echo(int hostPort, int clientPort) throws SocketException, IOException, UnknownHostException {
		String code = new String();
		String packetInfo = new String();
		String message;
		int choice = 0;
		double startTime = 0, endTime = 0, totalTime = 0, startDL = 0, endDL = 0, avgTime = 0, var = 0;
		int numOfPackets = 0;
		ArrayList<String> DLinfo = new ArrayList<String>(); // general info of download
		ArrayList<Double> DLtime = new ArrayList<Double>(); // download time of each packet
		ArrayList<Float> TPcount = new ArrayList<Float>(); // throughput buffer
		Scanner input = new Scanner(System.in);
		Scanner input2 = new Scanner(System.in);
		System.out.println("for receiving packets with no delay press 1");
		System.out.println("for receiving packets with delay press 2");
		try {
			choice = input.nextInt();
			switch (choice) {
			case 1: {
				code = "E0000";
				System.out.println("for including temperature in the packets press 0,otherwise press 1");

				choice = input.nextInt();
				if (choice == 0) {
					packetInfo = code + "T00";
				} else {
					packetInfo = code;
				}
				break;
			}
			case 2: {
				System.out.println("please give password for exchainging echo paclets in form EXXXX:");
				code = input2.nextLine();
				packetInfo = code;
				break;
			}
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		DatagramSocket s = new DatagramSocket();
		byte[] txbuffer = packetInfo.getBytes();
		byte[] hostIP = { (byte) 155, (byte) 207, 18, (byte) 208 };
		InetAddress hostAddress = InetAddress.getByAddress(hostIP);
		DatagramPacket p = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, hostPort);
		DatagramSocket r = new DatagramSocket(clientPort);
		r.setSoTimeout(5000);
		byte[] rxbuffer = new byte[2048];
		DatagramPacket q = new DatagramPacket(rxbuffer, rxbuffer.length);
		startDL = System.currentTimeMillis();
		while (endDL < 4 * 60 * 1000) {
			s.send(p);
			startTime = System.currentTimeMillis();
			for (;;) {
				try {
					r.receive(q);
					message = new String(rxbuffer, 0, q.getLength());
					System.out.println(message);
					endTime = System.currentTimeMillis() - startTime;
					break;
				} catch (Exception x) {
					System.out.println(x);
				}
			}
			DLtime.add(endTime);
			endDL = (System.currentTimeMillis() - startDL);
			totalTime += endTime;
			numOfPackets++;
		}
		avgTime = totalTime / numOfPackets;
		for (int i = 0; i < DLtime.size(); i++) {
			var += (DLtime.get(i) - avgTime) * (DLtime.get(i) - avgTime);
		}
		var /= numOfPackets;

		DLinfo.add("Total time of download:" + String.valueOf(totalTime) + "milisec");
		System.out.println("Total time of download:" + String.valueOf(totalTime) + "milisec");
		DLinfo.add("Total time of Comunication with server:" + String.valueOf(endDL) + "milisec");
		System.out.println("Total time of Comunication with server:" + String.valueOf(endDL) + "milisec");
		DLinfo.add("Number of packets downloaded: " + String.valueOf(numOfPackets));
		System.out.println("Number of packets downloaded: " + String.valueOf(numOfPackets));
		DLinfo.add("Average time for packet download : " + String.valueOf(avgTime) + "milisec");
		System.out.println("Average time for packet download : " + String.valueOf(avgTime) + "milisec");
		DLinfo.add("variation of packet response time:" + String.valueOf(var) + "milisec");
		System.out.println("variation of packet response time:" + String.valueOf(var));
		double sumInt = 0;
		float counterInt = 0;
		// throughput calculation
		for (int i = 0; i < DLtime.size(); i++) {
			int j = i;
			while ((sumInt < 8 * 1000) && (j < DLtime.size())) {
				sumInt += DLtime.get(j);
				counterInt++;
				j++;
			}
			counterInt = counterInt / 8;
			TPcount.add(counterInt);
			counterInt = 0;
			sumInt = 0;
		}

		BufferedWriter BW = null;
		try {
			File echofile = new File("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\Echo" + code + ".txt");
			BW = new BufferedWriter(new FileWriter(echofile, true));
			if (!echofile.exists()) {
				echofile.createNewFile();
			}
			for (int i = 0; i < DLinfo.size(); i++) {
				BW.write(String.valueOf(DLinfo.get(i)));
				BW.newLine();
			}
			BW.newLine();
		} catch (Exception e) {
			System.out.println("couldn't write a echofile");
		} finally {
			try {
				if (BW != null)
					BW.close();
			} catch (Exception e) {
				System.out.println("couldn't close the BufferedWriter" + e);
			}
		}
		BW = null;
		try {
			File echofile2 = new File("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\EchoTP" + code + ".txt");
			BW = new BufferedWriter(new FileWriter(echofile2, true));
			if (!echofile2.exists()) {
				echofile2.createNewFile();
			}
			for (int i = 0; i < TPcount.size(); i++) {
				BW.write(String.valueOf(TPcount.get(i)));
				BW.newLine();
			}
			BW.newLine();
		} catch (Exception e) {
			System.out.println("couldn't write an echofile");
		} finally {
			try {
				if (BW != null)
					BW.close();
			} catch (Exception e) {
				System.out.println("couldn't close the BufferedWriter" + e);
			}
		}
		BW = null;
		try {
			File echofile3 = new File("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\EchoDLtime" + code + ".txt");
			BW = new BufferedWriter(new FileWriter(echofile3, true));
			if (!echofile3.exists()) {
				echofile3.createNewFile();
			}
			for (int i = 0; i < DLtime.size(); i++) {
				BW.write(String.valueOf(DLtime.get(i)));
				BW.newLine();
			}
			BW.newLine();
		} catch (Exception e) {
			System.out.println("couldn't write an echofile");
		} finally {
			try {
				if (BW != null)
					BW.close();
			} catch (Exception e) {
				System.out.println("couldn't close the BufferedWriter" + e);
			}
		}
		s.close();
		r.close();
	}

	public void image(int hostPort, int clientPort) throws SocketException, IOException, UnknownHostException {
		String code = new String();
		String packetInfo = new String();
		String message;
		int choice = 0;
		String fileName;
		Scanner input = new Scanner(System.in);
		Scanner input2 = new Scanner(System.in);
		System.out.println("for receiving image by default  press 1");
		System.out.println("for receiving image with parameters press 2");
		try {
			choice = input.nextInt();
			if (choice == 1) {
				System.out.println("please give password for receiving image pachets in form MXXXX:");
				code = input2.nextLine();
				packetInfo = code;
			} else {
				System.out.println("please give password for receiving image pachets in form MXXXX:");
				code = input2.nextLine();
				System.out.println("for using fix cam please press 1");
				System.out.println("for using ptz cam please press 2");
				choice = input.nextInt();
				if (choice == 1) {
					packetInfo = code + "CAM=FIX";
				} else {
					packetInfo = code + "CAM=PTZ";
				}
			}

		} catch (Exception e) {
			System.out.println(e);
		}

		DatagramSocket s = new DatagramSocket();
		int length = 0, prelength = 0, start = 0;
		byte[] txbuffer = packetInfo.getBytes();
		byte[] hostIP = { (byte) 155, (byte) 207, 18, (byte) 208 };
		InetAddress hostAddress = InetAddress.getByAddress(hostIP);
		DatagramPacket p = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, hostPort);
		DatagramSocket r = new DatagramSocket(clientPort);
		r.setSoTimeout(5000);
		byte[] rxbuffer = new byte[2048];
		DatagramPacket q = new DatagramPacket(rxbuffer, rxbuffer.length);
		fileName = ("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\image" + packetInfo + ".jpeg");
		FileOutputStream imageFile = new FileOutputStream(fileName);
		s.send(p);
		for (;;) {
			try {
				r.receive(q);
				message = new String(rxbuffer, 0, q.getLength());// gia na ipologizoume to megethos tou paketou
				System.out.println(message);
				prelength = length;
				length = message.length();
				for (int i = 0; i < message.length(); i++) {
					imageFile.write(rxbuffer[i]);
				}
				if ((start > 0) && (prelength != length)) {
					break;
				}
				start++;
			} catch (Exception x) {
				System.out.println(x);
			}
		}
		imageFile.close();
		s.close();
		r.close();
	}

	public void DPCM_sound(int hostPort, int clientPort) throws SocketException, IOException, UnknownHostException {
		String code = new String();
		int temp1 = 0, temp2 = 0, mask1 = 15, mask2 = 240;
		int numOfPackets = 0, numOfPackets2 = 0;
		String packetInfo = new String();
		int choice = 0;
		String fileName;
		String message;
		Scanner input = new Scanner(System.in);
		Scanner input2 = new Scanner(System.in);
		try {
			System.out.println("please give password for receiving sound packets in form AXXXX:");
			code = input2.nextLine();
			System.out.println("please give number of packets in form XXX");
			numOfPackets = input.nextInt();// xrisimopoieitai ws metritis se vroxo for
			numOfPackets2 = numOfPackets;// xrisimopoieitai gia eggrafi ixou
			System.out.println("please give Y parameter");
			System.out.println("for Y=T press 1");
			System.out.println("for Y=F press 2");
			choice = input.nextInt();

			if (choice == 1) {
				packetInfo = code + "Y=T" + String.valueOf(numOfPackets);
			} else {
				packetInfo = code + "Y=F" + String.valueOf(numOfPackets);
			}

		} catch (Exception e) {
			System.out.println(e);
		}

		DatagramSocket s = new DatagramSocket();
		byte[] txbuffer = packetInfo.getBytes();
		byte[] hostIP = { (byte) 155, (byte) 207, 18, (byte) 208 };
		byte[] rxbuffer = new byte[2048];
		byte[] sound = new byte[256 * numOfPackets]; // apothikefsi deigmatwn ixou
		ArrayList<Integer> subsBuffer = new ArrayList<Integer>();// apothikefsi diaforwn
		InetAddress hostAddress = InetAddress.getByAddress(hostIP);
		DatagramPacket p = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, hostPort);
		DatagramSocket r = new DatagramSocket(clientPort);
		r.setSoTimeout(5000);
		DatagramPacket q = new DatagramPacket(rxbuffer, rxbuffer.length);
		int index = 0, base = 0;// gia ti metafora twn deigmatwn se pinaka(bytes).

		s.send(p);

		for (;;) {
			try {
				r.receive(q);
				message = new String(rxbuffer, 0, q.getLength());
				numOfPackets--;
				ArrayList<Integer> tempBuffer = new ArrayList<Integer>();
				int[] soundVector = new int[256];
				for (int i = 0; i < 256; i++) {
					soundVector[i] = 0;
				}
				for (int i = 0; i < 128; i++) {
					temp1 = rxbuffer[i] & mask1;
					temp2 = rxbuffer[i] & mask2;
					temp2 >>>= 4;
					int part1 = temp1 - 8;
					int part2 = temp2 - 8;
					tempBuffer.add(part1);
					tempBuffer.add(part2);
					subsBuffer.add(part1);
					subsBuffer.add(part2);
				}
				for (int i = 0; i < 255; i++) {
					soundVector[i + 1] = (soundVector[i] + tempBuffer.get(i));
					if (soundVector[i + 1] > 255) {
						soundVector[i + 1] = 255;
					}
					
				}

				for (int i = 0; i < 256; i++) {
					sound[base + i] = (byte) soundVector[i];
					index++;
				}
				base += index;
				index = 0;
				if (numOfPackets == 0)
					break;

			} catch (Exception x) {
				System.out.println(x);
			}
		}
		s.close();
		r.close();

		try {
			int indx = 0;
			int length = 8000;
			AudioFormat format = new AudioFormat(8000, 8, 1, true, false);
			SourceDataLine line = AudioSystem.getSourceDataLine(format);
			line.open(format, 32000);
			line.start();
			while (indx < 256 * numOfPackets2) {
				line.write(sound, indx, length);
				indx += 8000;
				if ((256 * numOfPackets2) - indx < 8000) {
					length = 256 * numOfPackets2 - indx;
				}
			}

			line.stop();
			line.close();
		} catch (LineUnavailableException e) {
			System.out.println(e);
		}

		BufferedWriter BW = null;
		try {
			File echofile = new File("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\sound.txt");
			BW = new BufferedWriter(new FileWriter(("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\sound.txt"), true));
			if (!echofile.exists()) {
				echofile.createNewFile();
			}
			for (int i = 0; i < sound.length; i++) {
				BW.write(String.valueOf(sound[i]));
				BW.newLine();
			}
			BW.newLine();
		} catch (Exception e) {
			System.out.println("couldn't write a soundfile");
		} finally {
			try {
				if (BW != null)
					BW.close();
			} catch (Exception e) {
				System.out.println("couldn't close the BufferedWriter" + e);
			}
		}

		BufferedWriter BW2 = null;
		try {
			File sound2file = new File("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\soundSubs.txt");
			BW2 = new BufferedWriter(new FileWriter(("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\soundSubs.txt"), true));
			if (!sound2file.exists()) {
				sound2file.createNewFile();
			}
			for (int i = 0; i < subsBuffer.size(); i++) {
				BW2.write(String.valueOf(subsBuffer.get(i)));
				BW2.newLine();
			}
			BW2.newLine();
		} catch (Exception e) {
			System.out.println("couldn't write a soundfile");
		} finally {
			try {
				if (BW2 != null)
					BW2.close();
			} catch (Exception e) {
				System.out.println("couldn't close the BufferedWriter" + e);
			}
		}
	}

	public void AQDPCM_sound(int hostPort, int clientPort) throws SocketException, IOException, UnknownHostException {
		String code = new String();
		int temp1 = 0, temp2 = 0;
		int numOfPackets = 0, numOfPackets2 = 0;
		String packetInfo = new String();
		double startDL = 0, endDL = 0;// des an xreiazontai
		int choice = 0;
		String fileName;
		String message;
		Scanner input = new Scanner(System.in);
		Scanner input2 = new Scanner(System.in);
		try {
			System.out.println("please give password for receiving sound packets in form AXXXX:");
			code = input2.nextLine();
			System.out.println("please give number of packets in form XXX");
			numOfPackets = input.nextInt();// gia meiwsi se for
			numOfPackets2 = numOfPackets;// gia eggrafi ixou
			System.out.println("please give Y parameter");
			System.out.println("for Y=T press 1");
			System.out.println("for Y=F press 2");
			choice = input.nextInt();

			if (choice == 1) {
				packetInfo = code + "AQ" + "Y=T" + String.valueOf(numOfPackets);
			} else {
				packetInfo = code + "AQ" + "Y=F" + String.valueOf(numOfPackets);
			}

		} catch (Exception e) {
			System.out.println(e);
		}

		DatagramSocket s = new DatagramSocket();
		byte[] txbuffer = packetInfo.getBytes();
		byte[] hostIP = { (byte) 155, (byte) 207, 18, (byte) 208 };
		byte[] rxbuffer = new byte[2048];
		byte[] sound = new byte[2 * 256 * numOfPackets];
		ArrayList<Integer> subsBuffer = new ArrayList<Integer>();
		ArrayList<Integer> medians = new ArrayList<Integer>();
		ArrayList<Integer> steps = new ArrayList<Integer>();
		ArrayList<Integer> samples = new ArrayList<Integer>();
		InetAddress hostAddress = InetAddress.getByAddress(hostIP);
		DatagramPacket p = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, hostPort);
		DatagramSocket r = new DatagramSocket(clientPort);
		r.setSoTimeout(800);
		DatagramPacket q = new DatagramPacket(rxbuffer, rxbuffer.length);
		startDL = System.currentTimeMillis();

		int counter = 0;

		s.send(p);

		for (;;) {
			try {
				r.receive(q);
				message = new String(rxbuffer, 0, q.getLength());
				numOfPackets--;
				ArrayList<Integer> tempBuffer = new ArrayList<Integer>();
				int bima = 0, med = 0;
				int step1 = 0, step2 = 0, part1 = 0, part2 = 0, sample1 = 0, sample2 = 0;
				int temp = 0;
				byte[] median = new byte[2];
				int[] soundVector = new int[256];
				for (int i = 0; i < 256; i++) {
					soundVector[i] = 0;
				}
				median[1] = rxbuffer[0];
				median[0] = rxbuffer[1];
				if ((median[0] & 0x10000000) == 0) {
					med = median[0];
					med <<= 8;
					temp = median[1];
					temp &= 0x000000FF;
					med |= temp;
					med &= 0x0000FFFF;
				} else {
					med = median[0];
					med <<= 8;
					temp = median[1];
					temp &= 0x000000FF;
					med |= temp;
					med |= 0xFFFF0000;
				}
				medians.add(med);

				step1 = rxbuffer[3];
				step2 = rxbuffer[2];
				step1 &= 0x000000FF;
				step2 &= 0x000000FF;

				bima = (step1 << 8) | step2;
				bima = bima & 0x0000FFFF;
				steps.add(bima);
				temp = 0;
				for (int i = 4; i < 132; i++) {
					temp1 = rxbuffer[i] & 0x0000000F;
					temp2 = rxbuffer[i] & 0x000000F0;
					temp2 >>>= 4;
					part1 = temp2 - 8;
					part2 = temp1 - 8;
					subsBuffer.add(part1);
					subsBuffer.add(part2);

					sample1 = temp + part1 * bima + med;
					samples.add(sample1);
					sample2 = part1 * bima + part2 * bima + med;
					temp = part2;
					samples.add(sample2);

					sound[counter] = (byte) (sample1 & 0x000000FF);
					sound[counter + 1] = (byte) ((sample1 & 0x0000FF00) >> 8);
					sound[counter + 2] = (byte) (sample2 & 0x000000FF);
					sound[counter + 3] = (byte) ((sample2 & 0x0000FF00) >> 8);
					counter += 4;
				}
				
				if (numOfPackets == 0)
					break;

			} catch (Exception x) {
				System.out.println(x);
			}
		}

		endDL = (System.currentTimeMillis() - startDL);
		s.close();
		r.close();

		try {
			int indx = 0;
			int length = 16000;
			AudioFormat format = new AudioFormat(8000, 16, 1, true, false);
			SourceDataLine line = AudioSystem.getSourceDataLine(format);
			line.open(format, 32000);
			line.start();
			while (indx < 2 * 256 * numOfPackets2) {
				line.write(sound, indx, length);
				indx += 16000;
				if ((2 * 256 * numOfPackets2) - indx < 16000) {
					length = 2 * 256 * numOfPackets2 - indx;
				}
			}

			line.stop();
			line.close();
		} catch (LineUnavailableException e) {
			System.out.println(e);
		}

		BufferedWriter BW = null;
		try {
			File sound1file = new File("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\AQsound.txt");
			BW = new BufferedWriter(new FileWriter(("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\AQsound.txt"), true));
			if (!sound1file.exists()) {
				sound1file.createNewFile();
			}
			for (int i = 0; i < samples.size(); i++) {
				BW.write(String.valueOf(samples.get(i)));
				BW.newLine();
			}
			BW.newLine();
		} catch (Exception e) {
			System.out.println("couldn't write a soundfile");
		} finally {
			try {
				if (BW != null)
					BW.close();
			} catch (Exception e) {
				System.out.println("couldn't close the BufferedWriter" + e);
			}
		}

		BufferedWriter BW2 = null;
		try {
			File sound2file = new File("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\AQsoundSubs.txt");
			BW2 = new BufferedWriter(new FileWriter(("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\AQsoundSubs.txt"), true));
			if (!sound2file.exists()) {
				sound2file.createNewFile();
			}
			for (int i = 0; i < subsBuffer.size(); i++) {
				BW2.write(String.valueOf(subsBuffer.get(i)));
				BW2.newLine();
			}
			BW2.newLine();
		} catch (Exception e) {
			System.out.println("couldn't write a soundfile");
		} finally {
			try {
				if (BW2 != null)
					BW2.close();
			} catch (Exception e) {
				System.out.println("couldn't close the BufferedWriter" + e);
			}
		}
		BufferedWriter BW3 = null;
		try {
			File sound3file = new File("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\AQsoundMedeans.txt");
			BW3 = new BufferedWriter(
					new FileWriter(("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\AQsoundMedeans.txt"), true));
			if (!sound3file.exists()) {
				sound3file.createNewFile();
			}
			for (int i = 0; i < medians.size(); i++) {
				BW3.write(String.valueOf(medians.get(i)));
				BW3.newLine();
			}
			BW3.newLine();
		} catch (Exception e) {
			System.out.println("couldn't write a soundfile");
		} finally {
			try {
				if (BW3 != null)
					BW3.close();
			} catch (Exception e) {
				System.out.println("couldn't close the BufferedWriter" + e);
			}
		}
		BufferedWriter BW4 = null;
		try {
			File sound4file = new File("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\AQsoundSteps.txt");
			BW4 = new BufferedWriter(new FileWriter(("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\AQsoundSteps.txt"), true));
			if (!sound4file.exists()) {
				sound4file.createNewFile();
			}
			for (int i = 0; i < steps.size(); i++) {
				BW4.write(String.valueOf(steps.get(i)));
				BW4.newLine();
			}
			BW4.newLine();
		} catch (Exception e) {
			System.out.println("couldn't write a soundfile");
		} finally {
			try {
				if (BW4 != null)
					BW4.close();
			} catch (Exception e) {
				System.out.println("couldn't close the BufferedWriter" + e);
			}
		}
	}

	public void ithakiCopter(int hostPort, int clientPort)
			throws SocketException, IOException, UnknownHostException, InterruptedException {
		Scanner input1 = new Scanner(System.in);
		Scanner input2 = new Scanner(System.in);
		String code = " ";
		ArrayList<String> telemetry = new ArrayList<String>();
		String flightlevel = " ", Lmotor = " ", Rmotor = " ";
		System.out.println("Please give code for comunication with ithakiCopter");
		code = input1.nextLine();
		String packetInfo = new String();
		byte[] hostIP = { (byte) 155, (byte) 207, 18, (byte) 208 };
		InetAddress hostAddress = InetAddress.getByAddress(hostIP);
		DatagramSocket s = new DatagramSocket();
		DatagramSocket r = new DatagramSocket(clientPort);
		byte[] rxbuffer = new byte[5000];
		DatagramPacket q = new DatagramPacket(rxbuffer, rxbuffer.length);
		double startDL = 0, endDL = 0;
		r.setSoTimeout(5000);
		packetInfo = code + "\r";
		byte[] txbuffer = packetInfo.getBytes();
		DatagramPacket p = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, hostPort);
		startDL = System.currentTimeMillis();
		while (endDL < 4 * 60 * 1000) {
			try {
				s.send(p);
				r.receive(q);
				String message = new String(rxbuffer, 0, q.getLength());
				System.out.println(message);
				telemetry.add(message);
			} catch (Exception ex) {
				System.out.println(ex);
			}
			endDL = System.currentTimeMillis() - startDL;
		}

		BufferedWriter BW = null;
		try {
			File copterfile = new File("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\copter.txt");
			BW = new BufferedWriter(new FileWriter(("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\copter.txt"), true));
			if (!copterfile.exists()) {
				copterfile.createNewFile();
			}
			for (int i = 0; i < telemetry.size(); i++) {
				BW.write(String.valueOf(telemetry.get(i)));
				BW.newLine();
			}
			BW.newLine();
		} catch (Exception e) {
			System.out.println("couldn't write a copterfile");
		} finally {
			try {
				if (BW != null)
					BW.close();
			} catch (Exception e) {
				System.out.println("couldn't close the BufferedWriter" + e);
			}
		}
		r.close();
		s.close();

	}

	public void vehicle(int hostPort, int clientPort)
			throws SocketException, IOException, UnknownHostException, InterruptedException {
		Scanner input1 = new Scanner(System.in);
		Scanner input2 = new Scanner(System.in);
		String code = new String();
		String pid = new String();
		int choice = 0;
		ArrayList<String> measure = new ArrayList<String>();// apothikefsi metrisewn oximatos
		System.out.println("Please give code for taking vehicle measurments");
		code = input1.nextLine();
		System.out.println("Please choose PID parameter");
		System.out.println("For engine run time press 1");
		System.out.println("For intake air temperature press 2");
		System.out.println("For throttle possition press 3");
		System.out.println("For engine RPM press 4");
		System.out.println("For vehicle speed press 5");
		System.out.println("For coolant temperature press 6");

		choice = input2.nextInt();
		switch (choice) {
		case 1: {
			pid = "1F";
			break;
		}
		case 2: {
			pid = "0F";
			break;
		}
		case 3: {
			pid = "11";
			break;
		}
		case 4: {
			pid = "0C";
			break;
		}
		case 5: {
			pid = "0D";
			break;
		}
		case 6: {
			pid = "05";
			break;
		}

		}

		String packetInfo = new String();
		byte[] hostIP = { (byte) 155, (byte) 207, 18, (byte) 208 };
		InetAddress hostAddress = InetAddress.getByAddress(hostIP);
		DatagramSocket s = new DatagramSocket();
		DatagramSocket r = new DatagramSocket(clientPort);
		byte[] rxbuffer = new byte[5000];
		DatagramPacket q = new DatagramPacket(rxbuffer, rxbuffer.length);
		double startDL = 0, endDL = 0;
		r.setSoTimeout(5000);
		String xx = " ", yy = " ";// 16dikes times metrisewn XX kai YY
		Integer xxdec = 0, yydec = 0;// 10dikes times metrisewn XX kai YY
		String measurType = " ";// typos metrisis
		int measurment = 0;// apokwdikopoihmenh timi metrisis
		startDL = System.currentTimeMillis();
		while (endDL < 4 * 60 * 1000) {
			packetInfo = code + "OBD=01 " + pid;
			byte[] txbuffer = packetInfo.getBytes();
			DatagramPacket p = new DatagramPacket(txbuffer, txbuffer.length, hostAddress, hostPort);

			s.send(p);

			try {

				r.receive(q);
				String message = new String(rxbuffer, 0, q.getLength());
				System.out.println(message);

				switch (pid) {
				case "1F": {
					xx = message.substring(6, 8);
					yy = message.substring(9, 11);
					xxdec = Integer.parseInt(xx, 16);
					yydec = Integer.parseInt(yy, 16);
					measurType = "ERT";
					measurment = 256 * xxdec + yydec;
					break;
				}
				case "0F": {
					xx = message.substring(6, 8);
					xxdec = Integer.parseInt(xx, 16);
					measurType = "IAT";
					measurment = xxdec - 40;
					break;
				}
				case "11": {
					xx = message.substring(6, 8);
					xxdec = Integer.parseInt(xx, 16);
					measurType = "Tpos";
					measurment = xxdec * 100 / 255;
					break;
				}
				case "0C": {
					xx = message.substring(6, 8);
					yy = message.substring(9, 11);
					xxdec = Integer.parseInt(xx, 16);
					yydec = Integer.parseInt(yy, 16);
					measurType = "RPM";
					measurment = ((xxdec * 256) + yydec) / 4;
					break;
				}
				case "0D": {
					xx = message.substring(6, 8);
					xxdec = Integer.parseInt(xx, 16);
					measurType = "speed";
					measurment = xxdec;
					break;
				}
				case "05": {
					xx = message.substring(6, 8);
					xxdec = Integer.parseInt(xx, 16);
					measurType = "cooltemp";
					measurment = xxdec - 40;
					break;
				}

				}
			} catch (Exception ex) {
				System.out.println(ex);
			}
			measure.add(String.valueOf(measurment));
			endDL = System.currentTimeMillis() - startDL;
		}
		r.close();
		s.close();

		BufferedWriter BW = null;
		try {
			File vehicleFile = new File("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\vehicle" + measurType + ".txt");
			BW = new BufferedWriter(
					new FileWriter(("C:\\Users\\ypatiapd\\Desktop\\δικτυα2\\vehicle" + measurType + ".txt"), true));
			if (!vehicleFile.exists()) {
				vehicleFile.createNewFile();
			}
			for (int i = 0; i < measure.size(); i++) {
				BW.write(measure.get(i));
				BW.newLine();
			}
			BW.newLine();
		} catch (Exception e) {
			System.out.println("couldn't write a vehiclefile");
		} finally {
			try {
				if (BW != null)
					BW.close();
			} catch (Exception e) {
				System.out.println("couldn't close the BufferedWriter" + e);
			}
		}
	}
}
