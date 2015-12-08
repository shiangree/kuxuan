package hudson.plugins.logparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.console.ConsoleNote;

/**
 * ConsoleOutputUtils contains static methods to extract information from
 * Jenkins console output.
 */
public class ConsoleOutputUtils {

    private static Pattern phasePattern = Pattern.compile("\\[\\w*\\] --- (.*) ---");
    private static Pattern separatorPattern = Pattern.compile("(\\s)*|\\[\\w*\\]( ---------*)?");
    private static Pattern sectionPattern = Pattern.compile("\\[(INFO|WARNING|ERROR)\\].*");

    /**
     * Extract title and content of maven phases from console output.
     * 
     * @param consoleOutputFile
     *            the console output file
     * @return a LinkedHashMap whose key is phase title, whose value is the
     *         content for the phase, and whose order is the same as it appears
     *         in console output
     * @throws IOException
     *             if any IO error occurs
     */
    public static LinkedHashMap<String, List<String>> extractMavenPhases(File consoleOutputFile)
            throws IOException {
        LinkedHashMap<String, List<String>> mavenPhases = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(consoleOutputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String mavenPhaseTitle = getMavenPhaseTitle(line);
                if (mavenPhaseTitle != null) {
                    mavenPhases.put(mavenPhaseTitle, getPhaseContent(reader));
                }
            }
        }

        return mavenPhases;
    }

    private static String getMavenPhaseTitle(String line) {
        line = ConsoleNote.removeNotes(line).trim();
        Matcher m = phasePattern.matcher(line);
        if (m.matches()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    private static List<String> getPhaseContent(BufferedReader reader) throws IOException {
        List<String> content = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            line = ConsoleNote.removeNotes(line);
            String trimmedLine = line.trim();
            if (separatorPattern.matcher(trimmedLine).matches()) {
                break;
            } else {
                content.add(line);
            }
        }

        return content;
    }

    /**
     * Extract sections from Jenkins console output. LogSection include: [INFO],
     * [WARNING], and [ERROR].
     * 
     * @param consoleOutputFile
     *            the Jenkins console output file
     * @return a map whose key is the section title (one of "INFO", "WARNING",
     *         or "ERROR"), and whose value is all the lines belonging to the
     *         section
     * @throws IOException
     *             if any IO error occurs
     */
    public static Map<String, List<String>> extractSections(File consoleOutputFile)
            throws IOException {
        Map<String, List<String>> sections = new HashMap<>();
        sections.put("INFO", new ArrayList<String>());
        sections.put("WARNING", new ArrayList<String>());
        sections.put("ERROR", new ArrayList<String>());
        
        try (BufferedReader reader = new BufferedReader(new FileReader(consoleOutputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = ConsoleNote.removeNotes(line);
                Matcher m = sectionPattern.matcher(line);
                if (m.matches()) {
                    sections.get(m.group(1)).add(line);
                }
            }
        }
        
        return sections;
    }
}
