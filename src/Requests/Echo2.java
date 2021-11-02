package Requests;

import ithakimodem.Modem;

import java.util.ArrayList;

public class Echo2 {
    private final ArrayList<String> packetList;
    private final ArrayList<Long> responseTimeList;
    private final ArrayList <Integer> counters;
    private final String ackCode;
    private final String nackCode;
    public Echo2(String ackCode, String nackCode){
        this.packetList = new ArrayList<>();
        this.responseTimeList = new ArrayList<>();
        this.counters = new ArrayList<>();
        this.ackCode = ackCode;
        this.nackCode = nackCode;
    }

    public ArrayList<Long> getResponseTimeList(){
        return this.responseTimeList;
    }

    /**
     * receives the packets from the server and prints them
     * @param modem Modem object
     * @param counter packets' counter
     */
    public void receiver(Modem modem, int counter){
        StringBuilder message = new StringBuilder();
        int k;

        for(int i = 0; i < counter; i++) {
            int counterOfRequests = 0;
            // starting time
            long initialTime = System.currentTimeMillis();
            if (modem.write(this.ackCode.getBytes())) { //sending request to the server for ACK

                while (true) {
                    try {
                        k = modem.read();

                        if (k == -1) {
                            System.out.println("Connection Timed out");
                            break;
                        }

                        //System.out.print((char) k);

                        message.append((char) k);

                        if (message.toString().endsWith("PSTOP")) {
                            //end time
                            long finalTime;
                            long differTime;

                            //System.out.println("Initial message: " + message.toString());

                            if (isComparisonOk(message.toString())) {
                                //end time
                                finalTime = System.currentTimeMillis();
                                differTime = finalTime - initialTime;

                                this.counters.add(counterOfRequests);

                                this.responseTimeList.add(differTime);
                                this.packetList.add(message.toString() + " " + differTime + " ms");
                                //System.out.println("Success: " + message.toString() + differTime);

                                message.setLength(0);
                                break;
                            } else {
                                while (true) {
                                    //System.out.println("Packet had error. Requesting again...");
                                    counterOfRequests++;
                                    String nackmessage = requestPacket(modem);

                                    if (!nackmessage.isEmpty()) {
                                        //in 0 index we have the message --> see requestPacket function
                                        //System.out.println("NACK Response " + nackmessage);
                                        if (isComparisonOk(nackmessage)) {
                                            finalTime = System.currentTimeMillis();
                                            differTime = finalTime - initialTime;

                                            //in 1 index we have the response time
                                            this.responseTimeList.add(differTime);
                                            this.packetList.add(nackmessage + " " + differTime + " ms");
                                            this.counters.add(counterOfRequests);
                                            //System.out.println("Success: " + nackmessage + differTime);
                                            message.setLength(0);
                                            break;
                                        }
                                    } else {
                                        return; //jump out of the function
                                    }
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                }
            }
        }
        for (long i : this.responseTimeList) {
            System.out.println(i);
        }
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
//        for (long t : this.responseTimeList) {
//            System.out.println(t);
//        }
        for (int i : this.counters) {
            System.out.println(i);
        }
    }

    /**
     * compares the message between the </> characters with the FCS so that we know we have received the right message
     * @param message what is written after we send the request to the server
     * @return true if right message is sent else false
     */

    private boolean isComparisonOk(String message){
        String data = message.substring(31, 47); //abstract the message between the "<>" characters
        //abstract the FCS number which is in "string" type
        int dataFcs = Integer.parseInt(message.substring(49,52)); //typecasting a string to int when it contains only numbers

        //calculating the XOR result from the "< message >"
        int xorResult = 0;
        for(int i = 0; i < data.length(); i++){
            xorResult = data.charAt(i) ^ xorResult;
        }

        //System.out.println("FCS: " + xorResult);        //???

        return xorResult == dataFcs;
    }

    /**
     * requests for sending again the packet when comparisonIsOk returns false
     * @param modem object type Modem
     * @return the new requested message
     */

    private String requestPacket(Modem modem) {
        StringBuilder message = new StringBuilder();
        int k;
        if (modem.write(this.nackCode.getBytes())) { //sending request to the server for NACK(negative acknowledgement)

            while (true) {
                try {
                    k = modem.read();

                    if (k == -1) {
                        System.out.println("Connection Timed out");
                        break;
                    }

                    //System.out.print((char) k);

                    message.append((char) k);

                    if (message.toString().endsWith("PSTOP")) {
                        break;
                    }

                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }

            return message.toString();
        }
        return "";
    }
}
