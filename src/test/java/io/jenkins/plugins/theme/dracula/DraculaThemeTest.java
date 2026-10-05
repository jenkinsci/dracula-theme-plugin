package io.jenkins.plugins.theme.dracula;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

import io.jenkins.plugins.thememanager.Theme;
import io.jenkins.plugins.thememanager.ThemeManagerPageDecorator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebRequest;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class DraculaThemeTest {

    @Test
    void servesStylesheet(JenkinsRule j) throws Exception {
        String css = stylesheet(j);
        assertThat(css, containsString("--dracula-background: #282a36"));
        assertThat(css, containsString("--dracula-background: #fffbeb"));
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
        assertThat(AbstractDraculaTheme.cssVersion(), matchesPattern("[0-9a-f]{8}"));
        for (AbstractDraculaTheme theme :
                List.of(new DraculaTheme(), new DraculaLightTheme(), new DraculaSystemTheme())) {
            assertThat(
                    theme.getTheme().getCssUrls(),
                    contains(endsWith("dracula.css?v=" + AbstractDraculaTheme.cssVersion())));
        }
    }

    @Test
    void setsBootstrapColorMode(JenkinsRule j) {
        assertThat(new DraculaTheme().getTheme().getProperty("bootstrap", "theme"), is(Optional.of("dark")));
        assertThat(new DraculaLightTheme().getTheme().getProperty("bootstrap", "theme"), is(Optional.of("light")));

        Theme system = new DraculaSystemTheme().getTheme();
        assertThat(system.isRespectSystemAppearance(), is(true));
        assertThat(system.getProperty("bootstrap", "theme-light"), is(Optional.of("light")));
        assertThat(system.getProperty("bootstrap", "theme-dark"), is(Optional.of("dark")));
    }

    @Test
    void stylesheetCoversEveryTheme(JenkinsRule j) throws Exception {
        List<Rule> rules = rules(stylesheet(j));
        String dracula = selectors(DraculaTheme.KEY);
        String light = selectors(DraculaLightTheme.KEY);
        String system = selectors(DraculaSystemTheme.KEY);

        // Alucard is the default of the system theme, so the dark mode copy must come after it
        assertThat(
                rules.stream().map(Rule::prelude).toList(),
                contains(
                        light + ", " + system,
                        dracula,
                        "@media (prefers-color-scheme: dark)",
                        String.join(", ", dracula, light, system)));

        // In dark mode the system theme gets the Dracula block
        List<Rule> darkMode = rules(rules.get(2).body());
        assertThat(darkMode.stream().map(Rule::prelude).toList(), contains(system));
        assertThat(normalize(darkMode.get(0).body()), is(normalize(rules.get(1).body())));

        // and that block overrides everything the Alucard block sets
        Set<String> alucard = properties(rules.get(0).body());
        assertThat(alucard.size(), greaterThan(20));
        assertThat(alucard, everyItem(is(in(properties(rules.get(1).body())))));
    }

    @Test
    void pagesUseTheThemeKey(JenkinsRule j) throws Exception {
        assertRenders(j, new DraculaTheme(), DraculaTheme.KEY, false);
        assertRenders(j, new DraculaLightTheme(), DraculaLightTheme.KEY, false);
        assertRenders(j, new DraculaSystemTheme(), DraculaSystemTheme.KEY, true);
    }

    private static void assertRenders(JenkinsRule j, AbstractDraculaTheme theme, String key, boolean system)
            throws Exception {
        ThemeManagerPageDecorator.get().setTheme(theme);
        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            wc.getOptions().setJavaScriptEnabled(false);
            HtmlPage page = wc.goTo("");
            String json = normalize(page.getElementById("theme-manager-theme").getTextContent());
            assertThat(json, containsString("\"id\": \"" + key + "\""));
            assertThat(json, containsString("\"respect_system_appearance\": " + system));
            assertThat(
                    page.getWebResponse().getContentAsString(),
                    containsString("theme-dracula/dracula.css?v=" + AbstractDraculaTheme.cssVersion()));
        }
    }

    private static String stylesheet(JenkinsRule j) throws Exception {
        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            Page page = wc.goTo("theme-dracula/dracula.css", "text/css");
            return page.getWebResponse().getContentAsString();
        }
    }

    private static String selectors(String key) {
        return "[data-theme=" + key + "], .app-theme-picker__picker[data-theme=" + key + "]";
    }

    /** A rule of the stylesheet: the selector list or at-rule, and the text between its braces. */
    private record Rule(String prelude, String body) {}

    /** Top-level rules of {@code css}, without comments. */
    private static List<Rule> rules(String css) {
        String text = css.replaceAll("(?s)/\\*.*?\\*/", "");
        List<Rule> rules = new ArrayList<>();
        int depth = 0;
        int start = 0;
        int open = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '{' && depth++ == 0) {
                open = i;
            } else if (text.charAt(i) == '}' && --depth == 0) {
                rules.add(new Rule(normalize(text.substring(start, open)), text.substring(open + 1, i)));
                start = i + 1;
            }
        }
        assertThat("unbalanced braces", depth, is(0));
        return rules;
    }

    private static String normalize(String declarations) {
        return declarations.replaceAll("\\s+", " ").trim();
    }

    private static Set<String> properties(String declarations) {
        Set<String> names = new TreeSet<>();
        Matcher m = Pattern.compile("(?m)^\\s*([a-z-]+)\\s*:").matcher(declarations);
        while (m.find()) {
            names.add(m.group(1));
        }
        return names;
    }
}
