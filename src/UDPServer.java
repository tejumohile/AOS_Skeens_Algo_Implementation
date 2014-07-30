
import helper.Message;
import helper.Neighbor;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import helper.FileOperations;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tejashree
 */
public class UDPServer implements Runnable {

    private DatagramSocket ds;
    private DatagramPacket dp;
    private int port;
    private String processID;
    private byte[] b;
    private HashMap<String, Neighbor> neighborList;
    private ArrayList<Message> messageList;
    private ArrayList<Message> sendMessageQueue;
    private ArrayList<String> receiveMessageQueue;
    private HashMap<String, Integer> undeliverableMessageCounts;
    private HashMap<String, Integer> proposalToBeSentCount;
    private HashMap<String, Integer> maxProposalTimeStamps;
    private PriorityQueue<Message> undeliveredMessagePQ;
    private int deliverableReceivedCount = 0;
    public DatagramSocket getDs() {
        return ds;
    }

    public void setDs(DatagramSocket ds) {
        this.ds = ds;
    }

    public DatagramPacket getDp() {
        return dp;
    }

    public void setDp(DatagramPacket dp) {
        this.dp = dp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public byte[] getB() {
        return b;
    }

    public void setB(byte[] b) {
        this.b = b;
    }

    public UDPServer(String port, String processID, HashMap<String, Neighbor> neighborList,
            ArrayList<Message> messageList, ArrayList<Message> sendMessageQueue,
            ArrayList<String> receiveMessageQueue,
            HashMap<String, Integer> undeliverableMessageCounts) {
        this.port = Integer.parseInt(port);
        this.processID = processID;
        this.neighborList = neighborList;
        this.messageList = messageList;
        this.sendMessageQueue = sendMessageQueue;
        this.receiveMessageQueue = receiveMessageQueue;
        this.undeliverableMessageCounts = undeliverableMessageCounts;
        this.proposalToBeSentCount = new HashMap<>();
        this.maxProposalTimeStamps = new HashMap<>();
        initializeUndeliveredMessagePQ();
        b = new byte[1024];
        try {
            ds = new DatagramSocket(this.port);
            dp = new DatagramPacket(b, 1024);
        } catch (SocketException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void run() {

        while (true) {
            getDataGramPacket();
            filterMessage();
            if(deliverableReceivedCount==undeliveredMessagePQ.size())
            {
                pollDeliverable();
            }
        }

    }

    private synchronized void getDataGramPacket() {
        try {
            ds.receive(dp);
        } catch (IOException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private synchronized void filterMessage() {
        String message = new String(dp.getData(), 0, dp.getLength());
        boolean newMessage = true;
        if (!message.split(":")[0].equals("ACK")) {
//            FileOperations.writeFile("receivedLog" + processID + ".txt", message);
            //SEND ACK
            sendACK(message);
            //sent message as duplicate message....arrived
            if (!receiveMessageQueue.isEmpty()&& receiveMessageQueue.contains(message)) {
                newMessage = false;
            } else {
                receiveMessageQueue.add(message);
            }
        }
        //check what type of message
        /*
         * 
         * Types of Messages
         * UNDELIVERABLE:<message_id>,<message>,<timestamp>,<src>,<dest>
         * PROPOSAL:<message_id>,<timestamp>,<src>,<dest>
         * DELIVERABLE:<message_id>,<message>,<timestamp>,<src>,<dest>
         */
        if (newMessage) {
            switch (message.split(":")[0]) {
                case "UNDELIVERABLE": {

                    String nextPart = message.split(":")[1];
                    // put in the send queue to be sent again.
                    Message proposal = new Message(nextPart.split(",")[0],
                            "PROPOSAL:" + nextPart.split(",")[0],
                            nextPart.split(",")[3]);
                    proposal.setTimestamp(Integer.parseInt(nextPart.split(",")[2]));
                    sendMessageQueue.add(proposal);
                    //Also add it in the PQ
                    //Message only contains the main message String
                    Message undeliveredMsg = new Message(nextPart.split(",")[0],
                            "UNDELIVERABLE:"+nextPart.split(",")[1],nextPart.split(",")[4]);
                    undeliveredMsg.setTimestamp(Integer.parseInt(nextPart.split(",")[2]));
                    undeliveredMessagePQ.add(undeliveredMsg);
                    
                    break;
                }

                case "PROPOSAL": {

                    if (isProposalsComplete(message)) {
                        String nextPart = message.split(":")[1];
                        String msgID = nextPart.split(",")[0];
                        int maxTimestamp = maxProposalTimeStamps.get(msgID);
                        // UNDELIVERABLE:<message_id
                        for (int proposal = 0; proposal < receiveMessageQueue.size(); proposal++) {
                            if (receiveMessageQueue.get(proposal).contains("PROPOSAL") 
                                    && (receiveMessageQueue.get(proposal)
                                            .split(":")[1].split(",")[0])
                                            .equals(msgID)) {
                                Message deliverable = new Message(msgID,
                                        "DELIVERABLE:" + msgID,
                                        receiveMessageQueue.get(proposal)
                                            .split(":")[1].split(",")[2]);
                                deliverable.setTimestamp(maxTimestamp);
//                    System.out.println(deliverable.toString());
                                sendMessageQueue.add(deliverable);
                            }
                        }
                    }
                    break;
                }

                case "DELIVERABLE": {
                    // Change the timestamp in the underliveredPQ and remove it
                    // and write in to the file.
                    // Updating the undelivered PQ                    
                    deliverableReceivedCount++;
                    String nextPart = message.split(":")[1];
                    String msgID = nextPart.split(",")[0];
                    int deliverableTS = Integer.parseInt(nextPart.split(",")[2]);
                    Iterator<Message> iter = undeliveredMessagePQ.iterator();
                    while(iter.hasNext())
                    {
                        Message undeliveredMessage = iter.next();
                        if(undeliveredMessage.getMessageId().equals(msgID)
                                && undeliveredMessage.getMessage().contains("UNDELIVERABLE:"))
                        {
                            undeliveredMessage.setTimestamp(deliverableTS);
                            //Marking the message as DELIVERABLE Message
                            undeliveredMessage.setMessage(
                                    undeliveredMessage.getMessage()
                                            .replaceFirst(
                                                    "UNDELIVERABLE:", 
                                                    "DELIVERABLE:"));
                            break;
                        }
                    }
//                    FileOperations.writeFile("deliverables" + processID + ".txt","PQ----->"+ undeliveredMessagePQ.toString());
                    break;
                }
                case "ACK": {
                    String ackmessage = message.split("ACK:")[1];
                    for (int i = 0; i < sendMessageQueue.size(); i++) {
                        if (sendMessageQueue.get(i).getMessage().equals(ackmessage)
                                && sendMessageQueue.get(i).isIsSent()) {
                            //mark this message as ack rcd
                            sendMessageQueue.get(i).setAckRcd(true);
                        }
                    }
                    break;
                }

            }
        }
    }

    private boolean isProposalsComplete(String message) {
        String messageID = (message.split(":")[1]).split(",")[0];
        if (proposalToBeSentCount.containsKey(messageID)) {
            proposalToBeSentCount.put(messageID,
                    proposalToBeSentCount.get(messageID) + 1);

        } else {
            proposalToBeSentCount.put(messageID,
                    1);
        }
        if (maxProposalTimeStamps.containsKey(messageID)) {
            int timestamp = Integer.parseInt(message.split(":")[1].split(",")[1]);
            if (timestamp < maxProposalTimeStamps.get(messageID)) {
                timestamp = maxProposalTimeStamps.get(messageID);
            }
            maxProposalTimeStamps.put(messageID, timestamp);
        } else {
            maxProposalTimeStamps.put(messageID, Integer.parseInt(message.split(":")[1].split(",")[1]));
        }
//        System.out.println("unerliverable message count" + undeliverableMessageCounts.toString());
//        System.out.println("proposalToBeSentCount-" + proposalToBeSentCount.toString());
//        System.out.println("maxProposalTimeStamps-" + maxProposalTimeStamps.toString());

        if (proposalToBeSentCount.get(messageID) == undeliverableMessageCounts.get(messageID)) {
            return true;
        } else {
            return false;
        }

    }

    private void sendACK(String message) {
        //to send ACK FOR routing updates received to the sender router
        String ack = "ACK:" + message;
        String[] messageSplitByComma = message.split(",");
        String dest = (messageSplitByComma[messageSplitByComma.length - 2]);
        int port = neighborList.get(dest).getPortNo();
        try {
            for (int p = 0; p < 5; p++) {
                if (Process.sendData(new DatagramPacket(ack.getBytes(),
                        ack.getBytes().length,
                        Process.getINETIPAddress(neighborList.get(dest).getIPAddress()), port))) {
//                  System.out.println("ACK for process update sent");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();;
        }
    }

    private void initializeUndeliveredMessagePQ() {
        undeliveredMessagePQ = new PriorityQueue<>(5,new UndeliveredComparator());   
    
    }

    private void pollDeliverable() {
        //Removing the deliverable messages by checking the timestamp
        while (!undeliveredMessagePQ.isEmpty()
                && !undeliveredMessagePQ.peek()
                .getMessage().contains("UNDELIVERABLE:")
                && undeliveredMessagePQ.peek()
                .getTimestamp() <= Process.getTimestamp()) {
            Message polledMsg = undeliveredMessagePQ.poll();
            FileOperations.writeFile("deliverables"
                    + processID + ".txt",
                    polledMsg.getMessageId()
                    + ":" + polledMsg.getMessage()
                    .replaceFirst("DELIVERABLE:", "")
                    + ":" + polledMsg.getTimestamp());

        }
    
    }
    
    private class UndeliveredComparator implements Comparator<Message>{

        @Override
        public int compare(Message o1, Message o2) {
            
             //select one with lesser timestamp to have higher priority
            if (o1.getTimestamp() > o2.getTimestamp()) {
                return 1;
            } else if (o1.getTimestamp() < o2.getTimestamp())  {
                return -1;
            } 
            else {
                //compare the message id and one with lower id has higher priority
                int id1 = Integer.parseInt(o1.getMessageId().split("m")[1]);
                int id2 = Integer.parseInt(o2.getMessageId().split("m")[1]);
                if (id1 > id2) {
                    return 1;
                } else {
                    return -1;
                }
            }
        
        }
    
    }

}
