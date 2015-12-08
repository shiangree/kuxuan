package org.jenkinsci.plugins.logparser;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.FilePath;
import hudson.plugins.logparser.DiffToHtmlGenerator;

public class DiffToHtmlGeneratorTest {

    final static String sep = ",";
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void testDiffAlgo1() {
        List<String> original = new ArrayList<String>();
        List<String> revised = new ArrayList<String>();

        original.add("first line");
        original.add("second line");
        original.add("third line");
        original.add("fourth line");

        revised.add("firt line");
        revised.add("third line");
        revised.add("fouth line");

        DiffToHtmlGenerator d2h = new DiffToHtmlGenerator(original, revised);
        assertEquals(2, d2h.getDeltas().size());
    }

    @Test
    public void testDiffAlgo2() {
        List<String> original = new ArrayList<String>();
        List<String> revised = new ArrayList<String>();

        original.add("first line");
        original.add("second line");
        original.add("third line");
        original.add("fourth line");

        revised.add("first line");
        revised.add("second line");
        revised.add("third line");
        revised.add("fouth line");

        DiffToHtmlGenerator d2h = new DiffToHtmlGenerator(original, revised);
        assertEquals(1, d2h.getDeltas().size());
    }

    @Test
    public void testDiffAlgo3() {
        List<String> original = new ArrayList<String>();
        List<String> revised = new ArrayList<String>();

        original.add("first line");
        original.add("second line");
        original.add("fourth line");
        original.add("fifth line");

        revised.add("first line");
        revised.add("second line");
        revised.add("third line");
        revised.add("fourth line");
        revised.add("four point five");
        revised.add("fifth line");
        revised.add("sixth line");

        DiffToHtmlGenerator d2h = new DiffToHtmlGenerator(original, revised);
        assertEquals(3, d2h.getDeltas().size());
    }

    @Test
    public void testDifflineInsert() throws Exception {

        String stuff1 = "1st line,2nd line,3rd line";
        String stuff2 = "1st line,2nd line,3rd line,4th line";
        String htmlTag = "div.d2h-ins";

        Elements divs = createFileThenDelete(stuff1, stuff2, htmlTag);

        assertEquals("4th line", divs.first().text());
    }

    @Test
    public void testDifflineUnchanged() throws Exception {

        String stuff1 = "1st line,2nd line,3rd line";
        String stuff2 = "1st line,2nd line,3rd line";
        String htmlTag = "div.d2h-cntx";

        Elements divs = createFileThenDelete(stuff1, stuff2, htmlTag);

        assertEquals("1st line", divs.first().text());
    }

    @Test
    public void testDifflineChanged() throws Exception {

        String stuff1 = "1st line,2nd line,3rd line";
        String stuff2 = "1st line,2nd line,4rd line";
        String htmlTag = "div.d2h-rev";

        Elements divs = createFileThenDelete(stuff1, stuff2, htmlTag);

        assertEquals("3rd line", divs.first().text());
    }

    @Test
    public void testDifflineDelete() throws Exception {

        String stuff1 = "1st line,2nd line,3rd line";
        String stuff2 = "1st line,3rd line";
        String htmlTag = "div.d2h-del";

        Elements divs = createFileThenDelete(stuff1, stuff2, htmlTag);

        assertEquals("2nd line", divs.first().text());
    }

    // Jenkins Rules tests

    @Test
    public void testCheckUnchanged() throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "logParserPublisherWorkflowStep");
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        workspace.unzipFrom(getClass().getResourceAsStream("./maven-project1.zip"));
        job.setDefinition(new CpsFlowDefinition("" + "node {\n" + "  sh \"/usr/bin/mvn clean install\"\n"
                + "  step([$class: 'LogParserPublisher', projectRulePath: 'logparser-rules.txt', useProjectRule: true, enableDiffBuild: true])\n"
                + "}\n", true));

        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(1));
        int lastBuildNumber = job.getLastBuild().number;
        int prevBuildNubmer = lastBuildNumber - 1;
        String url = "job/" + job.getName() + "/" + lastBuildNumber + "/diffbuild/consoleLineDiffDisplay/?prevBuild="
                + prevBuildNubmer;
        HtmlPage page = jenkinsRule.createWebClient().goTo(url);
        WebAssert.assertElementPresent(page, "check-cntx");
    }

    @Test
    public void testCheckChanged() throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "logParserPublisherWorkflowStep");
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        workspace.unzipFrom(getClass().getResourceAsStream("./maven-project1.zip"));
        job.setDefinition(new CpsFlowDefinition("" + "node {\n" + "  sh \"/usr/bin/mvn clean install\"\n"
                + "  step([$class: 'LogParserPublisher', projectRulePath: 'logparser-rules.txt', useProjectRule: true, enableDiffBuild: true])\n"
                + "}\n", true));

        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(1));
        int lastBuildNumber = job.getLastBuild().number;
        int prevBuildNubmer = lastBuildNumber - 1;
        String url = "job/" + job.getName() + "/" + lastBuildNumber + "/diffbuild/consoleLineDiffDisplay/?prevBuild="
                + prevBuildNubmer;
        HtmlPage page = jenkinsRule.createWebClient().goTo(url);
        WebAssert.assertElementPresent(page, "check-rev");
    }
    
    @Test
    public void testShowHideButton() throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "logParserPublisherWorkflowStep");
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        workspace.unzipFrom(getClass().getResourceAsStream("./maven-project1.zip"));
        job.setDefinition(new CpsFlowDefinition("" + "node {\n" + "  sh \"/usr/bin/mvn clean install\"\n"
                + "  step([$class: 'LogParserPublisher', projectRulePath: 'logparser-rules.txt', useProjectRule: true, enableDiffBuild: true])\n"
                + "}\n", true));

        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(1));
        int lastBuildNumber = job.getLastBuild().number;
        int prevBuildNubmer = lastBuildNumber - 1;
        String url = "job/" + job.getName() + "/" + lastBuildNumber + "/diffbuild/consoleLineDiffDisplay/?prevBuild="
                + prevBuildNubmer;
        HtmlPage page = jenkinsRule.createWebClient().goTo(url);
        WebAssert.assertElementPresent(page, "1");
    }

    // Helper functions in tests

    public void writeHelper(String filename, String stuff) throws Exception {
        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        String[] sepStuff = stuff.split(sep);
        for (String line : sepStuff) {
            writer.println(line);
        }
        writer.close();
    }

    public void printHelper(String filename) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(filename));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();
            System.out.println(everything);
        } finally {
            br.close();
        }
    }

    private Elements createFileThenDelete(String stuff1, String stuff2, String htmlTag) throws Exception, IOException {
        String filename1 = "file1.txt";
        String filename2 = "file2.txt";

        File path1 = new File(filename1);
        File path2 = new File(filename2);

        writeHelper(filename1, stuff1);
        writeHelper(filename2, stuff2);

        DiffToHtmlGenerator dhgenerator = new DiffToHtmlGenerator(path1.toString(), path2.toString(), 1, 2);
        String html = dhgenerator.generateHtmlString();
        String css = dhgenerator.generateCSS();

        File htmlPath = new File("html.html");
        File cssPath = new File("style.css");

        writeToFile(html, htmlPath.toString());
        writeToFile(css, cssPath.toString());

        Document doc = Jsoup.parse(htmlPath, "UTF-8", "");
        Elements divs = doc.select(htmlTag);

        path1.delete();
        path2.delete();
        htmlPath.delete();
        cssPath.delete();
        return divs;
    }
    
    public void writeToFile(String content, String savePath) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(savePath, "UTF-8");
            writer.println(content);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
