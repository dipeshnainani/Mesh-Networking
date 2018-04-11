import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

/**
 * This code implements client,server, table and Buoy class in it.
 * This code basically acts three ways. According to the command taken from
 * BuoyCommand.java it either shutsdown the thread, prints the temperature or
 * displays an image. In this the client and server act according to the command 
 * received by the user from the BuoyCommand.java file.
 * 
 * @author Dipesh Nainani
 *
 */

class Client extends Thread{
	
	/**
	 * This is the client class and each time for different 
	 * addresses different threads are created and they run 
	 * simultaneously.
	 * 
	 */
	String address1;
	String[] neighbor1;
	String network1;
	String command1 = "";
	String pathOfPic1 = "";

	
	
	public Client()
	{}
	public Client(String address, String[] neighbor, String network, String caseOperation, String pathOfPic)
	{
		address1 = address;
		neighbor1 = neighbor;
		network1 = network;
		command1 = caseOperation;
		pathOfPic1 = pathOfPic;
		
	}
	/**
	 * This method creates different threads for different neighbors and runs
	 */
	public void createThreads()
	{
		int i = 0;
		while(i < neighbor1.length)
		{
			if(neighbor1[i] !=null)
			{
				System.out.println("here");
				Client client = new Client();
				Thread threadNew = new Thread(client,neighbor1[i]);
				threadNew.start();
				
			}
			else
			{
				break;
			}
			i = i + 1;
		}		
	}
	
