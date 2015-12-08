package org.jenkinsci.plugins.logparser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.FilePath;

public class MenuBarTest {
	
	WorkflowJob job;
    int buildNumber;
    
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    
    @Before
    public void setup() throws Exception{
        job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "logParserPublisherWorkflowStep");
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        workspace.unzipFrom(getClass().getResourceAsStream("./maven-project1.zip"));
        job.setDefinition(
                new CpsFlowDefinition("" + "node {\n" + "  sh \"/usr/bin/mvn clean install\"\n"
                        + "  step([$class: 'LogParserPublisher', projectRulePath: 'logparser-rules.txt', useProjectRule: true, enableDiffBuild: true])\n"
                        + "}\n", true));
        
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        buildNumber = job.getLastBuild().getNumber();
    }
    
    @Test
    public void testMenuBar1() throws IOException, SAXException{
        String url = "job/" + job.getName() + "/" + buildNumber + "/diffbuild/consoleLineDiffDisplay/?prevBuild="
                + buildNumber;
        HtmlPage page = jenkinsRule.createWebClient().goTo(url);
        WebAssert.assertElementPresent(page, "consoleLineDiffLink");
    }
    
    @Test
    public void testMenuBar2() throws IOException, SAXException{
        String url = "job/" + job.getName() + "/" + buildNumber + "/diffbuild/consoleLineDiffDisplay/?prevBuild="
                + buildNumber;
        HtmlPage page = jenkinsRule.createWebClient().goTo(url);
        WebAssert.assertElementPresent(page, "sectionDiffLink");
    }
    
    @Test
    public void testMenuBar3() throws IOException, SAXException{
        String url = "job/" + job.getName() + "/" + buildNumber + "/diffbuild/logSectionDiffAction/?prevBuild="
                + buildNumber;
        HtmlPage page = jenkinsRule.createWebClient().goTo(url);
        WebAssert.assertElementPresent(page, "consoleLineDiffLink");
    }
    
    @Test
    public void testMenuBar4() throws IOException, SAXException{
        String url = "job/" + job.getName() + "/" + buildNumber + "/diffbuild/logSectionDiffAction/?prevBuild="
                + buildNumber;
        HtmlPage page = jenkinsRule.createWebClient().goTo(url);
        WebAssert.assertElementPresent(page, "sectionDiffLink");
    }
}