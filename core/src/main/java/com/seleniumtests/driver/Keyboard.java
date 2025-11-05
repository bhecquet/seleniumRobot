/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.driver;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.im.InputContext;
import java.io.Serial;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.osutility.OSUtility;
import org.apache.logging.log4j.Logger;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class Keyboard {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(Keyboard.class);
	
	private Map<Character,KeyStroke> strokeUsMap = new HashMap<Character,KeyStroke>(){
        @Serial
		private static final long serialVersionUID = 1L;{
		    put('\n',new KeyStroke(KeyEvent.VK_ENTER, false));
		    put('\t',new KeyStroke(KeyEvent.VK_TAB, false));
		    put('\r',new KeyStroke(KeyEvent.VK_HOME, false));
		    put(' ',new KeyStroke(KeyEvent.VK_SPACE, false));
		    put('!',new KeyStroke(KeyEvent.VK_1, true));
		    put('"',new KeyStroke(KeyEvent.VK_QUOTE, true));
		    put('#',new KeyStroke(KeyEvent.VK_3, true));
		    put('$',new KeyStroke(KeyEvent.VK_4, true));
		    put('%',new KeyStroke(KeyEvent.VK_5, true));
		    put('&',new KeyStroke(KeyEvent.VK_7, true));
		    put('\'',new KeyStroke(KeyEvent.VK_QUOTE, false));
		    put('(',new KeyStroke(KeyEvent.VK_9, true));
		    put(')',new KeyStroke(KeyEvent.VK_0, true));
		    put('*',new KeyStroke(KeyEvent.VK_8, true));
		    put('+',new KeyStroke(KeyEvent.VK_EQUALS, true));
		    put(',',new KeyStroke(KeyEvent.VK_COMMA, false));
		    put('-',new KeyStroke(KeyEvent.VK_MINUS, false));
		    put('.',new KeyStroke(KeyEvent.VK_PERIOD, false));
		    put('/',new KeyStroke(KeyEvent.VK_SLASH, false));
		    for(int i='0';i<='9';i++){
		        put((char)i,new KeyStroke(i, false));
		    }
		    put(':',new KeyStroke(KeyEvent.VK_SEMICOLON, true));
		    put(';',new KeyStroke(KeyEvent.VK_SEMICOLON, false));
		    put('<',new KeyStroke(KeyEvent.VK_COMMA, true));
		    put('=',new KeyStroke(KeyEvent.VK_EQUALS, false));
		    put('>',new KeyStroke(KeyEvent.VK_PERIOD, true));
		    put('?',new KeyStroke(KeyEvent.VK_SLASH, true));
		    put('@',new KeyStroke(KeyEvent.VK_2, true));
		    for(int i='A';i<='Z';i++){
		        put((char)i,new KeyStroke(i, true));
		    }
		    put('[',new KeyStroke(KeyEvent.VK_OPEN_BRACKET, false));
		    put('\\',new KeyStroke(KeyEvent.VK_BACK_SLASH, false));
		    put(']',new KeyStroke(KeyEvent.VK_CLOSE_BRACKET, false));
		    put('^',new KeyStroke(KeyEvent.VK_6, true));
		    put('_',new KeyStroke(KeyEvent.VK_MINUS, true));
		    put('`',new KeyStroke(KeyEvent.VK_BACK_QUOTE, false));
		    for(int i='A';i<='Z';i++){
		        put((char)(i+('a'-'A')),new KeyStroke(i, false));
		    }
		    put('{',new KeyStroke(KeyEvent.VK_OPEN_BRACKET, true));
		    put('|',new KeyStroke(KeyEvent.VK_BACK_SLASH, true));
		    put('}',new KeyStroke(KeyEvent.VK_CLOSE_BRACKET, true));
		    put('~',new KeyStroke(KeyEvent.VK_BACK_QUOTE, true));
		}};
        
    private Map<Character,KeyStroke> strokeFrMap = new HashMap<Character,KeyStroke>(){
    	@Serial
		private static final long serialVersionUID = 1L;{
    		put('\n',new KeyStroke(KeyEvent.VK_ENTER, false));
    		put('\t',new KeyStroke(KeyEvent.VK_TAB, false));
    		put('\r',new KeyStroke(KeyEvent.VK_HOME, false));
    		put(' ',new KeyStroke(KeyEvent.VK_SPACE, false));
    		put('!',new KeyStroke(KeyEvent.VK_EXCLAMATION_MARK, false));
    		put('§',new KeyStroke(KeyEvent.VK_EXCLAMATION_MARK, true));
    		put('"',new KeyStroke(KeyEvent.VK_3, false));
    		put('#',new KeyStroke(KeyEvent.VK_3, false, true));
    		put('$',new KeyStroke(KeyEvent.VK_DOLLAR, false));
    		put('¤',new KeyStroke(KeyEvent.VK_DOLLAR, false, true));
    		put('£',new KeyStroke(KeyEvent.VK_DOLLAR, true));
    		put('&',new KeyStroke(KeyEvent.VK_1, false));
    		put('%',new KeyStroke("37"));
    		put('é',new KeyStroke(KeyEvent.VK_2, false));
    		put('è',new KeyStroke(KeyEvent.VK_7, false));
    		put('ç',new KeyStroke(KeyEvent.VK_9, false));
    		put('^',new KeyStroke(KeyEvent.VK_9, false, true));
    		put('à',new KeyStroke(KeyEvent.VK_0, false));
    		put('\'',new KeyStroke(KeyEvent.VK_4, false));
    		put('(',new KeyStroke(KeyEvent.VK_5, false));
    		put(')',new KeyStroke(KeyEvent.VK_RIGHT_PARENTHESIS, false));
    		put('°',new KeyStroke(KeyEvent.VK_RIGHT_PARENTHESIS, true));
    		put('*',new KeyStroke(KeyEvent.VK_MULTIPLY, true));
    		put('µ',new KeyStroke(KeyEvent.VK_ASTERISK, true));
    		put('+',new KeyStroke(KeyEvent.VK_EQUALS, true));
    		put(',',new KeyStroke(KeyEvent.VK_COMMA, false));
    		put('-',new KeyStroke(KeyEvent.VK_6, false));
    		put('.',new KeyStroke(KeyEvent.VK_SEMICOLON, true));
    		put('/',new KeyStroke(KeyEvent.VK_COLON, true));
    		for(int i='0';i<='9';i++){
    			put((char)i,new KeyStroke(i, true));
    		}        		
    		put('€',new KeyStroke(KeyEvent.VK_E, false, true));
    		put(':',new KeyStroke(KeyEvent.VK_COLON, false));
    		put(';',new KeyStroke(KeyEvent.VK_SEMICOLON, false));
    		put('<',new KeyStroke(KeyEvent.VK_LESS, false));
    		put('=',new KeyStroke(KeyEvent.VK_EQUALS, false));
    		put('>',new KeyStroke(KeyEvent.VK_LESS, true));
    		put('?',new KeyStroke(KeyEvent.VK_COMMA, true));
    		put('@',new KeyStroke(KeyEvent.VK_0, false, true));
    		for(int i='A';i<='Z';i++){
    			put((char)i,new KeyStroke(i, true));
    		}
    		put('[',new KeyStroke(KeyEvent.VK_5, false, true));
    		put('\\',new KeyStroke(KeyEvent.VK_8, false, true));
    		put(']',new KeyStroke(KeyEvent.VK_RIGHT_PARENTHESIS, false, true));
    		put('¨',new KeyStroke(KeyEvent.VK_CIRCUMFLEX, true));
    		put('_',new KeyStroke(KeyEvent.VK_8, false));
    		put('`',new KeyStroke(KeyEvent.VK_7, false, true));
    		for(int i='A';i<='Z';i++){
    			put((char)(i+('a'-'A')),new KeyStroke(i, false));
    		}
    		put('{',new KeyStroke(KeyEvent.VK_4, false, true));
    		put('|',new KeyStroke(KeyEvent.VK_6, false, true));
    		put('}',new KeyStroke(KeyEvent.VK_EQUALS, false, true));
    		put('~',new KeyStroke(KeyEvent.VK_2, false, true));
    	}};
    	
	private Map<Character,KeyStroke> strokeMap;
    
    private final Robot robot;
    public Keyboard() throws AWTException{
        robot = new Robot();
        
        InputContext context = InputContext.getInstance(); 
        String language = context.getLocale().getLanguage();
        if (language.equals(new Locale("en").getLanguage())) {
        	strokeMap = strokeUsMap;
        } else if (language.equals(new Locale("fr").getLanguage())) {
        	strokeMap = strokeFrMap;
        } else {
        	throw new ConfigurationException("only french and english keyboards are supported");
        }
		
        
    }
    
    public void typeKey(char key){
        try{
        	strokeMap.get(key).type();
        }catch(NullPointerException ex){
            logger.error("'{}': no such key in mappings", key);
        }
    }
    
    public void typeKeys(final String text) {
	    if (text != null) {
            for (int i = 0; i < text.length(); i++) {
            	typeKey(text.charAt(i));
                robot.delay(50);
            }
	    }
    }
    
    private class KeyStroke{
        Integer code;
        boolean isShifted;
        boolean isAltGr;
		String asciiCode;

		public KeyStroke(String asciiCode) {
			this.asciiCode = asciiCode;
		}

        public KeyStroke(int keyCode,boolean shift){
            code=keyCode;
            isShifted=shift;
            isAltGr = false;
        }
        public KeyStroke(int keyCode,boolean shift, boolean altGr){
        	code=keyCode;
        	isShifted=shift;
        	isAltGr = altGr;
        }

		public void type() {
			if (asciiCode == null) {
				typeKeyEvent();
			} else {
				typeAsciiCode();
			}
		}

		private void typeAsciiCode() {

			if (OSUtility.isWindows()) {
				robot.keyPress(KeyEvent.VK_ALT);
				for (int i = 0; i < asciiCode.length(); i++) {
					int keyEvent = asciiCode.charAt(i) + 48;
					logger.info(keyEvent);
					robot.keyPress(keyEvent);
					robot.keyRelease(keyEvent);
				}
				robot.keyRelease(KeyEvent.VK_ALT);
			} else {
				throw new ScenarioException("Only Windows is supported for 'typeAsciiCode'");
			}
		}

        private void typeKeyEvent() {
            try{
                if (isShifted) {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                }
                if (isAltGr) {
                	robot.keyPress(KeyEvent.VK_ALT);
                	robot.keyPress(KeyEvent.VK_CONTROL);
                }
                robot.keyPress(code);
                robot.keyRelease(code);
                if (isShifted) {
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                }
                if (isAltGr) {
                	robot.keyRelease(KeyEvent.VK_ALT);
                	robot.keyRelease(KeyEvent.VK_CONTROL);
                }
                if(code==KeyEvent.VK_ENTER){
                    robot.keyPress(KeyEvent.VK_HOME);
                    robot.keyRelease(KeyEvent.VK_HOME);
                }

            }catch(IllegalArgumentException ex){
                String ch = "";
                for (Entry<Character, KeyStroke> entry: strokeMap.entrySet()) {
                    if(entry.getValue() == this) {
                        ch = "" + entry.getKey();
                        break;
                    }
                }
                logger.error("Key Code Not Recognized: '{}'->{}", ch, code);
            }
        }
    }

	public Robot getRobot() {
		return robot;
	}
}