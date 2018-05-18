package com.seleniumtests.driver;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class Keyboard {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(Keyboard.class);
	
	private Map<Character,KeyStroke> strokeUsMap = new HashMap<Character,KeyStroke>(){
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
		    for(int i=(int)'0';i<=(int)'9';i++){
		        put((char)i,new KeyStroke(i, false));
		    }
		    put(':',new KeyStroke(KeyEvent.VK_SEMICOLON, true));
		    put(';',new KeyStroke(KeyEvent.VK_SEMICOLON, false));
		    put('<',new KeyStroke(KeyEvent.VK_COMMA, true));
		    put('=',new KeyStroke(KeyEvent.VK_EQUALS, false));
		    put('>',new KeyStroke(KeyEvent.VK_PERIOD, true));
		    put('?',new KeyStroke(KeyEvent.VK_SLASH, true));
		    put('@',new KeyStroke(KeyEvent.VK_2, true));
		    for(int i=(int)'A';i<=(int)'Z';i++){
		        put((char)i,new KeyStroke(i, true));
		    }
		    put('[',new KeyStroke(KeyEvent.VK_OPEN_BRACKET, false));
		    put('\\',new KeyStroke(KeyEvent.VK_BACK_SLASH, false));
		    put(']',new KeyStroke(KeyEvent.VK_CLOSE_BRACKET, false));
		    put('^',new KeyStroke(KeyEvent.VK_6, true));
		    put('_',new KeyStroke(KeyEvent.VK_MINUS, true));
		    put('`',new KeyStroke(KeyEvent.VK_BACK_QUOTE, false));
		    for(int i=(int)'A';i<=(int)'Z';i++){
		        put((char)(i+((int)'a'-(int)'A')),new KeyStroke(i, false));
		    }
		    put('{',new KeyStroke(KeyEvent.VK_OPEN_BRACKET, true));
		    put('|',new KeyStroke(KeyEvent.VK_BACK_SLASH, true));
		    put('}',new KeyStroke(KeyEvent.VK_CLOSE_BRACKET, true));
		    put('~',new KeyStroke(KeyEvent.VK_BACK_QUOTE, true));
		}};
        
    private Map<Character,KeyStroke> strokeFrMap = new HashMap<Character,KeyStroke>(){
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
    		//put('%',new KeyStroke(KeyEvent.VK_5, true));
    		put('&',new KeyStroke(KeyEvent.VK_1, false));
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
    		for(int i=(int)'0';i<=(int)'9';i++){
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
    		for(int i=(int)'A';i<=(int)'Z';i++){
    			put((char)i,new KeyStroke(i, true));
    		}
    		put('[',new KeyStroke(KeyEvent.VK_5, false, true));
    		put('\\',new KeyStroke(KeyEvent.VK_8, false, true));
    		put(']',new KeyStroke(KeyEvent.VK_RIGHT_PARENTHESIS, false, true));
//    		put('^',new KeyStroke(KeyEvent.VK_CIRCUMFLEX, false));
    		put('¨',new KeyStroke(KeyEvent.VK_CIRCUMFLEX, true));
    		put('_',new KeyStroke(KeyEvent.VK_8, false));
    		put('`',new KeyStroke(KeyEvent.VK_7, false, true));
    		for(int i=(int)'A';i<=(int)'Z';i++){
    			put((char)(i+((int)'a'-(int)'A')),new KeyStroke(i, false));
    		}
    		put('{',new KeyStroke(KeyEvent.VK_4, false, true));
    		put('|',new KeyStroke(KeyEvent.VK_6, false, true));
    		put('}',new KeyStroke(KeyEvent.VK_EQUALS, false, true));
    		put('~',new KeyStroke(KeyEvent.VK_2, false, true));
    	}};
    	
	private Map<Character,KeyStroke> strokeMap;
    
    private Robot robot;
    public Keyboard() throws AWTException{
        robot = new Robot();
        
        // TODO: detect keyboard layout
        strokeMap = strokeFrMap;
        
    }
    
    public void typeKey(char key){
        try{
        	strokeMap.get(key).type();
        }catch(NullPointerException ex){
            logger.error("'"+key+"': no such key in mappings");
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
        int code;
        boolean isShifted;
        boolean isAltGr;
        
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
        public void type(){
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
                String ch="";
                for(char key:strokeMap.keySet()){
                    if(strokeMap.get(key)==this){
                        ch=""+key;
                        break;
                    }
                }
                logger.error("Key Code Not Recognized: '"+ch+"'->"+code);
            }
        }
    }

	public Robot getRobot() {
		return robot;
	}

//    private static AWTKeyStroke getKeyStroke(char c) {
////        String upper = "`~'\"!@#$%^&*()_+{}|:<>?";
////        String lower = "`~'\"1234567890-=[]\\;,./";
//
//        String upper = "1234567890°+¨£%µ?./§>";
//        String lower = "&é\"'(-è_çà)=^$ù*,;:!<";
//        String altgr = "&~#{[|`\\^@]} ¤ù*,;:!<";
//        
//        int index = upper.indexOf(c);
//        if (index != -1) {
//            int keyCode;
//            boolean shift = false;
//            switch (c) {
//                // these chars need to be handled specially because
//                // they don't directly translate into the correct keycode
//                case '~':
//                    shift = true;
//                case '`':
//                    keyCode = KeyEvent.VK_BACK_QUOTE;
//                    break;
//                case '\"':
//                    shift = true;
//                case '\'':
//                    keyCode = KeyEvent.VK_QUOTE;
//                    break;
//                default:
//                    keyCode = (int) Character.toUpperCase(lower.charAt(index));
//                    shift = true;
//            }
//            return getAWTKeyStroke(keyCode, shift ? SHIFT_DOWN_MASK : 0);
//        }
//        return getAWTKeyStroke((int) Character.toUpperCase(c), 0);
//    }
//
//    public void type(CharSequence chars) {
//        type(chars, 0);
//    }
//
//    public void type(CharSequence chars, int ms) {
//        ms = ms > 0 ? ms : 0;
//        for (int i = 0, len = chars.length(); i < len; i++) {
//            char c = chars.charAt(i);
//            AWTKeyStroke keyStroke = getKeyStroke(c);
//            int keyCode = keyStroke.getKeyCode();
//            boolean shift = Character.isUpperCase(c) || keyStroke.getModifiers() == (SHIFT_DOWN_MASK + 1);
//            
//            if (ms > 0) {
//                robot.delay(ms);
//                robot.keyPress(KeyEvent.VK_SHIFT);
//                robot.keyRelease(KeyEvent.VK_SHIFT);
//            }
//            System.out.println(keyCode + "-" + c);
//            pressKeys(keyCode, shift);
//            
//        }
//    }
//    
//    private void pressKeys(int keyCode, boolean shift) {
//    	if (shift) {
//            robot.keyPress(KeyEvent.VK_SHIFT);
//        }
//
//        robot.keyPress(keyCode);
//        robot.keyRelease(keyCode);
//
//        if (shift) {
//            robot.keyRelease(KeyEvent.VK_SHIFT);
//        }
//        
//    }
}