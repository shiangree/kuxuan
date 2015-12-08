package hudson.plugins.logparser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import difflib.Delta;
import difflib.DiffUtils;

/**
 * A model class that represents diff between two directories
 *
 * @author Charlie Tsai, Hanjie Wang
 */
public class SourceCodeDiff {

    // key is the name of a java file, value is the absolute path of the java file
    private Map<String, String> added;

    // key is the name of a java file, value is the absolute path of the java file
    private Map<String, String> removed;

    // key is the name of a java file, value is the array contain absolute path of both java files
    private Map<String, String[]> modified;

    // key is the name of a java file, value is the encapsulation of line diff
    private Map<String, LinesDiff> contentDiffs;

    // list of all java files
    private List<String> allFiles;

    /**
     * Construct the diff between two directories
     *
     * @param dir1 File object of the compared directory
     * @param dir2 File object of teh base directory
     * @throws IOException
     */
    public SourceCodeDiff(File dir1, File dir2) throws IOException {
        added = new HashMap<String, String>();
        removed = new HashMap<String, String>();
        modified = new HashMap<String, String[]>();
        allFiles = new ArrayList<String>();
        contentDiffs = new HashMap<String, LinesDiff>();

        Map<String, String> build1 = getFiles(dir1);
        Map<String, String> build2 = getFiles(dir2);

        for (String file : allFiles) {
            String file1 = build1.get(file);
            String file2 = build2.get(file);

            if ((file1 != null) && (file2 != null)) {
                putModified(file1, file2);
            } else if (file1 != null) {
                removed.put(file, file1);
            } else if (file2 != null) {
                added.put(file, file2);
            }
        }
    }

    /**
     * Get the files being added
     *
     * @return mapping from file name to file path of files being added
     */
    public Map<String, String> getAdded() {
        return added;
    }

    /**
     * Get the files being removed
     *
     * @return mapping from file name to file path of files being removed
     */
    public Map<String, String> getRemoved() {
        return removed;
    }

    /**
     * Get the files being modified
     *
     * @return mapping from file name to file path of both files being modified
     */
    public Map<String, String[]> getModified() {
        return modified;
    }

    /**
     * Put into the internal content diff if two files are different in content
     *
     * @param path1 String path to the compared file
     * @param path2 String path to the base file
     * @throws IOException
     */
    public void putModified(String path1, String path2) throws IOException {
        File file1 = new File(path1);
        File file2 = new File(path2);

        if (FileUtils.contentEquals(file1, file2)) {
            //System.out.println("two java files are identical");
        } else {
            modified.put(file1.getName(), new String[]{path1, path2});

            LinesDiff diff = new LinesDiff();
            List<String> lines1 = FileUtils.readLines(file1);
            List<String> lines2 = FileUtils.readLines(file2);
            List<Delta> deltas = DiffUtils.diff(lines1, lines2).getDeltas();
            for (Delta d : deltas) {
                if (d.getType() == Delta.TYPE.INSERT) {
                    diff.added.addAll((List<String>) d.getRevised().getLines());
                } else if (d.getType() == Delta.TYPE.DELETE) {
                    diff.removed.addAll((List<String>) d.getOriginal().getLines());
                } else {
                    diff.modified.add(
                            new String[]{
                                    (String) d.getOriginal().getLines().get(0),
                                    (String) d.getRevised().getLines().get(0)
                            });
                }
            }

            contentDiffs.put(file1.getName(), diff);
        }
    }

    /**
     * Get a list of all java files under a directory
     *
     * @param dir File object to the directory
     * @return Map, mapping from the file name to its file path
     */
    public Map<String, String> getFiles(File dir) {
        Map<String, String> result = new HashMap<>();
        Collection<File> tempAllFiles = FileUtils.listFiles(dir, new String[]{"java"}, true);
        for (File f : tempAllFiles) {
            result.put(f.getName(), f.getAbsolutePath());
            allFiles.add(f.getName());
        }
        return result;
    }

    /**
     * The inner class representing a lines diff between two blobs of texts
     */
    public class LinesDiff {
        List<String> added = new ArrayList<String>();
        List<String> removed = new ArrayList<String>();
        List<String[]> modified = new ArrayList<String[]>();
    }
}
