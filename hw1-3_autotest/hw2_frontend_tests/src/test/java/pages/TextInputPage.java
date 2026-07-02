package pages;

import org.openqa.selenium.By;
import java.util.Map;

public class TextInputPage extends BasePage implements Page {

    private final Map<String, By> fields = Map.of(
            "newButtonName", By.id("newButtonName")
    );

    private final Map<String, By> buttons = Map.of(
            "updatingButton", By.id("updatingButton")
    );

    private final Map<String, By> labels = Map.of(
            "updatingButton", By.id("updatingButton")
    );

    @Override
    public void fillField(String fieldName, String value) {
        fillField(fields.get(fieldName), value);
    }

    @Override
    public void clickButton(String buttonName) {
        click(buttons.get(buttonName));
    }

    @Override
    public void checkTextByAttributeValue(String elementName, String value) {
        shouldHaveText(labels.get(elementName), value);
    }

    @Override public void checkText(String fieldName, String value) { }
    @Override public void selectDropdown(String fieldName, String value) { }
    @Override public void switchToFrame(String frameName) { }
    @Override public void switchToDefaultContent() { }
}