package edu.illinois.ht.logparser;

/**
 * Represents a HathiTrust full-text query based on 
 * information derived from both the Apache web server
 * and Solr logs.
 *
 */
public class CombinedQuery implements Comparable<CombinedQuery >
{

	long uniqueId = 0;
	String ipaddr = "";
	String session = "";
	long timestamp = 0;
	String anyall1= "";
	String field1 = "";
	String q1 = "";
	String op2 = "";
	String anyall2 = "";
	String field2 = "";
	String q2 = "";
	int pn = 0;
	String lmt = "";
	String topicStr = "";
	String authorStr = "";
	String language = "";
	String countryOfPubStr = "";
	String publishDateRange = "";
	String publishDateTrie = "";
	String format = "";
	String htsource = "";
	long numFound = 0;
	String url = "";
	String referer = "";

	public CombinedQuery() {}
	
    public CombinedQuery(String str) 
    {
        String[] fields = str.split("\\|");
		
		try {
			 uniqueId = Long.valueOf(fields[0]);
		} catch (NumberFormatException e) {}
		ipaddr = fields[1];
		session = fields[2];
		 
		try {
			timestamp = Long.valueOf(fields[3]);
		} catch (NumberFormatException e) {}
		anyall1= fields[4];
		field1 = fields[5];
		q1 = fields[6];
		op2 = fields[7];
		anyall2 = fields[8];
		field2 = fields[9];
		q2 = fields[10];
		
		try {
			pn = Integer.valueOf(fields[11]);
		} catch (NumberFormatException e) {}
		lmt = fields[12];
		topicStr = fields[13];
		authorStr = fields[14];
		language = fields[15];
		countryOfPubStr = fields[16];
		publishDateRange = fields[17];
		publishDateTrie = fields[18];
		format =fields[19];
		htsource = fields[20];
		 
		try {
			numFound = Long.valueOf(fields[21]);
		} catch (NumberFormatException e) {}
		url = fields[22];
		referer = fields[23];
	}
	
	public CombinedQuery(String ipaddr, String session, long timestamp, String anyall1,
			String field1, String q1, String op2, String anyall2,
			String field2,	String q2, int pn, String lmt, 
			String topicStr, String authorStr, String language, String countryOfPubStr, 
			String publishDateRange, String publishDateTrie, String format, String htsource,
			long numFound)
	{
		this.ipaddr = ipaddr;
		this.session = session;
		this.timestamp = timestamp;
		this.anyall1 = anyall1;
		this.field1 = field1;
		this.q1 = q1;
		this.op2 = op2;
		this.anyall2 = anyall2;
		this.field2 = field2;
		this.q2 = q2;
		this.pn = pn;
		this.lmt = lmt;
		this.topicStr = topicStr;
		this.authorStr = authorStr;
		this.language = language;
		this.countryOfPubStr = countryOfPubStr;
		this.publishDateRange = publishDateRange;
		this.publishDateTrie = publishDateTrie;
		this.format = format;
		this.htsource = htsource;
		this.numFound = numFound;
	}
	
