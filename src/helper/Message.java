/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package helper;

/**
 *
 * @author Tejashree
 * 
 * Types of Messages
 * UNDELIVERABLE: <message_id> <timestamp> <src> <dest>
 * PROPOSAL: <message_id> <timestamp> <src> <dest>
 * DELIVERABLE : <message_id> <message> <timestamp> <src> <dest>
 */
public class Message {
    String messageId;
    String message;
    String receiverNode;
    boolean isSent = false;
    boolean ackRcd = false;
    int timestamp;

    public boolean isAckRcd() {
        return ackRcd;
    }

    public void setAckRcd(boolean ackRcd) {
        this.ackRcd = ackRcd;
    }
        
    public Message(String messageId, String message, String receiverNode) {
        this.messageId = messageId;
        this.message = message;
        this.receiverNode = receiverNode;
        timestamp = 0;
    }

    public boolean isIsSent() {
        return isSent;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public void setIsSent(boolean isSent) {
        this.isSent = isSent;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiverNode() {
        return receiverNode;
    }

    public void setReceiverNode(String receiverNode) {
        this.receiverNode = receiverNode;
    }

    @Override
    public String toString() {
        return "Message{" + "messageId=" + messageId + ", message=" + message + ", receiverNode=" + receiverNode + ", isSent=" + isSent + ", ackRcd=" + ackRcd + ", timestamp=" + timestamp + '}';
    }       
}
