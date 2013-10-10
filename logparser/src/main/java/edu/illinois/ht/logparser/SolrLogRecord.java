package edu.illinois.ht.logparser;


import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


/**
 * Represents a single record from the HT Solr logs
 */
public class SolrLogRecord implements Comparable<SolrLogRecord>{

	String ipaddr = "";
	String sessionId = ""; 
	String procId = "";
	String time = "";
	long timestamp;
	String qtime = "";
	long numFound;
	String url = "";
	String cgi = "";
	

	
	public SolrLogRecord(String ipaddr, String sessionId, String procId, 
			String time, long timestamp, String qtime, long numFound, 
			String url, String cgi)
	{
		this.ipaddr = ipaddr;
		this.sessionId = sessionId;
		this.procId = procId;
		this.time = time;
		this.timestamp = timestamp;
		this.qtime= qtime;
		this.numFound = numFound;
		this.url = url;
		this.cgi = cgi;
	}

	
	
	public String getIpaddr() {
		return ipaddr;
	}

	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getProcId() {
		return procId;
	}

	public void setProcId(String procId) {
		this.procId = procId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getQtime() {
		return qtime;
	}

	public void setQtime(String qtime) {
		this.qtime = qtime;
	}

	public long getNumFound() {
		return numFound;
	}

	public void setNumFound(long numFound) {
		this.numFound = numFound;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCgi() {
		return cgi;
	}

	public void setCgi(String cgi) {
		this.cgi = cgi;
	}

	public void setTime(String time) {
		this.time = time;
	}

	
	public long getTime() {
		return timestamp;
	}
	
	/** 
	 * Returns a combined query object based on the Solr query information
	 * found in the current record.
	 * @return
	 */
	public CombinedQuery getSolrQueryRecord()
	{

		CombinedQuery solrQuery = null;
		
		Pattern p = Pattern.compile("url=http://([^/]*)/([^\\?]*\\?)(.*$)");
		Matcher m = p.matcher(url);
		if (m.matches()) 
		{
			//String host = m.group(1);
			//String path = m.group(2);
			String query = m.group(3);
			String[] nvpairs = query.split("&");
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			for (String nvpair: nvpairs) {
				if (!nvpair.contains("="))
					continue;
				String name = nvpair.substring(0, nvpair.indexOf("="));
				String value = nvpair.substring(nvpair.indexOf("=")+1, nvpair.length());
				List<String> pvals = params.get(name);
				if (pvals == null) {
					pvals = new ArrayList<String>();
				}
				
				try {
					value = URLDecoder.decode(value, "UTF-8");
					if (!value.equals("")) 
						pvals.add(value);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				params.put(name, pvals);
			}
			
			solrQuery = new CombinedQuery();
			solrQuery.setIpaddr(ipaddr);
			solrQuery.setTimestamp(timestamp);
			
			int start = 0;
			int rows = 0;
			boolean hasRights = false;
			
			for (String key: params.keySet()) {
				List<String> values = params.get(key);
				if (key.equals("q") && values.size() > 0) {
					List<QueryPart> qparts = parseEdismax(values.get(0));
					if (qparts.size() > 0) {
						QueryPart q1 = qparts.get(0);

						solrQuery.setQ1(q1.getQuery());
						solrQuery.setField1(q1.getField());
						solrQuery.setOp2(q1.getOp().toLowerCase());
					}
					
					if (qparts.size() > 1) {
						QueryPart q2 = qparts.get(1);
						solrQuery.setQ2(q2.getQuery());
						solrQuery.setField2(q2.getField());
					}
				}
				else if (key.equals("anyall1")) {
					solrQuery.setAnyall1(StringUtils.join(values.toArray()));
				} 
				else if (key.equals("anyall2")) {
					solrQuery.setAnyall2(StringUtils.join(values.toArray()));
				} 
				else if (key.equals("fq")) {
					for (String fq: values) {
						if (!fq.contains(":")) 
							continue;
						String facetName = fq.substring(0, fq.indexOf(":"));
						String facetValue = fq.substring(fq.indexOf(":")+1, fq.length());
						
						facetValue = facetValue.replaceAll("\"", "");
						facetValue = facetValue.replaceAll("^\\(", "");
						facetValue = facetValue.replaceAll("  \\)$", "");
						
						if (facetName.equals("topicStr")) {
							solrQuery.setTopicStr(facetValue);
						}
						else if (facetName.equals("authorStr")) {
							solrQuery.setAuthorStr(facetValue);
						}
						else if (facetName.equals("language")) {
							solrQuery.setLanguage(facetValue);
						}
						else if (facetName.equals("countryOfPubStr")) {
							solrQuery.setCountryOfPubStr(facetValue);
						}
						else if (facetName.equals("publishDateRange")) {
							solrQuery.setPublishDateRange(facetValue);
						}
						else if (facetName.equals("publishDateTrie")) {
							solrQuery.setPublishDateTrie(facetValue);
						}
						else if (facetName.equals("format")) {
							solrQuery.setFormat(facetValue);
						}
						else if (facetName.equals("htsource")) {
							solrQuery.setHtsource(facetValue);
						}
						else if (facetName.startsWith("((rights")) {
							hasRights = true;
						}
					
					}
					
				} 
				else if (key.equals("rows")) {
					rows = Integer.valueOf(values.get(0));
				}
				else if (key.equals("start")) {
					if (values.size() == 0) 
						continue;
					start = Integer.valueOf(values.get(0));
				}
			}
			if (hasRights) 
				solrQuery.setLmt("ft");
			else
				solrQuery.setLmt("all");
			
			solrQuery.setSession(sessionId);
			solrQuery.setNumFound(numFound);
			solrQuery.setPn((start/rows) + 1);
			if (!solrQuery.getQ1().equals("") && !solrQuery.getQ2().equals("") && solrQuery.getOp2().equals("")) 
				solrQuery.setOp2("and");		

		} else {
			System.err.println("Failed to parse solr query from " + url);
		}
		
		
		return solrQuery;
	}
	
	public int compareTo(SolrLogRecord a) {
		return ((Long)timestamp).compareTo(a.getTime());
	}
	
	/**
	 * Naive edismax query parser used to extract query terms.
	 * @param query
	 * @return
	 */
	public static List<QueryPart> parseEdismax(String query)
	{
		List<QueryPart> eqs = new ArrayList<QueryPart>();
		
		String[] queries = query.split(" _query_");
		
		for (String q: queries)
		{
			if (q.length() == 0)
				continue;
			
			Pattern p = Pattern.compile("\\{!edismax\\ qf='([^\\^]*)[^\\}]*\\}\\ (.*)\"(.*)");
			Matcher m = p.matcher(q);
			while (m.find())
			{
				String field = m.group(1);
				String terms = m.group(2);
				terms = terms.replaceAll(" \"", "\"");
				terms = terms.replaceAll("\\\\", "");
				terms = terms.replaceAll(" OR ", " ");
				terms = terms.replaceAll("  ", " ");
				String op = m.group(3);	
				
				QueryPart eq = new QueryPart(field, terms, op);
				eqs.add(eq);
			}
			
		}
		return eqs;
	}

}
