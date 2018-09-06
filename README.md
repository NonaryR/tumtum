# tumtum
[![Build Status](https://api.travis-ci.com/NonaryR/tumtum.svg?branch=master)](https://travis-ci.com/NonaryR/tumtum)

1) Подключен travisCI
2) Web-sockets при помощи [pneumatic-tubes](https://github.com/drapanjanas/pneumatic-tubes)
3) Авторизация `buddy-auth`
4) Все сообщения пишутся в Postges
5) Приложение задеплоено на [digital ocean](http://207.154.234.84:8080/)

## Разработка

Терминал один

```
lein repl
user.my=> (reset)
```
В втором терминале
```
lein figwheel dev
```
[Откройте ссылку](http://localhost:8080).
