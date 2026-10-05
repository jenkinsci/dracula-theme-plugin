package io.jenkins.plugins.theme.dracula;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.UnprotectedRootAction;
import jakarta.servlet.ServletException;
import java.io.IOException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.verb.GET;

@Extension
public class DraculaRootAction implements UnprotectedRootAction {

    @Override
    public String getIconFileName() {
        return null; /* no UI */
    }

    @Override
    public String getDisplayName() {
        return null; /* no UI */
    }

    @Override
    public String getUrlName() {
        return "theme-" + DraculaThemeDescriptor.ID;
    }

    /**
     * Serves the theme stylesheet without a permission check, so the login page is themed too.
     * The file is public and reading it has no side effects.
     */
    @GET
    @SuppressWarnings("lgtm[jenkins/no-permission-check]")
    public void doDynamic(StaplerRequest2 req, StaplerResponse2 rsp) throws IOException, ServletException {
        String cssFile = req.getRestOfPath();
        if (cssFile.startsWith("/")) {
            cssFile = cssFile.substring(1);
        }
        if (!AbstractDraculaTheme.CSS.equals(cssFile)) {
            rsp.sendError(404);
            return;
        }
        final Plugin plugin = Jenkins.get().getPlugin("dracula-theme");
        if (plugin == null) {
            rsp.sendError(404);
            return;
        }
        plugin.doDynamic(req, rsp);
    }
}