	@SuppressWarnings({ "resource", "deprecation" })
	public void run()
	{
		try {
			if(command1 == "1")
			{
					System.out.println("Hi");
					String newAddress = Thread.currentThread().getName();
			
					// send to the server
					DatagramSocket socket = new DatagramSocket();	
					String address = newAddress.substring(0, newAddress.indexOf(':'));		
					String port = newAddress.substring(newAddress.indexOf(':')+1, newAddress.length());		
					InetAddress getaddress = InetAddress.getByName(address);
					String message = "HELLO!I am this";     	
					byte[] sendMessage = message.getBytes();	     	
		     		int Port = Integer.valueOf(port);     	
		     		DatagramPacket sentPacket = new DatagramPacket(sendMessage, sendMessage.length, getaddress, Port);	     	
		     		socket.send(sentPacket);
	 	     	
		     		// receive from the server
		     		byte[] messageReceive = new byte[256];
		     		MulticastSocket receiver = new MulticastSocket(Port);				
		     		InetAddress finalAddress = InetAddress.getByName(address);
		     		receiver.joinGroup(finalAddress);				
		     		DatagramPacket packetReceive = new DatagramPacket(messageReceive, messageReceive.length);
					receiver.receive(packetReceive);
					String messageNew = new String(messageReceive, 0, packetReceive.getLength());
					System.out.println("message from server " +messageNew);
			}
			else if(command1=="2")
			{
				String newAddress = Thread.currentThread().getName();
				String address = newAddress.substring(0, newAddress.indexOf(':'));		
				String port = newAddress.substring(newAddress.indexOf(':')+1, newAddress.length());
				int Port = Integer.valueOf(port);
				int ACK = 0;
				int SEQ = 0;
				DatagramSocket sendSock = new DatagramSocket();
			    DatagramPacket sendPacket;
			    InetAddress addressN = InetAddress.getByName(address);
			    File fileinputStream = new File(pathOfPic1);
			    byte[] newArray = new byte[(int) fileinputStream.length()];
			    InputStream input = new BufferedInputStream(new FileInputStream(fileinputStream));
			    
			    
			    ByteArrayOutputStream finalVar = new ByteArrayOutputStream(newArray.length);
				int bytesInPic = 0;
				
				while(bytesInPic!= -1)
				{
					bytesInPic = input.read(newArray);
					if(bytesInPic > 0)
					{
						finalVar.write(newArray , 0 , bytesInPic);
					}
				}
				
				byte[] finalArray = finalVar.toByteArray();
				
				byte[] sendArray = new byte[10240];
				String userData;

				System.out.println("enter the number of chunks you want the file to be transferred in");
				Scanner sc = new Scanner(System.in);
				int chunks = sc.nextInt();	
				
//				int chunks = 4;
		        int newChunks = 0;
		        int size = 0;
		        // sends chunks of data to the client
		        while(newChunks!=chunks-1)
		        {
		        	System.out.println("for chunk "+newChunks);
		        	for(int i=0; i<sendArray.length;i++)
		        	{
		        		sendArray[i] = finalArray[newChunks * 10240 + i];
		        		size = size +1;
		        	}
		        	sendPacket = new DatagramPacket(sendArray, sendArray.length, addressN, Port);
		    		sendSock.send(sendPacket);
		    	
		    		ACK = ACK + 1;
					SEQ = SEQ + 1;
					userData = " ACK " + ACK  + " SEQ " + SEQ + " received for chunk " + chunks;
		    		
					System.out.println(userData);
		    		
		        	newChunks = newChunks + 1;
		        }
		        // works for the extra bytes left for the image.
		        int j = 0;
		        for(int i= newChunks * 10240 ; i<finalArray.length;i++)
		    	{
		    		sendArray[j] = finalArray[i];
		    		j = j + 1;
		    		
		    	}
		        sendPacket = new DatagramPacket(sendArray, sendArray.length, addressN, Port);
				sendSock.send(sendPacket);
		        

			}
			else if(command1 == "3")
			{
				System.out.println("shutdown");
				Thread.currentThread().stop();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}

/**
 * This is the server class and it listens on every thread created by the client
 * and updates the routing table.
 *
 */
class Server{

	String address2;
	String[] neighbor2;
	String network2;
	Riptable newTable;
	int temperature1;
	String caseOperation1;
	String pathOfPic1 = "";
	
	public Server(String address, String[] neighbor, String network, Riptable newTable, String temperature, String caseOperation,String pathOfPic)
	{
		address2 = address;
		neighbor2 = neighbor;
		network2 = network;
		this.newTable = newTable;
		temperature1 = Integer.valueOf(temperature);
		caseOperation1 = caseOperation;
		pathOfPic1 = pathOfPic;
	}
	
	/**
	 * This method listens on every neighbor
	 */
	@SuppressWarnings("deprecation")
	public void listen() {
	    System.setProperty("java.net.preferIPv4Stack", "true");
	    MulticastSocket receiver;

	    if(caseOperation1 == "1")
	    {
	    	try{
				int i = 0;
				while( i < neighbor2.length)
				{
					if(neighbor2[i] != null)
					{
						// receive from the client
						String newAddress = neighbor2[i];					
						String address = newAddress.substring(0, newAddress.indexOf(':'));
						String port = newAddress.substring(newAddress.indexOf(':')+1, newAddress.length());					
						int Port = Integer.valueOf(port);					
						byte[] messageReceive = new byte[256];
						receiver = new MulticastSocket(Port);				
						InetAddress finalAddress = InetAddress.getByName(address);
						receiver.joinGroup(finalAddress);				
						DatagramPacket packetReceive = new DatagramPacket(messageReceive, messageReceive.length);
						receiver.receive(packetReceive);
						String message = new String(messageReceive, 0, packetReceive.getLength());
						System.out.println("message received " +message);
						this.newTable.update(temperature1,address);
						this.newTable.display();
						temperature1 = temperature1 + i;
						System.out.println("Temrperatur is " +temperature1);
						
						
						// send back to the client
						DatagramSocket socket = new DatagramSocket();
				     	InetAddress getaddress = InetAddress.getByName(address);
				     	String messageFinal = "Hi! I am this one";     	
				     	byte[] sendMessage = messageFinal.getBytes();	     	
				     	int PortNew = Integer.valueOf(port);     	
				     	DatagramPacket sentPacket = new DatagramPacket(sendMessage, sendMessage.length, getaddress, PortNew);	     	
				     	socket.send(sentPacket);
				 
					}
					else
					{
						break;
					}
					i = i + 1;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	    }
	    
	    else if(caseOperation1 == "2")
	    {
	    	try{
				int i = 0;
				while( i < neighbor2.length)
				{
					if(neighbor2[i] != null)
					{
						String newAddress = neighbor2[i];					
						String addressN = newAddress.substring(0, newAddress.indexOf(':'));
						String port = newAddress.substring(newAddress.indexOf(':')+1, newAddress.length());					
						int portN = Integer.valueOf(port);					
						int SEQ = 0 , j =0;
						DatagramSocket receive = new DatagramSocket(portN);
						DatagramPacket clientPacket;
						byte[] buffer = new byte[10240];
						byte[] newBuf;
						String userData;
						OutputStream output = new BufferedOutputStream(new FileOutputStream(pathOfPic1));
						int newChunks = 0;
						
						System.out.println("enter the number of chunks you want the file to be transferred in");
						Scanner sc = new Scanner(System.in);
						int chunks = sc.nextInt();
						
						// receives all the byte arrays send by the client.
						//int chunks = 3;
						int[] ACK = new int[chunks];
						
						while(newChunks!=chunks-1)
						{
							clientPacket = new DatagramPacket(buffer, buffer.length);
							receive.receive(clientPacket);
							newBuf = clientPacket.getData();
							int a = clientPacket.getOffset();
							int l = clientPacket.getLength();
							output.write(newBuf,a,l);
							
							userData = " ACK " + ACK  + " SEQ " + SEQ + " send for chunk " + chunks;
							System.out.println(userData);
							ACK[j] = newChunks;
							j = j + 1;
							SEQ = SEQ + 1;
							newChunks = newChunks + 1;
							
						}
						// works for the extra bytes left.
						
						clientPacket = new DatagramPacket(buffer, buffer.length);
						receive.receive(clientPacket);
						newBuf = clientPacket.getData();
						int a = clientPacket.getOffset();
						int l = clientPacket.getLength();
						output.write(newBuf,a,l);
						
						userData = " ACK " + ACK  + " SEQ " + SEQ + " send for chunk " + chunks;
						System.out.println(userData);
						ACK[j] = newChunks;
						j = j + 1;
						SEQ = SEQ + 1;
						newChunks = newChunks + 1;

				 
					}
					else
					{
						break;
					}
					i = i + 1;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	    }
	    
	    else if(caseOperation1 == "3")
	    {
	    	System.out.println("Shutdown the thread");
	    	Thread.currentThread().stop();
	    }
	    
		
			
	}
	
}
/**
 * This class updates the routing table and displays the routing table.
 */
class Riptable{
	
	ArrayList<Integer> table = new ArrayList<Integer>();
	ArrayList<String> newAddress = new ArrayList<String>();
	
	public void update(int i, String address)
	{
		table.add(i);
		newAddress.add(address);
	}
	
	public void display()
	{
		System.out.println("printing the arraylist");
		System.out.println("Hops              Neighbor");
		System.out.println("========================");
		
		for(int i = 0 ; i<table.size();i++)
		{
			System.out.println(table.get(i) + "        " + newAddress.get(i));
		}
	}
	
}

/**
 * This is the main class which runs for different buoys.
 * It takes different buoy files and calls the client to make threads 
 * and class server to listen on the threads.
 * 
 */
public class Buoy {
	
	@SuppressWarnings("resource")
	public static void main(String args[])
	{
		String line;
		String[] neighbor = new String[10];
		int i = 0;
		String address = ""; 
		String network = "";
		String temperature = "";
		String caseOperation = "";
		String pathOfPic = "";
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the path of the file");
		String path = sc.nextLine();
		try{
			BufferedReader buffer = new BufferedReader(new FileReader(path));
			while((line = buffer.readLine()) != null)
			{
				if(line.contains("ADDRESS: "))
				{	
					 address = line.substring(10,line.length());
				}
				else if(line.contains("NEIGHBOR: "))
				{
					neighbor[i] = line.substring(10,line.length());
					i = i + 1;
				}
				else if(line.contains("NETWORK: "))
				{
					network = line.substring(10,line.length());
				}
				else if(line.contains("TEMPERATURE: "))
				{
					temperature = line.substring(13,line.length());
					System.out.println(temperature);
				}
				else if(line.contains("PATH: "))
				{
					pathOfPic = line.substring(7,line.length());
					System.out.println(temperature);
				}
			}
			Riptable newTable = new Riptable();
			
			
			caseOperation = BuoyCommand.command;
			Client client = new Client(address,neighbor,network,caseOperation,pathOfPic);
			Server server = new Server(address,neighbor,network,newTable,temperature,caseOperation,pathOfPic);
			client.createThreads();
			server.listen();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	}

