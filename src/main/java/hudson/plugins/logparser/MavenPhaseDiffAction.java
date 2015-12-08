package hudson.plugins.logparser;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.kohsuke.stapler.bind.JavaScriptMethod;

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;

/**
 * The action to diff the maven information in Jenkins console output between
 * two builds of a project
 */
public class MavenPhaseDiffAction implements Action {

    public final String html;
    private final Run<?, ?> owner;
    private String fileName;

    /**
     * Construct a MavenPhaseDiffAction
     * 
     * @param job
     * @param job
     *            the project
     * @param build1Num
     *            build number 1
     * @param build2Num
     *            build number 2
     * @throws IOException
     */
    public MavenPhaseDiffAction(Job<?, ?> job, int build1Num, int build2Num) throws IOException {
        Run<?, ?> build1 = job.getBuildByNumber(build1Num);
        Run<?, ?> build2 = job.getBuildByNumber(build2Num);

        owner = build1;

        Map<String, List<String>> mavenPhases1 = ConsoleOutputUtils
                .extractMavenPhases(build1.getLogFile());
        Map<String, List<String>> mavenPhases2 = ConsoleOutputUtils
                .extractMavenPhases(build2.getLogFile());

        this.html = DiffToHtmlUtils.generateDiffHTML(build1Num, build2Num, "Maven Phase",
                mavenPhases1, mavenPhases2, null);
        
        fileName = "build_" + build1Num + "_" + build2Num + "_maven_phase_diff.html";
    }

    /**
     * @return the base build for the action
     */
    public Run<?, ?> getOwner() {
        return this.owner;
    }
    
    /**
     * returns html content
     * 
     * @return html content
     */
    
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

    @Override
    public String getIconFileName() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "Maven Phase Diff Page";
    }

    @Override
    public String getUrlName() {
        return "mavenPhaseDiffAction";
    }

}
