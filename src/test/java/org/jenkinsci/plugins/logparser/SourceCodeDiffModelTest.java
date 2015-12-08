package org.jenkinsci.plugins.logparser;

import hudson.FilePath;
import hudson.plugins.logparser.SourceCodeDiff;
import hudson.plugins.logparser.SourceCodeDiffAction;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Charlie Tsai, Hanjie Wang
 */
public class SourceCodeDiffModelTest {
    @ClassRule
    public static JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void automatedTest() throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "logParserPublisherWorkflowStep");
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        job.setDefinition(new CpsFlowDefinition("node{step([$class: 'LogParserPublisher', projectRulePath: 'logparser-rules.txt', useProjectRule: true])}"));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        SourceCodeDiffAction result = job.getLastBuild().getAction(SourceCodeDiffAction.class);
        //make a test with result
    }

    private File build1;
    private File build2;

    @Before
    public void setUp() {
        build1 = new File("src/test/resources/org/jenkinsci/plugins/logparser/DiffSourceCodeTest/Build1");
        build2 = new File("src/test/resources/org/jenkinsci/plugins/logparser/DiffSourceCodeTest/Build2");
    }

    @Test
    public void getFilesTest() throws IOException {
        Map<String, String> actual = new SourceCodeDiff(build1, build2).getFiles(build1);

        assertEquals(3, actual.size());
        assertEquals(new File(build1, "test.java").getAbsolutePath(), actual.get("test.java"));
        assertEquals(new File(build1, "subDirectory/test1.java").getAbsolutePath(), actual.get("test1.java"));
        assertEquals(new File(build1, "subDirectory/test2.java").getAbsolutePath(), actual.get("test2.java"));
    }

    @Test
    public void putModifiedTest() throws IOException {
        File file1 = new File(build1, "test.java");
        File file2 = new File(build2, "test.java");

        SourceCodeDiff diff = new SourceCodeDiff(build1, build2);
        diff.putModified(file1.getAbsolutePath(), file2.getAbsolutePath());

        assertNotNull(diff.getModified().get("test.java"));
        assertEquals(file1.getAbsolutePath(), diff.getModified().get("test.java")[0]);
        assertEquals(file2.getAbsolutePath(), diff.getModified().get("test.java")[1]);
    }

    @Test
    public void constructorTest() throws IOException {
        SourceCodeDiff diff = new SourceCodeDiff(build1, build2);

        assertEquals(1, diff.getAdded().size());
        assertEquals(2, diff.getRemoved().size());
        assertEquals(1, diff.getModified().size());

        assertEquals(new File(build2, "test3.java").getAbsolutePath(), diff.getAdded().get("test3.java"));
        assertEquals(new File(build1, "subDirectory/test1.java").getAbsolutePath(), diff.getRemoved().get("test1.java"));
        assertEquals(new File(build1, "subDirectory/test2.java").getAbsolutePath(), diff.getRemoved().get("test2.java"));

        assertNotNull(diff.getModified().get("test.java"));
    }


}
