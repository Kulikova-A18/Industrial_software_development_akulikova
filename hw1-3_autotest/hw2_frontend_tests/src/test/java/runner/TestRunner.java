package runner;

import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")

@ConfigurationParameter(
        key = GLUE_PROPERTY_NAME,
        value = "steps, hooks"
)

@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "pretty, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
)

public class TestRunner {

}
