package Requests;

import ithakimodem.Modem;

import java.io.*;
import java.util.ArrayList;

public class Image {
    private String imageCode;
    private final String imageErrorCode;
    private String cameraDirections;
    private int imageCounter = 0;

    public Image(String imageCode, String imageErrorCode) {
        this.imageCode = imageCode;
        this.imageErrorCode = imageErrorCode;
    }

    public void setCameraDirections(String cameraDirections) {
        this.cameraDirections = cameraDirections;

        //each code ends in \r. So we first abstract the \r, add the camera directions and add again the ending character
        this.imageCode = this.imageCode.substring(0, 5) + cameraDirections + "\r";

            System.out.println(imageCode);
    }

    /**
     * receives Bytes from the server and prints them
     * @param modem
     * @param hasErrors whether we want the image with errors or not
     */

    public void receiver(Modem modem, boolean hasErrors) {
        int k;
        ArrayList<Integer> imageBytes = new ArrayList<>();
        String code;
        //so as the server knows what data should send
        if(hasErrors){
          code = this.imageErrorCode;
        }
        else{
            code = this.imageCode;
        }

        // starting time
        long initialTime = System.currentTimeMillis();
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
                if (hasErrors) {
                    file = new FileOutputStream("./Images/ErrorImage.jpeg");
                } else {
                    file = new FileOutputStream("./Images/noErrorImage_" + this.imageCounter + ".jpeg");
                }
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
}
