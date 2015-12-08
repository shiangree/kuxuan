package hudson.plugins.logparser;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildStepMonitor;

public class LogParserBuildStep implements BuildStep {

    public boolean prebuild(final AbstractBuild<?, ?> build, final BuildListener listener) {
        return true;
    }

    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
            final BuildListener listener) throws InterruptedException, IOException {
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return null;
    }

    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        return Collections.emptyList();
    }
}