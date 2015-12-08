package hudson.plugins.logparser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;

/**
 * SCMUtils contains methods to get file from SCM.
 */
public class SCMUtils {
    /**
     * Get all the files which match the specified file name pattern from a
     * specified build of a project.
     * 
     * @param fileNamePattern
     *            the file name pattern
     * @param project
     *            the Jenkins project
     * @param buildNum
     *            the build number of the project from which to get files
     * @param launcher
     *            Abstracts away the machine that the files will be checked out
     * @param workspace
     *            workspace of the project
     * @return all the files matching the specified file name pattern as a map,
     *         with file name as the key, and file content as the value
     * @throws IOException
     *             if any IO error occurs
     * @throws InterruptedException
     *             the SCM checkout operation is interrupted
     */
    public static Map<String, List<String>> getFilesFromBuild(String fileNamePattern,
            AbstractProject<?, ?> project, int buildNum, Launcher launcher, FilePath workspace)
                    throws IOException, InterruptedException {

        SCM scm = project.getScm();

        FilePath tempDir = new FilePath(workspace, "tmp" + System.currentTimeMillis());
        Run<?, ?> build = project.getBuildByNumber(buildNum);
        scm.checkout(build, launcher, tempDir, TaskListener.NULL, null, null);

        File f = new File(tempDir.toURI());
        Collection<File> files = FileUtils.listFiles(f, new WildcardFileFilter(fileNamePattern),
                TrueFileFilter.INSTANCE);
        Map<String, List<String>> allContent = new LinkedHashMap<>();
        for (File file : files) {
            List<String> content = FileUtils.readLines(file, "UTF-8");
            allContent.put(file.getAbsolutePath().substring(f.getAbsolutePath().length() + 1),
                    content);
        }

        tempDir.deleteRecursive();
        return allContent;
    }
}
