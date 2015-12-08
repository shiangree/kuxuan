package org.jenkinsci.plugins.logparser;

import hudson.FilePath;
import hudson.plugins.logparser.LogParserAction;
import hudson.plugins.logparser.LogParserPublisher;
import hudson.tasks.Maven;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;

/**
 * In this test suite we initialize the Job workspaces with a resource (maven-project1.zip) that contains a Maven
 * project.
 */
public class LogParserWorkflowTest {

    @ClassRule
    public static JenkinsRule jenkinsRule = new JenkinsRule();

    private static LogParserAction result;

    @BeforeClass
    public static void init() throws Exception {
        Maven.MavenInstallation mavenInstallation = jenkinsRule.configureMaven3();
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "logParserPublisherWorkflowStep");
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        workspace.unzipFrom(LogParserWorkflowTest.class.getResourceAsStream("./maven-project1.zip"));
        job.setDefinition(
                new CpsFlowDefinition("" + "node {\n" + "  sh \"/usr/bin/mvn clean install\"\n"
                        + "  step([$class: 'LogParserPublisher', projectRulePath: 'logparser-rules.txt', useProjectRule: true])\n"
                        + "}\n", true));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        result = job.getLastBuild().getAction(LogParserAction.class);
    }

    /**
     * Run a workflow job using {@link LogParserPublisher} and check for success.
     */
    @Test
    public void logParserPublisherWorkflowStep() throws Exception {
        assertEquals(0, result.getResult().getTotalErrors());
        assertEquals(2, result.getResult().getTotalWarnings());
        assertEquals(0, result.getResult().getTotalInfos());
    }

    /**
     * Run a workflow job using {@link LogParserPublisher} and check for number of debug tags
     */
    @Test
    public void logParserPublisherWorkflowStepDebugTags() throws Exception {
        assertEquals(0, result.getResult().getTotalDebugs());
    }
}