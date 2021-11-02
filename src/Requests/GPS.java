package Requests;

import ithakimodem.Modem;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class GPS {
    private String GPScode;
    private ArrayList<String> coordinates = new ArrayList<>();
    private int imageCounter = 0;

    //so that I can change the ithaki codes as arguments
    public GPS(String GPScode) {
        this.GPScode = GPScode;
    }

    public void receiver(Modem modem) {
        StringBuilder message = new StringBuilder();
        ArrayList<String> GPGGAStrings = new ArrayList<>();
        ArrayList<String> GPGSAStrings = new ArrayList<>();
        ArrayList<String> GPRMCStrings = new ArrayList<>();
        int k;

        //sending request to the server for GPS data with the GPS code
        if (modem.write(this.GPScode.getBytes())) {
            while (true) {
                try {
                    k = modem.read();
                    if (k == -1) {
                        System.out.println("Connection Timed out");
                        break;
                    }

                    //System.out.print((char) k);   //debug do this

                    //whatever is read by the modem, typecast it and append it to the message
                    message.append((char) k);

                    //each message ends with \r\n
                    if (message.toString().endsWith("\r\n")) {

                        //we don't want to hold the packet which starts with "START"
                        if (message.toString().startsWith("START")) {
                            message.setLength(0);
                        }

                        //nor the message that starts with "STOP"
                        else if (message.toString().startsWith("STOP")) {
                            message.setLength(0);
                            break;              //break because the message ends with the "STOP"
                        } else {
                            //System.out.println(message.toString());

                            //we want to hold the GPS data to different arraylists based on the headers
                            switch (message.substring(0, 6)) {
                                case "$GPGGA":
                                    GPGGAStrings.add(message.toString());
                                    System.out.println(message.toString());
                                    break;
                                case "$GPGSA":
                                    GPGSAStrings.add(message.toString());
                                    break;
                                case "$GPRMC":
                                    GPRMCStrings.add(message.toString());
                                    break;
                            }
                            message.setLength(0);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
            addCoordinates(GPGGAStrings);
        }
        ImageCoordinates(modem);
    }

    public void ImageCoordinates(Modem modem) {
        int k;
        ArrayList<Integer> imageBytes = new ArrayList<>();
        String code = GPScode.substring(0, 5);
        for (String i : this.coordinates) {
            code += i;
        }

        code += "\r";
        System.out.println(code);
        if (modem.write(code.getBytes())) { //sending request to the server

            while (true) {
                try {
                    k = modem.read();

                    if (k == -1) {
                        System.out.println("Connection Timed out");
                        break;
                    }

                    //System.out.print(k + " ");
                    imageBytes.add(k);

                    //jpeg images end with D9FF, which counterpart to 217 and 255 in decimal
                    if (imageBytes.size() > 2) {
                        if (imageBytes.get(imageBytes.size() - 1) == 217 && imageBytes.get(imageBytes.size() - 2) == 255) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }

            //File file = new File("./image.jpeg");     //saving the image in "networks" directory


            try {
                FileOutputStream file = null;
                file = new FileOutputStream("./GPS/Image_" + this.imageCounter + ".jpeg");

                for (int b : imageBytes) {
                    //System.out.print(b + " ");
                    file.write(b);
                }

                file.close();
                this.imageCounter++;
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
    }


    //we want to receive each gps data line with time difference 5 seconds and store them to an ArrayList
    private void addCoordinates(ArrayList<String> messages) {
        int time = getTimeInSeconds(messages.get(0));
        this.coordinates.add(getCoordinates(messages.get(0)));

        for (String message : messages) {
            int messageTime = getTimeInSeconds(message);

            if (messageTime - time >= 5) {
                time = getTimeInSeconds(message);
                this.coordinates.add(getCoordinates(message));

                if (this.coordinates.size() >= 9) {
                    break;
                }
            }
        }
    }

    private int getTimeInSeconds(String message) {

        //In each GPS data line,after the first comma we have the time in which the trace was taken
        //So we restrict the time in a String
        String utcTime = message.split(",")[1];
        utcTime = utcTime.split("\\.")[0];

        //split the utc time to hours, minutes and seconds to convert all of them to seconds
        int messageHour = Integer.parseInt(utcTime.substring(0, 2));
        int messageMinutes = Integer.parseInt(utcTime.substring(2, 4));
        int messageSeconds = Integer.parseInt(utcTime.substring(4, 6));
        return messageMinutes * 60 + messageHour * 3600 + messageSeconds;
    }

    //convert the latitude and longitude to the appropriate form to get the coordinates
    private String getCoordinates(String gpggaString) {

        String latitude = gpggaString.split(",")[2];
        String integer_latitude = latitude.split("\\.")[0];

        //we want to multiply the float part of the latitude number with 60 so as to take hours, minutes and seconds
        String double_latitude = "0." + latitude.split("\\.")[1];
        double toLatitude = Double.parseDouble(double_latitude);
        toLatitude *= 60;
        if (toLatitude < 10)
            integer_latitude += "0" + (int) Math.round(toLatitude);
        else
            integer_latitude += (int) Math.round(toLatitude);


        String longitude = gpggaString.split(",")[4];
        String integer_longitude = longitude.split("\\.")[0];

        //in case there's a zero in front longitude
        //parseInt overlooks the zeros in front of the number
        integer_longitude = String.valueOf(Integer.parseInt(integer_longitude));
        String double_longitude = "0." + longitude.split("\\.")[1];
        double toLongitude = Double.parseDouble(double_longitude);
        toLongitude *= 60;
        if (toLongitude < 10)
            integer_longitude += "0" + (int) Math.round(toLongitude);
        else
            integer_longitude += (int) Math.round(toLongitude);

        return "T=" + integer_longitude + integer_latitude;
    }
}
