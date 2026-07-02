# language: ru
Функция: Проверка работы с вложенными фреймами

  Сценарий: нажатие кнопок, переключение во внешний фрейм и нажатие кнопок
    Дано загружаем страницу "FramesPage"
    Когда переключаемся во фрейм "outerFrame"
    Когда нажмем на кнопку "ButtonByDataAttribute"
    Тогда проверим, что в поле "Result" содержится значение "Button pressed: Edit"
    Когда нажмем на кнопку "ButtonByText"
    Тогда проверим, что в поле "Result" содержится значение "Button pressed: Submit"
    Когда нажмем на кнопку "ButtonByName"
    Тогда проверим, что в поле "Result" содержится значение "Button pressed: Click me"
    Когда нажмем на кнопку "ButtonByXPath"
    Тогда проверим, что в поле "Result" содержится значение "Button pressed: Primary"
    Когда переключаемся во фрейм "innerFrame"
    Когда нажмем на кнопку "ButtonByDataAttribute"
    Тогда проверим, что в поле "Result" содержится значение "Button pressed: Edit"
    Когда нажмем на кнопку "ButtonByText"
    Тогда проверим, что в поле "Result" содержится значение "Button pressed: Submit"
    Когда нажмем на кнопку "ButtonByName"
    Тогда проверим, что в поле "Result" содержится значение "Button pressed: Click me"
    Когда нажмем на кнопку "ButtonByXPath"
    Тогда проверим, что в поле "Result" содержится значение "Button pressed: Primary"