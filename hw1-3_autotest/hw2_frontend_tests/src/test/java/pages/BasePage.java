package pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

public class BasePage {
    
    protected void fillField(By locator, String value) {
        SelenideElement element = $(locator);
        
        String os = System.getProperty("os.name").toLowerCase();
        Keys selectAllKey = os.contains("mac") ? Keys.COMMAND : Keys.CONTROL;
        
        element.sendKeys(Keys.chord(selectAllKey, "a"));
        element.sendKeys(Keys.BACK_SPACE);
        element.clear();
        element.setValue(value);
    }

    protected void click(By locator) {
        $(locator).shouldBe(enabled).click();
    }

    protected void shouldHaveText(By locator, String text) {
        $(locator).shouldHave(text(text));
    }

    protected void shouldHaveTextByAttributeValue(By locator, String text) {
        $(locator).shouldHave(attribute("value", text), Duration.ZERO);
    }

    protected void open(String url) {
        Selenide.open(url);
    }

}