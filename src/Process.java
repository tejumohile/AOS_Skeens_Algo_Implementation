
/**
 *
 * @author Tejashree
 */
import java.util.*;
import helper.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Process {

    // Self Information
    private static String processID;
    private static String IPAddress;
    private static String portNo;
    private static HashMap<String, Neighbor> neighborList = new HashMap<>();
    private static ArrayList<Message> messageList = new ArrayList<>();
    private static UDPServer server;
    private static ArrayList<Message> sendMessageQueue;
    private static ArrayList<String> receiveMessageQueue;
    private static HashMap<String, Integer> undeliverableMessageCounts = new HashMap<>();
    private static int timestamp = 1;

    public static void main(String args[]) {
        if (initialize()) {
            startServer();
            startACKNotReceivedThread();
            sendUndeliverables();
            startClientThread();
        }
    }

    public static boolean initialize() {
        System.out.println("Enter the Process ID.");
        Scanner kbd = new Scanner(System.in);
        processID = kbd.next();
        String[] info = FileOperations.getOwnInformation(processID, "network.txt");
        if (info != null) {
            IPAddress = info[1];
            portNo = info[2];
        } else {
            IPAddress = null;
            portNo = null;
            return false;
        }
        neighborList = FileOperations.readNeighbors("network.txt", processID);

        messageList = FileOperations.readMessages("messages.txt", "messengerList.txt", processID);
        receiveMessageQueue = new ArrayList<>();
        setUndeliverableMessageCounts();
        createInitialMessages();
        return true;
    }

    public static void startServer() {
        try {
            server = new UDPServer(portNo, processID, neighborList, messageList, sendMessageQueue, receiveMessageQueue, undeliverableMessageCounts);
            Thread serverThread = new Thread(server);
            serverThread.start();
            System.out.println("Server started...........PRESS ANY KEY TO START THE PROCESS");
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String anyKey = input.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void startClientThread() {
        Thread sendingThread;
        sendingThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(3000);

                        if (!sendMessageQueue.isEmpty()) {
                            for (int p = 0; p < sendMessageQueue.size(); p++) {
//                                // If the message is not already sent....
                                // then send it. Else ignore that and move on.
                                if (!sendMessageQueue.get(p).isIsSent()) {
                                    Message msg = getNextMessageInQueue(sendMessageQueue.get(p));
                                    String m = msg.getMessage();
                                    String receiverNode = msg.getReceiverNode();
                                    byte[] msgByte = m.getBytes();
                                    if (sendData(new DatagramPacket(msgByte,
                                            msgByte.length,
                                            getINETIPAddress(
                                                    neighborList.get(receiverNode)
                                                    .getIPAddress()),
                                            neighborList.get(receiverNode).getPortNo()))) {
                                        sendMessageQueue.get(p).setIsSent(true);
                                        sentMessageLogging(sendMessageQueue.get(p).toString());
//                                        sendMessageQueue.remove(p);
                                    }
                                }
                            }
                        }

                    } catch (InterruptedException | UnknownHostException ex) {
                        Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        sendingThread.start();

    }

    public static void startACKNotReceivedThread()
    {
        Thread aCKNotReceivedThread;
        aCKNotReceivedThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(4000);
                        System.out.println("This is the ACK thread --->" + sendMessageQueue.toString());
                        if(!sendMessageQueue.isEmpty())
                        {
                            for (int p = 0; p < sendMessageQueue.size(); p++) {
                                if (sendMessageQueue.get(p).isIsSent())
                                {
                                    if(sendMessageQueue.get(p).isAckRcd())
                                    {
                                        sendMessageQueue.remove(p);
                                    }
                                    else
                                    {
                                        //Send the message again.....
                                        Message msg = sendMessageQueue.get(p);
                                        String m = msg.getMessage();
                                        String receiverNode = msg.getReceiverNode();
                                        byte[] msgByte = m.getBytes();
                                        if (sendData(new DatagramPacket(msgByte,
                                            msgByte.length,
                                            getINETIPAddress(
                                                    neighborList.get(receiverNode)
                                                    .getIPAddress()),
                                            neighborList.get(receiverNode).getPortNo()))) {
                                    
                                    }
                                
                                }
                            }
                        }
                        }
                    } catch (InterruptedException | UnknownHostException ex) {
                        Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    
                }
            }
        };
        aCKNotReceivedThread.start();
    }
    
    public static InetAddress getINETIPAddress(String address) throws UnknownHostException {
        if (address.equals("localhost")) {
            return InetAddress.getLocalHost();
        } else //add how to get the remote ipaddress
        {
            return InetAddress.getByName(address);
        }

    }

    public synchronized static boolean sendData(DatagramPacket c) {
        try {

            server.getDs().send(c);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    public synchronized static int getTimestamp()
    {
        return timestamp;
    }
    private static void createInitialMessages() {
        sendMessageQueue = new ArrayList<>();
        // UNDELIVERABLE:<message_id>,<message><timestamp>,<src>,<dest>
        // Only set UNDELIVERABLE:<message_id><message> as message.
//        undeliverableMessageCounts
        int i = 0;
        while(i < messageList.size()) {
            if(undeliverableMessageCounts.containsKey(messageList.get(i).getMessageId()))
            {
                String msgID = messageList.get(i).getMessageId();
                for(int k = 0 ; k < undeliverableMessageCounts.get(msgID) ; k++)
                {
                    Message m = messageList.get(i);
                    String msg = "UNDELIVERABLE:" + m.getMessageId()+","+m.getMessage();
                    Message temp = new Message(m.getMessageId(),
                                    msg, m.getReceiverNode());            
                    temp.setTimestamp(timestamp);
                    sendMessageQueue.add(temp);
                    i++;
                }        
                timestamp++;
            }
            
        }
    }

    private static void sentMessageLogging(String message) {
        FileOperations.writeFile("sentLog" + processID + ".txt", message);
    }

    /**
     *
     * @author Tejashree
     *
     * Types of Messages UNDELIVERABLE: <message_id> <timestamp> <src> <dest>
     * PROPOSAL: <message_id> <timestamp> <src> <dest>
     * DELIVERABLE : <message_id> <message> <timestamp> <src> <dest>
     * 
     */
    private synchronized static Message getNextMessageInQueue(Message m) {
        switch (m.getMessage().split(":")[0]) {
            case "UNDELIVERABLE": {
                // append ,<timestamp>,<src>,<dest>
                m.setMessage(m.getMessage() + "," + m.getTimestamp() + "," + processID + "," + m.getReceiverNode());
                break;
            }

            case "PROPOSAL": {
                if (timestamp < m.getTimestamp()) {
                    timestamp = m.getTimestamp();
                }
                timestamp++;
                m.setMessage(m.getMessage() + "," + timestamp + "," + processID + "," + m.getReceiverNode());
                m.setTimestamp(timestamp);
                break;
            }

            case "DELIVERABLE": {
                if (timestamp < m.getTimestamp()) {
                    timestamp = m.getTimestamp();
                }
                timestamp++;
                for (int i = 0; i < messageList.size(); i++) {
                    if (messageList.get(i).getMessageId().equals(m.getMessageId())
                            && messageList.get(i).getReceiverNode().equals(m.getReceiverNode())) {
                        String message = messageList.get(i).getMessage();
                        m.setMessage(m.getMessage() + ","
                                + message + "," + m.getTimestamp()
                                + "," + processID + "," + m.getReceiverNode());
                    }

                }

                break;
            }

        }
        return m;
    }

    private static void setUndeliverableMessageCounts() {
        for (int i = 0; i < messageList.size(); i++) {
            if (undeliverableMessageCounts.containsKey(messageList.get(i).getMessageId())) {
                int count = undeliverableMessageCounts.get(messageList.get(i).getMessageId());
                undeliverableMessageCounts.put(messageList.get(i).getMessageId(), count + 1);

            } else {
                undeliverableMessageCounts.put(messageList.get(i).getMessageId(), 1);
            }
        }
    }

    private static void sendUndeliverables() {
        
    
    }
}
