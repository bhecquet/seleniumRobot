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
import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.CustomFieldOption;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.MyPermissionsInput;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.collect.ImmutableList;
import com.seleniumtests.connectors.bugtracker.BugTracker;
import com.seleniumtests.connectors.bugtracker.IssueBean;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.TestStep;

public class JiraConnector extends BugTracker {


    private static Logger logger = Logger.getLogger(JiraConnector.class);
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
            throw new ConfigurationException(String.format("L'URL de Jira n'est pas correcte", server));
        }
        
        try {
        	project = restClient.getProjectClient().getProject(projectKey).claim();
        } catch (RestClientException e) {
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

        logger.info(String.format("Connection à l'API du serveur Jira[%s], sur le projet [%s] avec le user [%s]", server, projectKey, user));
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
		for (String variable: issueOptions.keySet()) {
			if (variable.startsWith("jira.field.")) {
				customFieldsValues.put(variable.replace("jira.field.", ""), issueOptions.get(variable));
			}
		}

		List<String> components = new ArrayList<>();
		if (issueOptions.get("jira.components") != null) {
			components = Arrays.asList(issueOptions.get("jira.components").split(","));
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
				components);
	}
    
    protected void formatDescription(String testName, List<TestStep> failedSteps, TestStep lastTestStep, String description, StringBuilder fullDescription) {

    	fullDescription.append(String.format("*Test:* %s\n", testName));
    	if (description != null) {
    		fullDescription.append(String.format("*Description:* %s\n", description));
    	}
		
    	if (!failedSteps.isEmpty()) {
			fullDescription.append("h2. Steps in error\n");
			for (TestStep failedStep: failedSteps) {
				fullDescription.append(String.format("* *%s*", failedStep.getName()));
				fullDescription.append(String.format("{code:java}%s{code}\n\n", failedStep.toString()));
			}
    	}
	
		fullDescription.append("h2. Last logs\n");
		fullDescription.append(String.format("{code:java}%s{code}\n\n", lastTestStep.toString()));
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
            	
                
                
//                new CustomFieldOption()
//                issueBuilder.setFieldValue(fields.get(fieldName).getId(), fieldValue);
            } else {
                logger.warn(String.format("Field %s does not exist", fieldName));
            }
        });

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

        if (!jiraBean.getScreenShots().isEmpty()) {
        	File[] files = jiraBean.getScreenShots()
                    .stream()
                    .peek(s -> logger.info("file -> " + s.getFullImagePath()))
                    .map(s -> new File(s.getFullImagePath()))
                    .collect(Collectors.toList())
                    .toArray(new File[] {});
            issueClient.addAttachments(issue.getAttachmentsUri(), files).claim();
        }

        if (jiraBean.getDetailedResult() != null) {
            issueClient.addAttachments(issue.getAttachmentsUri(),  jiraBean.getDetailedResult()).claim();
        }

        jiraBean.setId(issue.getKey());
        jiraBean.setAccessUrl(browseUrl + issue.getKey());
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
        		issueClient.transition(issue, new TransitionInput(transitions.get(transitionName).getId(), new ArrayList<>()));
        		
        		// update available transitions
        		transitions.clear();
        		issueClient.getTransitions(issue).claim().forEach(transition -> transitions.put(transition.getName(), transition));
        	}
        }
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

		IssueRestClient issueClient = restClient.getIssueClient();
		Issue issue;
		try{
            issue = issueClient.getIssue(issueId).claim();
		} catch (RestClientException e) {
	        throw new ScenarioException(String.format("Jira issue %s does not exist, cannot update it", issueId));
	    }
	
		try {
            // add comment
            issueClient.addComment(issue.getCommentsUri(), Comment.valueOf(messageUpdate));
            
            if (!screenShots.isEmpty()) {
	            issueClient.addAttachments(issue.getAttachmentsUri(), screenShots
	                    .stream()
	                    .peek(s -> logger.info("file ->" + s.getFullImagePath()))
	                    .map(s -> new File(s.getFullImagePath()))
	                    .collect(Collectors.toList())
	                    .toArray(new File[] {})
	            );
            }
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
	 * 		
	 */
	public static void main(String[] args) {
		
		if (args.length != 5) {
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
    		System.out.println(String.format("    - %s", priority));
    	}

    	System.out.println("\nComponents:");
    	for (String component: jiraConnector.components.keySet()) {
    		System.out.println(String.format("    - %s", component));
    	}

    	System.out.println(String.format("\nListing required fields and allowed values (if any) for issue '%s'", args[4]));

    	Map<String, CimFieldInfo> fieldInfos = jiraConnector.getCustomFieldInfos(jiraConnector.project, issueType);
		
    	for (Entry<String, List<String>> entry: jiraConnector.getRequiredFieldsAndValues(fieldInfos).entrySet()) {
    		System.out.println(String.format("Field '%s':", entry.getKey()));
    		
    		for (String value: entry.getValue()) {
    			System.out.println(String.format("    - %s", value));
    		}
    	}
    	
    	
	}

}


