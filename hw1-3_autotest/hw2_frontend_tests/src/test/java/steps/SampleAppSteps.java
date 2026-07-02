package steps;

import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import pages.Page;
import pages.PageRegistry;
import pages.TestContext;

public class SampleAppSteps {

    @Дано("загружаем страницу {string}")
    public void openPage(String pageName) {
        Page page = PageRegistry.load(pageName);
        TestContext.setCurrentPage(page);
    }

    @Когда("введем в поле {string} значение {string}")
    public void enterValueIntoField(String fieldName, String value) {
        TestContext.getCurrentPage().fillField(fieldName, value);
    }

    @Когда("нажмем на кнопку {string}")
    public void clickButton(String buttonName) {
        TestContext.getCurrentPage().clickButton(buttonName);
    }

    @Тогда("проверим, что в поле {string} содержится значение {string}")
    public void checkValueInField(String fieldName, String value) {
        TestContext.getCurrentPage().checkTextByAttributeValue(fieldName, value);
    }

    @Тогда("проверим, что в {string} содержится значение {string}")
    public void checkValueInElement(String elementName, String value) {
        TestContext.getCurrentPage().checkTextByAttributeValue(elementName, value);
    }
}