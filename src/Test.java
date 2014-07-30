/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tejashree
 */
import helper.FileOperations;
import java.util.*;
public class Test {
    
    public static void main(String a[])
    {
        ArrayList<String> processNames = FileOperations.getProcessesNames("network.txt");
        String inValidProcessName = null;
        for(int i = 0 ; i < processNames.size() ; i++)
        {
            ArrayList<String> fileLines = 
                    FileOperations.readFile("deliverables"
                            +processNames.get(i)+".txt");
            for(int j = 1 ; j < fileLines.size() ; j++)
            {
                int prevTimestamp = Integer.parseInt(fileLines.get(j-1).split(":")[2]);
                int currentTimestamp = Integer.parseInt(fileLines.get(j).split(":")[2]);
                if(prevTimestamp > currentTimestamp)
                {
                    inValidProcessName = processNames.get(i);
                    break;
                }
            }
        }
        
        if(inValidProcessName != null)
        {
            System.out.println("Process " + inValidProcessName + " failed to maintain the total ordering");
        } 
        else
        {
            System.out.println("Total ordering is maintain.");
        }
    }
    
}
