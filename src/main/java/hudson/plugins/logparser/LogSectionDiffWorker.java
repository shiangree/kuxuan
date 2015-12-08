package hudson.plugins.logparser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hudson.Functions;
import hudson.console.ConsoleNote;
import hudson.model.Run;
import jenkins.model.Jenkins;

/**
 * LogSectionDiffWorker contains methods to diff logs for two builds, and write
 * the diff results to HTML.
 */
public class LogSectionDiffWorker {
    private Run<?, ?> build1;
    private Run<?, ?> build2;

    public LogSectionDiffWorker(Run<?, ?> build1, Run<?, ?> build2) {
        this.build1 = build1;
        this.build2 = build2;
    }

    /**
     * Read the console output for the specified build.
     * 
     * @param build
     * @return the console output for the build as a list of String, with each
     *         line as a String, and tags removed.
     * @throws IOException
     */
    public static List<String> getBuildLog(Run<?, ?> build) throws IOException {
        List<String> buildLog = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new FileReader(build.getLogFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                buildLog.add(ConsoleNote.removeNotes(line));
            }
        }
        return buildLog;
    }

    public void writeHeader(StringBuilder sb) {
        sb.append("<br>");
        sb.append(String.format(
                "<font size = \"6\">Section Diff for Console Output between Build %s and %s</font>",
                build1.number, build2.number));
        sb.append("<br><br>");
        sb.append("<span style=\"color:green;font-weight:bold\">added<br></span>");
        sb.append("<span style=\"color:blue;font-weight:bold\">modified<br></span>");
        sb.append("<span style=\"color:red;font-weight:bold\">removed<br></span>");
        sb.append("<br>");
    }

    public void writeSectionHeader(StringBuilder sb, String sectionName, String imgName) {
        String hudsonRoot = Jenkins.getInstance().getRootUrl();
        String iconLocation = String.format("%s/images/16x16/", Functions.getResourcePath());
        sb.append("<img src=\"" + hudsonRoot + "/" + iconLocation + imgName + "\" "
                + "style=\"margin: 2px;\" width=\"24\" alt=\"Error Icon\" height=\"24\" />");
        sb.append(String.format("<strong>%s</strong><br>", sectionName));
    }

    public String writeSectionDiffToHTMLs() throws IOException {
        int num1 = build1.getNumber();
        int num2 = build2.getNumber();

        List<String> build1Log = LogSectionDiffWorker.getBuildLog(build1);
        List<String> build2Log = LogSectionDiffWorker.getBuildLog(build2);

        LogSection section1 = new LogSection(num1, build1Log);
        LogSection section2 = new LogSection(num2, build2Log);
        LogSectionDiff sectionDiff = new LogSectionDiff(section1, section2);

        Map<String, String> sectionHTMLs = LogSectionDiffWorker.sectionDiffToHTML(sectionDiff);

        StringBuilder sb = new StringBuilder();
        writeHeader(sb);

        for (Map.Entry<String, String> entry : sectionHTMLs.entrySet()) {
            if (entry.getKey().equals("ERROR")) {
                writeSectionHeader(sb, "Error", "red.gif");
            } else if (entry.getKey().equals("WARNING")) {
                writeSectionHeader(sb, "Warning", "yellow.gif");
            } else if (entry.getKey().equals("INFO")) {
                writeSectionHeader(sb, "Info", "blue.gif");
            }
            sb.append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * Transform section diff result to HTML string, one for each section.
     * 
     * @param sectionDiff
     *            the section diff result to transform
     * @return a map whose key is the section name, whose value is the HTML
     *         string for diff of the section
     */
    public static Map<String, String> sectionDiffToHTML(LogSectionDiff sectionDiff) {
        Map<String, String> sectionHTMLs = new HashMap<String, String>();

        for (Map.Entry<String, LogSectionDiff.Diff> entry : sectionDiff.getData().entrySet()) {
            sectionHTMLs.put(entry.getKey(), LogSectionDiffWorker.diffToHTML(entry.getValue()));
        }

        return sectionHTMLs;
    }

    public static String diffToHTML(LogSectionDiff.Diff diff) {
        StringBuilder sb = new StringBuilder();

        sb.append("<span style=\"color:green;font-weight:bold\">");
        for (String line : diff.added) {
            sb.append(line + "<br>");
        }
        sb.append("</span>");

        sb.append("<span style=\"color:blue;font-weight:bold\">");
        for (String line : diff.modified) {
            sb.append(line + "<br>");
        }
        sb.append("</span>");

        sb.append("<span style=\"color:red;font-weight:bold\">");
        for (String line : diff.removed) {
            sb.append(line + "<br>");
        }
        sb.append("</span>");

        sb.append("<br><br><br>");
        return sb.toString();
    }
}
