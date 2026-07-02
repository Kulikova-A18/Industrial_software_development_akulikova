package pages;

import org.openqa.selenium.By;
import java.util.Map;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.cssClass;

public class ClickPage extends BasePage implements Page {

    private final Map<String, By> buttons = Map.of(
            "Button", By.id("badButton")
    );

    @Override
    public void clickButton(String buttonName) {
        By locator = buttons.get(buttonName);
        $(locator).click();
    }

    @Override
    public void checkTextByAttributeValue(String elementName, String expectedClass) {
        By locator = buttons.get(elementName);
        $(locator).shouldHave(cssClass(expectedClass));
    }

    @Override public void fillField(String fieldName, String value) { }
    @Override public void checkText(String fieldName, String value) { }
    @Override public void selectDropdown(String fieldName, String value) { }
    @Override public void switchToFrame(String frameName) { }
    @Override public void switchToDefaultContent() { }
}