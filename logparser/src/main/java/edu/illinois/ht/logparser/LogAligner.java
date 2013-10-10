package edu.illinois.ht.logparser;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Reads in a set of Apache web server logs and Solr logs. 
 * Tries to find match all HT queries (/ls) and full-text (/pt) requests
 * in each log-file source. Outputs a set of pipe-delimited files
 * intended for import into MySQL.
 * 
 * Why go through this pain?
 * 		The Solr log is the only source of information about the
 *           number of records returned for a query.
 *      The Apache log is the only source of information about full-text 
 *           requests. 
 * 
 * Outputs the following files:
 *  	ls.out			Apache query requests
 *  	solr.out		Solr query requests
 * 		pt.out			Apache full-text requests
 *  	solr_ls.out		Map of Solr to Apache queries
 *  	pt_ls.out		Map of Apache full-text requests to Apache queries 
 *  	pt_pt.out		Map of Apache full-text requests to other full-text requests
 *
 * Known issues:
 * 		Does not attempt to cross date boundaries.
 */
public class LogAligner {

	/* Maximum time difference between Apache query and Solr query */
	static int MAX_DIFFERENCE = 30 *1000;
	/* Maximum time difference between full-text requests */
	static int MAX_FULLTEXT_DIFFERENCE = 60*60*1000;
	
