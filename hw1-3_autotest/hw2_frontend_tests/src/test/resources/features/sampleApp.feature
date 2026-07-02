#language: ru
Функция: Проверка входа на сайт Sample App

Сценарий: вход на сайт Sample App

  Дано загружаем страницу "SampleAppPage"
  Когда введем в поле "UserName" значение "testuser"
  Когда введем в поле "Password" значение "pwd"
  Когда нажмем на кнопку "Login"

  Тогда проверим, что в "Loginstatus" содержится значение "Welcome, testuser!"
