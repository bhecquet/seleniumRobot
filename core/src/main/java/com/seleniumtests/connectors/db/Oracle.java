/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.connectors.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.Platform;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.osutility.OSUtility;

import oracle.jdbc.OracleDriver;

/**
 * Class for accessing Oracle DB
 * tnsnames.ora file is used to get the connection string
 * Path to tnsnames.ora file is get from variable "tnsnamePath" set in env.ini file
 *
 */
public class Oracle {
	
	private String dbName;
	private String user;
	private String password;
	private String host;
	private String port;
	
	/**
	 * Constructor
	 * 
	 * @param dbName		DB name as stated in tnsnames.ora
	 * @param user			user to connect to database
	 * @param password		password to connect to database
	 * @param tnsNamePath	path to folder where tnsnames.ora file is located
	 */
    public Oracle(String dbName, String user, String password, String tnsNamePath) {
    	
    	this.user = user;
    	this.password = password;
    	this.dbName = dbName;

    	// check tnsname.ora path
    	if (tnsNamePath == null) {
    		throw new ConfigurationException("'tnsnamePath' configuration is null, it must be the path to folder where tnsnames.ora file is");
    	}
    	if (!new File(tnsNamePath).isDirectory()) {
    		throw new ConfigurationException("Folder " + tnsNamePath +  " does not exist, check your configuration in env.ini");
    	}
    	if (!new File(tnsNamePath + File.separator + "tnsnames.ora").isFile()) {
    		throw new ConfigurationException("File " + tnsNamePath + " tnsnames.ora does not exist, check your configuration in env.ini");
    	}
    	
        System.setProperty("oracle.net.tns_admin", tnsNamePath);

    }
    
    /**
     * Configure database using tnsnamePath configuration found in env.ini or variable server
     * @param dbName
     * @param user
     * @param password
     */
    @Deprecated
    public Oracle(String dbName, String user, String password) {
    	this(dbName, user, password, SeleniumTestsContextManager.getThreadContext().getConfiguration().get("tnsnamePath").getValue());
    }
    
    public Oracle(String dbName, String host, String port, String user, String password) {
    	this.user = user;
    	this.password = password;
    	this.dbName = dbName;
    	this.host = host;
    	this.port = port;
    }
    
    /**
     * Search Oracle client on test server
     * Steps are:
     * - use ORACLE_HOME env variable if it exists
     * - use C:\Oracle folder on windows
     *  
     * @return	root path of Oracle folder or null if none found
     */
    public static String findOracleClient() {
    	String oracleEnv = System.getenv("ORACLE_HOME");
    	if (oracleEnv != null) {
    		return oracleEnv;
    	}
    	
    	if (OSUtility.getCurrentPlatorm() == Platform.WINDOWS && new File("C:\\oracle").isDirectory()) {
    		for (File orCli: new File("C:\\oracle").listFiles()) {
    			if (new File(orCli.getAbsolutePath() + File.separator + "NETWORK\\ADMIN\\sqlnet.ora").isFile()) {
    				return orCli.getAbsolutePath();
    			}
    		}
    	}
    	return null;
    }
    
    /**
     * Connect to database
     * 
     * @return the connection
     * @throws SQLException
     */
    private Connection connect() throws SQLException {
    	String url;
    	if (host != null) {
    		url = String.format("jdbc:oracle:thin:@%s:%s:%s" ,host, port, dbName);
    	} else {
    		url = "jdbc:oracle:thin:@" + dbName;
    	}
    	DriverManager.registerDriver(new OracleDriver());
        return DriverManager.getConnection(url, user, password);
    }
    
    private void disconnect(Connection connection) throws SQLException {
    	connection.close();
    }
    
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
    

}