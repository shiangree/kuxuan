package hudson.plugins.logparser;

import java.io.IOException;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import hudson.model.Action;
import hudson.model.Run;

/**
 * This class gets user input, calls DiffToHtmlGenerator to generate the diff
 * result in html format, and then display the diff result on the build page
 */

public class ConsoleLineDiffDisplay implements Action{

    /**
     * the current build object
     */
    private final Run<?, ?> currentBuild;

    /**
     * previous build number got from request
     */
    private String prevBuild;

    /**
     * html string that will be displayed on the page
     */
    private String html;
    
    private String fileName;

    /**
     * Create new ConsoleLineDiffDisplay object
     * 
     * @param build
     *            current build object
     */
    public ConsoleLineDiffDisplay(Run<?, ?> build) {
        this.currentBuild = build;
        this.prevBuild = Stapler.getCurrentRequest().getParameter("prevBuild");
        int currBuildNum = build.getNumber();
        int prevBuildNum = Integer.parseInt(prevBuild);

        Run<?, ?> previousBuild = currentBuild.getParent().getBuildByNumber(prevBuildNum);

        String currLogFileLocation = currentBuild.getLogFile().getAbsolutePath();
        String prevLogFileLocation = previousBuild.getLogFile().getAbsolutePath();

        DiffToHtmlGenerator d2h = null;
        try {
            d2h = new DiffToHtmlGenerator(prevLogFileLocation, currLogFileLocation, currBuildNum, prevBuildNum);
        } catch (IOException e) {
            e.printStackTrace();
        }

        html = d2h.generateHtmlString();
        
        fileName = "build_" + prevBuildNum + "_" + currBuildNum + "_console_line_diff.html";
    }

    /**
     * returns the current build object
     * 
     * @return the current build object
     */
    public Run<?, ?> getOwner() {
        return this.currentBuild;
    }

    /**
     * returns previous build number
     * 
     * @return previous build number
     */
    public String getPrevBuild() {
        return this.prevBuild;
    }

    /**
     * returns html string
     * 
     * @return html string
     */
    public String getHtml() {
        return this.html;
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

    /** * {@inheritDoc} */
    @Override
    public String getDisplayName() {
        return "Console Line Diff Result Page";
    }

    /** * {@inheritDoc} */
    @Override
    public String getIconFileName() {
        return "";
    }

    /** * {@inheritDoc} */
    @Override
    public String getUrlName() {
        return "consoleLineDiffDisplay";
    }
}
