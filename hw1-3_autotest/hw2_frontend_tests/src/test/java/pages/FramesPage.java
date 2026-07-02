package pages;

import org.openqa.selenium.By;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;

public class FramesPage extends BasePage implements Page {

    private final Map<String, String> frames = Map.of(
            "outerFrame", "frame-outer",
            "innerFrame", "frame-inner"
    );

    private final Map<String, By> labels = Map.of(
            "Result", By.id("result")
    );

    private final Map<String, By> buttons = Map.of(
            "ButtonByDataAttribute", By.cssSelector("button[data-action=\"edit\"]"),
            "ButtonByText",          By.xpath("//button[text()='Submit']"),
            "ButtonByName",          By.cssSelector("button[name=\"my-button\"]"),
            "ButtonByXPath",         By.xpath("//button[@class='btn-class']")
    );

    @Override
    public void clickButton(String buttonName) {
        By locator = buttons.get(buttonName);
        click(locator);
    }

    @Override
    public void checkText(String fieldName, String value) {
        By locator = labels.get(fieldName);
        shouldHaveText(locator, value);
    }

    @Override
    public void checkTextByAttributeValue(String fieldName, String value) {
        By locator = labels.get(fieldName);
        shouldHaveText(locator, value);
    }

    public void switchToFrame(String frameId) {
        String frame = frames.get(frameId);
        switchTo().frame(frame);
    }

    public void switchToDefaultContent() {
        switchTo().defaultContent();
    }

    @Override
    public void fillField(String fieldName, String value) {

    }

    @Override
    public void selectDropdown(String fieldName, String value) {

    }
}