package edu.illinois.ht.logparser;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a basic Apache webserver log record.
 */
public class ApacheLogRecord implements Comparable<ApacheLogRecord> 
{

	String host;
	long timestamp;
	Date date;
	String request;
	int status;
	int bytes;
	String referer;
	String userAgent;
	String server;
	
	public ApacheLogRecord(String host, Date date, String request, int status, 
			int bytes, String referer, String useragent, String server)
	{
		this.host = host;
		this.date = date;
		this.timestamp = date.getTime();
		this.request = request;
		this.status = status;
		this.bytes = bytes;
		this.referer = referer;
		this.userAgent = useragent;
		this.server = server;
	}
	
	public String getHost() {
		return host;
	}
	
	public Date getDate() {
		return date;
	}
	
	public long getTime() {
		return timestamp;
	}
	
	public String getRequest() {
		return request;
	}
	
	public int getStatus() { 
		return status;
	}
	
	public int getBytes() { 
		return bytes;
	}
	
	public String getReferer() {
		return referer;
	}
	
	public String getServer() {
		return server;
	}
	
	public int compareTo(ApacheLogRecord a) {
		return ((Long)timestamp).compareTo(a.getTime());
	}
		
	
	/**
	 * If this is a full-text request (/pt), parse the 
	 * current record into a FullTextRecord  object.
	 * 
	 * @return
	 */
	public FullTextRecord getFullTextRecord() {
		
		if (!isFullText())
			return null;
		
		FullTextRecord record = new FullTextRecord();
		record.setIpaddr(getHost());
		record.setTimestamp(getTime());
		
		Pattern p = Pattern.compile("GET ([^\\s]*) HTTP.*");
		Matcher m = p.matcher(getRequest());
		if (m.matches()) {
			String uri = m.group(1);
			String path = uri.substring(uri.indexOf("?")+1, uri.length());

			path = path.replaceAll(";", "&");
			String[] nvpairs = path.split("&");
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			
			for (String nvpair: nvpairs) {
				if (!nvpair.contains("=")) 
					continue;
				String name = nvpair.substring(0, nvpair.indexOf("="));
				String value = nvpair.substring(nvpair.indexOf("=")+1, nvpair.length());
				if (!value.equals(""))
				{
					List<String> pvals = params.get(name);
					if (pvals == null) {
						pvals = new ArrayList<String>();
					}
					
					try {
						value = URLDecoder.decode(value, "UTF-8");
					} catch (Exception e) {
						e.printStackTrace();
					} catch (Error e) {
						e.printStackTrace();
					}
					
					pvals.add(value);
					params.put(name, pvals);
				}				
			}

			
			for (String key: params.keySet()) {
				List<String> values = params.get(key);
				if (key.equals("id")) {
					record.setId(StringUtils.join(values.toArray()));
				}
				else if (key.equals("num")) {
					int num = 0;
					try  {
						if (values.size() == 1) 
							num = Integer.valueOf(values.get(0));
					} catch (NumberFormatException e) {}
					record.setPn(num);
				}
				else if (key.equals("seq")) {
					int seq = 0;
					try  {
						if (values.size() == 1) 
							seq = Integer.valueOf(values.get(0));
					} catch (NumberFormatException e) {}
					record.setSeq(seq);
				}
				else if (key.equals("attr")) {
					record.setAttr(StringUtils.join(values.toArray()));
				}
				else if (key.equals("q1")) {
					record.setQ1(StringUtils.join(values.toArray()));
				}
				else if (key.equals("view")) {
					record.setView(StringUtils.join(values.toArray()));
				}
				else if (key.equals("orient")) {
					record.setOrient(StringUtils.join(values.toArray()));
				}				
				else if (key.equals("size")) {
					int size = 0;
					if (values.size() == 1) {
						try {
							size = Integer.valueOf(values.get(0));
						} catch (Exception e) {
							
						}
					}
					record.setSize(size);
				}
				else if (key.equals("start")) {
					int start = 0;
					if (values.size() == 1) {
						try {
							start = Integer.valueOf(values.get(0));
						} catch (Exception e) {
						}
					}
						
					record.setStart(start);
				}
				else if (key.equals("skin")) {
					record.setSkin(StringUtils.join(values.toArray()));
				}
				
			}
			String referer = getReferer();
			if (referer.length() > 10) {
				int pos = referer.indexOf("/", 8);
				if (pos > 0)
					referer = referer.substring(referer.indexOf("/", 8), referer.length());
			}
			record.setReferer(referer);
			record.setUrl(uri);
		}
		
		
		return record;
	}
	

