package com.seleniumtests.connectors.bugtracker;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.TestStep;

/**
 * Stores information about an issue to be created
 */
public class IssueBean {

    protected List<ScreenShot> screenShots = new ArrayList<>();
    protected String id;
    protected String accessUrl; // the HTTP URL to access the issue directly
    protected String reporter;
    protected String testName;
    protected TestStep testStep;
    protected String summary;
    protected ZonedDateTime date;
    protected ZonedDateTime creationDate;
    protected String assignee;
    protected String description;
    Map<String, String> fields = new HashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmZZZZZ");


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
    	this(id, summary, description, "", null, "", "", new ArrayList<>(), null);
    }
    
    /**
     * 
     * @param summary
     * @param description
     * @param testName
     * @param testStep
     * @param assignee
     * @param reporter
     * @param screenshots
     * @param detailedResultFile
     */
    public IssueBean(
    		String summary,
    		String description,
    		String testName,
    		TestStep testStep,
    		String assignee,
    		String reporter,
    		List<ScreenShot> screenshots,
    		File detailedResultFile) {
    	this(null, summary, description, testName, testStep, assignee, reporter, screenshots, detailedResultFile);
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
                    String testName,
                    TestStep testStep,
                    String assignee,
                    String reporter,
                    List<ScreenShot> screenshots,
                    File detailedResultFile) {
        setSummary(summary);
        this.id = id; // unknown on creation but may be updated later
        this.description = description;
        this.assignee = assignee;
        this.reporter = reporter;
        this.testName = testName;
        this.testStep = testStep;
        this.detailedResult = detailedResultFile;

        if (screenshots != null) {
            this.screenShots = screenshots;
        }

        date = ZonedDateTime.now().plusHours(3);
        creationDate = ZonedDateTime.now();
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

    public String getCreationDate() {
		return creationDate.format(formatter);
	}

	public void setCreationDate(ZonedDateTime creationDate) {
		this.creationDate = creationDate;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAccessUrl() {
		return accessUrl;
	}

	public void setAccessUrl(String accessUrl) {
		this.accessUrl = accessUrl;
	}


}
