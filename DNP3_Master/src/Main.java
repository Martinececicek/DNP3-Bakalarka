import com.automatak.dnp3.*;
import com.automatak.dnp3.enums.*;
import com.automatak.dnp3.impl.DNP3ManagerFactory;
import com.automatak.dnp3.mock.DefaultMasterApplication;
import com.automatak.dnp3.mock.PrintingChannelListener;
import com.automatak.dnp3.mock.PrintingLogHandler;
import com.automatak.dnp3.mock.PrintingSOEHandler;

//import javax.swing.event.ChangeEvent;
//import java.awt.desktop.OpenURIEvent;
import java.io.BufferedReader;
//import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
//import java.util.logging.ConsoleHandler;

import static java.lang.System.*;

public class Main {

    //private static String masterIP;
    private static String outStIP;
    private static int port;
    private boolean onStart = true;

    public static void main(String[] args) throws IOException {// create the root class with a thread pool size of 1

        String line = null;
        String splitedLine[] = null;
        boolean onStart = true;

        InputStreamReader converter = new InputStreamReader(in);
        BufferedReader in = new BufferedReader(converter);





        while (onStart) {
            int count = 0;
            out.println("((-help?))");
            out.print("Master input:  ");
            line = in.readLine();

            splitedLine = line.split(" ");
            String parsed[] = splitedLine;
            for (int i = 0; i < splitedLine.length; i++) {
                switch (splitedLine[i]) {
                    case ("-ip"): {
                        outStIP = splitedLine[i+1];
                        out.println("IP of outstation set to: " + outStIP);
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
                            run(manager1, outStIP, port);
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
                        out.println("-ip -> sets the ip of outstation to connect");
                        out.println("-port -> sets the port");
                        out.println("-run -> will start the Master station");
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
    private static void run(DNP3Manager manager, String outStIP, int port) throws DNP3Exception, IOException {
        Channel channel1 = manager.addTCPClient(
                "client",
                LogMasks.NORMAL | LogMasks.APP_COMMS,
                ChannelRetry.getDefault(),
                Collections.singletonList(new IPEndpoint(outStIP,port)),
                "0.0.0.0",
                PrintingChannelListener.getInstance()

        );

        MasterStackConfig config1 = new MasterStackConfig();
        config1.link.localAddr = 1;
        config1.link.remoteAddr = 10;


        DNPTime dnpTime = new DNPTime(System.currentTimeMillis());


        // Create a master instance, pass in a simple singleton to print received values to the console
        Master master1 = channel1.addMaster("master", PrintingSOEHandler.getInstance(), DefaultMasterApplication.getInstance(), config1);
        // do an integrity scan every 2 seconds
        byte variatinons = 3;
        byte group = 30;
        master1.addPeriodicScan(Duration.ofSeconds(10),Header.allObjects(group,variatinons),PrintingSOEHandler.getInstance());
        //master1.addPeriodicScan(Duration.ofSeconds(2), Header.getIntegrity(), PrintingSOEHandler.getInstance());
        master1.enable();

        // all this cruft just to read a line of text in Java. Oh the humanity.
        InputStreamReader converter = new InputStreamReader(in);
        BufferedReader in = new BufferedReader(converter);

        out.println("Ahoj");

        while (true) {
            out.println("Enter something to issue a command or type <quit> to exit");
            String line = in.readLine();
            if (line.equals("1")){
                master1.scan(Header.allObjects(group,variatinons),PrintingSOEHandler.getInstance());
            }
            switch (line) {
                case ("quit"):
                    return;
                case ("crob"):
                    ControlRelayOutputBlock crob = new ControlRelayOutputBlock(
                            OperationType.LATCH_ON,
                            TripCloseCode.NUL,
                            false,
                            (short) 1,
                            5000,
                            5000,
                            CommandStatus.SUCCESS);

                    master1.selectAndOperateCROB(crob, 0).thenAccept(
                            //asynchronously print the result of the command operation
                            (CommandTaskResult result) -> out.println(result)
                    );
                    break;
                case ("scan"):
                    //master1.scan(Header.allObjects((byte)30, (byte)1));
                    master1.scan(Header.getEventClasses(), PrintingSOEHandler.getInstance());
                    break;
                default:
                    out.println("Unknown command: " + line);
                    break;
            }
        }
    }
}
// -ip 127.0.0.1 -port 20000 -run