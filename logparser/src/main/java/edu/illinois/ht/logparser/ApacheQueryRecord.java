package edu.illinois.ht.logparser;

/**
 * Represents a HathiTrust full-text query as logged
 * in the Apache webserver logs
 */
public class ApacheQueryRecord {

	String ipaddr;
	long timestamp;
	String anyall1;
	String field1;
	String q1;
	String op2;
	String anyall2;
	String field2;
	String q2;
	String pn;
	String lmt;
	String topicStr;
	String authorStr;
	String language;
	String countryOfPubStr;
	String publishDateRange;
	String publishDateTrie;
	String format;
	String htsource;
	
	public ApacheQueryRecord(String ipaddr, long timestamp, String anyall1,
			String field1, String q1, String op2, String anyall2,
			String field2,	String q2, String pn, String lmt, 
			String topicStr, String authorStr, String language, String countryOfPubStr, 
			String publishDateRange, String publishDateTrie, String format, String htsource)
	{
		this.ipaddr = ipaddr;
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
		this.field1 = field1;
	}
	public String getQ1() {
		return q1;
	}
	public void setQ1(String q1) {
		this.q1 = q1;
	}
	public String getOp2() {
		return op2;
	}
	public void setOp2(String op2) {
		this.op2 = op2;
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
		this.field2 = field2;
	}
	public String getQ2() {
		return q2;
	}
	public void setQ2(String q2) {
		this.q2 = q2;
	}
	public String getPn() {
		return pn;
	}
	public void setPn(String pn) {
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
}
