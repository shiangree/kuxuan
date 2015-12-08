package org.jenkinsci.plugins.logparser;


import hudson.plugins.logparser.Dependency;
import hudson.plugins.logparser.DependencyDiffUtils;
import org.junit.Before;
import org.junit.Test;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class DependencyDiffTest {
    ArrayList<Dependency> deplist1;
    ArrayList<Dependency> deplist2;
    Map<String, ArrayList<Dependency>> difflist; 
    
    @Before
    public void setUp() throws Exception
    {
        BufferedReader br1 = new BufferedReader(
                new FileReader("src/test/resources/org/jenkinsci/plugins/logparser/pom1.xml"));
        BufferedReader br2 = new BufferedReader(
                new FileReader("src/test/resources/org/jenkinsci/plugins/logparser/pom2.xml"));
        List<String> contentlist1 = new ArrayList<String>();
        List<String> contentlist2 = new ArrayList<String>();
        String line = "";
        while ((line = br1.readLine()) != null) {
            contentlist1.add(line);
        }
        while ((line = br2.readLine()) != null) {
            contentlist2.add(line);
        }

        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        for (String s : contentlist1) {
            baos1.write(s.getBytes());
        }
        for (String s : contentlist2) {
            baos2.write(s.getBytes());
        }

        byte[] bytes1 = baos1.toByteArray();
        InputStream in1 = new ByteArrayInputStream(bytes1);
        deplist1 = DependencyDiffUtils.parsePom(in1);
        byte[] bytes2 = baos2.toByteArray();
        InputStream in2 = new ByteArrayInputStream(bytes2);
        deplist2 = DependencyDiffUtils.parsePom(in2);
        difflist = DependencyDiffUtils.diff(deplist1, deplist2);
        br1.close();
        br2.close();
    }
    /**
 *      * test the diff function from two pom xml files
 *           * 
 *                * @throws SAXException and IOException
 *                     */
    @Test
    public void dependencyDiffCoreTestModified() throws Exception {
        ArrayList<Dependency> modified = difflist.get("Modified");
        assertEquals(modified.size(), 2);
    }
    @Test
    public void dependencyDiffCoreTestAdded() throws Exception {
        ArrayList<Dependency> added = difflist.get("Added");
        assertEquals(added.size(), 3);
    }
    
    @Test
    public void dependencyDiffCoreTestDeleted() throws Exception {
        ArrayList<Dependency> deleted = difflist.get("Deleted");
        assertEquals(deleted.size(), 1);        
    }
    /**
 *      * test the the content of the generated html page
 *           * 
 *                * @throws IOException
 *                     */
    @Test
    public void dependencyDiffHtmlTest() throws Exception {
        String html = DependencyDiffUtils.toHtml(deplist1, deplist2, difflist, 2, 1);
        String modifiedcontent ="<b>Dependency modified:</b><br />\n"
                + "<br> groupId: com.opencsv</br>\n"
                + "<br> artifactId: opencsv</br>\n"
                + "<br> build #1 dependency version: 3.2</br>\n"
                + "<br> build #2 dependency version: 3.5<hr>\n"
                + "<br />\n"
                + "<br> groupId: org.apache.struts</br>\n"
                + "<br> artifactId: struts2-core</br>\n"
                + "<br> build #1 dependency version: 2.5.20</br>\n"
                + "<br> build #2 dependency version: 2.3.20<hr>\n"
                + "<br />\n";
        assertTrue(html.contains(modifiedcontent));
        String addedcontent = "<b>Dependency added to build #1</b><br />\n"
                + "<br> groupId: com.google.code.gson</br>\n"
                + "<br> artifactId: guice</br>\n"
                + "<br> version: 4.0</br>\n"
                + "<hr>\n"
                + "<br />\n"
                + "<br> groupId: org.scalatest</br>\n"
                + "<br> artifactId: scalatest_2.10</br>\n"
                + "<br> version: 3.0.0-M14</br>\n"
                + "<hr>\n"
                + "<br />\n"
                + "<br> groupId: org.scalatest</br>\n"
                + "<br> artifactId: scalatest_2.10</br>\n"
                + "<br> version: 3.0.0-M14</br>\n"
                + "<hr>\n"
                + "<br />\n";
        assertTrue(html.contains(addedcontent));
        String deletedcontent = "<b>Dependency deleted from build #2</b><br />\n"
                + "<br> groupId: com.google.inject</br>\n"
                + "<br> artifactId: guice</br>\n"
                + "<br> version: 4.0</br>\n"
                + "<hr>\n"
                + "<br />\n";
        assertTrue(html.contains(deletedcontent));
    }
    
}

