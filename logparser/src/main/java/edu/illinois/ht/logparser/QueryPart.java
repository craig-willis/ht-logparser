package edu.illinois.ht.logparser;

public class QueryPart {

	String field = "";
	String query = "";
	String op = "";
	
	public QueryPart(String field, String query, String op) {
		this.field = field;
		this.query = query;
		this.op = op;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}
	
	public String toString() {
		return field + "," + query + "," + op;
	}
	
}