	public String getIpaddr() {
		return ipaddr;
	}
	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getAnyall1() {
		return anyall1;
	}
	public void setAnyall1(String anyall1) {
		this.anyall1 = anyall1;
	}
	public String getField1() {
		return field1;
	}
	public void setField1(String field1) {
	
		if (field1.equals("publishDateRange"))
			field1 = "year";
		if (field1.equals("titleProper"))
			field1 = "title";
		if (field1.equals("serialTitleProper"))
			field1 = "series";
		if (field1.equals("topicProper"))
			field1 = "subject";
		if (field1.equals("titleProper"))
			field1 = "title";
		if (field1.equals("issn"))
			field1 = "isn";
		
		this.field1 = field1;
	}
	public String getQ1() {
		return q1;
	}
	public void setQ1(String q1) {
		this.q1 = q1.trim();
	}
	public String getOp2() {
		return op2;
	}
	public void setOp2(String op2) {
		if (op2.equals("AND"))
			op2 = "and";
		if (op2.equals("OR"))
			op2 = "or";
		this.op2 = op2.trim();
	}
	public String getAnyall2() {
		return anyall2;
	}
	public void setAnyall2(String anyall2) {
		this.anyall2 = anyall2;
	}
	public String getField2() {
		return field2;
	}
	public void setField2(String field2) {
		
		if (field2.equals("publishDateRange"))
			field2 = "year";
		if (field2.equals("titleProper"))
			field2 = "title";
		if (field2.equals("serialTitleProper"))
			field2 = "series";
		if (field2.equals("topicProper"))
			field2 = "subject";
		if (field2.equals("titleProper"))
			field2 = "title";
		if (field2.equals("issn"))
			field2 = "isn";
		this.field2 = field2;
	}
	public String getQ2() {
		return q2;
	}
	public void setQ2(String q2) {
		this.q2 = q2.trim();
	}
	public int getPn() {
		return pn;
	}
	public void setPn(int pn) {
		this.pn = pn;
	}
	public String getLmt() {
		return lmt;
	}
	public void setLmt(String lmt) {
		this.lmt = lmt;
	}
	public String getTopicStr() {
		return topicStr;
	}
	public void setTopicStr(String topicStr) {
		this.topicStr = topicStr;
	}
	public String getAuthorStr() {
		return authorStr;
	}
	public void setAuthorStr(String authorStr) {
		this.authorStr = authorStr;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getCountryOfPubStr() {
		return countryOfPubStr;
	}
	public void setCountryOfPubStr(String countryOfPubStr) {
		this.countryOfPubStr = countryOfPubStr;
	}
	public String getPublishDateRange() {
		return publishDateRange;
	}
	public void setPublishDateRange(String publishDateRange) {
		this.publishDateRange = publishDateRange;
	}
	public String getPublishDateTrie() {
		return publishDateTrie;
	}
	public void setPublishDateTrie(String publishDateTrie) {
		this.publishDateTrie = publishDateTrie;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getHtsource() {
		return htsource;
	}
	public void setHtsource(String htsource) {
		this.htsource = htsource;
	}
	
	
	
	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
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
	

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	public long getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(long uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	public int compareTo(CombinedQuery a) {
		return ((Long)timestamp).compareTo(a.getTimestamp());
	}
	
	public int sameAs(CombinedQuery q, long timeDiff) {
		int error = 0;
		//if (!anyall1.equals(q.getAnyall1()))
		//	return ++error;
		
		//if (!anyall2.equals(q.getAnyall2()))
		//	return ++error;
		
		if (!field1.equals(q.getField1()))
			return 1;
		
		if (!field2.equals(q.getField2()))
			return 2;
		
		if (!q1.equals(q.getQ1()))
			return 3;
		
		if (!q2.equals(q.getQ2()))
			return 4;
		
		if (!op2.equals(q.getOp2()))
			return 5;
		
		if (pn != q.getPn())
			return 6;
		
		if (!lmt.equals(q.getLmt()))
			return 7;
		
		if (!topicStr.equals(q.getTopicStr()))
			return 8;
		
		if (!authorStr.equals(q.getAuthorStr()))
			return 9;
		
		if (!language.equals(q.getLanguage()))
			return 10;
		
		if (!countryOfPubStr.equals(q.getCountryOfPubStr()))
			return 11;
		
		if (!publishDateRange.equals(q.getPublishDateRange()))
			return 12;
		
		if (!publishDateTrie.equals(q.getPublishDateTrie()))
			return 13;
		
		if (!format.equals(q.getFormat()))
			return 14;
		
		if (!htsource.equals(q.getHtsource()))
			return 15;

		long diff = timestamp - q.getTimestamp();
		if (diff >= 0 && diff < timeDiff)
		{
			return 0;
		}
		else
			return 16;
	
	}
	
	public String toString() {
		 return uniqueId + "|" + ipaddr + "|" +
		 session + "|" +
		 timestamp + "|" +
		 anyall1 + "|" +
		 field1 + "|" +
		 q1 + "|" +
		 op2 + "|" +
		 anyall2 + "|" +
		 field2 + "|" +
		 q2 + "|" +
		 pn + "|" +
		 lmt + "|" +
		 topicStr + "|" +
		 authorStr + "|" +
		 language + "|" +
		 countryOfPubStr + "|" +
		 publishDateRange + "|" +
		 publishDateTrie + "|" +
		 format + "|" +
		 htsource + "|" +
		 numFound + "|" + 
		 url + "|" + 
		 referer;
	}
}
