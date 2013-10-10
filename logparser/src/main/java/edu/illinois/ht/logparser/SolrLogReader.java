package edu.illinois.ht.logparser;

import java.io.BufferedReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Reads an Solr log file and parses into a set of SolrLogRecord objects
 */
public class SolrLogReader {

	public List<SolrLogRecord> parse(String log) throws IOException
	{
		String logDate = log.substring(log.lastIndexOf("/")+1, log.lastIndexOf("."));
		List<SolrLogRecord> records = new ArrayList<SolrLogRecord>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(log), "UTF-8"));
		String line;
		while ((line = br.readLine()) != null) 
		{
			try
			{
				// Ignore records measuring query elapsed time.
				if (line.indexOf("total elapsed") > 0 || line.indexOf("&rows=0&") > 0)
					continue;
				
				String[] fields = line.split(" ");
				String ipaddr = fields[0];
				String sessionId = fields[1];
				String procId = fields[2];
				String time = fields[3];
				String qtime = fields[4];
				qtime = qtime.substring(qtime.indexOf("=")+1, qtime.length());
				String numfound = fields[5];
				numfound = numfound.substring(numfound.indexOf("=")+1, numfound.length());
				String url = fields[6];
				String cgi = "";
				if (fields.length == 8)
					 cgi = fields[7];
					
				SimpleDateFormat df = new SimpleDateFormat("'q'-yyyy-MM-dd HH:mm:ss");
				Date date = null;
				try {
					date = df.parse(logDate + " " + time );
				} catch (ParseException e) { 
					e.printStackTrace();
				}
				SolrLogRecord record = new SolrLogRecord(ipaddr, sessionId, procId, time, 
						date.getTime(), qtime, Long.valueOf(numfound), url, cgi);
				records.add(record);
			} catch (Exception e) {
				System.err.println("Failed to read solr record: " + line);
			}
		}
		br.close();
		return records;
	}
	
	public static void main(String[] args) throws IOException {
		try
		{
			SolrLogReader alr = new SolrLogReader();
			List<SolrLogRecord> list1 = alr.parse("test-logs/solr/q-2012-05-10.log");
			List<SolrLogRecord> list2 = alr.parse("test-logs/solr/q-2012-05-11.log");
			list1.addAll(list2);
			
			Collections.sort(list1);
			for (SolrLogRecord log: list1) {
				System.out.println(log.getTime() + ", " + log.getUrl());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
