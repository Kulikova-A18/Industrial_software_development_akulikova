# language: ru
Функция: Проверка ввода текста

  Сценарий: изменение названия кнопки после ввода текста
    Дано загружаем страницу "TextInputPage"
    Когда введем в поле "newButtonName" значение "Butttoooooon"
    Когда нажмем на кнопку "updatingButton"
    Тогда проверим, что в "updatingButton" содержится значение "Butttoooooon"