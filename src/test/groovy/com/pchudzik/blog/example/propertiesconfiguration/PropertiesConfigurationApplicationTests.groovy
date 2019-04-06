package com.pchudzik.blog.example.propertiesconfiguration


import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.support.PropertiesLoaderUtils
import spock.lang.Specification

import static com.pchudzik.blog.example.propertiesconfiguration.PropertiesConfigurationApplicationTests.ConfigurationKeysAlignmentChecker.checkConfigurationKeys
import static com.pchudzik.blog.example.propertiesconfiguration.PropertiesConfigurationApplicationTests.ConfigurationLoader.loadAllConfigurations

class PropertiesConfigurationApplicationTests extends Specification {

    def "properties keys are aligned across all environments"() {
        given:
        final allConfigurations = loadAllConfigurations(new File("src/main/resources"))

        when:
        final keysAlignmentChecker = checkConfigurationKeys(allConfigurations)

        then:
        assert keysAlignmentChecker.propertiesKeysAreAligned  \
            : "Not aligned configuration keys:\n" + keysAlignmentChecker.alignmentSummary
    }

    private static class ConfigurationKeysAlignmentChecker {
        private List<ConfigurationsKeyAlignmentSummary> alignmentSummary

        static ConfigurationKeysAlignmentChecker checkConfigurationKeys(List<ConfigurationLoader.ProfileConfiguration> configurations) {
            def allKeys = configurations
                    .collect { it.configurationKeys }
                    .flatten()
                    .toSet()

            return new ConfigurationKeysAlignmentChecker(alignmentSummary: allKeys.collect { key ->
                new ConfigurationsKeyAlignmentSummary(
                        configurationKey: key,
                        presentIn: findAllKeys(configurations, { profileConfig -> profileConfig.hasKey(key) }),
                        missingIn: findAllKeys(configurations, { profileConfig -> !profileConfig.hasKey(key) }))
            })
        }

        private static List<String> findAllKeys(List<ConfigurationLoader.ProfileConfiguration> configurations, Closure<Boolean> keyMatcher) {
            configurations.findAll(keyMatcher).collect { it.fileName }
        }

        String getAlignmentSummary() {
            findAllInvalidConfigurations()
                    .collect { "    " + it.summary }
                    .join("\n")
        }

        boolean getPropertiesKeysAreAligned() {
            findAllInvalidConfigurations().isEmpty()
        }

        private List<ConfigurationsKeyAlignmentSummary> findAllInvalidConfigurations() {
            alignmentSummary.findAll { !it.missingIn.isEmpty() }
        }

        private static class ConfigurationsKeyAlignmentSummary {
            private String configurationKey
            private List<String> missingIn
            private List<String> presentIn

            private String getSummary() {
                "Configuration key: '${configurationKey}' missing in ${missingIn} present in ${presentIn}"
            }
        }
    }

    private static class ConfigurationLoader {
        static List<ProfileConfiguration> loadAllConfigurations(File location) {
            final files = location.listFiles({ dir, name -> name.endsWith(".properties") } as FilenameFilter)

            if (files == null || files.length == 0) {
                throw new IllegalStateException("No configuration found in directory " + location.getAbsolutePath())
            }

            return files
                    .collect {
                        new ProfileConfiguration(
                                fileName: it.name,
                                configurationKeys: loadPropertiesKeys(it))
                    }
        }

        private static List<String> loadPropertiesKeys(File file) {
            Collections
                    .list(PropertiesLoaderUtils
                            .loadProperties(new FileSystemResource(file))
                            .keys())
                    .collect { Objects.toString(it) }
        }

        static class ProfileConfiguration {
            private String fileName
            private List<String> configurationKeys

            boolean hasKey(String key) {
                configurationKeys.contains(key)
            }
        }
    }
}
