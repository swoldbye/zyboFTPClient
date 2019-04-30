import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;

public class FTPProtocol {

    public static void main(String[] args) {
        try {

            //Host Name
            String host = "IP address or url here.";

            //Create Control Channel
            Socket clientSocket = new Socket(host, 21);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //get acknowledgement on connection to new socket (temporary)
            System.out.println("-----------------");
            getTxt(inFromServer);
            System.out.println("-----------------");

            //login
            System.out.println("Logining into FTP server...");
            outToServer.writeBytes("USER anonymous\r\n");
            outToServer.writeBytes("PASS \r\n");

            //cd to directory with CSV file
            outToServer.writeBytes("cwd ____name_here____\r\n");

            //retrieve CSV file
            String dlFileName = "sensor4Data.csv";
            Socket dataSocketDl = generateDataSocket(outToServer, inFromServer);
            InputStreamReader inFromDataSocketDl = new InputStreamReader((dataSocketDl.getInputStream()));
            outToServer.writeBytes("retr sensor4Data.csv\r\n");
            dlFile(inFromDataSocketDl);

        } catch (java.io.IOException ioE) {
            System.out.println(ioE);
        }
    }

    public static void getTxt(BufferedReader inFromServer) throws java.io.IOException {
        do {
            String line = inFromServer.readLine();
            System.out.println(line);

        } while (inFromServer.ready());
    }

    public static Socket generateDataSocket(DataOutputStream outToServer, BufferedReader inFromServer) throws java.io.IOException {
        String newSocketAddress = null;
        outToServer.writeBytes("PASV\r\n");
        newSocketAddress = inFromServer.readLine();
        System.out.println(newSocketAddress);

        //convert socket address to IP and Port
        String[] line = newSocketAddress.split(Pattern.quote("("));
        String address = getAddress(line[1]);
        int port = getPort(line[1]);
        System.out.println("-----------------");
        System.out.println("Connecting to new socket...     IP: " + address + ",     Port Nmr: " + port);
        System.out.println("-----------------");

        //create new Socket & input/output Stream
        return new Socket(address, port);
    }

    public static String getAddress(String str) {
        String[] ip = str.split(Pattern.quote(","));
        String finalIP = ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3];

        return finalIP;
    }

    public static int getPort(String str) {
        String[] port = str.split(Pattern.quote(","));


        int finalP = ((Integer.parseInt(port[4]) * 256)
                + Integer.parseInt(port[5].substring(0, port[5].length() - 1)));

        return finalP;
    }

    public static void dlFile(InputStreamReader inFromDataSocketDl)throws java.io.IOException {

        String sensorFilePath = "ext/sensorData.csv";
        String sensor4FilePath = "ext/sensor4Highest.csv";

        File drop = new File(sensorFilePath);
        drop.delete();

        File drop2 = new File(sensor4FilePath);
        drop2.delete();

        File newSensors = new File(sensorFilePath);
        File newSensor4 = new File(sensor4FilePath);

        FileOutputStream fileOutputStream = new FileOutputStream(newSensors, true);
        do {
            int line = inFromDataSocketDl.read();
            fileOutputStream.write(line);
        } while (inFromDataSocketDl.ready());
        fileOutputStream.close();

        int maxVal = 0;
        int tempVal;

        CSVReader csvReader = new CSVReader(new FileReader(sensorFilePath));
        String[] row = null;
        while ((row = csvReader.readNext()) != null) {
            tempVal = Integer.parseInt(row[4]);
            if (tempVal > maxVal) maxVal = tempVal;
        }

        CSVWriter writer = new CSVWriter(new FileWriter(sensor4FilePath));

        String max = Integer.toString(maxVal);
        String[] maxArr = {max};
        writer.writeNext(maxArr);
        writer.close();

    }
}


