package org.jenkinsci.plugins.logparser;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import hudson.plugins.logparser.ConsoleOutputUtils;

public class ConsoleOutputUtilsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * test whether maven phase information is extracted correctly
     * 
     * @throws IOException
     *             if any IO error occurs during testing
     */
    @Test
    public void testExtractMavenPhase1() throws IOException {
        File file = folder.newFile();

        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("[INFO] --- maven-clean-plugin:2.4.1:clean (default-clean) @ mp3 ---");
            pw.println(
                    "[INFO] Deleting /home/wei29/.jenkins/jobs/mp3_maven/workspace/mp3/trunk/target");
            pw.println("[INFO] ");
        }

        LinkedHashMap<String, List<String>> lhm = ConsoleOutputUtils.extractMavenPhases(file);

        assertEquals(1, lhm.size());

        Iterator<Map.Entry<String, List<String>>> itr = lhm.entrySet().iterator();
        Map.Entry<String, List<String>> entry = itr.next();
        assertEquals("maven-clean-plugin:2.4.1:clean (default-clean) @ mp3", entry.getKey());
        assertEquals(
                Arrays.asList(
                        "[INFO] Deleting /home/wei29/.jenkins/jobs/mp3_maven/workspace/mp3/trunk/target"),
                entry.getValue());
    }

    /**
     * test extracted maven phase information keeps order
     * 
     * @throws IOException
     *             if any IO error occurs during testing
     */
    @Test
    public void testExtractMavenPhase2() throws IOException {
        File file = folder.newFile();

        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("[INFO] --- maven-hpi-plugin:1.106:hpi (default-hpi) @ log-parser ---");
            pw.println("[INFO] placeholder1");
            pw.println("[INFO] placeholder2");
            pw.println("[INFO] ");
            pw.println(
                    "[INFO] --- maven-install-plugin:2.3.1:install (default-install) @ log-parser ---");
            pw.println("[INFO] placeholder3");
            pw.println(
                    "[INFO] ------------------------------------------------------------------------");
        }

        LinkedHashMap<String, List<String>> lhm = ConsoleOutputUtils.extractMavenPhases(file);

        assertEquals(2, lhm.size());

        Iterator<Map.Entry<String, List<String>>> itr = lhm.entrySet().iterator();
        Map.Entry<String, List<String>> entry = itr.next();
        assertEquals("maven-hpi-plugin:1.106:hpi (default-hpi) @ log-parser", entry.getKey());
        assertEquals(Arrays.asList("[INFO] placeholder1", "[INFO] placeholder2"), entry.getValue());

        entry = itr.next();
        assertEquals("maven-install-plugin:2.3.1:install (default-install) @ log-parser",
                entry.getKey());
        assertEquals(Arrays.asList("[INFO] placeholder3"), entry.getValue());
    }

    /**
     * test whether section is extracted correctly from Jenkins console output
     * @throws IOException 
     */
    @Test
    public void testExtractSections1() throws IOException {
        File file = folder.newFile();
        
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("[WARNING] 1");
            pw.println("[WARNING]");
            pw.println("[INFO] 2");
            pw.println("[WARNING] 3");
        }
        
        Map<String, List<String>> sections = ConsoleOutputUtils.extractSections(file);
        
        assertEquals(3, sections.size());
        assertEquals(Arrays.asList("[WARNING] 1", "[WARNING]", "[WARNING] 3"), sections.get("WARNING"));
        assertEquals(Arrays.asList("[INFO] 2"), sections.get("INFO"));
        assertEquals(0, sections.get("ERROR").size());
    }

    /**
     * test whether section is extracted correctly from Jenkins console output
     * @throws IOException 
     */
    @Test
    public void testExtractSections2() throws IOException {
File file = folder.newFile();
        
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("[WARNING] 1");
            pw.println("[WARNING]");
            pw.println("[INFO] 2");
            pw.println("[WARNING] 3");
            pw.println("[ERROR] 4");
        }
        
        Map<String, List<String>> sections = ConsoleOutputUtils.extractSections(file);
        
        assertEquals(3, sections.size());
        assertEquals(Arrays.asList("[WARNING] 1", "[WARNING]", "[WARNING] 3"), sections.get("WARNING"));
        assertEquals(Arrays.asList("[INFO] 2"), sections.get("INFO"));
        assertEquals(Arrays.asList("[ERROR] 4"), sections.get("ERROR"));
    }

}
