package hudson.plugins.logparser;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import hudson.model.Action;
import hudson.model.Run;
import hudson.model.AbstractProject;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Job;

/**
 *  * DependencyDiffAction is the action to diff dependencies in POM.xml between
 *   * two builds.
 *    */
public class DependencyDiffAction implements Action {
    private Run<?, ?> prevBuild;
    public String html;
    private final Run<?, ?> owner;
    public String fileName;

    /**
 *      * Construct a dependency diff action
 *           *
 *                * @param job
 *                     *            the project
 *                          * @param build1
 *                               *            build number 1
 *                                    * @param build2
 *                                         *            build number 2
 *                                              * @param launcher
 *                                                   *            launcher
 *                                                        * @param workspace
 *                                                             *            path to workspace
 *                                                                  * @throws Exception
 *                                                                       *             if SCM fails to checkout
 *                                                                            */
    public DependencyDiffAction(Job<?, ?> job, int build1, int build2, Launcher launcher, FilePath workspace)
            throws Exception {
        this.prevBuild = job.getBuildByNumber(build2);
        this.owner = job.getBuildByNumber(build1);
        Map<String, List<String>> pomcontent1 = SCMUtils.getFilesFromBuild("pom.xml", (AbstractProject<?, ?>) job,
                build1, launcher, workspace);
        Map<String, List<String>> pomcontent2 = SCMUtils.getFilesFromBuild("pom.xml", (AbstractProject<?, ?>) job,
                build2, launcher, workspace);
        this.html = "";

        String configPath = job.getConfigFile().getFile().getAbsolutePath();
        BufferedReader br = new BufferedReader(new FileReader(configPath));
        String line;
        String pomPath = "null";
        while ((line = br.readLine()) != null) {
            if (line.contains("pom.xml")) {
                pomPath = line.substring(line.indexOf("<rootPOM>") + 9, line.indexOf("</rootPOM>")).trim();
                break;
            }
        }
        List<String> contentlist1 = pomcontent1.get(pomPath);
        List<String> contentlist2 = pomcontent2.get(pomPath);
        
        InputStream in1 = getInStream(contentlist1);
        ArrayList<Dependency> deplist1 = DependencyDiffUtils.parsePom(in1);
        InputStream in2 = getInStream(contentlist2);
        ArrayList<Dependency> deplist2 = DependencyDiffUtils.parsePom(in2);
        br.close();
        this.html += DependencyDiffUtils.toHtml(deplist1, deplist2, DependencyDiffUtils.diff(deplist1, deplist2),
                owner.getNumber(), prevBuild.getNumber());
        this.fileName = "build_" + prevBuild.getNumber() + "_" + owner.getNumber() + "_dependency_diff.html";

    }
    
    private InputStream getInStream(List<String> contentlist) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for(String s : contentlist)
        {
            baos.write(s.getBytes());
        }
        byte[] bytes = baos.toByteArray();
        return new ByteArrayInputStream(bytes);
    }
    
    public Run<?, ?> getOwner() {
        return this.owner;
    }

    public Run<?, ?> getPrevBuild() {
        return this.prevBuild;
    }

    public String getHtml() {
        return this.html;
    }

    @JavaScriptMethod
    public String exportHtml() {
        return this.html;
    }

    @JavaScriptMethod
    public String exportFileName() {
        return this.fileName;
    }

    @Override
    public String getIconFileName() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "DependencyDiff Page";
    }

    @Override
    public String getUrlName() {
        return "dependencyDiff";
    }
}

