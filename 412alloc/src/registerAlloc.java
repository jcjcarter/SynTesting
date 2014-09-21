import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


/**
 * @author Ace
 *
 */
public class registerAlloc {
  
  //Mapping to Register's furtherest next use
  private static HashMap<String, String> registerNextU = new HashMap<String, String>();
  
  //Locations starting at 32768 in data memory are reserved for the register allocator. The    
  //allocator will store spilled values into these locations.      
  private static int dataMemoryLoc = 32768;

  // Class variable for virtual and physical mappings of registers, KEY = virtural register VALUE = Physical Register
  private static HashMap<String, String> registerVMapped = new HashMap<String, String>();

  // Class variable that keeps track of the physicals that are still live
  private static Stack<String> registerInUse = new Stack<String>();

  // Key is the register, value is the last lined used for the register
  private static HashMap<String, String> registerLinesUsage = new HashMap<String, String>();

  // Line count for program being read
  private static int programLineCount;

  // The program in vector line form
  private static HashMap<Integer, Vector> allocationActions = new HashMap<Integer, Vector>();

  // Class variable that holds the register names and the live ranges, index 0 is the start and
  // index 1 is the end for the array
  private static HashMap<String, int[]> registerList = new HashMap<String, int[]>();

  // Class variable that holds the register names and the live ranges listed out i.e. 0, .., 100 in
  // an arrayList
  private static HashMap<String, ArrayList<Integer>> registerRanges =
      new HashMap<String, ArrayList<Integer>>();

  // Class variable that keeps track of X input physical registers available in a set for the
  // bottom-up algorithm
  private static Stack<String> registerAvail = new Stack<String>();

  // Class variable that holds the number of live registers for each line
  private static HashMap<Integer, Integer> maxLiveHash = new HashMap<Integer, Integer>();

  public static void main(String[] args) {

    String[] inputLine = {"2", "/Users/Ace/Downloads/HolderJar/block1.i"};

    //Check if the file exists
    File f = new File(inputLine[1]);
    if(!f.exists() || f.isDirectory()) {
      System.out.println("Failure to open '"+inputLine[1]+"' as the input file.");
      System.exit(0);
    }
    
    // Check to see if the parameter -h is present
    if (hFlag(inputLine)) {
      System.exit(0);
    }

    // Look for the number of registers or throw an error if not there
    //String inputRegNumber = args[0];
    if (!generateXRegisters(inputLine[0])) {
      //System.out.println("Will attempt to read from 'stdin'.");
      System.out.println("Cannot allocate with fewer than 2 registers.");
      System.exit(0);
    }

    /**
     * Test to see if X registers are in the set Iterator<String> allRegistersHere =
     * registerAvail.iterator(); while(allRegistersHere.hasNext()){
     * System.out.println(allRegistersHere.next()); }
     */

    // Opens the file, stores the program, and prints program
    readMicrosyntax(openAndRead(inputLine[1]), programLineCount);

    /**
     * Iterate through allocationActions, if the OpCode is Empty then skip the line. If maxLiveHash
     * is greater than the number of registers available then spill a register. check out a virtual
     * register to a physical register, keep track of mappings using a HashMap and pop registers
     * from the registerAvail and placed them into the set where registerUsing. If virtual next used
     * is empty then free the physical and place it back into registerAvail. print out the result of
     * the vector.
     * */
    assignPhyRegAndPrintVector();
    
    
    System.out.println("Finished.");
  }
  
