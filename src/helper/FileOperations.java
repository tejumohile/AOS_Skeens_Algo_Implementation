package helper;

import java.io.*;
import java.util.*;

/**
 *
 * @author Tejashree
 */
public class FileOperations {

    public static HashMap<String, Neighbor> readNeighbors(String fileName, String currentNodeID) {
        File file = new File(getCurrentDir() + fileName);
        HashMap<String, Neighbor> neighborList;
        neighborList = new HashMap<>();
        try {
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream(file);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            strLine = null;

            while ((strLine = br.readLine()) != null) {
                String str[] = strLine.split(",");
                if (!str[0].equals(currentNodeID)) {
                    neighborList.put(str[0], new Neighbor(str[0], str[1], Integer.parseInt(str[2])));
                }
            }
            in.close();

        } catch (Exception e) {//Catch exception if any
            e.printStackTrace();

        }
        return neighborList;
    }

    public static ArrayList<Message> readMessages(String messageFileName,
            String messageListFileName, String currentNodeID) {
        File messageFile = new File(getCurrentDir() + messageFileName);
        File messageListFile = new File(getCurrentDir() + messageListFileName);
        ArrayList<Message> messageListMap = new ArrayList<>();
        HashMap<String, String> messageMap = new HashMap<>();
        try {
            // Open the file that is the first 
            // command line parameter
            FileInputStream messageFileStream = new FileInputStream(messageFile);
            FileInputStream messageListFileStream = new FileInputStream(messageListFile);

            // Get the object of DataInputStream
            DataInputStream messageFileIn = new DataInputStream(messageFileStream);
            DataInputStream messageListFileIn = new DataInputStream(messageListFileStream);
            BufferedReader messageFileBr = new BufferedReader(new InputStreamReader(messageFileIn));
            BufferedReader messageListFileBr = new BufferedReader(new InputStreamReader(messageListFileIn));

            String strLine;
            strLine = null;
            //Reading message File
            while ((strLine = messageFileBr.readLine()) != null) {
                String str[] = strLine.split(",");
                messageMap.put(str[0], str[1]);

            }
            messageFileIn.close();

            //Reading messenger List
            while ((strLine = messageListFileBr.readLine()) != null) {
                String str[] = strLine.split(":");
                if (str[0].equals(currentNodeID)) {
                    String[] messages = str[1].split(",");
                    String[] messageReceivers = str[2].split(",");
                    for (int i = 0; i < messages.length; i++) {

                        for (int j = 0; j < messageReceivers.length; j++) {
                            messageListMap.add(new Message(messages[i],
                                    messageMap.get(messages[i]), messageReceivers[j]));
                        }
                    }
                }
            }
            messageListFileIn.close();

        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());

        }
        return messageListMap;
    }

    /*
     * This method is for general writing.
     */
    public static boolean writeFile(String fileName, String message) {
        File file = new File(getCurrentDir() + fileName);

        try {
            // Create file 
            FileWriter fstream = new FileWriter(file, true);
            try (BufferedWriter out = new BufferedWriter(fstream)) {
                out.write(message);
                out.newLine();
            }
            return true;
        } catch (IOException e) {
            //Catch exception if any				  
            return false;
        }

    }

    public static String getCurrentDir() {
        return "E:\\AOS\\Project 1\\tejumohile-aos-skeens-algorithm-implementation-d55e64e489cd\\SkeensAlgorithm\\src\\";
    }

    public static void main(String arg[]) {
        System.out.println(readNeighbors("network.txt", "1"));
        System.out.println(readMessages("messages.txt", "messengerList.txt", "1"));

    }

    public static String[] getOwnInformation(String currentNodeID, String fileName) {
        File file = new File(getCurrentDir() + fileName);
        String str[] = null;
        try {
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream(file);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            strLine = null;

            while ((strLine = br.readLine()) != null) {
                str = strLine.split(",");
                if (str[0].equals(currentNodeID)) {
                    break;
                }
            }
            in.close();

        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());

        }
        return str;
    }

    public static ArrayList<String> getProcessesNames(String fileName) {
        File file = new File(getCurrentDir() + fileName);
        String str[] = null;
        ArrayList <String> processNames = new ArrayList<>();
        try {
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream(file);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            strLine = null;

            while ((strLine = br.readLine()) != null) {
                str = strLine.split(",");
                processNames.add(str[0]);
            }
            in.close();

        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());

        }
        return processNames;
    }
    
    public static ArrayList<String> readFile(String fileName) {
        File file = new File(getCurrentDir() + fileName);
        String str[] = null;
        ArrayList <String> lines = new ArrayList<>();
        try {
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream(file);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            strLine = null;

            while ((strLine = br.readLine()) != null) {
                
                lines.add(strLine);
            }
            in.close();

        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());

        }
        return lines;
    }
    
    
}
