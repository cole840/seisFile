package edu.sc.seis.seisFile.seedlink;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import edu.sc.seis.seisFile.client.AbstractClient;
import edu.sc.seis.seisFile.mseed.DataRecord;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

/**
 * Added support for an info output file and specifying a start and end time.
 */
@Command(name="seedlinkclient", versionProvider=edu.sc.seis.seisFile.client.VersionProvider.class)
public class Client extends AbstractClient {

    @Option(names= {"-h", "--host"}, description="host to connect to, defaults to IRIS, "+SeedlinkReader.DEFAULT_HOST)
    public String host = SeedlinkReader.DEFAULT_HOST;
    
    @Option(names= {"-p", "--port"}, description="port to connect to, defaults to IRIS, "+SeedlinkReader.DEFAULT_PORT)
    public Integer port = SeedlinkReader.DEFAULT_PORT;
    
    @Option(names= {"-n", "--network"}, description="list of networks to search")
    List<String> network;
    @Option(names= {"-s", "--station"}, description="list of stations to search")
    List<String> station ;
    @Option(names= {"-l", "--location"}, description="list of locations to search")
    List<String> location;
    @Option(names= {"-c", "--channel"}, description="list of channels to search")
    List<String> channel;
    
    @Option(names= {"-b", "--start"}, description="start time")
    Instant start;
    @Option(names= {"-e", "--end"}, description="end time")
    Instant end;

    @Option(names= {"--itype"}, description="info typ, ex "+SeedlinkReader.INFO_STREAMS)
    String infoType = SeedlinkReader.EMPTY;
    @Option(names= { "--iout"}, description="info out file")
    String ioutFile = SeedlinkReader.EMPTY;
    
    @Option(names= { "--max"}, description="number of packets to receive before ending the connection")
    public int maxRecords = 10;

    @Option(names = { "-o", "--out" }, description = "Output file (default: print to console)")
    private File outputFile;
    
    @Option(names= {"--timeout"}, description="timeout seconds")
    public Integer timeoutSec = SeedlinkReader.DEFAULT_TIMEOUT_SECOND;
    
    public Client() {
    }
    
    @Override
    public Integer call() throws Exception {
        ParseResult parsedArgs = spec.commandLine().getParseResult();
        if (requiresAtLeastOneArg() && parsedArgs.expandedArgs().size() == 0) {
            throw new ParameterException(spec.commandLine(), "Must use at least one option");
        }
        DataOutputStream dos = null;
        PrintWriter out = new PrintWriter(System.out, true);
        if (maxRecords < -1) {
            maxRecords = -1;
        }
        if (outputFile != null) {
            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        }
        SeedlinkReader reader = new SeedlinkReader(host, port, timeoutSec, verbose);
        if (verbose) {
            reader.setVerboseWriter(out);
            String[] lines = reader.sendHello();
            out.println("line 1 :" + lines[0]);
            out.println("line 2 :" + lines[1]);
            out.flush();
        }
		if (infoType.length() != 0 || ioutFile.length() != 0)
		{
        	if (infoType.length() == 0) {
        		infoType = SeedlinkReader.INFO_STREAMS;
        	}
        	String infoString = reader.getInfoString(infoType);
        	if (ioutFile == null) {
        		out.print(infoString);
        	} else {
        		PrintWriter pw = null;
        		try {
        			pw = new PrintWriter(ioutFile);
        			pw.print(infoString);
        		}
        		finally {
        			if (pw != null) {
        				pw.close();
        			}
        		}
        	}
        }
        if (maxRecords != 0) {
        reader.select(String.join(",", network), String.join(",", station), String.join(",", location), String.join(",", channel));
        reader.startData(start, end);
        int i = 0;
        try {
            while ((maxRecords == -1 || i < maxRecords) && reader.hasNext()) {
                SeedlinkPacket slp = reader.readPacket();
                DataRecord dr = slp.getMiniSeed();
                if (dos != null) {
                    dr.write(dos);
                }
                if (dos == null || verbose) {
                    // print something to the screen if we are not saving to
                    // disk
                    dr.writeASCII(out, "    ");
                    out.flush();
                }
                i++;
            }
        } catch(EOFException e) {
            // done I guess
        }
        }
        if (dos != null) {
            dos.close();
        }
        reader.close();
        out.println("Finished: " + Instant.now());
        return 0;
    }
    
    public static void main(String... args) { // bootstrap the application
        System.exit(new CommandLine(new Client()).execute(args));
    }

}