  public static void changeVRegisterMappings(HashMap<String, String> map, String SwapRegister, String MemoryLocation){
    Iterator it = map.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String,String> pairs = (Map.Entry)it.next();
      //If a register currently does not have a next use, return that register.
      if(pairs.getValue() == SwapRegister){
        map.put(pairs.getKey(), MemoryLocation);
        break;
      }
  }
    }
  
  /**
   * @param map: registerNextU which is hash where Key = Physical Register and Value = Next Line number used
   * @return
   */
  public static String iterateRegisterNextU(HashMap<String, String> map){
    //Test to see if all the registers are there.
    //System.out.println("\t Number of Registers here: \t" + map.size());
    int furtherestUse = -100, compareNum = -1000;
    String furtherestReg = "ERROR";
    Iterator it = map.entrySet().iterator();
    //search for a register that has the furtherest next used
    while (it.hasNext()) {
        Map.Entry<String,String> pairs = (Map.Entry)it.next();
        //If a register currently does not have a next use, return that register.
        if(pairs.getValue().contains("Empty") && pairs.getKey().contains("r")){
          //System.out.println(pairs.getKey() + " = " + pairs.getValue());
          return pairs.getKey();
        }
        //Keep track of the furtherest register
        compareNum = Integer.parseInt(pairs.getValue());
        if(compareNum > furtherestUse && pairs.getKey().contains("r")){
          furtherestUse = compareNum;
          furtherestReg = pairs.getKey();
          //System.out.println(pairs.getKey() + " = " + pairs.getValue());
        }

    }
    return furtherestReg;
  }

  /**
   * Function goes through the all the vectors stored in the allocationActions, assigns physical
   * registers, and prints to console.
   */
  public static void assignPhyRegAndPrintVector() {
    String physicalRegister = "", virtualRegister = "";
    int numberOfLines = allocationActions.size();
    for (int i = 0; i < numberOfLines; i++) {
      // If there is no opcode skip the line
      if (allocationActions.get(i).getTheOpcode().contains("Empty")) {
        continue;
      }
      // System.out.println("Line number: "+ i+ "\t" +allocationActions.get(i).getTheOpcode());
      if ((maxLiveHash.get(i) - registerInUse.size()) > registerAvail.size()) {
        /*
         * Need to figure out how to handle the spilling for the
         * registers*****************************
         */
        // System.out.println("Need to spill a register to memory. \t Line: " + i);
        // continue;
        
        //select the PR whose next use is furtherest in the future
        
      }

      if (allocationActions.get(i).getTheOpcode().contains("output")) {
        System.out.println(allocationActions.get(i).getTheOpcode() + "\t "
            + allocationActions.get(i).getVROp1());
        continue;
      }

      // opCode1
      if (allocationActions.get(i).getVROp1().contains("r")) {
        performAllocationOP1(allocationActions.get(i).getVROp1(), i);
      } else {
        //This signifies that the OP1 is doing an operation from memory
        if(allocationActions.get(i).getVROp2().contains("r") && !registerVMapped.get(allocationActions.get(i).getVROp1()).contains("pr")){
          //Pick the register with the furtherest next use
            physicalRegister = iterateRegisterNextU(registerNextU);
            
            //Change the mapping for register Change
            changeVRegisterMappings(registerVMapped, physicalRegister,Integer.toString(dataMemoryLoc));
            
            
            //Spill the contents of the furtherest register to memory
            System.out.println("LoadI \t" + dataMemoryLoc + "\t \t => \t " + physicalRegister + "\t //Spill (k is minimal)");
            dataMemoryLoc += 4;
            
          //Change the mappings for the virtual and physical register
            registerVMapped.put(virtualRegister, physicalRegister);
            allocationActions.get(i).setPROp1(registerVMapped.get(virtualRegister));

          }
        allocationActions.get(i).setPROp1(allocationActions.get(i).getVROp1());
      }

      // opCode2
      if (allocationActions.get(i).getVROp2().contains("r")) {
        performAllocationOP2(allocationActions.get(i).getVROp2(), i);
        // check to see if the virtual register is assigned to physical register
      } else {
        if (allocationActions.get(i).getVROp2().contains("Empty")) {
          allocationActions.get(i).setPROp2("");
        } else {
          allocationActions.get(i).setPROp2(allocationActions.get(i).getVROp2());
        }
      }

      // opCode3
      virtualRegister = allocationActions.get(i).getVROp3();
      if (virtualRegister.contains("r")) {//allocationActions.get(i).getVROp3()
        if(registerVMapped.containsKey(virtualRegister)){
        if(!registerVMapped.get(virtualRegister).contains("r")){
        //Pick the register with the furtherest next use
          physicalRegister = iterateRegisterNextU(registerNextU);
          
          //Change the mapping for register Change
          changeVRegisterMappings(registerVMapped, physicalRegister,Integer.toString(dataMemoryLoc));
          
          
          //Spill the contents of the furtherest register to memory
          System.out.println("LoadI \t" + dataMemoryLoc + "\t \t => \t " + physicalRegister + "\t //Spill (k is minimal)");
          dataMemoryLoc += 4;
          
        //Change the mappings for the virtual and physical register
          registerVMapped.put(virtualRegister, physicalRegister);
          allocationActions.get(i).setPROp3(registerVMapped.get(virtualRegister));

        }
        }
        performAllocationOP3(virtualRegister, i);//allocationActions.get(i).getVROp3()
        // check to see if the virtual register is assigned to physical register
      } else {
      //Pick the register with the furtherest next use
        physicalRegister = iterateRegisterNextU(registerNextU);
        
        //Change the mapping for register Change
        changeVRegisterMappings(registerVMapped, physicalRegister,Integer.toString(dataMemoryLoc));
        
        
        //Spill the contents of the furtherest register to memory
        System.out.println("LoadI \t" + dataMemoryLoc + "\t \t => \t " + physicalRegister + "\t //Spill (k is minimal)");
        dataMemoryLoc += 4;
        
      //Change the mappings for the virtual and physical register
        registerVMapped.put(virtualRegister, physicalRegister);
        allocationActions.get(i).setPROp3(registerVMapped.get(virtualRegister));

      }
      
      //Prints out the lines for the standard output
      System.out.println(allocationActions.get(i).getTheOpcode() + "\t "
          + allocationActions.get(i).getPROp1() + "\t " + allocationActions.get(i).getPROp2()
          + " =>" + "\t" + allocationActions.get(i).getPROp3());
      

      //Test check the values in registerNextU
      //System.out.println("Line number: \t" + i);
      //printMap(registerNextU);
      //System.out.println("This is the furtherest Register: \t"+iterateRegisterNextU(registerNextU));
      //System.out.println("Line number: \t" + i);
    }
  }

  public static void performAllocationOP3(String registerName, int index) {
    String registerChange = "SwitchRegister";
    // System.out.println("Line Number: " + index);
    // check to see if the virtual register is assigned to physical register
    if (registerVMapped.containsKey(allocationActions.get(index).getVROp3())) {

      // write the PR to opcode1 location
      allocationActions.get(index).setPROp3(registerVMapped.get(registerName));
      
      //update the next use for the register in the hashmap
      registerNextU.put(registerVMapped.get(registerName), allocationActions.get(index).getNUOp3());

    } else {
      // if reg does not have a physical, assign one and write the PR to opcode1 location
      if (registerAvail.empty()) {

        // Spill to register
        // System.out.println("spill to register");
        
      //Pick the register with the furtherest next use
        registerChange = iterateRegisterNextU(registerNextU);
        
        //Change the mapping for register Change
        changeVRegisterMappings(registerVMapped, registerChange,Integer.toString(dataMemoryLoc));
        
        
        //Spill the contents of the furtherest register to memory
        System.out.println("LoadI \t" + dataMemoryLoc + " => " + registerChange + "\t //Spill (k is minimal)");
        dataMemoryLoc += 4;
        
      //Change the mappings for the virtual and physical register
        registerVMapped.put(registerName, registerChange);
        allocationActions.get(index).setPROp3(registerVMapped.get(registerName));
      } else {

        String assignRegister = registerAvail.pop();

        registerInUse.push(assignRegister);

        registerVMapped.put(registerName, assignRegister);

        // write the PR to opcode1 location
        allocationActions.get(index).setPROp3(registerVMapped.get(registerName));
        
        //update the next use for the register in the hashmap
        registerNextU.put(registerVMapped.get(registerName), allocationActions.get(index).getNUOp3());

      }

    }

  }


  public static void performAllocationOP2(String registerName, int index) {
String registerChange = "MoveRegister";
    // check to see if the virtual register is assigned to physical register
    if (registerVMapped.containsKey(allocationActions.get(index).getVROp2())) {

      // write the PR to opcode1 location
      allocationActions.get(index).setPROp2(registerVMapped.get(registerName));

      //update the next use for the register in the hashmap
      registerNextU.put(registerVMapped.get(registerName), allocationActions.get(index).getNUOp2());
      
      // check to see if there is a next use
      if (allocationActions.get(index).getNUOp2().contains("Empty")) {
        // remove from the mapping
        String freedRegister = registerVMapped.remove(registerName);
        // List as available
        registerAvail.push(freedRegister);
        // list as no longer in use
        registerInUse.remove(freedRegister);
      }
    } else {
      // if reg does not have a physical, assign one and write the PR to opcode1 location
      // check to see there are available registers
      if (allocationActions.get(index).getVROp2().contains("Empty")) {
        System.out.println("\t\t Nothing here.");
        return;
      }
      if (registerAvail.empty()) {
        // Spill to register
        //System.out.println("Next use check for OP2 Register:\t " + allocationActions.get(index).getNUOp2());
        //System.out.println("loadI \t" + dataMemoryLoc + "\t => \t" + allocationActions.get(index).getVROp2()+"\t //Spilled to memory");
        
      //Pick the register with the furtherest next use
        registerChange = iterateRegisterNextU(registerNextU);
        
        //Change the mapping for register Change
        changeVRegisterMappings(registerVMapped, registerChange,Integer.toString(dataMemoryLoc));
        
        
        //Spill the contents of the furtherest register to memory
        System.out.println("LoadI \t" + dataMemoryLoc + " => " + registerChange + "\t //Spill (k is minimal)");
        dataMemoryLoc += 4;
        
      //Change the mappings for the virtual and physical register
        registerVMapped.put(registerName, registerChange);
        allocationActions.get(index).setPROp1(registerVMapped.get(registerName));
        
      } else {
        String assignRegister = registerAvail.pop();
        registerInUse.push(assignRegister);
        registerVMapped.put(registerName, assignRegister);

        // write the PR to opcode1 location
        allocationActions.get(index).setPROp2(registerVMapped.get(registerName));
        
        //update the next use for the register in the hashmap
        registerNextU.put(registerVMapped.get(registerName), allocationActions.get(index).getNUOp2());
        
        if (allocationActions.get(index).getNUOp2().contains("Empty")) {
          // remove from the mapping
          String freedRegister1 = registerVMapped.remove(registerName);
          // List as available
          registerAvail.push(freedRegister1);
          // list as no longer in use
          registerInUse.remove(freedRegister1);
        }
      }

    }

  }


  /**
   * @param registerName: Virtual Register for Operation 1 is passed in.
   * @param index
   */
  public static void performAllocationOP1(String registerName, int index) {
    String registerChange = "OpenRegister";

    // check to see if the virtual register is assigned to physical register
    if (registerVMapped.containsKey(allocationActions.get(index).getVROp1())) {
      /**
       * Need another if statement to load from memory
       * */
      /**if(allocationActions.get(index).getVROp2().contains("r") && !allocationActions.get(index).getPROp1().contains("r")){
      //Pick the register with the furtherest next use
        registerChange = iterateRegisterNextU(registerNextU);
        
        //Change the mapping for register Change
        changeVRegisterMappings(registerVMapped, registerChange,Integer.toString(dataMemoryLoc));
        
        
        //Spill the contents of the furtherest register to memory
        System.out.println("LoadI \t" + dataMemoryLoc + "\t \t => \t " + registerChange + "\t //Spill (k is minimal)");
        dataMemoryLoc += 4;
        
      //Change the mappings for the virtual and physical register
        registerVMapped.put(registerName, registerChange);
        allocationActions.get(index).setPROp1(registerVMapped.get(registerName));
      } */
      
      // write the PR to opcode1 location
      allocationActions.get(index).setPROp1(registerVMapped.get(registerName));
      
      //update the next use for the register in the hashmap
      registerNextU.put(registerVMapped.get(registerName), allocationActions.get(index).getNUOp1());

      // check to see if there is a next use
      if (allocationActions.get(index).getNUOp1().contains("Empty")) {
        // remove from the mapping
        String freedRegister = registerVMapped.remove(registerName);
        // List as available
        registerAvail.push(freedRegister);
        // list as no longer in use
        registerInUse.remove(freedRegister);
      }
    } else {
      // if reg does not have a physical, assign one and write the PR to opcode1 location
      // check to see there are available registers
      if (registerAvail.empty()) {
        // Spill to register
        //System.out.println("Next use check for OP1 Register:\t " + allocationActions.get(index).getNUOp1());
        
      //Pick the register with the furtherest next use
        registerChange = iterateRegisterNextU(registerNextU);
        
        //Change the mapping for register Change
        changeVRegisterMappings(registerVMapped, registerChange,Integer.toString(dataMemoryLoc));
        
        
        //Spill the contents of the furtherest register to memory
        System.out.println("LoadI \t" + dataMemoryLoc + "\t \t => \t " + registerChange + "\t //Spill (k is minimal)");
        dataMemoryLoc += 4;
        
      //Change the mappings for the virtual and physical register
        registerVMapped.put(registerName, registerChange);
        allocationActions.get(index).setPROp1(registerVMapped.get(registerName));
        
      } else {
        String assignRegister = registerAvail.pop();
        registerInUse.push(assignRegister);
        registerVMapped.put(registerName, assignRegister);

        // write the PR to opcode1 location
        allocationActions.get(index).setPROp1(registerVMapped.get(registerName));
        
        //update the next use for the register in the hashmap
        registerNextU.put(registerVMapped.get(registerName), allocationActions.get(index).getNUOp1());
        
        if (allocationActions.get(index).getNUOp1().contains("Empty")) {
          // remove from the mapping
          String freedRegister1 = registerVMapped.remove(registerName);
          // List as available
          registerAvail.push(freedRegister1);
          // list as no longer in use
          registerInUse.remove(freedRegister1);
        }
      }

    }

  }

  /**
   * Fills the arrayList for generating all the numbers between start and end
   * 
   * @param regName
   * @param startIndex
   * @param endIndex
   */
  public static void fillInLiveRanges(String regName, int startIndex, int endIndex) {
    ArrayList<Integer> linesLiveRange = new ArrayList<Integer>();
    for (int i = startIndex; i <= endIndex; i++) {
      linesLiveRange.add(i);
      // System.out.print(i +" ");
    }
    // System.out.println(regName);
    registerRanges.put(regName, linesLiveRange);
    registerLinesUsage.put(regName, "Empty");
    return;
  }

  /**
   * Parses the command line to find the number of registers to produce and places that number of
   * registers into the class variable Set<String> holding all the registers.
   * 
   * @param filePath
   */
  public static boolean generateXRegisters(String strInt) {
    String regName = "pr";
    int numRegisters;
    numRegisters = Integer.parseInt(strInt);
    if (numRegisters < 2) {
      System.out.println("Cannot allocate with fewer than 2 registers.");
      System.exit(0);
    }
    for (int j = 0; j < numRegisters; j++) {
      regName += Integer.toString(j);
      // add to the set
      registerAvail.add(regName);
      registerLinesUsage.put(regName, "Empty");
      registerNextU.put(regName, "Empty");
      regName = "pr";
    }
    return true;
  }

  /**
   * Methods prints out all the available options for parameters.
   * 
   * @param commandLine
   * @param exitProgram
   * @return
   */
  public static boolean hFlag(String[] commandLine) {
    int arrayLen = commandLine.length;
    for (int i = 0; i < arrayLen; i++) {
      if (commandLine[i] == "-h") {
        System.out.println(" ");
        System.out.println("Command Syntax: " + "\n\t    ./412alloc k filename [-h] [-l]\n\n"
            + "\n Required arguments:" + "\n\t    k     specifies the number of register available"
            + "\n\t filename  is the pathname (absolute or relative) to the input file\n\n"
            + "\n Optional flags:" + "\n\t    -h    prints this message"
            + "\n\t    -l    additive flag; increases detail written to './LogFile'");
        return true;
      }
    }
    return false;
  }

  /**
   * Opens up a text file and prints all the characters in the text.
   * 
   * @param filename
   * @return
   */
  public static HashMap<Integer, String> openAndRead(String filename) {
    // Structure reverses the file and does not hold comments
    Stack<String> lifo = new Stack<String>();

    // line index from bottom to top
    int stackIndex = 0;

    // line index from top to bottom
    int countToBottom = 0;

    // lines popped from stack
    String ilocLine = "";

    // Used to iterator through the registers found in the program going top down
    Set<String> printRegList;

    // storage of the program
    HashMap<Integer, String> dataStruct = new HashMap<Integer, String>();

    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      String line;
      // read in each line from the block
      while ((line = reader.readLine()) != null) {

        // Skip pass the comment section of the file
        if (line.contains("/") && line.charAt(0) == '/') {
          // Check to see if the comments are skipped.
          // System.out.println(line);//When I parse the line for registers, I am going to far.
          continue;
        }

        // Put the line in the correct format
        line = reformatLine(line);

        readTopDown(line, countToBottom);
        countToBottom++;

        // Places the lines onto a stack, to prepare for bottom up reading
        lifo.push(line);

      }
      reader.close();
    } catch (Exception e) {
      System.err.format("Exception occurred trying to read '%s'.", filename);
      e.printStackTrace();
    }

    printRegList = registerList.keySet();
    Iterator<String> pass = printRegList.iterator();
    String keyHere;
    while (pass.hasNext()) {
      keyHere = pass.next();
       //System.out.println(keyHere);
       //System.out.println("First appeared: " + registerList.get(keyHere)[0]);
       //System.out.println("Last appeared: " + registerList.get(keyHere)[1]);
      fillInLiveRanges(keyHere, registerList.get(keyHere)[0], registerList.get(keyHere)[1]);
    }

    // Produce MaxLive for Each from the top to bottom read, countToBottom -1 to account for the
    // extra increment
    maxLiveLines(countToBottom - 1);

    while (!lifo.isEmpty()) {
      ilocLine = (String) lifo.pop();

      // Fills the data structure with the ILOC Program
      dataStruct.put(stackIndex, ilocLine);

      // System.out.println(stackIndex);
      programLineCount = stackIndex;
      // increment counter
      stackIndex++;
    }
    // System.out.println(programLineCount);
    return dataStruct;
  }


  /**
   * Takes in the line from the block makes the format into this:
   * 
   * Add r2,r3 => r4
   * 
   * @param lineIn
   * @return
   */
  public static String reformatLine(String lineIn) {

    String lineFormatted = "", buildToken = "";
    int index = 0, strLen = lineIn.length(), commaIndex, slashIndex;
    // System.out.println("Format this: \t" + lineIn + "\t Length: " + strLen);
    if (strLen == 0) {
      return lineFormatted;
    }

    // just return line if output
    if (lineIn.contains("output")) {
      return lineIn.trim();
    }

    // append the operation code
    while (lineIn.charAt(index) < strLen) {
      if (Character.isWhitespace(lineIn.charAt(index))) {
        break;
      }
      lineFormatted += lineIn.charAt(index);
      index++;
    }

    // keep moving to skip the white space
    while (Character.isWhitespace(lineIn.charAt(index))) {
      index++;
    }

    // get the first and second operation
    lineFormatted += " ";
    if (lineIn.contains(",")) {
      commaIndex = lineIn.indexOf(',');
      while (index <= commaIndex) {
        lineFormatted += lineIn.charAt(index);
        index++;
      }
      // go to the second register
      while (lineIn.charAt(index) != 'r') {
        index++;
      }
      // gets the second register
      while (!Character.isWhitespace(lineIn.charAt(index))) {
        lineFormatted += lineIn.charAt(index);
        index++;
      }
      lineFormatted += " ";
      lineFormatted += lineIn.charAt((lineIn.indexOf('=')));
      lineFormatted += lineIn.charAt((lineIn.indexOf('>')));
      lineFormatted += " ";
      while (lineIn.charAt(index) != 'r') {
        index++;
      }
      lineFormatted += lineIn.charAt(index);
      index++;
      while (Character.isDigit(lineIn.charAt(index))) {
        lineFormatted += lineIn.charAt(index);
        if (index + 1 < strLen) {
          index++;
        } else {
          break;
        }
      }
      return lineFormatted;
    }

    // for the other operations like LoadI

    if (lineIn.contains("/")) {
      slashIndex = lineIn.indexOf("/");
      while (index < slashIndex) {
        if (lineIn.charAt(index) == '=') {
          lineFormatted += '=';
          index++;
          continue;
        }
        if (lineIn.charAt(index) == '>') {
          lineFormatted += "> ";
          index++;
        }
        if (!Character.isWhitespace(lineIn.charAt(index))) {
          buildToken += lineIn.charAt(index);
        } else {
          lineFormatted += buildToken + " ";
          buildToken = "";
        }
        index++;
      }
      return lineFormatted;
    }

    if (lineIn.contains("=")) {
      slashIndex = lineIn.indexOf("=");
      while (index <= slashIndex + 1) {
        if (lineIn.charAt(index) == '=') {
          lineFormatted += '=';
          index++;
          continue;
        }
        if (lineIn.charAt(index) == '>') {
          lineFormatted += "> ";
          index++;
        }
        if (!Character.isWhitespace(lineIn.charAt(index))) {
          buildToken += lineIn.charAt(index);
        } else {
          lineFormatted += buildToken + " ";
          buildToken = "";
        }
        index++;
      }
      index--;
      while (index < strLen) {
        if (lineIn.charAt(index) == 'r') {
          lineFormatted += 'r';
          index++;
          while (Character.isDigit(lineIn.charAt(index))) {
            lineFormatted += lineIn.charAt(index);
            if (index + 1 < strLen) {
              index++;
            } else {
              break;
            }
          }
          break;
        }
        index++;
      }
      return lineFormatted;
    }

    return lineFormatted;
  }

  /**
   * The function MaxLive updates a class variable HashMap where the key is the line and value is
   * number of live registers.
   * 
   * @param programLength
   */
  public static void maxLiveLines(int programLength) {
    int liveRegistersCounted = 0;

    for (int i = programLength; i >= 0; i--) {
      Set<String> listedRegistersSet = registerList.keySet();
      Iterator<String> listedRegister = listedRegistersSet.iterator();
      while (listedRegister.hasNext()) {
        if (registerRanges.get(listedRegister.next()).contains(i)) {
          // System.out.println(i);
          liveRegistersCounted++;
        }
      }
      // System.out.println("Line Number: "+ i);
      // System.out.println("\t Number of live Registers: " + liveRegistersCounted);
      maxLiveHash.put(i, liveRegistersCounted);
      liveRegistersCounted = 0;
    }
    return;
  }

  /**
   * Will write to HashMap to demonstrate the live range.
   * 
   * @param textLine
   */
  public static void readTopDown(String textLine, int lineIndex) {
    //System.out.println("Reading this Line: \t" + textLine);
    String buildToken = "";
    int strLen = textLine.length();
    // Holds the start
    for (int i = 0; i < strLen; i++) {
      // step through the tokens as they build
      // System.out.println(textLine.charAt(i));
      if (Character.isWhitespace(textLine.charAt(i)) != true) {
        /*
         * if (textLine.charAt(i) == '=') { if (textLine.charAt(i + 1) == '>') {
         * System.out.println(textLine.charAt(i + 2)); break; } }
         */
        buildToken += textLine.charAt(i);
        // System.out.println(textLine.charAt(i));
        if(i+1 == strLen){
          liveRanges(buildToken,lineIndex);
        }
        continue;
        
      }
      // display the token built
      // System.out.println(buildToken);

      // Live ranges for all the registers in the program
      liveRanges(buildToken, lineIndex);

      buildToken = "";
    }
    return;
  }

  /**
   * Finds the live ranges for the registers in the ILOC program.
   * 
   * @param tokenWord
   * @param lineIndex
   */
  public static void liveRanges(String tokenWord, int lineIndex) {
    int[] regIndices = new int[2];
    // index of the comma in a operation
    int commaIndex;
    String firstReg = "", secondReg = "";
    if (tokenWord != "") {

      //System.out.println("Token passed in: " + tokenWord);
      if (tokenWord.length() >= 2) {
        if (tokenWord.contains(",")) {
          commaIndex = tokenWord.indexOf(",");
          // parse the string to find the first register
          for (int i = 0; i < commaIndex; i++) {
            firstReg += tokenWord.charAt(i);
          }
          // parse the string to find the second register
          for (int i = commaIndex + 1; i < tokenWord.length(); i++) {
            secondReg += tokenWord.charAt(i);
          }
          /*
           * Takes care of the first register in updating the live range
           */
          if (firstReg.charAt(0) == 'r' && testForRegNum(firstReg.charAt(1))) {

            if (!registerList.containsKey(firstReg)) {
              regIndices = new int[2];
              // the first occurrance of the register in the program
              regIndices[0] = lineIndex;
              // the last occurrance of the register in the program
              regIndices[1] = lineIndex;
              registerList.put(firstReg, regIndices);
              System.out.println("First found: "+ tokenWord + " at Line: " + lineIndex);
            } else {
              regIndices = new int[2];
              // the first occurrance of the register in the program
              regIndices[0] = registerList.get(firstReg)[0];
              // the last occurrance of the register in the program
              regIndices[1] = lineIndex;
              // update the indices list
              registerList.put(firstReg, regIndices);
              // System.out.println("Updated: "+ buildToken + " at Line: " + lineIndex);
            }
          }

          // Make sure we do not go out of bounds
          if (secondReg.length() > 1) {
            /*
             * Takes care of the second register in updating the live range
             */
            if (secondReg.charAt(0) == 'r' && testForRegNum(secondReg.charAt(1))) {

              if (!registerList.containsKey(secondReg)) {
                regIndices = new int[2];
                // the first occurrance of the register in the program
                regIndices[0] = lineIndex;
                // the last occurrance of the register in the program
                regIndices[1] = lineIndex;
                registerList.put(secondReg, regIndices);
                 //System.out.println("First found: "+ tokenWord + " at Line: " + lineIndex);
                return;
              } else {
                regIndices = new int[2];
                // the first occurrance of the register in the program
                regIndices[0] = registerList.get(secondReg)[0];
                // the last occurrance of the register in the program
                regIndices[1] = lineIndex;
                // update the indices list
                registerList.put(secondReg, regIndices);
                // System.out.println("Updated: "+ buildToken + " at Line: " + lineIndex);
                return;
              }
            }// end of of rXX if-statement
          }
        }// end of if-statement testing for a comma
      }
    }

    if (tokenWord != "") {
      if (tokenWord.length() >= 2) {
        if (tokenWord.charAt(0) == 'r' && testForRegNum(tokenWord.charAt(1))) {
          if (!registerList.containsKey(tokenWord)) {
            regIndices = new int[2];
            // the first occurrance of the register in the program
            regIndices[0] = lineIndex;
            // the last occurrance of the register in the program
            regIndices[1] = lineIndex;
            registerList.put(tokenWord, regIndices);
            //System.out.println("First found: "+ tokenWord + " at Line: " + lineIndex);
            return;
          } else {
            regIndices = new int[2];
            // the first occurrance of the register in the program
            regIndices[0] = registerList.get(tokenWord)[0];
            // the last occurrance of the register in the program
            regIndices[1] = lineIndex;
            // update the indices list
            registerList.put(tokenWord, regIndices);
            // System.out.println("Updated: "+ buildToken + " at Line: " + lineIndex);
            return;
          }
        }
      }
    }
    return;
  }

  /**
   * Determines if the token word is a register number
   * 
   * @param number
   * @return
   */
  public static boolean testForRegNum(char number) {
    char[] alphaNumbers = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    for (int i = 0; i < alphaNumbers.length; i++) {
      if (number == alphaNumbers[i]) {
        return true;
      }
    }
    return false;
  }

  /**
   * Appends characters to the class variable tokenWord.
   * 
   * @param alphabet
   */
  public static void readMicrosyntax(HashMap<Integer, String> storedData, int numberOfLinesCounted) {// numberOfLinesCounted
                                                                                                     // is
                                                                                                     // used
                                                                                                     // as
                                                                                                     // the
                                                                                                     // Key
                                                                                                     // for
                                                                                                     // allocationActions
                                                                                                     // as
                                                                                                     // lines,
                                                                                                     // must
                                                                                                     // be
                                                                                                     // decremented
    String buildToken = "", textLine = "", previousWord = "";
    Vector lineVectorOP;

    // System.out.println("In the readMirco function: " + numberOfLinesCounted);
    for (int j = 0; j < storedData.size(); j++) {
      // Vector class that holds the opCode, Op1, Op2, and Op3
      lineVectorOP = new Vector();
      textLine = storedData.get(j);
      // Prints out all the lines in the stack from the bottom up
      // System.out.println(textLine);
      for (int i = 0; i < textLine.length(); i++) {

        if (testForCharacters(textLine.charAt(i)) && i + 1 < textLine.length()
            && !Character.isWhitespace(textLine.charAt(i + 1))) {
          buildToken += textLine.charAt(i);
          continue;
        } else {
          buildToken += textLine.charAt(i);
        }
        if (buildToken != "" && !buildToken.contains("\n") && !buildToken.contains("\t")
            && !buildToken.contains(" ")) {
          // Prints out all the tokens in the line
          // System.out.println(buildToken);

          /**
           * Now I need to write functions that will taken in buildToken as a String parameter and
           * return booleans if that string fits the description. If the boolean is true then I need
           * fill in the vector for the corresponding operation. If there is a comma present, write
           * a method for that. Completed.
           **/
          previousWord =
              sortMicrosyntax(buildToken, previousWord, lineVectorOP, numberOfLinesCounted);

        }
        // previousWord = "";
        // previousWord.concat(buildToken);// += buildToken;
        buildToken = "";
      }
      // System.out.println(lineVectorOP.getTheOpcode()+"\t"+
      // lineVectorOP.getVROp1()+"\t Next Used On Line: \t"+lineVectorOP.getNUOp1());
      allocationActions.put(numberOfLinesCounted, lineVectorOP);
      // numberOfLinesCounted is used as the Key for allocationActions as lines, must be decremented
      numberOfLinesCounted--;
      // System.out.println("\tEnd of the line...");
    }
    return;

  }

  /**
   * Retrieves the second operation from the string.
   * 
   * @param second
   * @return
   */
  public static String secondOperation(String second) {
    String value = "";
    int index = second.indexOf(',') + 1;
    while (index < second.length()) {
      value += second.charAt(index);
      index++;
    }
    return value;
  }

  /**
   * Retrieves the first operation from the string.
   * 
   * @param first
   * @return
   */
  public static String firstOperation(String first) {
    String value = "";
    int index = 0;
    while (first.charAt(index) != ',') {
      value += first.charAt(index);
      index++;
    }
    return value;
  }

  /**
   * Checks to see if the character corresponds to a microsyntax.
   * 
   * @param Character
   * @return
   */
  public static boolean testForCharacters(char Character) {
    char[] alphaChars =
        {'s', 't', 'o', 'r', 'e', 'l', 'a', 'd', 'I', ',', 'h', 'i', 'f', 'u', 'b', 'p', 'n', 'm',
            '=', '>', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    for (int i = 0; i < alphaChars.length; i++) {
      if (alphaChars[i] == Character) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks to if the input is a memory location.
   * 
   * @param constant
   * @return
   */
  public static boolean testForConstants(String constant) {
    int stringLeng = constant.length();
    for (int i = 0; i < stringLeng; i++) {
      if (!Character.isDigit(constant.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Finds the microsyntax for the operation code.
   * 
   * @param alphabet
   */
  public static boolean OpcodeSyntax(String OpcodeSyn) {
    String[] OperationCodes =
        {"load", "loadI", "store", "lshift", "sub", "output", "nop", "add", "mult", "rshift"};
    int arrayLength = OperationCodes.length;
    for (int i = 0; i < arrayLength; i++) {
      if (OpcodeSyn.contains(OperationCodes[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Prints out the registers and the lines that they
   * were lasted used.
   * 
   * @param mp
   */
  public static void printMap(HashMap<String,String> mp) {
    Iterator it = mp.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry)it.next();
        System.out.println(pairs.getKey() + " = " + pairs.getValue());
    }
}
  
  /**
   * Sorts the microsyntax into the vector.
   * 
   * @param alphabet
   */
  public static String sortMicrosyntax(String buildToken, String previousWord, Vector lineVectorOP,
      int lineNumber) {
    if (OpcodeSyntax(buildToken)) {
      lineVectorOP.setTheOpcode(buildToken);
      // System.out.println(buildToken);
      return buildToken;
    }
    if (testForConstants(buildToken)) {
      lineVectorOP.setVROp1(buildToken);
      return buildToken;
    }
    if (buildToken.contains(",")) {
      /**
       * Need to mark where I have seen the registers in the code.
       * */
      lineVectorOP.setVROp1(firstOperation(buildToken));
      // get the line used
      lineVectorOP.setNUOp1(registerLinesUsage.get(firstOperation(buildToken)));
      // set the line used
      registerLinesUsage.put(firstOperation(buildToken), Integer.toString(lineNumber));
      lineVectorOP.setVROp2(secondOperation(buildToken));
      // get the line used
      lineVectorOP.setNUOp2(registerLinesUsage.get(secondOperation(buildToken)));
      // set the line used
      registerLinesUsage.put(secondOperation(buildToken), Integer.toString(lineNumber));
      return buildToken;
    }
    if (buildToken.charAt(0) == 'r') {
      // System.out.println(buildToken);
      // System.out.println(previousWord);
      if (previousWord.contains("=>")) {
        lineVectorOP.setVROp3(buildToken);
        // get the line used
        lineVectorOP.setNUOp3(registerLinesUsage.get(buildToken));
        // set the line used
        registerLinesUsage.put(buildToken, Integer.toString(lineNumber));
      } else {
        lineVectorOP.setVROp1(buildToken);
        // get the line used
        lineVectorOP.setNUOp1(registerLinesUsage.get(buildToken));
        // set the line used
        registerLinesUsage.put(buildToken, Integer.toString(lineNumber));
      }
      return buildToken;
    }
    return buildToken;
  }

}
