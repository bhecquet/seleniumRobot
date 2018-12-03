/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
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
package com.seleniumtests.core;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.seleniumtests.customexception.DatasetException;

/**
 * Provides mechanism to retrieve records from spreadsheets.
 */
public class Filter {

	private String name;

    private Object[] values;

    private Operator operator;

    private Filter left;

    private Filter right;
	
    enum Operator {
        EQUALS,
        EQUALS_IGNORE_CASE,
        LESS_THAN,
        GREATER_THAN,
        BETWEEN,
        IN,
        IS_NULL,
        NOT,
        CONTAINS,
        CONTAINS_IGNORE_CASE,
        STARTS_WITH,
        STARTS_WITH_IGNORE_CASE,
        ENDS_WITH,
        ENDS_WITH_IGNORE_CASE,
        OR,
        AND;
    }
    
    private Filter(final Filter left, final Filter right, final Operator condition) {
        this.left = left;
        this.right = right;
        this.operator = condition;
    }

    public Filter(final String name, final Object value, final Operator condition) {
        this(name, new Object[] {value}, condition);
    }

    public Filter(final String name, final Object[] values, final Operator condition) {
        super();
        this.name = name;
        this.values = values;
        this.operator = condition;
    }

    public static Filter and(final Filter left, final Filter right) {
        return new Filter(left, right, Operator.AND);
    }

    public static Filter contains(final String name, final String value) {
        return new Filter(name, value, Operator.CONTAINS);
    }

    public static Filter containsIgnoreCase(final String name, final String value) {
        return new Filter(name, value, Operator.CONTAINS_IGNORE_CASE);
    }

    public static Filter isEqual(final String name, final Object value) {
        return new Filter(name, value, Operator.EQUALS);
    }

    public static Filter isEqualIgnoreCase(final String name, final String value) {
        return new Filter(name, value, Operator.EQUALS_IGNORE_CASE);
    }

    public static Filter greaterThan(final String name, final Number value) {
        return new Filter(name, value, Operator.GREATER_THAN);
    }

    public static Filter in(final String name, final Object[] values) {
        return new Filter(name, values, Operator.IN);
    }

    public static Filter lt(final String name, final Date value) {
        return new Filter(name, value, Operator.LESS_THAN);
    }

    public static Filter lt(final String name, final Number value) {
        return new Filter(name, value, Operator.LESS_THAN);
    }
    
    public static Filter not(final Filter exp) {
        return new Filter((Filter) null, exp, Operator.NOT);
    }

    public static Filter or(final Filter left, final Filter right) {
        return new Filter(left, right, Operator.OR);
    }
    
    public boolean match(final Map<String, Object> parameters) {
        Map<String, Object> parameters2 = new HashMap<>();
        for (Entry<String, Object> entry : parameters.entrySet()) {
            parameters2.put(entry.getKey().toUpperCase(), entry.getValue());
        }

        return match(this, parameters2);
    }