	public static void main(String[] args) throws Exception
	{
		/* Path to apache logs */
		String apacheDir = args[0]; //"test-logs/";
		/* Path to solr logs */
		String solrDir = args[1];   //"test-logs/solr/";
		
		String startDate = args[2]; //"05/10/2012";
		String endDate = args[3];   //"05/11/2012";
		
		boolean restart = Boolean.parseBoolean(args[3]); //false
		LogAligner aligner = new LogAligner();
		
		aligner.alignLogs(apacheDir, solrDir, startDate, endDate, restart);
	}
	
	
	/**
	 * This is ugly. Forgive me...
	 * 
	 * This is the main alignment logic.  Given a set of Apache web server logs
	 * and a set of Solr logs, try to match up queries and full-text requests.
	 * 
	 * Assign each query and full-text request a unique identifier and output
	 * pipe delimited files for further analysis.
	 * 
	 * @param apacheDir		Directory with Apache logs (with per-server subdir)
	 * @param solrDir		Directory with Solr logs
	 * @param startDate		Start date for processing
	 * @param endDate		End date for processing
	 * @param restart		Whether to initialize from existing output files
	 * @throws Exception
	 */
	public void alignLogs(String apacheDir, String solrDir, String startDate, String endDate,
			boolean restart) throws Exception
	{
		String[] apacheHosts = {"lassi", "moxie-1", "moxie-2", "sharbat" };

		ApacheLogReader alr = new ApacheLogReader();
		SolrLogReader slr = new SolrLogReader();
		
		List<ApacheLogRecord> apacheRecords = new ArrayList<ApacheLogRecord>();
		List<SolrLogRecord> solrRecords = new ArrayList<SolrLogRecord>();
		
		/* For each date in the range, read the Apache and Solr logs */
		List<Date> dateRange = getDatesForRange(startDate, endDate);
		for (Date date: dateRange)
		{
			DateFormat apacheFormat = new SimpleDateFormat("yyyyMMdd");
			DateFormat solrFormat = new SimpleDateFormat("yyyy-MM-dd");
				
			/* Read log files for each Apache host for current date*/
			for (String host: apacheHosts) 
			{
				String file = apacheDir + host + File.separator + "access_log-" + apacheFormat.format(date) + "-anon";
				if (!new File(file).exists())
					continue;
				
				System.out.println("Reading apache logs from " + file);
				List<ApacheLogRecord> arecs = alr.parse(file);
				apacheRecords.addAll(arecs);
			}
			
			/* Read Solr log file for date */
			String solrFile = solrDir + "q-" + solrFormat.format(date) + ".log";
			System.out.println("Reading solr log " + solrFile);
			List<SolrLogRecord> srecs = slr.parse(solrFile);
			solrRecords.addAll(srecs);
		}
		
		System.out.println("Read " + apacheRecords.size() + " apache records");
		System.out.println("Read " + solrRecords.size() + " solr records");
		
		/* Parse the full-text (/pt) requests from the Apache logs*/
		System.out.println("Parsing full text requests from apache logs");
		SortedMap<Long, FullTextRecord> fullTextRecords = getFullTextRecords(apacheRecords, restart);
		Map<String, List<Long>> fullTextRecordsByURL = new HashMap<String, List<Long>>();
		for (FullTextRecord record: fullTextRecords.values()) {
			String url = record.getUrl();
			long id = record.getUniqueId();
			
			List<Long> ids = fullTextRecordsByURL.get(url);
			if (ids == null)
				ids = new ArrayList<Long>();

			ids.add(id);
			fullTextRecordsByURL.put(url, ids);
		}
		
		/* Write the full-text request records to output file */
		if (!restart || !new File("pt.out").exists()) {
			System.out.println("Writing full text requests to pt.out");
			OutputStreamWriter oswPt = new OutputStreamWriter(new FileOutputStream("pt.out"), "UTF-8");
			for (FullTextRecord fullTextRecord: fullTextRecords.values()) {
				oswPt.write(fullTextRecord.toString() + "\n");
			}
			oswPt.close();
		}
		
		/* Parse queries (/ls) from Apache logs */
		System.out.println("Reading queries from apache logs");
		long minApacheTimestamp = 0;
		SortedMap<Long, CombinedQuery> apacheQueries = getApacheQueries(apacheRecords, restart);
		Map<String, List<Long>> apacheQueriesByURL = new HashMap<String, List<Long>>();
		for (CombinedQuery query: apacheQueries.values()) {
			if (minApacheTimestamp == 0 || query.getTimestamp() < minApacheTimestamp)
				minApacheTimestamp = query.getTimestamp();
			String url = query.getUrl();
			long id = query.getUniqueId();
			
			List<Long> ids = apacheQueriesByURL.get(url);
			if (ids == null)
				ids = new ArrayList<Long>();

			ids.add(id);
			apacheQueriesByURL.put(url, ids);
		}
		
		/* Write Apache query information to output file */
		if (!restart || !new File("ls.out").exists()) {
			System.out.println("Writing apache queries to ls.out");
			OutputStreamWriter oswLs = new OutputStreamWriter(new FileOutputStream("ls.out"), "UTF-8");
			for (CombinedQuery apacheQuery: apacheQueries.values()) {
				oswLs.write(apacheQuery.toString() + "\n");
			}
			oswLs.close();	
		}
		
		
		/* Parse queries from Solr logs */
		System.out.println("Reading queries from solr logs");
		SortedMap<Long, CombinedQuery> solrQueries = solrQueries = getSolrQueries(solrRecords, restart);	
		
		/* Write Solr query information to output file */
		if (!restart || !new File("solr.out").exists()) {
			System.out.println("Writing solr queries to solr.out");
			OutputStreamWriter oswSolr = new OutputStreamWriter(new FileOutputStream("solr.out"), "UTF-8");
			for (CombinedQuery solrQuery: solrQueries.values()) {
				oswSolr.write(solrQuery.toString() + "\n");
			}
			oswSolr.close();
		}

		
		/* For every full-text request (/pt), try to find the associated query in the Apache
		 * logs.
		 */
		System.out.println("Aligning full text requests with apache queries");
		long lastApacheTimestamp = 0;
		int good = 0;
		int bad = 0;
		// Find the query record associated with each full-text
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("pt_ls.out"), "UTF-8");
		for (FullTextRecord fullTextRecord: fullTextRecords.values()) {
			if (fullTextRecord.timestamp > minApacheTimestamp) 
			{
				if (!fullTextRecord.getReferer().contains("/ls?")) 
					continue;
				
				CombinedQuery apacheQuery = findApacheQueryForFullTextRecord(fullTextRecord, apacheQueries, 
						apacheQueriesByURL,  lastApacheTimestamp);
				if (apacheQuery == null)
				{
					System.err.println("No LS for PT: " + fullTextRecord);
					bad++;
				}
				else {
					osw.write(fullTextRecord.getUniqueId() + "," + apacheQuery.getUniqueId() + "\n");
					good++;
					lastApacheTimestamp = apacheQuery.getTimestamp();
				}
			}
		}
		osw.close();
		
