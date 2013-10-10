package edu.illinois.ht.logparser;

/**
 * Represents a HathiTrust full-text (pt or "page-turner" request)
 */
public class FullTextRecord implements Comparable<FullTextRecord>
{

	long uniqueId = 0;
	String ipaddr ="";
	long timestamp = 0;
	int pn = 0;
	int seq = 0;
	String attr = "";
	String q1 = "";
	String view = "";
	String orient = "";
	String page = "";
	int size = 0;
	int start = 0;
	String skin = "";
	String referer = "";
	String id = "";
	String url = "";
	
	/* Default constructor */
	public FullTextRecord() {}
	
	/**
	 * Constructs a new FullTextRecord based on a simple
	 * pipe-delimited serialization
	 * 
	 * @param str
	 */
	public FullTextRecord(String str) 
	{
		String[] fields = str.split("\\|");
		try {
			uniqueId = Long.valueOf(fields[0]);
		} catch (NumberFormatException e) { }

		ipaddr = fields[1];

		try {
			timestamp = Long.valueOf(fields[2]);
		} catch (NumberFormatException e) { }

		
		id = fields[3];
		q1 = fields[4];

		try {
			pn = Integer.valueOf(fields[5]);
		} catch (NumberFormatException e) { }
		try {
			seq = Integer.valueOf(fields[6]);
		} catch (NumberFormatException e) { }

		
		attr = fields[7];
		view = fields[8];
		orient = fields[9];
		page = fields[10];
		
		
		try {
			size = Integer.valueOf(fields[11]);
		} catch (NumberFormatException e) { }
		try {
			start = Integer.valueOf(fields[12]);
		} catch (NumberFormatException e) { }

		skin = fields[13];
		referer = fields[14];
		url = fields[15];
	}
	
	public FullTextRecord( String ipaddr, long timestamp, int pn, int seq, String attr,
		String q1, String view, String orient, String page, int size,
		int start, String skin, String referer)	
	{
		this.ipaddr = ipaddr;
		this.timestamp = timestamp;
		this.pn = pn;
		this.seq = seq;
		this.attr = attr;
		this.q1 = q1;
		this.view = view;
		this.orient = orient;
		this.page = page;
		this.size = size;
		this.start = start;
		this.skin = skin;
		this.referer = referer;
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


	public int getPn() {
		return pn;
	}


	public void setPn(int pn) {
		this.pn = pn;
	}


	public int getSeq() {
		return seq;
	}


	public void setSeq(int seq) {
		this.seq = seq;
	}


	public String getAttr() {
		return attr;
	}


	public void setAttr(String attr) {
		this.attr = attr;
	}


	public String getQ1() {
		return q1;
	}


	public void setQ1(String q1) {
		this.q1 = q1;
	}


	public String getView() {
		return view;
	}


	public void setView(String view) {
		this.view = view;
	}


	public String getOrient() {
		return orient;
	}


	public void setOrient(String orient) {
		this.orient = orient;
	}


	public String getPage() {
		return page;
	}


	public void setPage(String page) {
		this.page = page;
	}


	public int getSize() {
		return size;
	}


	public void setSize(int size) {
		this.size = size;
	}


	public int getStart() {
		return start;
	}


	public void setStart(int start) {
		this.start = start;
	}


	public String getSkin() {
		return skin;
	}


	public void setSkin(String skin) {
		this.skin = skin;
	}


	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}
	
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(long uniqueId) {
		this.uniqueId = uniqueId;
	}

	public int compareTo(FullTextRecord a) {
		return ((Long)timestamp).compareTo(a.getTimestamp());
	}
	

	public String toString() {
		 return uniqueId + "|" + ipaddr + "|" +
		 timestamp + "|" +
		 id + "|" +
		 q1 + "|" +
		 pn + "|" +
		 seq + "|" +
		 attr + "|" +
		 view + "|" +
		 orient + "|" +
		 page + "|" +
		 size + "|" +
		 start + "|" +
		 skin + "|" +
		 referer + "|" + 
		 url;
	}
	


}
