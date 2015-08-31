package com.roderickhodgson;

import com.dialogic.XMSClientLibrary.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 *
 * @author roderickhodgson
 */
public class Recorder {
    public static void main(String[] args) {
        // TODO code application logic here

        String destination = null;
        if (args.length!=1){
            System.out.println("Error - please enter a destination phone number");
            System.exit(0);
        }
        else {
            destination = args[0];
        }

        XMSObjectFactory myFactory = new XMSObjectFactory();
        XMSConnector myConnector = myFactory.CreateConnector("XMSConnectorConfig.xml");
        XMSCall myCall = myFactory.CreateCall(myConnector);

        String PlayFile=null;

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("config.properties"));

            PlayFile=prop.getProperty("PlayFile");

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        while (true) {
            myCall.Waitcall();


            if (myCall.getState() == XMSCallState.CONNECTED) {
                System.out.println("CALL ACTIVE!!!");

                myCall.PlayOptions.SetMediaType(XMSMediaType.AUDIO);
                myCall.Play("leavemessage.wav");

                myCall.RecordOptions.SetMaxTime(10);
                myCall.RecordOptions.SetTerminateDigits("#");
                myCall.Record("echotest.wav");

                //Save the connection address for later
                String addr = myCall.getConnectionAddress();
                System.out.print("=== RECORDING COMPLETE === ADDR IS: " + addr);

                myCall.Dropcall();

                try {
                    Thread.sleep(1000); //sleep for 1 second
                } catch (InterruptedException ex) {
                    System.out.print(ex);
                }

                //RunCommandLocally("python test.py");
                boolean s = false;
                int c = 0;
                while (s == false && c < 10) {
                    s = RunCommandLocally("python voicechanger.py 1.1 /var/lib/xms/media/en_US/echotest.wav /var/lib/xms/media/en_US/echotest2.wav");
                    c++;
                }

                try {
                    Thread.sleep(2000); //sleep for 2 seconds
                } catch (InterruptedException ex) {
                    System.out.print(ex);
                }

            /*myCall.PlayOptions.SetMediaType(XMSMediaType.AUDIO);
            myCall.Play("echotest.wav");

            try {
                Thread.sleep(60000); //sleep for 1 minute
            } catch (InterruptedException ex) {
                System.out.print(ex);
            }*/
                //Hangup the call
            }

        /*
        //Make an outbound call to the same address that you just received a call from
        +44number@tad-rod.pstn.ie1.twilio.com */
            myCall.MakecallOptions.setSourceAddress("sip:+447900000000@52.28.137.49");
            myCall.Makecall("sip:" + destination + "@tad-rod.pstn.ie1.twilio.com");


            myCall.PlayOptions.SetMediaType(XMSMediaType.AUDIO);
            myCall.Play("newmessage.wav");
            myCall.Play("echotest2.wav");

            try {
                Thread.sleep(1000); //sleep for 10 seconds
            } catch (InterruptedException ex) {
                System.out.print(ex);
            }

            //Hangup the call
            myCall.Dropcall();
        }
    }

    public static boolean RunCommandLocally(String cmd) {

        String s = null;
        boolean retval = true;

        try {

            // run the Unix "ps -ef" command
            // using the Runtime exec method:
            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
                if (s.length() > 1)
                    retval = false;
            }

        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            retval = false;
        }
        return retval;
    }
}