	/**
	 * If this is a query (/ls), parse the current 
	 * record into a CombinedQuery object.
	 * @return
	 */
	public CombinedQuery getCombinedQuery() {
		
		if (!isQuery())
			return null;
		
		CombinedQuery query = new CombinedQuery();
		query.setIpaddr(getHost());
		query.setTimestamp(getTime());
		Pattern p = Pattern.compile("GET ([^\\s]*) HTTP.*");
		Matcher m = p.matcher(getRequest());
		if (m.matches()) {
			String uri = m.group(1);
			String path = uri.substring(uri.indexOf("?")+1, uri.length());

			path = path.replaceAll(";", "&");
			String[] nvpairs = path.split("&");
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			String yop = "";
			
			for (String nvpair: nvpairs) {
				if (!nvpair.contains("=")) 
					continue;
				String name = nvpair.substring(0, nvpair.indexOf("="));
				String value = nvpair.substring(nvpair.indexOf("=")+1, nvpair.length());
				if (!value.equals(""))
				{
					List<String> pvals = params.get(name);
					if (pvals == null) {
						pvals = new ArrayList<String>();
					}
					
					try {
						value = URLDecoder.decode(value, "UTF-8");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if (name.startsWith("facet") || name.equals("format"))
					{
						String[] fields = value.split(":");
						String facetName = fields[0];
						if (fields.length == 2) {
							String facetValue = fields[1];
							facetValue = facetValue.replaceAll("\"", "");
							pvals.add(facetValue);
							params.put(facetName,pvals);
						}
					}
					else if (name.equals("yop")) {
						yop = value;
					}
					else
					{
						pvals.add(value);
						params.put(name, pvals);
					}
				}
				
			}
			
			String pdateStart = "";
			if (params.get("pdate_start") != null) 
				pdateStart = params.get("pdate_start").get(0);
			String pdateEnd = "";
			if (params.get("pdate_end") != null) 
				pdateEnd = params.get("pdate_end").get(0);
			String pdate = "";
			if (params.get("pdate") != null)
				pdate = params.get("pdate").get(0);
			
			String trie = "";
			if (!yop.equals("")) {
				if (!pdateStart.equals("") && pdateEnd.equals("")) {
	            	// [+1900+TO+*+]
	            	trie = "[ " + pdateStart + " TO * ]";
	        	} else if (pdateStart.equals("") && !pdateEnd.equals("")) {
	            	// [+*+TO+1900+]
	            	trie = "[ * TO " + pdateEnd + " ]";
	            } else if (!pdateStart.equals("") && !pdateEnd.equals("")) {
	            	// [+1700+TO+1900+]
	            	trie = "[ " + pdateStart + " TO " + pdateEnd + " ]";
	            } 
				
	            if (!trie.equals("")) {
	            	List<String> pvals = params.get("publishDateTrie");
					if (pvals == null) {
						pvals = new ArrayList<String>();
					}
					pvals.add(trie);
					params.put("publishDateTrie", pvals);
	            }
	            
	            if (!pdate.equals("")) {
	            	List<String> pvals = params.get("publishDateRange");
					if (pvals == null) {
						pvals = new ArrayList<String>();
					}
					pvals.add(pdate);
					params.put("publishDateRange", pvals);
	            }
	        }
			if (params.get("pn") == null) {
				List<String> pvals = new ArrayList<String>();
				pvals.add("1");
				params.put("pn", pvals);
			}
			
			for (String key: params.keySet()) {
				List<String> values = params.get(key);
				
				if (values == null)
					continue;
				if (key.equals("q1")) {

					String q1 = values.get(0);
					q1 = q1.replaceAll("\\*", "");
					q1 = q1.replaceAll(":", "");
					
					int quotes = 0;
					int pos = 0;
					while ((pos = q1.indexOf('"', pos)) > 0) {
						pos++;
						quotes++;
					}
					if (quotes % 2 > 0) {
						q1 = q1.replaceAll("\"", "");
					}
					q1 = q1.replaceAll(" \\+ ", " ");
					q1 = q1.replaceAll("\\.\\.\\.", "");
					q1 = q1.replaceAll(" \\. ", "");
					q1 = q1.replaceAll(" \" ", " ");
					
					q1 = q1.replaceAll("&amp;", "&");
					q1 = q1.replaceAll("&", "");
					q1 = q1.replaceAll("^\" ", "");
					q1 = q1.replaceAll("/", "");
					q1 = q1.replaceAll("\\\\", "");
					q1 = q1.replaceAll("\\s+", " ");
					q1 = q1.replaceAll("\\?\"$", "");
					q1 = q1.replaceAll("#", "");
					q1 = q1.replaceAll("\"\"", "\"");
					q1 = q1.replaceAll(" and ", " =and= ");
					//q1 = q1.replaceAll(" AND ", " =and= ");
					q1 = q1.replaceAll(" or ", " =or= ");
					//q1 = q1.replaceAll(" OR ", " =or= ");
					query.setQ1(q1);
				}
				else if (key.equals("q2")) {
					query.setQ2(StringUtils.join(values.toArray()));
				}
				else if (key.equals("anyall1")) {
					query.setAnyall1(StringUtils.join(values.toArray()));
				} 
				else if (key.equals("anyall2")) {
					query.setAnyall2(StringUtils.join(values.toArray()));
				} 
				else if (key.equals("field1")) {
					query.setField1(StringUtils.join(values.toArray()));
				}
				else if (key.equals("field2")) {
					query.setField2(StringUtils.join(values.toArray()));
				}
				else if (key.equals("op2")) {
					query.setOp2(StringUtils.join(values.toArray()));
				}
				else if (key.equals("pn")) {
					try {
						query.setPn(Integer.valueOf(StringUtils.join(values.toArray())));
					} catch (Exception e) {}
				}
				else if (key.equals("lmt")) {
					query.setLmt(StringUtils.join(values.toArray()));
				}
				else if (key.equals("topicStr")) {
					String topic = StringUtils.join(values.toArray());
					topic = topic.replaceAll("\\\\", "");
					query.setTopicStr(topic);
				}
				else if (key.equals("authorStr")) {
					String author = StringUtils.join(values.toArray());
					author = author.replaceAll("\\\\", "");
					query.setAuthorStr(author);
				}
				else if (key.equals("language")) {
					String lang = StringUtils.join(values.toArray());
					lang = lang.replaceAll("\\\\", "");
					query.setLanguage(lang);
				}
				else if (key.equals("countryOfPubStr")) {
					String country = StringUtils.join(values.toArray());
					country = country.replaceAll("\\\\", "");
					query.setCountryOfPubStr(country);
				}
				else if (key.equals("publishDateRange")) {
					String pdateRange = StringUtils.join(values.toArray());
					pdateRange = pdateRange.replaceAll("\\\\", "");
					query.setPublishDateRange(pdateRange);
				}
				else if (key.equals("publishDateTrie")) {
					query.setPublishDateTrie(StringUtils.join(values.toArray()));
				}
				else if (key.equals("format")) {
					query.setFormat(StringUtils.join(values.toArray()));
				}
				else if (key.equals("htsource")) {
					query.setHtsource(StringUtils.join(values.toArray()));
				}
			}
			query.setUrl(uri);
			query.setReferer(getReferer());
		}
		
		if (query.getField1().equals("") || query.getField1().equals("ocronly"))
			query.setField1("ocr");
		if (query.getField2().equals("ocronly"))
			query.setField2("ocr");
		if (query.getLmt().equals(""))
			query.setLmt("all");
		if (query.getLmt().equals("so")) 
			query.setLmt("ft");
		if (query.getAnyall1().equals("phrase"))
			query.setQ1("\"" + query.getQ1() + "\"");
		if (query.getAnyall2().equals("phrase"))
			query.setQ2("\"" + query.getQ2() + "\"");
		if (query.getQ2().equals("")) {
			query.setField2("");
			query.setAnyall2("");
			query.setOp2("");
		}
		
		if (query.getQ1().equals("") && !query.getQ2().equals("")) {
			query.setQ1(query.getQ2());
			query.setQ2("");
			query.setField1(query.getField2());
			query.setAnyall1(query.getAnyall2());
			query.setField2("");
			query.setOp2("");
			query.setAnyall2("");
		}
		return query;
	}
	
	public boolean isFullText() {
		if (getRequest().contains("/pt?"))
			return true;
		else
			return false;
	}
	
	public boolean isQuery() {
		if (getRequest().contains("/ls?"))
			return true;
		else
			return false;
	}
	
}
