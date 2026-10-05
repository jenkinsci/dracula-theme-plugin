package io.jenkins.plugins.theme.dracula;

import static io.jenkins.plugins.casc.misc.Util.toStringFromYamlFile;
import static io.jenkins.plugins.casc.misc.Util.toYamlString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import hudson.ExtensionList;
import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.impl.configurators.GlobalConfigurationCategoryConfigurator;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.casc.misc.junit.jupiter.WithJenkinsConfiguredWithCode;
import io.jenkins.plugins.casc.model.CNode;
import io.jenkins.plugins.casc.model.Mapping;
import io.jenkins.plugins.thememanager.ThemeManagerFactory;
import io.jenkins.plugins.thememanager.ThemeManagerPageDecorator;
import java.util.Objects;
import jenkins.appearance.AppearanceCategory;
import jenkins.model.GlobalConfigurationCategory;
import org.junit.jupiter.api.Test;

@WithJenkinsConfiguredWithCode
class DraculaThemeJCasCTest {

    @Test
    @ConfiguredWithCode("ConfigurationAsCode.yml")
    void testConfig(JenkinsConfiguredWithCodeRule j) {
        assertTheme(DraculaTheme.class);
    }

    @Test
    @ConfiguredWithCode("ConfigurationAsCodeLight.yml")
    void testConfigLight(JenkinsConfiguredWithCodeRule j) {
        assertTheme(DraculaLightTheme.class);
    }

    @Test
    @ConfiguredWithCode("ConfigurationAsCodeSystem.yml")
    void testConfigSystem(JenkinsConfiguredWithCodeRule j) {
        assertTheme(DraculaSystemTheme.class);
    }

    @Test
    @ConfiguredWithCode("ConfigurationAsCode.yml")
    void testExport(JenkinsConfiguredWithCodeRule j) throws Exception {
        assertExport("ConfigurationAsCodeExport.yml");
    }

    @Test
    @ConfiguredWithCode("ConfigurationAsCodeLight.yml")
    void testExportLight(JenkinsConfiguredWithCodeRule j) throws Exception {
        assertExport("ConfigurationAsCodeExportLight.yml");
    }

    @Test
    @ConfiguredWithCode("ConfigurationAsCodeSystem.yml")
    void testExportSystem(JenkinsConfiguredWithCodeRule j) throws Exception {
        assertExport("ConfigurationAsCodeExportSystem.yml");
    }

    private static void assertTheme(Class<? extends ThemeManagerFactory> type) {
        ThemeManagerPageDecorator decorator = ThemeManagerPageDecorator.get();

        ThemeManagerFactory theme = decorator.getTheme();
        assertNotNull(theme);
        assertThat(decorator.isDisableUserThemes(), is(true));
        assertThat(theme, instanceOf(type));
    }

    private void assertExport(String expectedFile) throws Exception {
        ConfigurationContext context = new ConfigurationContext(ConfiguratorRegistry.get());
        CNode themeManager = getAppearanceRoot(context).get("themeManager");

        String exported = toYamlString(themeManager);
        String expected = toStringFromYamlFile(this, expectedFile);

        assertThat(exported, is(expected));
    }

    private static Mapping getAppearanceRoot(ConfigurationContext context) {
        GlobalConfigurationCategory category =
                ExtensionList.lookup(AppearanceCategory.class).get(0);
        GlobalConfigurationCategoryConfigurator configurator = new GlobalConfigurationCategoryConfigurator(category);
        GlobalConfigurationCategory target = configurator.getTargetComponent(context);
        return Objects.requireNonNull(configurator.describe(target, context)).asMapping();
    }
}
