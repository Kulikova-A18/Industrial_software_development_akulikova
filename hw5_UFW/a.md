Краткая инструкция:
Данные для доступа ко всем машинам:
Логин: vagrant

Пароль: vagrant

Команды для запуска:
cmd

# 1. Запустить сервер (самое главное)

vagrant up server

# 2. После успешного запуска сервера, запустить остальные:

vagrant up admin
vagrant up developer  
vagrant up analyst

# Или все сразу (но дольше):

vagrant up
Для подключения к машинам:
cmd

# К серверу

vagrant ssh server

# К админскому ПК

vagrant ssh admin

# К ПК разработчика

vagrant ssh developer

# К ПК аналитика

vagrant ssh analyst

# К заблокированному ПК

vagrant ssh blocked
Для тестирования:
На админском ПК:

cmd
vagrant ssh admin
./test_admin.sh
Должны быть доступны: SSH, Prometheus, Node Exporter

Должен быть заблокирован: Grafana

На ПК разработчика:

cmd
vagrant ssh developer
./test_developer.sh
Должен быть доступен: Grafana

Должны быть заблокированы: SSH, Prometheus, Node Exporter

C:\Users\USER\Desktop\hack\Industrial_software_development_akulikova\hw5_UFW>vagrant up admin
Bringing machine 'admin' up with 'virtualbox' provider...
==> admin: Importing base box 'ubuntu/jammy64'...
==> admin: Matching MAC address for NAT networking...
==> admin: Setting the name of the VM: admin-pc
==> admin: Clearing any previously set network interfaces...
==> admin: Preparing network interfaces based on configuration...
admin: Adapter 1: nat
admin: Adapter 2: hostonly
==> admin: Forwarding ports...
admin: 22 (guest) => 2222 (host) (adapter 1)
==> admin: Running 'pre-boot' VM customizations...
==> admin: Booting VM...
==> admin: Waiting for machine to boot. This may take a few minutes...
admin: SSH address: 127.0.0.1:2222
admin: SSH username: vagrant
admin: SSH auth method: private key
admin: Warning: Connection reset. Retrying...
admin: Warning: Remote connection disconnect. Retrying...
==> admin: Machine booted and ready!
==> admin: Checking for guest additions in VM...
admin: The guest additions on this VM do not match the installed version of
admin: VirtualBox! In most cases this is fine, but in rare cases it can
admin: prevent things such as shared folders from working properly. If you see
admin: shared folder errors, please make sure the guest additions within the
admin: virtual machine match the version of VirtualBox you have installed on
admin: your host and reload your VM.
admin:
admin: Guest Additions Version: 6.0.0 r127566
admin: VirtualBox Version: 7.1
==> admin: Setting hostname...
==> admin: Configuring and enabling network interfaces...
==> admin: Running provisioner: shell...
admin: Running: inline script
══════════════════════════════════════════════════════════════════════
admin: НАСТРОЙКА ПК АДМИНИСТРАТОРА
admin: ══════════════════════════════════════════════════════════════════════
admin: Этап 1/2: Обновление системы...
admin:
admin: Этап 2/2: Создание тестового скрипта...
admin:
admin: ══════════════════════════════════════════════════════════════════════
admin: НАСТРОЙКА ЗАВЕРШЕНА!
admin: ══════════════════════════════════════════════════════════════════════
admin:
admin: Данные для доступа:
admin: • IP: 192.168.55.90
admin: • Логин: vagrant
admin: • Пароль: vagrant
admin:
admin: Для тестирования выполните:
admin: ./test_admin.sh
admin:
admin: Для подключения к серверу:
admin: ssh vagrant@192.168.55.50
admin: ══════════════════════════════════════════════════════════════════════
admin:

C:\Users\USER\Desktop\hack\Industrial_software_development_akulikova\hw5_UFW>

---

