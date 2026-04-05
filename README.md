# gRPC Key-Value Service (Tarantool 3.2)

Данный проект представляет собой gRPC сервис на Java, работающий с базой данных **Tarantool 3.2.x**. Сервис реализует API для работы с парами ключ-значение и оптимизирован для нагрузок в **5 000 000+** записей.

## Основные характеристики
* **Стек:** Java 17, gRPC (Proto3), Tarantool-Java-SDK 1.5.0.
* **База данных:** Tarantool (движок `memtx`).
* **Схема данных:** Спейс `KV` с полями `key` (string) и `value` (varbinary, nullable).
* **Производительность:** Поддержка стриминга данных (gRPC Server Streaming) для эффективной работы метода `Range`.

---

## Архитектурные особенности

### Работа с типами данных
Поле `value` имеет тип `varbinary`.
> **Важно:** При тестировании через консольные инструменты (например, `grpcurl`), значения в формате JSON передаются и отображаются в кодировке **Base64**.

### Метод Range (Стриминг)
Для обеспечения стабильной работы с миллионами записей метод `Range` реализован как **Server Stream**. Поиск осуществляется по первичному `TREE` индексу с использованием Lua-итератора `GE`.

---

## Запуск проекта

///

## Тестирование (Powershell)

Используйте следующие команды для проверки стандартных методов

### put (сохранение):
* grpcurl -plaintext -d '{\"key\": \"test\", \"value\": \"SGVsbG8=\"}' localhost:9090 kv.KvService/Put
* grpcurl -plaintext -d '{\"key\": \"null\"}' localhost:9090 kv.KvService/Put

### get (получение):
* grpcurl -plaintext -d '{\"key\": \"test\"}' localhost:9090 kv.KvService/Get
* grpcurl -plaintext -d '{\"key\": \"null\"}' localhost:9090 kv.KvService/Get

### count (подсчет общего количества):
* grpcurl -plaintext localhost:9090 kv.KvService/Count

### delete (удаление):
* grpcurl -plaintext -d '{\"key\": \"test\"}' localhost:9090 kv.KvService/Delete

### put (вставка 10 значений сразу):
* grpcurl -plaintext -d '{\"key\": \"a\", \"value\": \"YQ==\"}' localhost:9090 kv.KvService/Put
  grpcurl -plaintext -d '{\"key\": \"b\", \"value\": \"Yg==\"}' localhost:9090 kv.KvService/Put
  grpcurl -plaintext -d '{\"key\": \"c\", \"value\": \"Yw==\"}' localhost:9090 kv.KvService/Put
  grpcurl -plaintext -d '{\"key\": \"d\", \"value\": \"ZA==\"}' localhost:9090 kv.KvService/Put
  grpcurl -plaintext -d '{\"key\": \"e\", \"value\": \"ZQ==\"}' localhost:9090 kv.KvService/Put
  grpcurl -plaintext -d '{\"key\": \"f\", \"value\": \"Zg==\"}' localhost:9090 kv.KvService/Put
  grpcurl -plaintext -d '{\"key\": \"g\", \"value\": \"Zw==\"}' localhost:9090 kv.KvService/Put
  grpcurl -plaintext -d '{\"key\": \"h\", \"value\": \"aA==\"}' localhost:9090 kv.KvService/Put
  grpcurl -plaintext -d '{\"key\": \"i\", \"value\": \"aQ==\"}' localhost:9090 kv.KvService/Put
  grpcurl -plaintext -d '{\"key\": \"j\", \"value\": \"ag==\"}' localhost:9090 kv.KvService/Put

### range (получение диапазона):
* grpcurl -plaintext -d '{\"key_since\": \"a\", \"key_to\": \"j\"}' localhost:9090 kv.KvService/Range
* grpcurl -plaintext -d '{\"key_since\": \"c\", \"key_to\": \"h\"}' localhost:9090 kv.KvService/Range


-----

### Для заполнения базы данных 5_000_000 записями можно раскомментировать блок инициализации в Main.java, а после протестировать следующим методом.
>
> Measure-Command {
> 
>     $results = grpcurl -plaintext -d '{\"key_since\": \"key_00\", \"key_to\": \"key_01\"}' localhost:9090 kv.KvService/Range
>     $results | Select-Object -First 20 | Out-Host
>     $objectCount = ($results | Select-String -Pattern '"key":').Count
>     Write-Host $objectCount
> 
> }
