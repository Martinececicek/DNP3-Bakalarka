
/*
 * Copyright 2013-2020 Automatak, LLC
 *
 * Licensed to Green Energy Corp (www.greenenergycorp.com) and Automatak
 * LLC (www.automatak.com) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Green Energy Corp and Automatak LLC license
 * this file to you under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain
 * a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//package com.automatak.dnp3.example;

import com.automatak.dnp3.*;
import com.automatak.dnp3.enums.*;
import com.automatak.dnp3.impl.DNP3ManagerFactory;
import com.automatak.dnp3.mock.DefaultOutstationApplication;
import com.automatak.dnp3.mock.PrintingChannelListener;
import com.automatak.dnp3.mock.PrintingLogHandler;
import com.automatak.dnp3.mock.SuccessCommandHandler;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;

import static java.lang.System.in;
import static java.lang.System.out;

/**
 * Example master than can be run against the example outstation
 */
public class Main {

    private static String MasterIP;
    private static int port;
    private boolean onStart = true;
    public static void main(String[] args) throws Exception {

        String line = null;
        String splitedLine[] = null;
        boolean onStart = true;

        InputStreamReader converter = new InputStreamReader(in);
        BufferedReader in = new BufferedReader(converter);






        while (onStart) {
            int count = 0;
            out.println("((-help?))");
            out.print("Outstation input:  ");
            line = in.readLine();

            splitedLine = line.split(" ");
            String parsed[] = splitedLine;
            for (int i = 0; i < splitedLine.length; i++) {
                switch (splitedLine[i]) {
                    case ("-ip"): {
                        MasterIP = splitedLine[i+1];
                        out.println("IP of outstation set to: " + MasterIP);
                        break;
                    }
                    case ("-port"):{
                        port = isInt(splitedLine[i+1]);
                        out.println("Port for communication set to: " + port);
                        break;
                    }
                    case ("-run"):{
                        DNP3Manager manager1 = DNP3ManagerFactory.createManager(1, PrintingLogHandler.getInstance());
                        try {
                            run(manager1, MasterIP, port);
                        } catch (Exception ex) {
                            System.out.println("Exception: " + ex.getMessage());
                        } finally {
                            // This call is needed b/c the thread-pool will stop the application from exiting
                            // and the finalizer isn't guaranteed to run b/c the GC might not be collected during main() exit
                            manager1.shutdown();
                        }
                        break;
                    }
                    case ("-help"):
                        out.println("-ip <IP>-> sets the ip of Master station to connect");
                        out.println("-port <port> -> sets the port");
                        out.println("-run -> will start the Outstation");
                        out.println("-quit");
                        out.println("(-run must be last parameter)");
                        break;
                    case ("-quit"):
                        out.println("Bye");
                        onStart = false;
                        break;
                }

            }


        }
    }

    private static int isInt(String string){
        port = Integer.parseInt(string);
        return port;
    }



