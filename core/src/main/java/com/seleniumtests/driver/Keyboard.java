package com.seleniumtests.driver;

import java.awt.AWTException;
import java.awt.AWTKeyStroke;
import static java.awt.AWTKeyStroke.getAWTKeyStroke;
import java.awt.Robot;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import java.awt.event.KeyEvent;

public class Keyboard {

    private final Robot robot;

    public Keyboard() throws AWTException {
        this.robot = new Robot();
    }

    private static AWTKeyStroke getKeyStroke(char c) {
        String upper = "`~'\"!@#$%^&*()_+{}|:<>?";
        String lower = "`~'\"1234567890-=[]\\;,./";

        int index = upper.indexOf(c);
        if (index != -1) {
            int keyCode;
            boolean shift = false;
            switch (c) {
                // these chars need to be handled specially because
                // they don't directly translate into the correct keycode
                case '~':
                    shift = true;
                case '`':
                    keyCode = KeyEvent.VK_BACK_QUOTE;
                    break;
                case '\"':
                    shift = true;
                case '\'':
                    keyCode = KeyEvent.VK_QUOTE;
                    break;
                default:
                    keyCode = (int) Character.toUpperCase(lower.charAt(index));
                    shift = true;
            }
            return getAWTKeyStroke(keyCode, shift ? SHIFT_DOWN_MASK : 0);
        }
        return getAWTKeyStroke((int) Character.toUpperCase(c), 0);
    }

    public void type(CharSequence chars) {
        type(chars, 0);
    }

    public void type(CharSequence chars, int ms) {
        ms = ms > 0 ? ms : 0;
        for (int i = 0, len = chars.length(); i < len; i++) {
            char c = chars.charAt(i);
            AWTKeyStroke keyStroke = getKeyStroke(c);
            int keyCode = keyStroke.getKeyCode();
            boolean shift = Character.isUpperCase(c) || keyStroke.getModifiers() == (SHIFT_DOWN_MASK + 1);
            
            if (ms > 0) {
                robot.delay(ms);
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            }
            
            pressKeys(keyCode, shift);
            
        }
    }
    
    private void pressKeys(int keyCode, boolean shift) {
    	if (shift) {
            robot.keyPress(KeyEvent.VK_SHIFT);
        }

        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);

        if (shift) {
            robot.keyRelease(KeyEvent.VK_SHIFT);
        }
        
    }
}