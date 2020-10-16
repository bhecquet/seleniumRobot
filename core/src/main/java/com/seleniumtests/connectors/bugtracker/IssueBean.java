package com.seleniumtests.connectors.bugtracker;

import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.TestStep;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores information about an issue to be created
 */
public class IssueBean {

    private List<ScreenShot> screenShots;
    private String id;
    private String reporter;
    private String testName;
    private String issueType;
    private TestStep testStep;
    private String summary;
    private ZonedDateTime date;
    private String assignee;
    private String description;
    private String priority;
    Map<String, String> fields = new HashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmZ");


    private File detailedResult;

    /**
     * For test only!
     * @param summary
     * @param description
     */
    public IssueBean(
    		String id,
    		String summary,
    		String description
    		) {
    	this(id, summary, description, "", "", null, "", "", new ArrayList<>(), null, new HashMap<>());
    }
    
    /**
     * 
     * @param summary
     * @param description
     * @param priority
     * @param testName
     * @param testStep
     * @param assignee
     * @param reporter
     * @param screenshots
     * @param detailedResultFile
     * @param fields				All custom fields that should be filled
     */
    public IssueBean(
    		String summary,
    		String description,
    		String priority,
    		String testName,
    		TestStep testStep,
    		String assignee,
    		String reporter,
    		List<ScreenShot> screenshots,
    		File detailedResultFile,
    		Map<String, String> fields) {
    	this(null, summary, description, priority, testName, testStep, assignee, reporter, screenshots, detailedResultFile, fields);
    }
    

	/**
     * @param id					id of the issue on bugtracker. May ben null if the issue has not been created
     * @param summary
     * @param description
     * @param priority
     * @param testName
     * @param testStep
     * @param assignee
     * @param reporter
     * @param screenshots
     * @param detailedResultFile
     * @param fields				All custom fields that should be filled
     */
    public IssueBean(String id,
    				String summary,
                    String description,
                    String priority,
                    String testName,
                    TestStep testStep,
                    String assignee,
                    String reporter,
                    List<ScreenShot> screenshots,
                    File detailedResultFile,
                    Map<String, String> fields) {
        setSummary(summary);
        this.id = id; // unknown on creation but may be updated later
        this.description = description;
        this.assignee = assignee;
        this.reporter = reporter;
        this.priority = priority;
        this.testName = testName;
        this.screenShots = screenshots;
        this.testStep = testStep;
        this.detailedResult = detailedResultFile;

        if (fields != null) {
            this.fields = fields;
        }

        date = ZonedDateTime.now().plusHours(3);
        setDescription(StringUtils.stripAccents(description));
    }

    public void setField(String fieldName, String fieldValue) {
        fields.put(fieldName, fieldValue);
    }

    public File getDetailedResult() {
        return detailedResult;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<ScreenShot> getScreenShots() {
        return screenShots;
    }

    public TestStep getTestStep() {
        return testStep;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = StringUtils.stripAccents(summary);
    }

    public String getDate() {
        return date.format(formatter);
    }

    public ZonedDateTime getDateTime() {
        return date;
    }

    public DateTime getJodaDateTime() {
        return new DateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), date.getHour(), date.getMinute(), date.getSecond(), DateTimeZone.forID(date.getZone().getId()));
    }

    /**
     * Date at format 'yyyy-MM-dd'T'HH:mmZ'
     * @param date
     */
    public void setDate(String date) {
        this.date = ZonedDateTime.parse(date, formatter);
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getFields() {
        return fields;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


}
