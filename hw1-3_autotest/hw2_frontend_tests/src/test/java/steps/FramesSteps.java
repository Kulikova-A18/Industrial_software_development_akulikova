package steps;

import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import pages.FramesPage;
import pages.Page;
import pages.PageRegistry;
import pages.TestContext;

public class FramesSteps {

    @Когда("переключаемся во фрейм {string}")
    public void switchToFrame(String frameName) {
        TestContext.getCurrentPage().switchToFrame(frameName);
    }

    @Когда("возвращаемся на основной контент")
    public void switchToDefaultContent() {
        TestContext.getCurrentPage().switchToDefaultContent();
    }
}