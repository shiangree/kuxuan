package hudson.plugins.logparser;

import java.io.IOException;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import com.google.gson.Gson;

import hudson.model.Action;
import hudson.model.Run;

public class LogSectionDiffAction implements Action {
    final private Run<?, ?> thisBuild;
    private String thatBuild;
    public String html;
    private String fileName;

    public LogSectionDiffAction(Run<?, ?> build) {
        this.thisBuild = build;
        this.thatBuild = Stapler.getCurrentRequest().getParameter("prevBuild");
        Run<?, ?> that = thisBuild.getParent().getBuildByNumber(Integer.parseInt(this.thatBuild));
        LogSectionDiffWorker lsdw = new LogSectionDiffWorker(thisBuild, that);
        try {
            this.html = lsdw.writeSectionDiffToHTMLs();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.html += "<br><br>";
        
        fileName = "build_" + thatBuild + "_" + thisBuild.getNumber() + "_console_section_diff.html";
    }

    public Run<?, ?> getOwner() {
        return this.thisBuild;
    }

    public String getPrevBuild() {
        return this.thatBuild;
    }
    
    /**
 *      * returns html content
 *           * 
 *                * @return html content
 *                     */
    
    @JavaScriptMethod
    public String exportHtml(){
        return this.html;
    }
    
    /**
 *      * returns download file name
 *           *  
 *                * @return download file name
 *                     */
    
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
        return "SectionDiff Page";
    }

    @Override
    public String getUrlName() {
        return "logSectionDiffAction";
    }
}

