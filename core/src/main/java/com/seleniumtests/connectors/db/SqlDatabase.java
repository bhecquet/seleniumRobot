package com.seleniumtests.connectors.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class SqlDatabase {


	protected  abstract Connection connect() throws SQLException;
	
    /**
     * connect to database, execute query and disconnect
     * 
     * @param query		query to execute on database
     * @return 			List of search results
     */
    public List<List<String>> executeQuery(String query) throws SQLException {
    	
    	List<List<String>> result = new ArrayList<>();

    	Connection connection = connect();
    	try (Statement stmt = connection.createStatement();
    		 ResultSet rs = stmt.executeQuery(query);
    			) {
	    	
	        
	        result = new ArrayList<>();
	        
        	while (rs.next()) {
	        	List<String> row = new ArrayList<>();
	        	for (int i=1; i < rs.getMetaData().getColumnCount() + 1; i++) {
	        		row.add(rs.getString(i));
	        	}
				result.add(row);
        	}
    	} catch (SQLException e) {
    		if (!e.getMessage().contains("next")) {
    			throw e;
    		}
    	} 
        
        return result;
    }
    
    private List<HashMap<String, String>> readRows(ResultSet rs) throws SQLException {
    	List<HashMap<String, String>> result = new ArrayList<>();
    	while (rs.next()) {
			HashMap<String, String> row = new HashMap<>();
			for (int j=1; j < rs.getMetaData().getColumnCount() + 1; j++) {
				row.put(rs.getMetaData().getColumnName(j), rs.getString(j));
			}
			result.add(row);
		}
    	return result;
    }
    
    /**
     * connect to database, execute query and disconnect
     * 
     * @param query		query to execute on database with '?' replacing values. Values are given in params
     * @param params	parameters to the query
     * @return 			List of search results
     */
    public List<HashMap<String, String>> executeParamQuery(String query, Object...params) throws SQLException {
    	

    	List<HashMap<String, String>> result = new ArrayList<>();

    	Connection connection = connect();
    	
    	try (PreparedStatement pstmt = connection.prepareStatement(query);){
    		int i = 1;
    		for (Object param: params) {
    			pstmt.setObject(i, param);
    			i += 1;
    		}
    		
    		try (ResultSet rs = pstmt.executeQuery();) {
	    		if (!query.toLowerCase().startsWith("update") && !query.toLowerCase().startsWith("delete")) {
	    			result = readRows(rs);
	    		}
    		}
    		
    	} catch (SQLException e) {
    		if (!e.getMessage().contains("next")) {
    			throw e;
    		}
    	} 
    	return result;
    }
    
    
    protected void disconnect(Connection connection) throws SQLException {
    	connection.close();
    }
}
