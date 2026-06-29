# Tasks

## Active

- [x] ~~**[Этап 2] Backend — DTO**~~ (выполнено: ActDistributionRow.java, ActBuhInfo.java)
- [x] ~~**[Этап 3] Backend — Repository**~~ (выполнено: ActDistributionRepository.java)
- [x] ~~**[Этап 4] Backend — Service**~~ (выполнено: ActDistributionService + Impl)
- [x] ~~**[Этап 5] Backend — Controller**~~ (выполнено: ActDistributionController.java)
- [x] ~~**[Этап 6] Frontend**~~ (выполнено: act-distribution.html)
- [x] ~~**[Этап 7] Security + Navigation**~~ (выполнено: SecurityConfig, navigation.html)

- [ ] **[Этап 1] Анализ связей act_buh → department** - выяснить, как определить отдел акта до того, как project_buh заполнен. Проверить таблицу `project` (project_id есть в act_buh), возможно там есть dep_id. Если нет — обсудить с Еленой запасной вариант (фильтр по дате/вручную или показывать все нераспределённые).
  - Запрос: `SELECT TOP 3 * FROM project WHERE id IN (SELECT project_id FROM act_buh WHERE project_id IS NOT NULL)`

- [ ] **[Этап 2] Backend — DTO / Entity** - создать класс `ActDistributionRow.java` в `entites/`, маппинг полей `project_buh`:
  - act_id, responsible_user_name/id, resecher_name/id, summ_*, percent_*, depatment/id, teame_leader_name/id, city_*, depatment_resecher/id

- [ ] **[Этап 3] Backend — Repository** - создать `ActDistributionRepository.java`:
  - `getUndistributedActs(int depId)` — акты из act_buh без строк в project_buh (для отдела тимлида)
  - `getRowsByActId(int actId)` — строки project_buh по act_id
  - `addRow(ActDistributionRow row)` — INSERT в project_buh
  - `updateRow(ActDistributionRow row)` — UPDATE по id
  - `deleteRow(int id)` — DELETE по id

- [ ] **[Этап 4] Backend — Service** - создать `ActDistributionService.java` с бизнес-логикой:
  - Расчёт суммы по % от total_no_nds акта (summ = total_no_nds * percent / 100)
  - Определение dep_id тимлида по логину (через user_position + Login_Role)

- [ ] **[Этап 5] Backend — Controller** - создать `ActDistributionController.java`:
  - `GET /act-distribution` → страница со списком нераспределённых актов
  - `GET /act-distribution/{actId}/rows` → JSON текущих строк project_buh
  - `POST /act-distribution/{actId}/row` → добавить строку
  - `PUT /act-distribution/row/{id}` → обновить строку
  - `DELETE /act-distribution/row/{id}` → удалить строку

- [ ] **[Этап 6] Frontend — act-distribution.html** - Thymeleaf-шаблон:
  - Таблица актов: номер, дата, компания, кандидат, сумма, статус (заполнено/нет)
  - Модальное окно по клику на акт: список строк + кнопка «Добавить строку»
  - Форма строки: выбор консультанта (select из UserList), % участия, выбор ресечера, % ресечера, отдел, город
  - Авторасчёт суммы при вводе %
  - Кнопки «Сохранить» / «Удалить» для каждой строки

- [ ] **[Этап 7] Security + Navigation** - в `SecurityConfig.java` открыть маршрут `/act-distribution/**` для ролей `ROLE_MANAGER` и `ROLE_BUHADMIN`. В `navigation.html` добавить пункт меню «Распределение актов».

## Waiting On

- [ ] **Уточнить фильтрацию нераспределённых актов по команде** — результат анализа из Этапа 1. Как связать act_buh с dep_id тимлида без заполненного project_buh?

## Someday

## Done
