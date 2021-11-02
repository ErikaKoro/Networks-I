package Requests;

import ithakimodem.Modem;

import java.util.ArrayList;

public class Echo {
    private final ArrayList<String> packetList;
    private final ArrayList<Long> responseTimeList;
    private final String echoCode;
    public Echo(String echoCode){
        this.echoCode = echoCode;
        this.packetList = new ArrayList<>();
        this.responseTimeList = new ArrayList<>();
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
            // starting time
            long initialTime = System.currentTimeMillis();
            if (modem.write(this.echoCode.getBytes())) { //sending request to the server

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
                            long finalTime = System.currentTimeMillis();
                            long differTime = finalTime - initialTime;
                            this.responseTimeList.add(differTime);
                            this.packetList.add(message.toString() + " " + differTime + " ms");
//                            System.out.println(message);
//                            System.out.println("Message is over");
                            message.setLength(0);
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                }
            }
        }
//        for (String i : this.packetList) {
//            System.out.println(i);
//        }
        for (long t : this.responseTimeList) {
           System.out.println(t);
        }
    }
}
