package org.jenkinsci.plugins.logparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.FilePath;
import hudson.model.Run;
import hudson.plugins.logparser.DiffBuildAction;
import hudson.util.ListBoxModel;

public class DiffBuildActionTest {
	
	WorkflowJob job;
	DiffBuildAction act;

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    
    @Before
    public void setUp() throws Exception {
    	job = jenkinsRule.jenkins.createProject(WorkflowJob.class,
                "logParserPublisherWorkflowStep");
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        workspace.unzipFrom(getClass().getResourceAsStream("./maven-project1.zip"));
        job.setDefinition(
                new CpsFlowDefinition("" + "node {\n" + "  sh \"/usr/bin/mvn clean install\"\n"
                        + "  step([$class: 'LogParserPublisher', projectRulePath: 'logparser-rules.txt', useProjectRule: true, enableDiffBuild: true])\n"
                        + "}\n", true));

        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        act = job.getLastBuild().getAction(DiffBuildAction.class);
    }

    /**
     * test whether the checkbox for enable diff build works or not.
     * 
     * @throws IOException
     */
    @Test
    public void testCheckboxDeactivated() throws Exception {
        job.setDefinition(
                new CpsFlowDefinition("" + "node {\n" + "  sh \"/usr/bin/mvn clean install\"\n"
                        + "  step([$class: 'LogParserPublisher', projectRulePath: 'logparser-rules.txt', useProjectRule: true, enableDiffBuild: false])\n"
                        + "}\n", true));

        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        DiffBuildAction act1 = job.getLastBuild().getAction(DiffBuildAction.class);
        assertEquals(null, act1);
    }

    /**
     * test whether the checkbox for enable diff build works or not.
     * 
     * @throws IOException
     */
    @Test
    public void testCheckboxActivated() throws Exception {
        assertTrue(act != null);
    }

    @Test
    public void testGetOwner() throws Exception {
        assertEquals(job.getLastBuild().number, act.getOwner().number);
    }

    @Test
    public void testGetIconFileName() throws Exception {
        assertEquals("document.gif", act.getIconFileName());
    }

    @Test
    public void testGetDisplayName() throws Exception {
        assertEquals("Diff Against Other Build", act.getDisplayName());
    }

    @Test
    public void testGetUrlName() throws Exception {
        assertEquals("diffbuild", act.getUrlName());
    }

    @Test
    public void testDoFillAllBuildItems1() throws Exception {
        DiffBuildAction.DescriptorImpl descriptor = (DiffBuildAction.DescriptorImpl) act
                .getDescriptor();
        ListBoxModel listBox = descriptor.doFillAllBuildItems();
        ArrayList<Integer> allBuildFromDescriptor = new ArrayList<Integer>();
        for (int i = 0; i < listBox.size(); i++) {
            allBuildFromDescriptor.add(Integer.parseInt(listBox.get(i).value));
        }
        ArrayList<Integer> allBuildFromJenkins = new ArrayList<Integer>();
        Run<?, ?> tmpBuild = job.getLastBuild();
        allBuildFromJenkins.add(tmpBuild.number);
        while (tmpBuild.getPreviousBuild() != null) {
            tmpBuild = tmpBuild.getPreviousBuild();
            allBuildFromJenkins.add(tmpBuild.number);
        }
        assertTrue(allBuildFromDescriptor.equals(allBuildFromJenkins));
    }
    
    @Test
    public void testDoFillAllBuildItems99Time() throws Exception { 
        DiffBuildAction.DescriptorImpl descriptor = (DiffBuildAction.DescriptorImpl) act
                .getDescriptor();
        ListBoxModel listBox = descriptor.doFillAllBuildItems();
        ArrayList<String> prevBuildFromDescriptor = new ArrayList<String>();
        for (int i = 0; i < listBox.size(); i++) {
            prevBuildFromDescriptor.add(listBox.get(i).name);
        }
        ArrayList<String> prevBuildFromJenkins = new ArrayList<String>();
        Run<?, ?> tmpBuild = job.getLastBuild();
        prevBuildFromJenkins.add("build" + " " + tmpBuild.number + "\t" + tmpBuild.getTimestamp().getTime().toString());
        while (tmpBuild.getPreviousBuild() != null) {
            tmpBuild = tmpBuild.getPreviousBuild();
            prevBuildFromJenkins.add("build" + " " + tmpBuild.number + "\t" + tmpBuild.getTimestamp().getTime().toString());
        }
        assertTrue(prevBuildFromDescriptor.equals(prevBuildFromJenkins));
        assertTrue(prevBuildFromDescriptor == null);
        assertTrue(prevBuildFromJenkins == null);
    }

    @Test
    public void testDoFillTypeDiffItems1() throws Exception {
        DiffBuildAction.DescriptorImpl descriptor = (DiffBuildAction.DescriptorImpl) act
                .getDescriptor();
        ListBoxModel listBox = descriptor.doFillTypeDiffItems();
        ArrayList<String> typeDiffFromDescriptor = new ArrayList<String>();
        for (int i = 0; i < listBox.size(); i++) {
            typeDiffFromDescriptor.add(listBox.get(i).value);
        }
        String typeDiffValue[] = {"consoleLineDiffDisplay",
                "logSectionDiffAction", "sourceCodeDiffAction", "mavenPhaseDiffAction",
                "pomDepDiffAction"};
        ArrayList<String> typeDiff = new ArrayList<String>();
        for (int i = 0; i < typeDiffValue.length; i++) {
            typeDiff.add(typeDiffValue[i]);
        }
        assertTrue(typeDiffFromDescriptor.equals(typeDiff));
    }

    @Test
    public void testDoFillTypeDiffItems2() throws Exception {
        DiffBuildAction.DescriptorImpl descriptor = (DiffBuildAction.DescriptorImpl) act
                .getDescriptor();
        ListBoxModel listBox = descriptor.doFillTypeDiffItems();
        String currentValue = "";
        for (int i = 0; i < listBox.size(); i++) {
            if (listBox.get(i).selected) {
                currentValue = listBox.get(i).value;
            }
        }
        assertEquals("consoleLineDiffDisplay", currentValue);
    }

    @Test
    public void testGetDisplayNameDescriptor() throws Exception {
        DiffBuildAction.DescriptorImpl descriptor = (DiffBuildAction.DescriptorImpl) act
                .getDescriptor();
        assertEquals("Diff Build Action", descriptor.getDisplayName());
    }

    @Test
    public void testAllBuildDropdown() throws Exception {
        String url = "job/" + job.getName() + "/" + job.getLastBuild().number + "/diffbuild/";
        HtmlPage page = jenkinsRule.createWebClient().goTo(url);
        WebAssert.assertElementPresent(page, "allBuildSelect");
    }

    @Test
    public void testTypeDiffDropdown() throws Exception {
        String url = "job/" + job.getName() + "/" + job.getLastBuild().number + "/diffbuild/";
        HtmlPage page = jenkinsRule.createWebClient().goTo(url);
        WebAssert.assertElementPresent(page, "typeDiffSelect");
    }

    @Test
    public void testOkLink() throws Exception {
        String url = "job/" + job.getName() + "/" + job.getLastBuild().number + "/diffbuild/";
        HtmlPage page = jenkinsRule.createWebClient().goTo(url);
        WebAssert.assertElementPresent(page, "okLink");
    }
}