		System.out.println("bad = " + bad + ", good=" + good);
		
		/* For each full-text record with full-text (/pt) referrer, try to find 
		 * the source request in the Apache logs */
		
		System.out.println("Aligning full text requests with source full text requests");
		lastApacheTimestamp = 0;
		good = 0;
		bad = 0;
		OutputStreamWriter ptMapOsw = new OutputStreamWriter(new FileOutputStream("pt_pt.out"), "UTF-8");
		for (FullTextRecord fullTextRecord: fullTextRecords.values()) {
			if (fullTextRecord.timestamp > minApacheTimestamp) 
			{
				if (!fullTextRecord.getReferer().contains("/pt?")) 
					continue;
				FullTextRecord fullTextRecord2 = 
						findFullTextRecordForFullTextRecord(fullTextRecord, fullTextRecords,
								fullTextRecordsByURL, lastApacheTimestamp);
				if (fullTextRecord2 == null)
				{
					System.err.println("No PT for PT: " + fullTextRecord);
					bad++;
				}
				else {
					ptMapOsw.write(fullTextRecord.getUniqueId() + "," + fullTextRecord2.getUniqueId() + "\n");
					good++;
					lastApacheTimestamp = fullTextRecord.getTimestamp();
				}
			}
		}
		System.out.println("PT-PT bad = " + bad + ", good=" + good);
		ptMapOsw.close();
	
		
		/* For each Solr query record, try to find the associated query request 
		 * in the Apache logs */
		System.out.println("Aligning solr queries with apache queries");
		lastApacheTimestamp = 0;
		good = 0;
		bad = 0;
		Map<String, String> found = new HashMap<String, String>();
		OutputStreamWriter solrLsOsw = new OutputStreamWriter(new FileOutputStream("solr_ls.out"), "UTF-8");
		long lastApacheQuery = 0;
		for (CombinedQuery solrQuery: solrQueries.values()) {
			if (solrQuery.timestamp > minApacheTimestamp) 
			{
				CombinedQuery apacheQuery = findApacheQuery(solrQuery, apacheQueries.tailMap(lastApacheQuery-100), 
						lastApacheTimestamp, found);
				if (apacheQuery == null)
				{
					System.err.println("No query for Solr query: " + solrQuery);
					bad++;
				}
				else {
					lastApacheQuery = apacheQuery.getUniqueId();
					solrLsOsw.write(solrQuery.getUniqueId() + "," + apacheQuery.getUniqueId() + "\n");
					found.put(apacheQuery.getIpaddr() + "_" + apacheQuery.getTimestamp(), 
							solrQuery.getIpaddr()  + "_" + solrQuery.getTimestamp());
					
					good++;
				}
					
			}
			lastApacheTimestamp = solrQuery.getTimestamp();
		}
		System.out.println("SOLR-LS bad = " + bad + ", good=" + good);
		solrLsOsw.close();
		
