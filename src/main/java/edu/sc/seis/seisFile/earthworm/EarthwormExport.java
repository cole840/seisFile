package edu.sc.seis.seisFile.earthworm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

import picocli.CommandLine.Command;

@Command(name="earthwormExportTest", versionProvider=edu.sc.seis.seisFile.client.VersionProvider.class)
public class EarthwormExport {

    public EarthwormExport() {
        
    }
    
    public EarthwormExport(int port, 
                           int module, 
                           int institution, 
                           final String heartbeatMessage, 
                           final int heartbeatSeconds) throws IOException {
        this.port = port;
        this.module = module;
        this.institution = institution;
        this.heartbeatMessage = heartbeatMessage;
        initSocket();
        setHeartbeater(new EarthwormHeartbeater(null, heartbeatSeconds, heartbeatMessage, institution, module));
    }
    
    /** mostly just for testing */
    EarthwormExport(EarthwormEscapeOutputStream outStream, int module, int institution) {
        this.outStream = outStream;
        this.module = module;
        this.institution = institution;
        setHeartbeater(new EarthwormHeartbeater(outStream, 10, heartbeatMessage, institution, module));
    }
    
    public void exportWithRetry(TraceBuf2 traceBuf) throws IOException {
        boolean notSent = true;
        while (notSent) {
            try {
                export(traceBuf);
                notSent = false;
            } catch(Throwable e) {
                closeClient();
                logger.warn("Caught exception, waiting for reconnect, will resend tracebuf", e);
                waitForClient();
            }
        }
    }

    public void export(TraceBuf2 traceBuf) throws IOException {
        if ( ! isConnected()) {
            waitForClient();
        }
        traceBufSent++;
        if (traceBuf.getSize() > TraceBuf2.MAX_TRACEBUF_SIZE) {
            List<TraceBuf2> split = traceBuf.split(TraceBuf2.MAX_TRACEBUF_SIZE);
            if (verbose) {
                System.out.println("TraceBuf too large: "+traceBuf.getSize()+" split into "+split.size()+" pieces.");
            }
            splitTraceBufSent++;
            for (TraceBuf2 splitTB : split) {
                writeTraceBuf(splitTB);
            }
        } else {
            writeTraceBuf(traceBuf);
        }
    }
    
    protected void writeTraceBuf(TraceBuf2 tb) throws IOException {
        synchronized(outStream) {
            outStream.startTransmit();
            outStream.writeThreeChars(institution);
            outStream.writeThreeChars(module);
            outStream.writeThreeChars(EarthwormMessage.MESSAGE_TYPE_TRACEBUF2);
            DataOutputStream dos = new DataOutputStream(outStream);
            tb.write(dos);
            outStream.endTransmit();
            outStream.flush();
        }
    }

    void initSocket() throws IOException {
        logger.info("init socket on port: "+port);
        closeSocket();
        if (serverSocket != null) {
            serverSocket.close();
        }
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(30*1000);
    }
    
    public boolean isConnected() {
        // if the heartbeater had an error sending a heartbeat, it closes its
        // outStream, so check heartbeater outstream is not null as well
        return outStream != null && getHeartbeater().getOutStream() != null;
    }
    
    public synchronized void waitForClient() throws IOException {
        while( ! isConnected()) {
            try {
                getHeartbeater().setOutStream(null);
                if (serverSocket == null) {
                    initSocket();
                }
                logger.info("Wait for client: "+serverSocket.getLocalSocketAddress()+" "+serverSocket.getLocalPort());
                clientSocket = serverSocket.accept(); // block until client connects
                logger.info("Connections from: "+clientSocket.getRemoteSocketAddress()+" "+clientSocket.getPort());
                inStream = new BufferedInputStream(clientSocket.getInputStream());
                outStream = new EarthwormEscapeOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                getHeartbeater().setOutStream(outStream);
                for (int i = 0; i < 10; i++) {
                    getHeartbeater().heartbeat();
                    try {
                        Thread.sleep(100);
                    } catch(InterruptedException e) {
                    }
                }
                getHeartbeater().heartbeat();
                logger.info("initial heartbeat successful");
                if (verbose) {
                    System.out.println("accept connection from "+clientSocket.getInetAddress()+":"+clientSocket.getPort());
                }
                return;
            } catch(SocketTimeoutException e) {
                // try again...
                logger.info("Socket timeout, close and try again", e);
                closeClient();
            }
        }
    }
    
    public synchronized void closeClient() {
        logger.info("close client connection");
        if (outStream != null) {
            getHeartbeater().setOutStream(null);
        }
        EarthwormEscapeOutputStream.closeIfNotNull(outStream);
        outStream = null;
        EarthwormEscapeOutputStream.closeIfNotNull(inStream);
        inStream = null;
        EarthwormEscapeOutputStream.closeIfNotNull(clientSocket);
        clientSocket = null;
    }

    public void closeSocket() {
        logger.info("close socket");
        closeClient();
        EarthwormEscapeOutputStream.closeIfNotNull(serverSocket);
        serverSocket = null;
    }
    
    public int getNumTraceBufSent() {
        return traceBufSent;
    }
    
    public int getNumSplitTraceBufSent() {
        return splitTraceBufSent;
    }
    
    int getNextSeqNum() {
        if (seqNum == 999) {
            seqNum = 0;
        }
        return seqNum++;
    }

    public static void main(String[] args) throws Exception {
        // testing
        EarthwormExport exporter = new EarthwormExport(10002, 43, 255, "heartbeat", 5);
        exporter.waitForClient();
        int[] data = new int[14000];
        for (int i = 0; i < data.length; i++) {
            data[i] = i%100;
        }
        long Y1970_TO_Y2000_SECONDS = 946728000l;
        TraceBuf2 tb = new TraceBuf2(1,
                                     data.length,
                                     Y1970_TO_Y2000_SECONDS,
                                     1,
                                     "XXX",
                                     "SS",
                                     "HHZ",
                                     "00",
                                     data);
        for (int i = 0; i < 10; i++) {
            if (exporter.inStream.available() > 0) {
                byte[] b = new byte[1024];
                exporter.inStream.read(b);
                String s = new String(b);
                System.out.println("In: "+s);
            }
            Thread.sleep(1000);

            boolean notSent = true;
            while(notSent) {
                try {
                    exporter.export(tb);
                    notSent = false;
                    tb.startTime += data.length;
                    tb.endTime += data.length;
                    System.out.println("Set tb "+tb);
                } catch(IOException e) {
                    exporter.closeClient();
                    exporter.waitForClient();
                }
            }
        }
        System.out.println("Done");
    }

    int traceBufSent = 0;
    
    int splitTraceBufSent = 0;
    
    private String heartbeatMessage = "heartbeat";
    
    int module;

    int institution;

    EarthwormEscapeOutputStream outStream;
    
    BufferedInputStream inStream;

    int seqNum = 0;

    ServerSocket serverSocket;

    Socket clientSocket = null;
    
    private EarthwormHeartbeater heartbeater = null;

    int port;

    public boolean verbose = true;
    
    public static final byte ESC = 27;

    public static final byte STX = 2;

    public static final byte ETX = 3;

    public static final String SEQ_CODE = "SQ:";

    public void setVerbose(boolean b) {
        verbose = b;
    }

    public EarthwormHeartbeater getHeartbeater() {
        return heartbeater;
    }

    public void setHeartbeater(EarthwormHeartbeater heartbeater) {
        if (this.heartbeater != null) {
            this.heartbeater.cancel();
        }
        this.heartbeater = heartbeater;
    }
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EarthwormExport.class);

}
