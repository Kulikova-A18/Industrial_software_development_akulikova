package steps;

import io.cucumber.java.ru.Когда;

public class ClassAttrSteps {

    @Когда("принимаем всплывающее сообщение")
    public void acceptAlert() {
        com.codeborne.selenide.Selenide.confirm();
    }

}