    private static void run(DNP3Manager manager, String masterIP,int port) throws Exception {

        // Create a tcp channel class that will connect to the loopback
        Channel channel = manager.addTCPServer(
                "client",
                LogMasks.NORMAL | LogMasks.APP_COMMS,
                ServerAcceptMode.CloseExisting,
                new IPEndpoint(MasterIP, port),
                PrintingChannelListener.getInstance()
        );

        //DatabaseConfig.allValues()    promenna
        int databasenum = 5;
        //EventBufferConfig.allTypes()  promenna
        int eventbufferconfignum = 5;
        // Create the default outstation configuration
        OutstationStackConfig config = new OutstationStackConfig(DatabaseConfig.allValues(databasenum), EventBufferConfig.allTypes(eventbufferconfignum));


        config.linkConfig.localAddr = 10;
        config.linkConfig.remoteAddr = 1;





        //Add new outstation to the channel
        Outstation outstation = channel.addOutstation(
                "outstation",
                SuccessCommandHandler.getInstance(),
                DefaultOutstationApplication.getInstance(),
                config
        );

        //Enables oustation
        outstation.enable();


        // all this stuff just to read a line of text in Java. Oh the humanity.
        String line = "";
        InputStreamReader converter = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(converter);

        //INICIALIZACE PROMENNYCH PRO JEDNOTLIVE RESPONSES
        double analognumber = 0;
        boolean binaryinput = false;
        long counter = 0;

        OutstationChangeSet set = new OutstationChangeSet(); //inicializace Outstation set
        Flags flags = new Flags(); //inicializace Flags

        DNPTime dnpTime = new DNPTime(System.currentTimeMillis(),TimestampQuality.SYNCHRONIZED);

        int i = 34;
        while (true) {
            System.out.println("Enter something to update any value");
            line = in.readLine();
            switch(line) {

                case "1": //ANALOG INPUT CHANGE
                    analognumber++;
                    flags.set(AnalogQuality.ONLINE);
                    set.update(new AnalogInput(analognumber, flags, dnpTime), 0);
                    set.update(new AnalogInput((analognumber - 3 * 14 / 3), flags, dnpTime), 1);
                    outstation.apply(set);
                    break;

                case "2": //BINARY INPUT CHANGE
                    if (binaryinput == false){
                        binaryinput = true;
                    flags.set(BinaryQuality.ONLINE);
                    set.update(new BinaryInput(binaryinput, flags, new DNPTime(System.currentTimeMillis(), TimestampQuality.SYNCHRONIZED)), 0);
                    outstation.apply(set);
                    }else if(binaryinput == true){
                        binaryinput = false;
                        flags.set(BinaryQuality.ONLINE);
                        set.update(new BinaryInput(binaryinput, flags, new DNPTime(System.currentTimeMillis(), TimestampQuality.SYNCHRONIZED)), 0);
                        outstation.apply(set);
                        //BinaryInput binaryInput = new BinaryInput(binaryinput,flags,new DNPTime(System.currentTimeMillis(),TimestampQuality.SYNCHRONIZED));
                        //out.println(binaryInput.toString());
                    }
                    break;

                case "3": // DOUBLE BIT BINARY INPUT
                    flags.set(DoubleBitBinaryQuality.LOCAL_FORCED);
                    set.update(new DoubleBitBinaryInput(DoubleBit.DETERMINED_ON, flags, new DNPTime(System.currentTimeMillis(), TimestampQuality.INVALID)), 0);
                    set.update(new DoubleBitBinaryInput(DoubleBit.DETERMINED_OFF, flags, new DNPTime(System.currentTimeMillis(), TimestampQuality.INVALID)), 1);
                    flags.set(DoubleBitBinaryQuality.RESTART);
                    set.update(new DoubleBitBinaryInput(DoubleBit.INDETERMINATE,flags,new DNPTime(System.currentTimeMillis(),TimestampQuality.SYNCHRONIZED)),2);
                    set.update(new DoubleBitBinaryInput(DoubleBit.INTERMEDIATE,flags,new DNPTime(System.currentTimeMillis(),TimestampQuality.SYNCHRONIZED)),3);
                    outstation.apply(set);
                    break;

                case "4": //COUNTER CHANGE
                        flags.set(CounterQuality.RESTART);
                    for (int y = 0; y <= databasenum; y++) { // HODNE JEDNODUCHY COUNTER :)
                        set.update(new Counter(counter,flags,new DNPTime(System.currentTimeMillis(),TimestampQuality.UNSYNCHRONIZED)),y);

                        counter++;
                    }
                    outstation.apply(set);
                    break;

                case "5": //FROZEN COUNTER CHANGE

                    //TODO

                case "6": //BINARY OUTPUT STATUS CHANGE

                    break;

                case "7": //ANALOG OUTPUT STATUS CHANGE

                    break;

                case "s": //Zkouska vypinat zapinat Solicited/Unsolicited messages /// JEDNODUCHY PREPINAC
                    if (config.outstationConfig.allowUnsolicited) {
                        config.outstationConfig.allowUnsolicited = false;   //Zakaze unsolicited zpravy
                    }else {
                        config.outstationConfig.allowUnsolicited = true;    //Povoli unsolicited zpravy
                    }
                    break;

                default:
                    break;
            }
        }
    }
}

/*
-ip 127.0.0.1 -port 20000 -run

 */