		int missing = 0;
		for (CombinedQuery apacheQuery: apacheQueries.values()) {
			String key = apacheQuery.getIpaddr() + "_" + apacheQuery.getTimestamp();
			if (found.get(key) == null) 
			{
				missing++;
				System.err.println("Never found apache query: " + apacheQuery);
			}
		}
		System.out.println("SOL-LS missing " + missing);
	}
	
	
	/**
	 * Given a query (/ls) from the Solr logs, find the assicated 
	 * record in the Apache logs
	 * 
	 * @param solrQuery				Solr query
	 * @param apacheQueries			List of Apache queries
	 * @param lastApacheTimestamp	Timestamp of last Apache query processed
	 * @param matched				List of already matched queries					
	 * @return
	 */
	public CombinedQuery findApacheQuery(CombinedQuery solrQuery, 
			Map<Long, CombinedQuery> apacheQueries, long lastApacheTimestamp, 
			Map<String, String> matched) 
	{
		CombinedQuery found = null;
		
		// Assume that both sets of queries are sorted by timestamp
		for (CombinedQuery apacheQuery: apacheQueries.values()) {
			if (apacheQuery.getTimestamp() < (lastApacheTimestamp - MAX_DIFFERENCE))
				continue;
			if (matched.containsKey(apacheQuery.getIpaddr() + "_" + apacheQuery.getTimestamp()))
				continue;
			long diff = solrQuery.getTimestamp() - apacheQuery.getTimestamp();
			if  (diff >= 0 )  {
				if (diff < MAX_DIFFERENCE) {
					int ret = 0;
					if ((ret = solrQuery.sameAs(apacheQuery, MAX_DIFFERENCE)) == 0) {
							found = apacheQuery;
							break;
					}
				}
			} else
				break;
		}
		return found;
	}

	
	/**
	 * Find the Apache query record (/ls) associated a full-text (/pt) request
	 * 
	 * @param fullTextRecord		Full-text reuqest
	 * @param apacheQueries			List of Apache queries
	 * @param apacheQueriesByURL	Map of Apache queries by URL
	 * @param lastApacheTimestamp	Timestamp of last-matched Apache query
	 * @return
	 */
	public CombinedQuery findApacheQueryForFullTextRecord(FullTextRecord fullTextRecord, 
			Map<Long, CombinedQuery> apacheQueries, Map<String, List<Long>> apacheQueriesByURL, 
			long lastApacheTimestamp) 
	{
		CombinedQuery found = null;
		// Assume that both sets of queries are sorted by timestamp

		List<Long> candidateIds = apacheQueriesByURL.get(fullTextRecord.getReferer());
		if (candidateIds != null) 
		{
			for (Long id: candidateIds) {
				CombinedQuery candidate = apacheQueries.get(id);
				long diff = fullTextRecord.getTimestamp() - candidate.getTimestamp();
				if (diff >= 0) {
					found = candidate;
					break;
				}
			}
		}

		return found;
	}
	
	
	/**
	 * 
	 * @param fullTextRecord
	 * @param fullTextRecords
	 * @param fullTextRecordsByURL
	 * @param lastApacheTimestamp
	 * @return
	 */
	public FullTextRecord findFullTextRecordForFullTextRecord(FullTextRecord fullTextRecord, 
			Map<Long, FullTextRecord> fullTextRecords, Map<String, List<Long>> fullTextRecordsByURL, 
			long lastApacheTimestamp) 
	{
		FullTextRecord found = null;

		List<Long> candidateIds = fullTextRecordsByURL.get(fullTextRecord.getReferer());
		if (candidateIds != null) 
		{
			for (Long id: candidateIds) {
				FullTextRecord candidate = fullTextRecords.get(id);
				long diff = fullTextRecord.getTimestamp() - candidate.getTimestamp();
				if (diff >= 0) {
					found = candidate;
					break;
				}
			}
		}

		return found;
	}
	
	
	/**
	 *	Returns a list of Date objects for a given date range. 
	 * @param startDateStr
	 * @param endDateStr
	 * @return
	 * @throws ParseException
	 */
	public List<Date> getDatesForRange(String startDateStr, String endDateStr) throws ParseException {
	    List<Date> dates = new ArrayList<Date>();
	    Calendar calendar = new GregorianCalendar();
	   
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Date  startDate = (Date)df.parse(startDateStr); 
		Date  endDate = (Date)df.parse(endDateStr);
	    calendar.setTime(startDate);

	    while (calendar.getTime().before(endDate))
	    {
	        Date resultado = calendar.getTime();
	        dates.add(resultado);
	        calendar.add(Calendar.DATE, 1);
	    }
	    return dates;
	}
	
	/** 
	 * Read a list of queries from Apache logs
	 * 
	 * @param logRecords 	Raw log records
	 * @param restart		Whether to read from existing ls.out, if present.
	 * @return
	 * @throws IOException
	 */
	public SortedMap<Long, CombinedQuery> getApacheQueries(List<ApacheLogRecord> logRecords,
			boolean restart) throws IOException {
		
		SortedMap<Long, CombinedQuery> apacheQueries = new TreeMap<Long, CombinedQuery>();
		if (restart && new File("ls.out").exists()) {
			System.out.println("Reading apache records from ls.out");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream("ls.out"), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) 
			{
				CombinedQuery record = new CombinedQuery(line);
				apacheQueries.put(record.getUniqueId(), record);
			}
			br.close();
		}
		else
		{
			List<CombinedQuery> queries = new ArrayList<CombinedQuery>();
			for (ApacheLogRecord logRecord: logRecords) {
				if (logRecord.isQuery()) {
					CombinedQuery query = logRecord.getCombinedQuery();
					
					queries.add(query);
				}
			}	
			
			long id = 1;
			Collections.sort(queries);
			for (CombinedQuery query: queries) {
				query.setUniqueId(id);
				apacheQueries.put(id, query);
				id++;
			}
		}
		return apacheQueries;
	}
	
	/**
	 * Read full-text requests (/pt) from the Apache web server logs
	 * @param logRecords
	 * @param restart
	 * @return
	 * @throws IOException
	 */
	public SortedMap<Long, FullTextRecord> getFullTextRecords(List<ApacheLogRecord> logRecords, 
			boolean restart) throws IOException 
	{
		List<FullTextRecord> records = new ArrayList<FullTextRecord>();
		
		SortedMap<Long, FullTextRecord> fullTextRecords = new TreeMap<Long, FullTextRecord>();
		
		/* Restart from an existing run */
		if (restart && new File("pt.out").exists()) {
			System.out.println("Reading full text records from pt.out");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream("pt.out"), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) 
			{
				FullTextRecord record = new FullTextRecord(line);
				fullTextRecords.put(record.getUniqueId(), record);
			}
			br.close();
		}
		else
		{
			for (ApacheLogRecord logRecord: logRecords)  {
				if (logRecord.isFullText()) {
					FullTextRecord record = logRecord.getFullTextRecord();
					records.add(record);
				}
			}

			/* Assign a unique ID to each request */
			long id = 1;
			Collections.sort(records);
			for (FullTextRecord record: records) {
				record.setUniqueId(id);
				fullTextRecords.put(id, record);
				id++;
			}
		}
		return fullTextRecords;
	}
	
	/** 
	 * Read a list of combined queries from the Solr logs.
	 * 
	 * @param logRecords	Solr log records
	 * @param restart		Whether to read from an existing solr.out if present
	 * @return
	 * @throws IOException
	 */
	public SortedMap<Long, CombinedQuery> getSolrQueries(List<SolrLogRecord> logRecords, 
			boolean restart) throws IOException {
		
		SortedMap<Long, CombinedQuery> solrQueries = new TreeMap<Long, CombinedQuery>();
		if (restart && new File("solr.out").exists())
		{
			System.out.println("Reading solr records from solr.out");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream("solr.out"), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) 
			{
				CombinedQuery query = new CombinedQuery(line);
				solrQueries.put(query.getUniqueId(), query);
			}
			br.close();
		}
		else
		{
			System.out.println("Parsing solr queries from solr records");
			List<CombinedQuery> queries = new ArrayList<CombinedQuery>();
			for (SolrLogRecord solrLogRecord: logRecords) {
				CombinedQuery solrQuery = solrLogRecord.getSolrQueryRecord();
				if (solrQuery != null) {
					queries.add(solrQuery);
				}
			}

			long id = 1;
			Collections.sort(queries);
			for (CombinedQuery record: queries) {
				record.setUniqueId(id);
				solrQueries.put(id, record);
				id++;
			}
			
			System.out.println("Parsed " + solrQueries.size() + " queries from solr logs");
			
		}
		return solrQueries;
	}
	

}