C:\Users\USER\Desktop\hack\Industrial_software_development_akulikova\hw5_UFW>vagrant up developer
Bringing machine 'developer' up with 'virtualbox' provider...
==> developer: Importing base box 'ubuntu/jammy64'...
==> developer: Matching MAC address for NAT networking...
==> developer: Setting the name of the VM: developer-pc
==> developer: Fixed port collision for 22 => 2222. Now on port 2200.
==> developer: Clearing any previously set network interfaces...
==> developer: Preparing network interfaces based on configuration...
developer: Adapter 1: nat
developer: Adapter 2: hostonly
==> developer: Forwarding ports...
developer: 22 (guest) => 2200 (host) (adapter 1)
==> developer: Running 'pre-boot' VM customizations...
==> developer: Booting VM...
==> developer: Waiting for machine to boot. This may take a few minutes...
developer: SSH address: 127.0.0.1:2200
developer: SSH username: vagrant
developer: SSH auth method: private key
developer: Warning: Connection reset. Retrying...
developer: Warning: Remote connection disconnect. Retrying...
==> developer: Machine booted and ready!
==> developer: Checking for guest additions in VM...
developer: The guest additions on this VM do not match the installed version of
developer: VirtualBox! In most cases this is fine, but in rare cases it can
developer: prevent things such as shared folders from working properly. If you see
developer: shared folder errors, please make sure the guest additions within the
developer: virtual machine match the version of VirtualBox you have installed on
developer: your host and reload your VM.
developer:
developer: Guest Additions Version: 6.0.0 r127566
developer: VirtualBox Version: 7.1
==> developer: Setting hostname...
==> developer: Configuring and enabling network interfaces...
==> developer: Running provisioner: shell...
developer: Running: inline script
══════════════════════════════════════════════════════════════════════
developer: НАСТРОЙКА ПК РАЗРАБОТЧИКА
developer: ══════════════════════════════════════════════════════════════════════
developer: Этап 1/2: Обновление системы...
developer:
developer: Этап 2/2: Создание тестового скрипта...
developer:
developer: ══════════════════════════════════════════════════════════════════════
developer: НАСТРОЙКА ЗАВЕРШЕНА!
developer: ══════════════════════════════════════════════════════════════════════
developer:
developer: Данные для доступа:
developer: • IP: 192.168.55.91
developer: • Логин: vagrant
developer: • Пароль: vagrant
developer:
developer: Для тестирования выполните:
developer: ./test_developer.sh
developer:
developer: Для доступа к Grafana:
developer: Откройте браузер и перейдите по адресу http://192.168.55.50:3000
developer: Логин: admin
developer: Пароль: admin123
developer: ══════════════════════════════════════════════════════════════════════
developer:

C:\Users\USER\Desktop\hack\Industrial_software_development_akulikova\hw5_UFW>

C:\Users\USER\Desktop\hack\Industrial_software_development_akulikova\hw5_UFW>vagrant up analyst
Bringing machine 'analyst' up with 'virtualbox' provider...
==> analyst: Importing base box 'ubuntu/jammy64'...
==> analyst: Matching MAC address for NAT networking...
==> analyst: Setting the name of the VM: analyst-pc
==> analyst: Fixed port collision for 22 => 2222. Now on port 2201.
==> analyst: Clearing any previously set network interfaces...
==> analyst: Preparing network interfaces based on configuration...
analyst: Adapter 1: nat
analyst: Adapter 2: hostonly
==> analyst: Forwarding ports...
analyst: 22 (guest) => 2201 (host) (adapter 1)
==> analyst: Running 'pre-boot' VM customizations...
==> analyst: Booting VM...
==> analyst: Waiting for machine to boot. This may take a few minutes...
analyst: SSH address: 127.0.0.1:2201
analyst: SSH username: vagrant
analyst: SSH auth method: private key
analyst: Warning: Connection reset. Retrying...
analyst: Warning: Connection aborted. Retrying...
==> analyst: Machine booted and ready!
==> analyst: Checking for guest additions in VM...
analyst: The guest additions on this VM do not match the installed version of
analyst: VirtualBox! In most cases this is fine, but in rare cases it can
analyst: prevent things such as shared folders from working properly. If you see
analyst: shared folder errors, please make sure the guest additions within the
analyst: virtual machine match the version of VirtualBox you have installed on
analyst: your host and reload your VM.
analyst:
analyst: Guest Additions Version: 6.0.0 r127566
analyst: VirtualBox Version: 7.1
==> analyst: Setting hostname...
==> analyst: Configuring and enabling network interfaces...
==> analyst: Running provisioner: shell...
analyst: Running: inline script
══════════════════════════════════════════════════════════════════════
analyst: НАСТРОЙКА ПК АНАЛИТИКА
analyst: ══════════════════════════════════════════════════════════════════════
analyst: Этап 1/1: Обновление системы...
analyst:
analyst: ══════════════════════════════════════════════════════════════════════
analyst: НАСТРОЙКА ЗАВЕРШЕНА!
analyst: ══════════════════════════════════════════════════════════════════════
analyst:
analyst: Данные для доступа:
analyst: • IP: 192.168.55.15
analyst: • Логин: vagrant
analyst: • Пароль: vagrant
analyst:
analyst: Проверьте доступность Grafana:
analyst: curl -I http://192.168.55.50:3000
analyst:
analyst: Grafana должен быть доступен по адресу:
analyst: http://192.168.55.50:3000
analyst: Логин: admin
analyst: Пароль: admin123
analyst: ══════════════════════════════════════════════════════════════════════
analyst:

C:\Users\USER\Desktop\hack\Industrial_software_development_akulikova\hw5_UFW>
