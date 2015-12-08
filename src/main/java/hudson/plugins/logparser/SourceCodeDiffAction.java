package hudson.plugins.logparser;

import java.util.List;
import java.util.Map;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import org.kohsuke.stapler.bind.JavaScriptMethod;

/**
 * SourceCodeDiffAction is the entry point for the functionality to diff source
 * codes between two builds.
 */
public class SourceCodeDiffAction implements Action {
    public final String html;
    private final Run<?, ?> owner;
    private final String fileName;

    /**
     * Construct a source code diff action
     *
     * @param job the project
     * @param build1 build number 1
     * @param build2 build number 2
     * @param launcher launcher
     * @param workspace path to workspace
     * @throws Exception if SCM fails to checkout
     */
    public SourceCodeDiffAction(Job<?, ?> job, int build1, int build2, Launcher launcher,
            FilePath workspace) throws Exception {
        this.owner = job.getBuildByNumber(build1);

        Map<String, List<String>> content1 = SCMUtils.getFilesFromBuild("*.java",
                (AbstractProject<?, ?>) job, build1, launcher, workspace);
        Map<String, List<String>> content2 = SCMUtils.getFilesFromBuild("*.java",
                (AbstractProject<?, ?>) job, build2, launcher, workspace);

        this.html = DiffToHtmlUtils.generateDiffHTML(build1, build2, "Source Code", content1,
                content2, null);
        fileName = "build_" + build1 + "_" + build2 + "_source_code_diff.html";
    }

    public Run<?, ?> getOwner() {
        return this.owner;
    }

    @Override
    public String getIconFileName() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "Source Code Diff Page";
    }

    @Override
    public String getUrlName() {
        return "sourceCodeDiffAction";
    }

    @JavaScriptMethod
    public String exportHtml(){
        return this.html;
    }

    /**
     * returns download file name
     *
     * @return download file name
     */

    @JavaScriptMethod
    public String exportFileName(){
        return this.fileName;
    }
}
