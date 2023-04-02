
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
        // Create the default outstation configuration
        OutstationStackConfig config = new OutstationStackConfig(DatabaseConfig.allValues(2), EventBufferConfig.allTypes(50));

        config.linkConfig.localAddr = 10;
        config.linkConfig.remoteAddr = 1;

        config.outstationConfig.allowUnsolicited = true; //Povoli unsolicited zpravy
        //config.outstationConfig.

        //Add new outstation to the channel
        Outstation outstation = channel.addOutstation(
                "outstation",
                SuccessCommandHandler.getInstance(),
                DefaultOutstationApplication.getInstance(),
                config
        );

        //Enables oustation
        outstation.enable();


        double cislo = 10;
        OutstationChangeSet set = new OutstationChangeSet();


        // all this stuff just to read a line of text in Java. Oh the humanity.
        String line = "";
        InputStreamReader converter = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(converter);


        int i = 34;
        while (true) {
            System.out.println("Enter something to update a counter or type <quit> to exit");
            line = in.readLine();
            if(line.equals("quit")) {
                break;
            }else if(line.equals("1")){

                cislo++;
                Flags flags = new Flags();
                flags.set(AnalogQuality.ONLINE);
                set.update(new AnalogInput(cislo,flags,new DNPTime(System.currentTimeMillis(),TimestampQuality.UNSYNCHRONIZED)),0);
                outstation.apply(set);

/*
                set.update(new AnalogInput(cislo,flags,new DNPTime(System.currentTimeMillis())),0);
                //flags.set(CounterQuality.COMM_LOST);
                //set.update(new Counter(i, flags, new DNPTime(0)), 0);
                outstation.apply(set);
                ++i;*/
            }
        }
    }
}
