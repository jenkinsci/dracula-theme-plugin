package io.jenkins.plugins.theme.dracula;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

import io.jenkins.plugins.thememanager.Theme;
import java.util.Optional;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebRequest;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class DraculaThemeTest {

    @Test
    void servesStylesheet(JenkinsRule j) throws Exception {
        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            Page page = wc.goTo("theme-dracula/dracula.css", "text/css");
            assertThat(page.getWebResponse().getContentAsString(), containsString("--dracula-background: #282a36"));
        }
    }

    @Test
    void servesOnlyTheStylesheet(JenkinsRule j) throws Exception {
        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            wc.assertFails("theme-dracula/other.css", 404);
            wc.assertFails("theme-dracula/META-INF/MANIFEST.MF", 404);
        }
    }

    @Test
    void servesStylesheetOnlyOnGet(JenkinsRule j) throws Exception {
        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            wc.setThrowExceptionOnFailingStatusCode(false);
            WebRequest request = new WebRequest(wc.createCrumbedUrl("theme-dracula/dracula.css"), HttpMethod.POST);
            assertThat(wc.getPage(request).getWebResponse().getStatusCode(), is(404));
        }
    }

    @Test
    void cssUrlChangesWithContent(JenkinsRule j) {
        assertThat(DraculaTheme.cssVersion(), matchesPattern("[0-9a-f]{8}"));
        Theme theme = new DraculaTheme().getTheme();
        assertThat(theme.getCssUrls(), contains(endsWith("dracula.css?v=" + DraculaTheme.cssVersion())));
        assertThat(theme.getProperty("bootstrap", "theme"), is(Optional.of("dark")));
    }
}
