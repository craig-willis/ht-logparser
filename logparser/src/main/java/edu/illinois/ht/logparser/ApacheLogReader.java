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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads an Apache log file and parses into a set of ApacheLogRecord objects
 */
public class ApacheLogReader 
{

	/**
	 * Parse an Apache log file into a list of ApacheLogRecord objects
	 * 
	 * @param log  Path to log file
	 * @return
	 * @throws IOException
	 */
	public List<ApacheLogRecord> parse(String log) throws IOException
	{
		List<ApacheLogRecord> records = new ArrayList<ApacheLogRecord>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(log), "UTF-8"));
		String line;
		while ((line = br.readLine()) != null) 
		{
			//Pattern LOG_PATTERN = Pattern.compile("([^ ]*) - - \\[([^\\]]*)\\] \"([^\"]*)\" ([0-9]*) ([0-9]*) \"([^\"]*)\" \"([^\"]*)\" ([^ ]*) \"([^\"]*)\"");
			Pattern LOG_PATTERN = Pattern.compile("([^ ]*) - - \\[([^\\]]*)\\] \"(GET [^ ]* HTTP[^\"]*)\" ([0-9]*) ([^ ]*) \"(.*)\" \"([^\"]*)\" ([^ ]*) \"([^\"]*)\"");
			
			Matcher matcher = LOG_PATTERN.matcher(line);
			if (matcher.matches()) {
				String host = matcher.group(1);
				String datetime = matcher.group(2);
				String request = matcher.group(3);
				String status = matcher.group(4);
				String bytes = matcher.group(5);
				String referer = matcher.group(6);
				String useragent = matcher.group(7);
				String server = matcher.group(8);
				
				//10/May/2012:01:02:46 -0400
				SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss");
				Date date = null;
				try {
					date = df.parse(datetime.substring(0, datetime.indexOf(" ")));
				} catch (ParseException e) { 
					e.printStackTrace();
				}
				if (bytes.equals("-"))
					bytes = "0";
				ApacheLogRecord record = new ApacheLogRecord(host, date, request, Integer.valueOf(status), 
						Integer.valueOf(bytes), referer, useragent, server);
				records.add(record);
			}
			else
				System.out.println("WARN: failed to parse " + line);
			
		}
		br.close();
		return records;
	}
	
	
	public static void main(String[] args) throws IOException {
		try
		{
			ApacheLogReader alr = new ApacheLogReader();
			List<ApacheLogRecord> list1 = alr.parse("test-logs/lassi/access_log-20120510-anon");
			List<ApacheLogRecord> list2 = alr.parse("test-logs/moxie-1/access_log-20120510-anon");
			list1.addAll(list2);
			
			Collections.sort(list1);
			for (ApacheLogRecord log: list1) {
				System.out.println(log.getTime() + ", " + log.getRequest());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
