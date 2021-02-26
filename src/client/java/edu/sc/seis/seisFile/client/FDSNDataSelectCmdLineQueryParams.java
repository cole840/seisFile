
package edu.sc.seis.seisFile.client;

import java.time.Instant;
import picocli.CommandLine.Option;

import edu.sc.seis.seisFile.fdsnws.FDSNDataSelectQueryParams;

/** Autogenerated by groovy FDSNQueryParamGenerator.groovy in src/metacode/groovy
 */
public class FDSNDataSelectCmdLineQueryParams {

    FDSNDataSelectQueryParams queryParams;

    public FDSNDataSelectCmdLineQueryParams() {
        this(FDSNDataSelectQueryParams.DEFAULT_HOST);
    }

    public FDSNDataSelectCmdLineQueryParams(String host) {
        this.queryParams = new FDSNDataSelectQueryParams();
        setHost(host==null ? FDSNDataSelectQueryParams.DEFAULT_HOST : host);
    }

    @Option(names = { "--host" }, description="host to connect to")
    public FDSNDataSelectQueryParams setHost(String host) {
        return this.queryParams.setHost(host);
    }

    @Option(names = "--port", description = "port to connect to, defaults to 80", defaultValue="80")
    public FDSNDataSelectQueryParams setPort(int port) {
        return this.queryParams.setPort(port);
    }


    /** Limit results to time series samples on or after the specified start time
     */
    @Option(names = { "-b","--starttime","--start" }, description="Limit results to time series samples on or after the specified start time", converter=FloorISOTimeParser.class)
    public FDSNDataSelectQueryParams setStartTime(Instant value) {
        queryParams.setStartTime(value);
        return queryParams;
    }


    /** Limit results to time series samples on or before the specified end time
     */
    @Option(names = { "-e","--endtime","--end" }, description="Limit results to time series samples on or before the specified end time", converter=FloorISOTimeParser.class)
    public FDSNDataSelectQueryParams setEndTime(Instant value) {
        queryParams.setEndTime(value);
        return queryParams;
    }



    @Option(names = { "-n","--network","--net" }, description="Select one or more network codes. Can be SEED network codes or data center defined codes. Multiple codes are comma-separated.")
    public FDSNDataSelectQueryParams setNetwork(String[] value) {
      queryParams.clearNetwork();
      for(String v: value) queryParams.appendToNetwork(v);
      return queryParams;
    }



    @Option(names = { "-s","--station","--sta" }, description="Select one or more SEED station codes. Multiple codes are comma-separated.")
    public FDSNDataSelectQueryParams setStation(String[] value) {
      queryParams.clearStation();
      for(String v: value) queryParams.appendToStation(v);
      return queryParams;
    }



    @Option(names = { "-l","--location","--loc" }, description="Select one or more SEED location identifiers. Multiple identifiers are comma-separated. As a special case -- (two dashes) will be translated to a string of two space characters to match blank location IDs.")
    public FDSNDataSelectQueryParams setLocation(String[] value) {
      queryParams.clearLocation();
      for(String v: value) queryParams.appendToLocation(v);
      return queryParams;
    }



    @Option(names = { "-c","--channel","--cha" }, description="Select one or more SEED channel codes. Multiple codes are comma-separated.")
    public FDSNDataSelectQueryParams setChannel(String[] value) {
      queryParams.clearChannel();
      for(String v: value) queryParams.appendToChannel(v);
      return queryParams;
    }


    /** Select a specific SEED quality indicator, handling is data center dependent.
     */
    @Option(names = { "--quality" }, description="Select a specific SEED quality indicator, handling is data center dependent.")
    public FDSNDataSelectQueryParams setQuality(String value) {
        queryParams.setQuality(value);
        return queryParams;
    }


    /** Limit results to continuous data segments of a minimum length specified in seconds.
     */
    @Option(names = { "--minimumlength" }, description="Limit results to continuous data segments of a minimum length specified in seconds.")
    public FDSNDataSelectQueryParams setMinimumLength(int value) {
        queryParams.setMinimumLength(value);
        return queryParams;
    }


    /** Limit results to the longest continuous segment per channel.
     */
    @Option(names = { "--longestonly" }, description="Limit results to the longest continuous segment per channel.")
    public FDSNDataSelectQueryParams setLongestOnly(boolean value) {
        queryParams.setLongestOnly(value);
        return queryParams;
    }



    @Option(names = {"--post"}, description="use http POST instead of GET")
    boolean doPost = false;

    public String getServiceName() {
        return queryParams.getServiceName();
    }


    

}

