package pages;

public interface Page {

    void fillField(String fieldName, String value);

    void clickButton(String buttonName);

    void checkText(String fieldName, String value);

    void checkTextByAttributeValue(String fieldName, String value);

    void selectDropdown(String fieldName, String value);

    void switchToFrame(String frameName);

    void switchToDefaultContent();
}