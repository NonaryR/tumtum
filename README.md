# tumtum
[![Build Status](https://api.travis-ci.com/NonaryR/tumtum.svg?branch=master)](https://travis-ci.com/NonaryR/tumtum)

1) Подключен travisCI
2) Web-sockets при помощи [pneumatic-tubes](https://github.com/drapanjanas/pneumatic-tubes)
3) Авторизация `buddy-auth`
4) Все сообщения пишутся в postgres
5) Приложение задеплоено на `digital ocean` https://chatapp.nonaryr.com/

## Локальное тестирование
```
docker volume create --name=pgdata
docker-compose -f docker-compose.yml -f docker-compose.local.yml up --build -d
```
[Откройте ссылку](http://localhost:8081).

Для остановки
```
docker-compose stop
```

## Локальная разработка
Будем считать, что мы разработываем приложение в IDE, и нам необходим коннект к БД

Сбилдим докер-контейнер для нее
```
# если вы использовали docker-compose из предыдущего шага, то контейнер уже собран, шаг с билдом можно пропустить
docker build -f Dockerfile.db -t tumtum_db .
docker run --name tdb --rm -d -p 15444:5432 tumtum_db
```

После чего:

Терминал 1

```
lein repl
user.my=> (reset)
```
Терминал 2
```
lein figwheel dev
```
[Откройте ссылку](http://localhost:8080).


Остановить контейнер командой
```
docker stop tdb
```
