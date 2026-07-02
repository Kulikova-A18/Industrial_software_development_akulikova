package pages;

import org.openqa.selenium.By;

import java.util.Map;

public class SampleAppPage extends BasePage implements Page {

    private final Map<String, By> fields = Map.of(
            "UserName", By.name("UserName"),
            "Password", By.name("Password")

    );

    private final Map<String, By> buttons = Map.of(
            "Login", By.id("login")
    );

    private final Map<String, By> labels = Map.of(
            "Loginstatus", By.id("loginstatus")
    );

    @Override
    public void fillField(String fieldName, String value) {
        By locator = fields.get(fieldName);
        fillField(locator, value);
    }

    @Override
    public void clickButton(String buttonName) {
        By locator = buttons.get(buttonName);
        click(locator);
    }

    @Override
    public void checkText(String fieldName, String value) {
        By locator = fields.get(fieldName);
        shouldHaveText(locator, value);
    }

    @Override
    public void checkTextByAttributeValue(String fieldName, String value) {
        By locator = labels.get(fieldName);
        shouldHaveText(locator, value);
    }

    @Override
    public void selectDropdown(String fieldName, String value) {

    }

    @Override
    public void switchToFrame(String frameName) {

    }

    @Override
    public void switchToDefaultContent() {

    }
}