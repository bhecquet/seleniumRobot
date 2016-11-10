package com.seleniumtests.uipage;

/**
 * This annotation is used in ReplayAction.aj so that any HtmlElement method annotated by ReplayOnError
 * will be replayed during 30 secs if any error occurs
 * Moreover, ReplayAction is in charge of entering iframe when action on element is performed
 * e.g: clicking on an element raises an error, seleniumRobot will retry several times until really failing
 *      or until click is successful
 *      
 * Do not annotate all HtmlElement methods because ReplayAction would be called several times for the same action
 * e.g: use HtmlElement.isTextPresent() on an element existing in an iframe
 *      HtmlElement.isTextPresent() calls HtmlElement.getText() and assume both are annotated
 *      When calling isTextPresent(), ReplayAction.aj would be activated one time, entering iframes
 *      As isTextPresent() calls getText(), seleniumRobot tries to enter iframes and fails because driver is already in iframe
 *      
 * Only methods calling findElement or any of the similar methods should be annotated because these ones will 
 * act directly on element
 * @author behe
 *
 */
public @interface ReplayOnError {

}
