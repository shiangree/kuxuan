package hudson.plugins.logparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A model class represents sections of console logs
 *
 * @author Hanjie Wang, Xiang Li
 */
public class LogSection {
    // The build number
    int build;

    // The actual diff
    HashMap<String, List<String>> data;

    /**
     * Construct sections of console logs
     *
     * @param build the build number
     */
    public LogSection(int build) {
        this.build = build;
        data = new HashMap<String, List<String>>();
    }

    /**
     * Set the internal mapping from section name to section content
     *
     * @param sectionName the section name
     * @param list the section's corresponding content
     */
    public void setData(String sectionName, List<String> list) {
        data.put(sectionName, list);
    }

    /**
     * Construct a sections of console logs from a text blob
     *
     * @param build the build number
     * @param list the text blob
     */
    public LogSection(int build, List<String> list) {
        this(build);
        List<String> errorList = new ArrayList<String>();
        List<String> warningList = new ArrayList<String>();
        List<String> infoList = new ArrayList<String>();
        for (String s : list) {
            if (s.startsWith("[" + LogParserConsts.ERROR))
                errorList.add(s);
            else if (s.startsWith("[" + LogParserConsts.INFO))
                infoList.add(s);
            else if (s.startsWith("[" + LogParserConsts.WARNING))
                warningList.add(s);

        }
        setData(LogParserConsts.ERROR, errorList);
        setData(LogParserConsts.INFO, infoList);
        setData(LogParserConsts.WARNING, warningList);
    }

    /**
     * Get the internal mapping from section name to section content
     *
     * @return the mapping
     */
    public HashMap<String, List<String>> getData() {
        return data;
    }

}