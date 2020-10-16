package com.seleniumtests.connectors.bugtracker.jira;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Component;
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
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.screenshots.ScreenShot;

public class JiraConnector extends BugTracker {


    private static Logger logger = Logger.getLogger(JiraConnector.class);
    private String projectKey;
    private Map<String, BasicComponent> components;
    private Map<String, IssueType> issueTypes;
    private Map<String, Priority> priorities;
    private Map<String, Field> fields;
    private Map<String, Version> versions;
    private JiraRestClient restClient;
    private Project project;

    /**
     *
     * @param server        exemple: http://jira.covea.priv
     * @param user
     * @param password
     */
    public JiraConnector(String server, String projectKey, String user, String password, Map<String, String> jiraOptions) {
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
        
        components = getComponents();
        issueTypes = getIssueTypes();
        priorities = getPriorities();
        fields = getCustomFields();
        versions = getVersions();

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
    	
        String jql = String.format("project=%s and summary ~ \"%s\" and status = Open", projectKey, jiraBean.getSummary()
                .replace("[", "\\\\[")
                .replace("]", "\\\\]")
        );

        SearchRestClient searchClient = restClient.getSearchClient();
        List<Issue> issues = ImmutableList.copyOf(searchClient.searchJql(jql).claim().getIssues());
        if (issues.size() > 0) {
        	Issue issue = issues.get(0);
            return new JiraBean(issue.getKey(),
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
            		jiraBean.getFields(), 
            		jiraBean.getComponents());
        } else {
            return null;
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

        IssueRestClient issueClient = restClient.getIssueClient();
        IssueInputBuilder issueBuilder = new IssueInputBuilder(project, issueType, jiraBean.getSummary())
                //.setDueDate(jiraBean.getJodaDateTime())
                .setDescription(jiraBean.getDescription())
                ;

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
        jiraBean.getFields().forEach((fieldName, fieldValue) -> {
            if (fields.get(fieldName) != null) {
                issueBuilder.setFieldValue(fields.get(fieldName).getId(), fieldValue);
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
            issueClient.addAttachments(issue.getAttachmentsUri(), jiraBean.getScreenShots()
                    .stream()
                    .peek(s -> logger.info("file -> " + s.getFullImagePath()))
                    .map(s -> new File(s.getFullImagePath()))
                    .collect(Collectors.toList())
                    .toArray(new File[] {})
            );
        }

        if (jiraBean.getDetailedResult() != null) {
            issueClient.addAttachments(issue.getAttachmentsUri(),  jiraBean.getDetailedResult());
        }

        jiraBean.setId(issue.getKey());
    }

    /**
     * Close issue
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

        List<Transition> transitions = new ArrayList<>();
        issueClient.getTransitions(issue).claim().forEach(transition -> transitions.add(transition));

        issueClient.transition(issue, new TransitionInput(transitions.get(1).getId(), new ArrayList<>()));

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

}


