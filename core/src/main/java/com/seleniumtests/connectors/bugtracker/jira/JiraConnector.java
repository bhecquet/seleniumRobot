package com.seleniumtests.connectors.bugtracker.jira;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.CustomFieldOption;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.collect.ImmutableList;
import com.seleniumtests.connectors.bugtracker.BugTracker;
import com.seleniumtests.connectors.bugtracker.IssueBean;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class JiraConnector extends BugTracker {


    private static final String ITEM = "    - %s";
	private static Logger logger = SeleniumRobotLogger.getLogger(JiraConnector.class);
    private String projectKey;
    private String browseUrl;
    private Map<String, BasicComponent> components;
    private Map<String, IssueType> issueTypes;
    private Map<String, Priority> priorities;
    private Map<String, Field> fields;
    private Map<String, Version> versions;
    private JiraRestClient restClient;
    private Project project;

    List<String> openStates;
    String closeTransition;
    
    /**
     * Constructor only there to allow getting information about a project
     * @param server        exemple: http://jira.covea.priv
     * @param user
     * @param password
     */
    public JiraConnector(String server, String projectKey, String user, String password) {
    	this.projectKey = projectKey;
    	
        
        if (server == null || server.isEmpty() 
        		|| projectKey == null || projectKey.isEmpty()
        		|| user == null || user.isEmpty()
        		|| password == null || password.isEmpty()) {
        	throw new ConfigurationException("Missing configuration for Jira, please provide 'bugtrackerUrl', 'bugtrackerProject', 'bugtrackerUser' and bugtrackerPassword' parameters");
        }
        
        try {
            restClient = new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(new URI(server), user, password);
        } catch (URISyntaxException e) {
            throw new ConfigurationException(String.format("Jira URL %s is incorrect", server));
        }
        
        try {
        	
        	project = restClient.getProjectClient().getProject(projectKey).claim();
        } catch (RestClientException e) {
        	for (BasicProject project: restClient.getProjectClient().getAllProjects().claim()) {
        		logger.info(project.getKey());
        	}
        	throw new ConfigurationException(String.format("Project with key '%s' cannot be found on jira server %s", projectKey, server));
        }
        
        browseUrl = server + "/browse/";
        
        components = getComponents();
        issueTypes = getIssueTypes();
        priorities = getPriorities();
        fields = getCustomFields();
        versions = getVersions();
    }
    

    /**
     * constructor to call for creating new issues
     * @param server        exemple: http://jira.covea.priv
     * @param user
     * @param password
     */
    public JiraConnector(String server, String projectKey, String user, String password, Map<String, String> jiraOptions) {
        this(server, projectKey, user, password);
        
        // check jira options
		closeTransition = jiraOptions.get("jira.closeTransition");
		
		if (jiraOptions.get("jira.openStates") == null) {
			throw new ConfigurationException("'bugtracker.jira.openStates' MUST be set. It's the state of an issue when it has juste been create. Used to search for open issues");
		} else {
			openStates = Arrays.asList(jiraOptions.get("jira.openStates").split(",")).stream().map(String::trim).collect(Collectors.toList());
		}
		if (closeTransition == null) {
			throw new ConfigurationException("'bugtracker.jira.closeTransition' MUST be set. It's the name of the transition that will close an issue");
		}

        logger.info(String.format("Connecting to Jira API [%s], on project [%s] with user [%s]", server, projectKey, user));
    }

    private Map<String, BasicComponent> getComponents() {
        Map<String, BasicComponent> jiraComponents = new HashMap<>();
        project.getComponents().forEach(basicComponent -> jiraComponents.put(basicComponent.getName(), basicComponent));
        return jiraComponents;
    }

    private Map<String, IssueType> getIssueTypes() {
        Map<String, IssueType> jiraIssueTypes = new HashMap<>();
        project.getIssueTypes().forEach(issueType -> jiraIssueTypes.put(issueType.getName(), issueType));
        return jiraIssueTypes;
    }

    private Map<String, Version> getVersions() {
        Map<String, Version> jiraVersions = new HashMap<>();
        project.getVersions().forEach(version -> jiraVersions.put(version.getName(), version));
        return jiraVersions;
    }

    private Map<String, Priority> getPriorities() {
        Map<String, Priority> jiraPriorities = new HashMap<>();
        restClient.getMetadataClient().getPriorities().claim().forEach(priority -> jiraPriorities.put(priority.getName(), priority));
        return jiraPriorities;
    }
    
    private Map<String, Field> getCustomFields() {
    	Map<String, Field> jiraFields = new HashMap<>();
    	restClient.getMetadataClient().getFields().claim().forEach(field -> jiraFields.put(field.getName(), field));
    	return jiraFields;
    }
    
    private Map<String, CimFieldInfo> getCustomFieldInfos(Project project, IssueType issueType) {
    	Map<String, CimFieldInfo> jiraFieldValues = new HashMap<>();
    	restClient.getIssueClient().getCreateIssueMetaFields(project.getKey(), issueType.getId().toString(), null, null)
    		.claim()
    		.getValues()
    		.forEach(fieldInfo -> jiraFieldValues.put(fieldInfo.getName(), fieldInfo));
    	
    	
    	return jiraFieldValues;
    }

    private User getUser(String email) {
        try {
           return ImmutableList.copyOf(restClient.getUserClient().findUsers(email).claim()).get(0);
        } catch (IndexOutOfBoundsException | RestClientException e) {
            return null;
        }
    }

    /**
     * Check if issue already exists, and if so, returns an updated IssueBean
     * @param issueBean		a JiraBean instance
     *
     * @return
     */
    @Override
    public IssueBean issueAlreadyExists(IssueBean issueBean) {

    	if (!(issueBean instanceof JiraBean)) {
    		throw new ClassCastException("JiraConnector needs JiraBean instances");
    	}
    	JiraBean jiraBean = (JiraBean)issueBean;
    	
        String jql = String.format("project=%s and summary ~ \"%s\" and status in (\"%s\")", projectKey, jiraBean.getSummary()
        		.replace("[", "\\\\[")
                .replace("]", "\\\\]")
        		, StringUtils.join(openStates, "\",\"")
                
        );

        SearchRestClient searchClient = restClient.getSearchClient();
        List<Issue> issues = ImmutableList.copyOf(searchClient.searchJql(jql).claim().getIssues());
        if (!issues.isEmpty()) {
        	Issue issue = issues.get(0);
            JiraBean updatedJiraBean = new JiraBean(issue.getKey(),
            		jiraBean.getSummary(), 
            		issue.getDescription(),
            		jiraBean.getPriority(),
            		jiraBean.getIssueType(),
            		jiraBean.getTestName(), 
            		jiraBean.getTestStep(),
            		jiraBean.getAssignee(), 
            		jiraBean.getReporter(), 
            		jiraBean.getScreenShots(), 
            		jiraBean.getDetailedResult(),
            		jiraBean.getCustomFields(),
            		jiraBean.getComponents());
            updatedJiraBean.setDate(issue.getCreationDate().toString("yyyy-MM-dd'T'HH:mmZZ"));
            updatedJiraBean.setAccessUrl(browseUrl + issue.getKey());
            return updatedJiraBean;
        } else {
            return null;
        }
    }
    
    /**
     * Creates the bean effectively
     */
    @Override
	protected IssueBean createIssueBean(
			String summary,
			String fullDescription,
			String testName,
			TestStep lastStep, 
			String assignee,
			String reporter,
			List<ScreenShot> screenShots,
			File zipFile,
			Map<String, String> issueOptions) {
		
		String priority = issueOptions.get("priority");
		String issueType = issueOptions.get("jira.issueType");
		Map<String, String> customFieldsValues = new HashMap<>();
		for (Entry<String, String> variable: issueOptions.entrySet()) {
			if (variable.getKey().startsWith("jira.field.")) {
				customFieldsValues.put(variable.getKey().replace("jira.field.", ""), variable.getValue());
			}
		}

		List<String> comps = new ArrayList<>();
		if (issueOptions.get("jira.components") != null) {
			comps = Arrays.asList(issueOptions.get("jira.components").split(","));
		}
		
		
		return new JiraBean(summary,
				fullDescription,
				priority,
				issueType,
				testName,
				lastStep,
				assignee,
				reporter,
				screenShots,
				zipFile,
				customFieldsValues,
				comps);
	}
    
    /**
     * Format description of the issue
     */
    @Override
    protected void formatDescription(String testName, List<TestStep> failedSteps, TestStep lastTestStep, String description, StringBuilder fullDescription) {

    	fullDescription.append(String.format("*Test:* %s\n", testName));
    	if (description != null) {
    		fullDescription.append(String.format("*Description:* %s\n", description.replace("Test goal:", "*Test goal:*")));
    		
    	}
    	if (SeleniumTestsContextManager.getThreadContext().getStartedBy() != null) {
			fullDescription.append(String.format("*Started by:* %s\n", SeleniumTestsContextManager.getThreadContext().getStartedBy()));
		}
    	for (TestStep failedStep: failedSteps) {
			fullDescription.append(String.format("*Error step #%d (%s):* *{color:#de350b}%s{color}*\n", failedStep.getPosition(), failedStep.getName(), failedStep.getActionException()));
		}

    	formatFailedStep(failedSteps, fullDescription);
	
		fullDescription.append("h2. Last logs\n");
		
		if (lastTestStep != null) {
			fullDescription.append(String.format("{code:java}%s{code}", lastTestStep.toString()));
			fullDescription.append("\n\nh2. Associated screenshots\n");
			
    		List<ScreenShot> screenshots = lastTestStep.getSnapshots().stream()
    				.map(Snapshot::getScreenshot)
    				.collect(Collectors.toList());
    		for (ScreenShot screenshot: screenshots) {
    			if (screenshot.getFullImagePath() != null && new File(screenshot.getFullImagePath()).exists()) {
    				fullDescription.append(String.format("!%s|thumbnail!\n", new File(screenshot.getFullImagePath()).getName()));
    			}
    		}
    	}
	}


	/**
	 * @param failedSteps
	 * @param fullDescription
	 */
	private void formatFailedStep(List<TestStep> failedSteps, StringBuilder fullDescription) {
		if (!failedSteps.isEmpty()) {
			fullDescription.append("h2. Steps in error\n");
			for (TestStep failedStep: failedSteps) {
				fullDescription.append(String.format("* *" + STEP_KO_PATTERN + "%s*\n", failedStep.getPosition(), failedStep.getName().trim()));
				if (failedStep.getRootCause() != null) {
					fullDescription.append(String.format("+Possible cause:+ %s%s\n", failedStep.getRootCause(), failedStep.getRootCauseDetails() == null || failedStep.getRootCauseDetails().trim().isEmpty() ? "": " => " + failedStep.getRootCauseDetails()));
				}
				fullDescription.append(String.format("{code:java}%s{code}\n\n", failedStep.toString()));
			}
    	}
	}
    


    /**
     * Create issue
     */
    public void createIssue(IssueBean issueBean) {

    	if (!(issueBean instanceof JiraBean)) {
    		throw new ClassCastException("JiraConnector needs JiraBean instances");
    	}
    	JiraBean jiraBean = (JiraBean)issueBean;
    	
        	
    	IssueType issueType = issueTypes.get(jiraBean.getIssueType());
    	if (issueType == null) {
    		throw new ConfigurationException(String.format("Issue type %s cannot be found among valid issue types %s", jiraBean.getIssueType(), issueTypes.keySet()));
    	}


    	Map<String, CimFieldInfo> fieldInfos = getCustomFieldInfos(project, issueType);
    	
        IssueRestClient issueClient = restClient.getIssueClient();
        IssueInputBuilder issueBuilder = new IssueInputBuilder(project, issueType, jiraBean.getSummary())
                .setDescription(jiraBean.getDescription())
                ;
        
        if (isDueDateRequired(fieldInfos)) {
        	issueBuilder.setDueDate(jiraBean.getJodaDateTime());
        }
        

        if (jiraBean.getAssignee() != null && !jiraBean.getAssignee().isEmpty()) {
        	User user = getUser(jiraBean.getAssignee());
        	if (user == null) {
        		throw new ConfigurationException(String.format("Assignee %s cannot be found among jira users", jiraBean.getAssignee()));
        	}
            issueBuilder.setAssignee(user);
        }
        if (jiraBean.getPriority() != null && !jiraBean.getPriority().isEmpty()) {
        	Priority priority = priorities.get(jiraBean.getPriority());
        	if (priority == null) {
        		throw new ConfigurationException(String.format("Priority %s cannot be found on this jira project, valid priorities are %s", jiraBean.getPriority(), priorities.keySet()));
        	}
            issueBuilder.setPriority(priority);
        }
        if (jiraBean.getReporter() != null && !jiraBean.getReporter().isEmpty()) {
            issueBuilder.setReporterName(jiraBean.getReporter());
        }
        
        // set fields
        setCustomFields(jiraBean, fieldInfos, issueBuilder);

        // set components
        issueBuilder.setComponents(jiraBean.getComponents()
                .stream()
                .filter(component -> components.get(component) != null)
                .map(component -> components.get(component))
                .collect(Collectors.toList())
                .toArray(new BasicComponent[] {}));

        // add issue
        IssueInput newIssue = issueBuilder.build();
        BasicIssue basicIssue = issueClient.createIssue(newIssue).claim();
        Issue issue = issueClient.getIssue(basicIssue.getKey()).claim();

        addAttachments(jiraBean, issueClient, issue);

        jiraBean.setId(issue.getKey());
        jiraBean.setAccessUrl(browseUrl + issue.getKey());
    }


	/**
	 * Add attachments to the issue
	 * @param jiraBean
	 * @param issueClient
	 * @param issue
	 */
	private void addAttachments(JiraBean jiraBean, IssueRestClient issueClient, Issue issue) {
		if (!jiraBean.getScreenShots().isEmpty()) {
        	File[] files = jiraBean.getScreenShots()
                    .stream()
                    .peek(s -> logger.info("file -> " + s.getFullImagePath()))
                    .map(s -> new File(s.getFullImagePath()))
                    .filter(File::exists)
                    .collect(Collectors.toList())
                    .toArray(new File[] {});
        	if (files.length > 0) {
        		issueClient.addAttachments(issue.getAttachmentsUri(), files).claim();
        	}
        }

        if (jiraBean.getDetailedResult() != null && jiraBean.getDetailedResult().exists()) {
            issueClient.addAttachments(issue.getAttachmentsUri(),  jiraBean.getDetailedResult()).claim();
        }
	}


	/**
	 * Add custom fields to the issue
	 * @param jiraBean
	 * @param fieldInfos
	 * @param issueBuilder
	 */
	private void setCustomFields(JiraBean jiraBean, Map<String, CimFieldInfo> fieldInfos, IssueInputBuilder issueBuilder) {
		
		jiraBean.getCustomFields().forEach((fieldName, fieldValue) -> {
            if (fieldInfos.get(fieldName) != null && fields.get(fieldName) != null) {
            	if ("option".equals(fields.get(fieldName).getSchema().getType())) {
        			CustomFieldOption option = getOptionForField(fieldInfos, fieldName, fieldValue);
        			if (option == null) {
        				logger.warn(String.format("Value %s for field %s does not exist", fieldValue, fieldName));
        			} else {
        				issueBuilder.setFieldValue(fields.get(fieldName).getId(), option);
        			}
            				
            	} else if ("string".equals(fields.get(fieldName).getSchema().getType())) {
            		issueBuilder.setFieldValue(fields.get(fieldName).getId(), fieldValue);
            	} else {
            		logger.warn(String.format("Field %s type cannot be handled", fieldName));
            	}
            	
            } else {
                logger.warn(String.format("Field %s does not exist", fieldName));
            }
        });
	}

    /**
     * Search the right field option among all allowed values or return null if value is not valid or field cannot be found
     * @param fieldInfos
     * @param fieldName
     * @param fieldValue
     * @return
     */
	private CustomFieldOption getOptionForField(Map<String, CimFieldInfo> fieldInfos, String fieldName, String fieldValue) {
		CimFieldInfo fieldInfo = fieldInfos.get(fieldName);
		if (fieldInfo == null) {
			return null;
		} else {
			for (Object obj: fieldInfo.getAllowedValues()) {
				if (obj instanceof CustomFieldOption && ((CustomFieldOption)obj).getValue().equals(fieldValue)) {
					return (CustomFieldOption)obj;
				}
			}
		}
			
		return null;
	}
	
	private boolean isDueDateRequired(Map<String, CimFieldInfo> fieldInfos) {
		for (CimFieldInfo fieldInfo: fieldInfos.values()) {
			if ("duedate".equals(fieldInfo.getSchema().getSystem()) && fieldInfo.isRequired()) {
				return true;
			}
		}
		return false;
	}
	
	private Map<String, List<String>> getRequiredFieldsAndValues(Map<String, CimFieldInfo> fieldInfos) {
		
		Map<String, List<String>> requiredFields = new HashMap<>();
		
		for (Entry<String, CimFieldInfo> entry: fieldInfos.entrySet()) {
			
			// exclude some "mandatory" fields from list as they are always provided
			if (Arrays.asList("project", "summary", "description", "issuetype").contains(entry.getValue().getSchema().getSystem())) {
				continue;
			}
			
			if (entry.getValue().isRequired() 
					&& ("option".equals(entry.getValue().getSchema().getType())
						|| "array".equals(entry.getValue().getSchema().getType())
						)
					) {
				List<String> allowedValues = new ArrayList<>();
				entry.getValue().getAllowedValues().forEach(v -> {
					if (v instanceof CustomFieldOption) {
						allowedValues.add(((CustomFieldOption)v).getValue());
					} else if (v instanceof BasicComponent) {
						allowedValues.add(((BasicComponent)v).getName());
					} else if (v instanceof BasicPriority) {
						allowedValues.add(((BasicPriority)v).getName());
					}
				});
				requiredFields.put(entry.getKey(), allowedValues);
			} else if (entry.getValue().isRequired()) {
				requiredFields.put(entry.getKey(), new ArrayList<>());
			}
		}
		return requiredFields;
	}

    /**
     * Close issue, applying all necessary transitions
     * @param issueId           ID of the issue
     * @param closingMessage    Message of closing
     */
    public void closeIssue(String issueId, String closingMessage){

    	logger.info(String.format("closing issue %s", issueId));
        IssueRestClient issueClient = restClient.getIssueClient();
        Issue issue;
        try {
        	issue = issueClient.getIssue(issueId).claim();
        } catch (RestClientException e) {
        	throw new ScenarioException(String.format("Jira issue %s does not exist, cannot close it", issueId));
        }

        Map<String, Transition> transitions = new HashMap<>();
        issueClient.getTransitions(issue).claim().forEach(transition -> transitions.put(transition.getName(), transition));
        List<String> closeWorkflow = Arrays.asList(closeTransition.split("/"));
        
        int workflowPosition = -1;
        for (String transitionName: transitions.keySet()) {
        	workflowPosition = closeWorkflow.indexOf(transitionName);
        	if (workflowPosition != -1) {
        		break;
        	}
        }
        if (workflowPosition == -1) {
        	throw new ConfigurationException(String.format("'bugtracker.jira.closeTransition' values [%s] are unknown for this issue, allowed transitions are %s", closeTransition, transitions.keySet()));
        } else {
        	for (String transitionName: closeWorkflow.subList(workflowPosition, closeWorkflow.size())) {
        		transitionIssue(issueClient, issue, transitions, transitionName);
        	}
        }
    }


	/**
	 * Transition issue
	 * @param issueClient
	 * @param issue
	 * @param transitions
	 * @param transitionName
	 */
	private void transitionIssue(IssueRestClient issueClient, Issue issue, Map<String, Transition> transitions,
			String transitionName) {
		
		for (int i = 0; i < 5; i++) {
			
			if (transitions.get(transitionName) == null) {
				
				// update available transitions
				WaitHelper.waitForMilliSeconds(500);
				transitions.clear();
				issueClient.getTransitions(issue).claim().forEach(transition -> transitions.put(transition.getName(), transition));
				continue;
			}
			
			issueClient.transition(issue, new TransitionInput(transitions.get(transitionName).getId(), new ArrayList<>()));
			return;
			
		}
		throw new ConfigurationException(String.format("'bugtracker.jira.closeTransition': value [%s] is invalid for this issue in its current state, allowed transitions are %s", transitionName, transitions.keySet()));
	}
    


    /**
     * Create description for issue update
     * @param messageUpdate
     * @param screenShots
     * @param lastFailedStep
     * @return
     */
    private StringBuilder formatUpdateDescription(String messageUpdate, List<ScreenShot> screenShots, TestStep lastFailedStep) {
    	StringBuilder fullDescription = new StringBuilder(messageUpdate + "\n");
		if (SeleniumTestsContextManager.getThreadContext().getStartedBy() != null) {
			fullDescription.append(String.format("*Started by:* %s\n", SeleniumTestsContextManager.getThreadContext().getStartedBy()));
		}

    	if (lastFailedStep != null) {
			fullDescription.append(String.format("*" + STEP_KO_PATTERN + "%s*\n", lastFailedStep.getPosition(), lastFailedStep.getName().trim()));
			fullDescription.append(String.format("{code:java}%s{code}\n\n", lastFailedStep.toString()));
    	}
		
        // add snapshots to comment
        for (ScreenShot screenshot: screenShots) {
        	fullDescription.append(String.format("!%s|thumbnail!\n", new File(screenshot.getFullImagePath()).getName()));
        }
        
        return fullDescription;
    }

    /**
	 * Update an existing jira issue with a new message and new screenshots.
	 * Used when an issue has already been raised, we complete it
	 * @param issueId			Id of the issue
	 * @param messageUpdate		message to add to description
	 * @param screenShots		New screenshots
	 */
	@Override
	public void updateIssue(String issueId, String messageUpdate, List<ScreenShot> screenShots) {
		updateIssue(issueId, messageUpdate, screenShots, null);
	}
	
	@Override
	public void updateIssue(String issueId, String messageUpdate, List<ScreenShot> screenShots, TestStep lastFailedStep) {

		IssueRestClient issueClient = restClient.getIssueClient();
		Issue issue;
		try{
            issue = issueClient.getIssue(issueId).claim();
		} catch (RestClientException e) {
	        throw new ScenarioException(String.format("Jira issue %s does not exist, cannot update it", issueId));
	    }
	
		try {
			

	        if (!screenShots.isEmpty()) {
	            issueClient.addAttachments(issue.getAttachmentsUri(), screenShots
	                    .stream()
	                    .peek(s -> logger.info("file ->" + s.getFullImagePath()))
	                    .map(s -> new File(s.getFullImagePath()))
	                    .collect(Collectors.toList())
	                    .toArray(new File[] {})
	            );
	        }
			
            // add comment
            issueClient.addComment(issue.getCommentsUri(), Comment.valueOf(formatUpdateDescription(messageUpdate, screenShots, lastFailedStep).toString()));
            
            
            logger.info(String.format("Jira %s updated", issueId));
            
        } catch (Exception e) {
        	logger.error(String.format("Jira %s not modified: %s", issueId, e.getMessage()));
        	throw e;
        }
    }

	public List<String> getOpenStates() {
		return openStates;
	}
	
	/**
	 * Method for getting required fields and allowed values for creating an issue on a project
	 * 
	 * @param args
	 * 		server => url of jira server
	 * 		user => user to connect to jira
	 * 		password => password to connect to jira
	 * 		project => projectkey
	 * 		issueType => type of the issue that will be created
	 * 		issueKey (optional) => give the transitions for a specific issue 
	 * 		
	 */
	public static void main(String[] args) {
		
		if (args.length < 5) {
			System.out.println("Usage: JiraConnector <server> <projectKey> <user> <password> <issueType>");
			System.exit(1);
		}
		
		JiraConnector jiraConnector = new JiraConnector(args[0], args[1], args[2], args[3]);
		
		
    	IssueType issueType = jiraConnector.issueTypes.get(args[4]);
    	if (issueType == null) {
    		throw new ConfigurationException(String.format("Issue type %s cannot be found among valid issue types %s", args[4], jiraConnector.issueTypes.keySet()));
    	}
    	
    	System.out.println("Proprities:");
    	for (String priority: jiraConnector.priorities.keySet()) {
    		System.out.println(String.format(ITEM, priority));
    	}

    	System.out.println("\nComponents:");
    	for (String component: jiraConnector.components.keySet()) {
    		System.out.println(String.format(ITEM, component));
    	}

    	System.out.println(String.format("\nListing required fields and allowed values (if any) for issue '%s'", args[4]));

    	Map<String, CimFieldInfo> fieldInfos = jiraConnector.getCustomFieldInfos(jiraConnector.project, issueType);
		
    	for (Entry<String, List<String>> entry: jiraConnector.getRequiredFieldsAndValues(fieldInfos).entrySet()) {
    		System.out.println(String.format("Field '%s':", entry.getKey()));
    		
    		for (String value: entry.getValue()) {
    			System.out.println(String.format(ITEM, value));
    		}
    	}
    	
    	if (args.length >= 6) {
    		IssueRestClient issueClient = jiraConnector.restClient.getIssueClient();
            Issue issue;
            try {
            	issue = issueClient.getIssue(args[5]).claim();
            } catch (RestClientException e) {
            	throw new ScenarioException(String.format("Jira issue %s does not exist, cannot close it", args[5]));
            }

            Map<String, Transition> transitions = new HashMap<>();
            issueClient.getTransitions(issue).claim().forEach(transition -> transitions.put(transition.getName(), transition));
            System.out.println(transitions);
    	}
    	
    	
	}

}


