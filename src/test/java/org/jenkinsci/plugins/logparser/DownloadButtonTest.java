package org.jenkinsci.plugins.logparser;

import java.io.IOException;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.FilePath;

public class DownloadButtonTest {
    
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
    public void testDownloadButton1() throws IOException, SAXException{
        String url = "job/" + job.getName() + "/" + buildNumber + "/diffbuild/consoleLineDiffDisplay/?prevBuild="
                + buildNumber;
        HtmlPage page = jenkinsRule.createWebClient().goTo(url);
        WebAssert.assertElementPresent(page, "downloadHTML");
    }
    
    @Test
    public void testDownloadButton2() throws IOException, SAXException{
        String url = "job/" + job.getName() + "/" + buildNumber + "/diffbuild/logSectionDiffAction/?prevBuild="
                + buildNumber;
        HtmlPage page = jenkinsRule.createWebClient().goTo(url);
        WebAssert.assertElementPresent(page, "downloadHTML");
    }
    
    @Test
    public void testDownloadFunction1() throws IOException, SAXException{
        String url = "job/" + job.getName() + "/" + buildNumber + "/diffbuild/consoleLineDiffDisplay/?prevBuild="
                + buildNumber;
        HtmlPage page = jenkinsRule.createWebClient().goTo(url);
        String jscode = "jQuery(function ($){\n"
                      + "$('#downloadHTML').click();\n"
                      + "});\n";
        Page newPage = page.executeJavaScript(jscode).getNewPage();
        WebAssert.assertElementPresent((HtmlPage)newPage, "downloadlink");
    }
    
    @Test
    public void testDownloadFunction2() throws IOException, SAXException{
        String url = "job/" + job.getName() + "/" + buildNumber + "/diffbuild/logSectionDiffAction/?prevBuild="
                + buildNumber;
        HtmlPage page = jenkinsRule.createWebClient().goTo(url);
        String jscode = "jQuery(function ($){\n"
                      + "$('#downloadHTML').click();\n"
                      + "});\n";
        Page newPage = page.executeJavaScript(jscode).getNewPage();
        WebAssert.assertElementPresent((HtmlPage)newPage, "downloadlink");
    }

}
