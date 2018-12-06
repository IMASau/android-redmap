package au.com.ionata.redmap.data.tasks;

import java.util.ArrayList;

public class QueryContract<T> {

	public QueryResultStatus status;
	public ArrayList<T> results = null;
	
	public QueryContract(ArrayList<T> results, QueryResultStatus status) {
		this.status = status;
		this.results = results;
    }
	
}
