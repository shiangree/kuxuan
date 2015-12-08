package hudson.plugins.logparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffUtils;

/**
 * A model class represents diff between two sections of console logs
 *
 * @author Hanjie Wang, Xiang Li
 */
public class LogSectionDiff {
    // Build numbers
    int build1;
    int build2;

    // The actual diff, mapping from the tag of section to the diff of this section
    HashMap<String, Diff> data = new LinkedHashMap<String, Diff>();

    /**
     * The inner class representing diff of two text blobs
     */
    class Diff {
        List<String> added = new ArrayList<String>();
        List<String> removed = new ArrayList<String>();
        List<String> modified = new ArrayList<String>();
    }

    /**
     * Construct a diff between two sections of console logs
     *
     * @param logSection1 the compared section
     * @param logSection2 the base section
     */
    public LogSectionDiff(LogSection logSection1, LogSection logSection2) {
        this.build1 = logSection1.build;
        this.build2 = logSection2.build;

        List<String> allSectionNames = Arrays.asList(LogParserConsts.ERROR, LogParserConsts.WARNING,
                LogParserConsts.INFO);

        for (String sectionName : allSectionNames) {
            if (logSection1.data.containsKey(sectionName)
                    && logSection2.data.containsKey(sectionName)) {
                List<String> build1Strings = logSection1.data.get(sectionName);
                List<String> build2Strings = logSection2.data.get(sectionName);
                this.data.put(sectionName, generateDiff(build1Strings, build2Strings));
            }
        }
    }

    private Diff generateDiff(List<String> base, List<String> comp) {
        Diff diff = new Diff();

        List<Delta> deltas = DiffUtils.diff(base, comp).getDeltas();
        for (Delta delta : deltas) {
            if (delta.getType() == TYPE.INSERT) {
                diff.added.addAll((List<String>) delta.getRevised().getLines());
            } else if (delta.getType() == TYPE.DELETE) {
                diff.removed.addAll((List<String>) delta.getOriginal().getLines());
            } else if (delta.getType() == TYPE.CHANGE) {
                diff.modified.addAll((List<String>) delta.getRevised().getLines());
            }
        }

        return diff;
    }

    public enum DiffType {
        ADD, REMOVED, MODIFIED
    }

    /**
     * Get the diff by section name and diff type
     * @param section the section name
     * @param type the diff type
     * @return list of changed texts
     */
    public List<String> getDiffBySection(String section, DiffType type) {
        if (type == DiffType.ADD) {
            return data.get(section).added;
        } else if (type == DiffType.REMOVED) {
            return data.get(section).removed;
        } else if (type == DiffType.MODIFIED) {
            return data.get(section).modified;
        } else {
            return null;
        }
    }

    /**
     * Get the inner mapping from section name to section diff
     * @return the mapping
     */
    public Map<String, Diff> getData() {
        return this.data;
    }
}