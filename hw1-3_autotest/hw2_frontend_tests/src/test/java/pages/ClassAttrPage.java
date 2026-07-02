package pages;

import org.openqa.selenium.By;
import java.util.Map;

public class ClassAttrPage extends BasePage implements Page {

    private final Map<String, By> buttons = Map.of(
            "PrimaryButton", By.xpath("//button[contains(concat(' ', normalize-space(@class), ' '), ' btn-primary ')]")
    );

    @Override
    public void clickButton(String buttonName) {
        click(buttons.get(buttonName));
    }

    @Override public void fillField(String fieldName, String value) { }
    @Override public void checkText(String fieldName, String value) { }
    @Override public void checkTextByAttributeValue(String fieldName, String value) { }
    @Override public void selectDropdown(String fieldName, String value) { }
    @Override public void switchToFrame(String frameName) { }
    @Override public void switchToDefaultContent() { }
}
