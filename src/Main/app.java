package Main;
import Requests.Echo;
import Requests.Echo2;
import Requests.GPS;
import Requests.Image;
import ithakimodem.Modem;


public class app {
    /**
     * echoCode = args[0]
     * imageCode = args[1]
     * imageErrorCode = args[2]
     * GPSCode = args[3]
     * ackCode = args[4]
     * nackCode = args[5]
     */

    public static void main(String[] args) {
        Modem modem = new Modem(76000);
        modem.setTimeout(10000);
        if(args.length != 6){
            System.out.println("Wrong arguments...");
            return;
        }
        String echoCode = args[0] + "\r";
        String imageCode = args[1] + "\r";
        String imageErrorCode = args[2] + "\r";
        String GPScode = args[3] + "R=1030099" + "\r";
        String ackCode = args[4] + "\r";
        String nackCode = args[5] + "\r";




        modem.open("ITHAKI"); //data connection

        int k;

        StringBuilder message = new StringBuilder();

        while(true) {
            try{
                k = modem.read();

                if (k == -1){
                    System.out.println("Connection Timed out");
                    break;
                }

                System.out.print((char) k);

                message.append((char) k);

                if (message.toString().endsWith("\r\n\n\n")){
                    System.out.println("Message is over");
                    break;
                }
            }
            catch (Exception e){
                System.out.println(e.toString());
            }
        }
        //Echo echo = new Echo(echoCode);
        //echo.receiver(modem, 4000);
        //Echo2 echo2 = new Echo2(ackCode, nackCode);
        //echo2.receiver(modem, 4000);
        //Image image = new Image(imageCode, imageErrorCode);
        //image.setCameraDirections("CAM=PTZ DIR=R");
        /*for(int i = 0; i < 5; i++) {
            image.receiver(modem, false);
        }*/
//        image.receiver(modem, false);
//        image.receiver(modem, true);
        GPS gps = new GPS(GPScode);
        gps.receiver(modem);
        modem.close();
    }
}