    /**
     * Test if the given filter's value matches the given parameters'. 
     * @param filter
     * @param parameters
     * @return boolean
     */
    private static boolean match(final Filter filter, final Map<String, Object> parameters) {
        String name = filter.name != null ? filter.name.toUpperCase() : null;
        Object[] values = filter.values;
        Operator operator = filter.operator;
        Filter left = filter.left;
        Filter right = filter.right;

        switch (operator) {

        	case AND :
        		return left.match(parameters) && right.match(parameters);
        		
        	case OR : 
        		return left.match(parameters) || right.match(parameters);
        		
        	case NOT :
        		return !right.match(parameters);
        		
        	default :
                break;
        }
        
        if (!parameters.containsKey(name)) {
            return false;
        } 	
        
        switch (operator) {
        
        	case IS_NULL :
        		return parameters.get(name) == null;
        		
        	case EQUALS :
        		if (values == null || (values.length == 1 && values[0] == null))
        			return parameters.get(name) == null;
        		else 
        			return parameters.get(name).equals(values[0]);
        		
        	case EQUALS_IGNORE_CASE :
        		if (values == null || (values.length == 1 && values[0] == null))
        			return parameters.get(name) == null;
        		else
        			return parameters.get(name).toString().equalsIgnoreCase(values[0].toString());
        
        	case IN :
                for (Object value : values) {
                    if (parameters.get(name).equals(value))
                        return true;
                }
                return false;
        		
        	default :
                break;
        }
        
        if (values == null || values[0] == null) {
            throw new DatasetException("Filter Operation does not support Null values: " + operator);
        } 
        
        if (values[0] instanceof String) {
        	return stringFilterCase(name, values, operator, parameters);
        }
        	
        if (values[0] instanceof Number) {
        	return numberFilterCase(name, values, operator, parameters);
        }
                
        if (values[0] instanceof Date) {
        	return dateFilterCase(name, values, operator, parameters);
        }

        throw new DatasetException("Filter NOT Implemented Yet" + "\n" + filter + "\n" + parameters);
    }
    
    private static boolean stringFilterCase(String name, Object[] values, Operator operator, 
											final Map<String, Object> parameters)
    {
    	switch (operator) {
    	
        case CONTAINS :
            return parameters.get(name).toString().contains(values[0].toString());

        case CONTAINS_IGNORE_CASE :
            return parameters.get(name).toString().toLowerCase().contains(values[0].toString()
                        .toLowerCase());

        case STARTS_WITH :
            return parameters.get(name).toString().startsWith(values[0].toString());

        case STARTS_WITH_IGNORE_CASE :
            return parameters.get(name).toString().toLowerCase().startsWith(values[0].toString()
                        .toLowerCase());

        case ENDS_WITH :
            return parameters.get(name).toString().endsWith(values[0].toString());

        case ENDS_WITH_IGNORE_CASE :
            return parameters.get(name).toString().toLowerCase().endsWith(values[0].toString()
                        .toLowerCase());
            
        default :
        	throw new DatasetException(name + "filter NOT implemented yet for strings.");
    }
    }
    
    private static boolean numberFilterCase(String name, Object[] values, Operator operator, 
												final Map<String, Object> parameters)
    {
    	BigDecimal val = new BigDecimal(parameters.get(name).toString());
        BigDecimal leftValue = new BigDecimal(values[0].toString());
        BigDecimal rightValue;
        
        switch (operator) {

            case BETWEEN :
                rightValue = new BigDecimal(values[1].toString());
                return leftValue.compareTo(val) < 1 && rightValue.compareTo(val) > -1;

            case LESS_THAN :
                return val.compareTo(leftValue) < 0;

            case GREATER_THAN :
                return val.compareTo(leftValue) > 0;
        
            default :
            	throw new DatasetException(name + "filter NOT implemented yet for numbers.");
        }
    }

    private static boolean dateFilterCase(String name, Object[] values, Operator operator, 
    										final Map<String, Object> parameters)
    {
    	Date date;
        try {
            date = DateFormat.getDateInstance().parse(parameters.get(name).toString());
        } catch (ParseException e) {
            date = (Date) parameters.get(name);
        }
        Date dateLeft = (Date) values[0];
        Date dateRight;

        switch (operator) {
        
            case BETWEEN :
                dateRight = (Date) values[1];
                return (dateLeft.before(date) || dateLeft.equals(date))
                        && (date.before(dateRight) || date.equals(dateRight));

            case LESS_THAN :
                return date.before(dateLeft);

            case GREATER_THAN :
                return date.after(dateLeft);

            default :
            	throw new DatasetException(name + "filter NOT implemented yet for dates.");
        }
    }
    
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(name + " " + operator.toString() + " " + Arrays.toString(values));
        } else {
            sb.append((left != null ? left.toString() : "") + " " + operator.toString() + " " + right.toString());
        }
        return "(" + sb.toString() + ")";
    }

}
