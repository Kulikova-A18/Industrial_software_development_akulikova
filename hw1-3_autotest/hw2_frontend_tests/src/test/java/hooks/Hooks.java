package hooks;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import io.qameta.allure.selenide.AllureSelenide;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

public class Hooks {

    private static final Logger log = LoggerFactory.getLogger(Hooks.class);

    @BeforeAll
    public static void setup() {
        log.info("Установка конфигурации браузера для Linux");
        
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10_000;
        Configuration.headless = false;

        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .screenshots(true)
                .savePageSource(false)
                .includeSelenideSteps(true)
        );
    }

    @After
    public void addScreenshotOnFailure(Scenario scenario) {
        if (scenario.isFailed()) {
            if (WebDriverRunner.hasWebDriverStarted()) {
                try {
                    byte[] screenshot = ((TakesScreenshot) WebDriverRunner.getWebDriver())
                            .getScreenshotAs(OutputType.BYTES);

                    Allure.getLifecycle().addAttachment(
                            "Screenshot on failure",
                            "image/png",
                            "png",
                            new ByteArrayInputStream(screenshot)
                    );
                } catch (Exception e) {
                    log.error("Failed to take screenshot", e);
                }
            }
        }
    }

    @io.cucumber.java.AfterAll
    public static void afterAll() {
        log.info("Закрытие веб-драйвера");
        SelenideLogger.removeListener("AllureSelenide");
        Selenide.closeWebDriver();
    }
}