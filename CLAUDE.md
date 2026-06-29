# CLAUDE.md — thirdproj

## Описание проекта

Внутреннее веб-приложение компании **Alta Personnel** (ru.alta.thirdproj).  
Система управления актами, бонусами, зарплатами, личными данными и маржинальностью рекрутинговых сотрудников.

- **Backend:** Java 11 + Spring Boot 2.6.6, Thymeleaf, Spring Security, Sql2o, JWT
- **Frontend:** Thymeleaf-шаблоны (основной) + отдельный Angular-клиент (angularclient/)
- **БД:** MS SQL Server (`213.156.198.49\serverastra:1433`, база `recruting`)
- **Сборка:** Maven (pom.xml), сервер на порту `8181`
- **Контейнеризация:** Dockerfile в корне

---

## Структура проекта

```
thirdproj/
├── pom.xml                         # Maven-конфиг, зависимости (Spring Boot 2.6.6, Sql2o, JWT, Aspose)
├── Dockerfile
├── HELP.md
├── angularclient/                  # Отдельный Angular SSR клиент (не основной фронт)
│   └── src/app/                   # app.component, app.routes, app.config
├── src/
│   └── main/
│       ├── java/ru/alta/thirdproj/
│       │   ├── ThirdprojApplication.java        # Точка входа Spring Boot
│       │   ├── aspect/
│       │   │   └── AppLoggingAspect.java        # AOP-логирование
│       │   ├── config/
│       │   │   ├── AppConfig.java               # Настройка DataSource, Sql2o
│       │   │   ├── SecurityConfig.java           # Spring Security, роли, маршруты
│       │   │   ├── SwaggerConfig.java
│       │   │   ├── GlobalModelAttributes.java   # Глобальные атрибуты для всех шаблонов
│       │   │   ├── CustomAuthenticationSuccessHandler.java
│       │   │   ├── CustomAuthenticationFailureHandler.java
│       │   │   ├── CustomAccessDeniedHandler.java
│       │   │   └── RequestLoggingFilter.java
│       │   ├── controllers/
│       │   │   ├── MainController.java          # Главная страница, навигация
│       │   │   ├── ActsController.java          # Акты
│       │   │   ├── ActBonusController.java      # Бонусы по актам
│       │   │   ├── ActPutController.java        # Редактирование актов
│       │   │   ├── BonusController.java         # Бонусы общие
│       │   │   ├── BonusPaymentController.java  # Выплаты бонусов
│       │   │   ├── BonusSchemeController.java   # Схемы бонусов
│       │   │   ├── MarginController.java        # Маржинальность
│       │   │   ├── PersonalDataController.java  # Личные данные сотрудников
│       │   │   ├── UserController.java          # Пользователи
│       │   │   ├── UserSalaryController.java    # Зарплата
│       │   │   ├── AdditionalController.java
│       │   │   ├── EchoPostController.java
│       │   │   └── MyErrorController.java       # Обработка ошибок
│       │   ├── entites/                         # Модели / DTO
│       │   │   ├── Act.java, ActBuilder.java
│       │   │   ├── User.java, UserLogin.java, UserEmail.java
│       │   │   ├── Salary.java, UserSalary.java, UserSalaryDetail.java
│       │   │   ├── UserBonus*.java              # Бонусные модели (несколько вариантов)
│       │   │   ├── MarginBonus.java, MarginBonusBDM.java
│       │   │   ├── PersonalData.java
│       │   │   ├── BonusScheme*.java, Scheme*.java
│       │   │   ├── Payment*.java
│       │   │   ├── Department.java, Position.java, Role.java
│       │   │   └── birthday/                    # BirthdayMan, UserBirthDay, Email
│       │   ├── repositories/                    # Доступ к БД через Sql2o (не JPA)
│       │   │   ├── UserRepositorySlqO2.java
│       │   │   ├── UserLoginRepositorySlqO2.java
│       │   │   ├── BonusRepositoryImpl.java
│       │   │   ├── BonusPaymentRepositoryImpl.java
│       │   │   ├── BonusSchemeRepository.java
│       │   │   ├── PersonalDataRepository.java
│       │   │   ├── ActPutRepository.java
│       │   │   ├── ActBonusPercentRepositories.java
│       │   │   ├── UserSalaryRepImplRep.java
│       │   │   ├── UserBonusKPIRepositoryImpl.java
│       │   │   ├── ExpectedMoneyByFinalistRepository.java
│       │   │   └── userbirthday/BirthDayRepositories.java
│       │   ├── services/                        # Бизнес-логика
│       │   │   ├── UserService.java / UserServiceImpl.java
│       │   │   ├── UserLoginService.java / UserLoginServiceImpl.java
│       │   │   ├── BonusSchemeService.java / BonusSchemeServiceImpl.java
│       │   │   ├── PersonalDataServiceImpl.java
│       │   │   ├── MarginBonusServiceImpl.java
│       │   │   ├── UserBonusServiceImpl.java
│       │   │   ├── UserBonusKPIServiceImpl.java
│       │   │   ├── UserSalaryServiceImpl.java
│       │   │   ├── UserSalesServiceImpl.java
│       │   │   ├── UserPaymentBonusServiceImpl.java
│       │   │   ├── EmailSenderService.java      # Отправка писем (SMTP)
│       │   │   ├── SchedulerBirthdayService.java # Планировщик поздравлений
│       │   │   ├── ScheduledConfiguration.java
│       │   │   ├── DepartmentService.java
│       │   │   ├── EmployeesService.java
│       │   │   └── ExpectedMoneyByFinalistService.java
│       │   ├── jwt/
│       │   │   ├── JwtService.java
│       │   │   ├── JwtAuthFilter.java
│       │   │   ├── JwtAuthToken.java
│       │   │   ├── JwtAuthenticatedProfile.java
│       │   │   └── JwtAuthenticationProvider.java
│       │   ├── export/
│       │   │   └── ExcelGenerator.java          # Генерация Excel (Aspose/Apache POI)
│       │   ├── exceptions/
│       │   │   └── UserBonusNotFoundException.java
│       │   ├── response/
│       │   │   ├── JsonResponse.java
│       │   │   └── ResponseHandler.java
│       │   └── utils/
│       │       ├── CurrentFormat.java
│       │       └── DataSourceProvider.java
│       └── resources/
│           ├── application.properties           # Конфиг БД, почта, Thymeleaf, порт 8181
│           ├── template/                        # Thymeleaf HTML-шаблоны
│           │   ├── index.html, login.html, navigation.html
│           │   ├── acts.html, bonus.html, bonuses.html, bonus-schemes.html
│           │   ├── payment.html, paymentNew.html
│           │   ├── margin.html, salary.html
│           │   ├── personaldate.html
│           │   ├── interpreter.html, summary.html, tabs.html
│           │   ├── fragments/                   # Переиспользуемые фрагменты (фильтры, таблицы)
│           │   └── js/actions.js, postScript.js
│           └── static/images/                   # Логотип Alta
```

---

## База данных MS SQL Server

**Сервер:** `213.156.198.49\serverastra:1433`  
**БД:** `recruting`  
**Пользователь:** `ganiushina`

### Основные таблицы

| Группа | Таблицы |
|--------|---------|
| **Акты / проекты** | `act_buh`, `project_buh`, `project_buh_failed_probation_period`, `project`, `project_state`, `project_type`, `project_buh_copy` |
| **Бонусы** | `bonus_payed`, `bonus_kpi`, `user_bonus`, `extra_bonus`, `scheme`, `scheme_limits`, `scheme_limits_bdm` |
| **Выплаты** | `payment_buh`, `paymentSuccess`, `paymentPeriodSuccess`, `buh_account` |
| **Пользователи / логины** | `login`, `login_new`, `login_web`, `Login_Role`, `UserList`, `user_position`, `user_status` |
| **Зарплата** | `salary`, `salary_by_user` |
| **Кандидаты / люди** | `man`, `candidates`, `man_phone`, `man_email`, `man_education`, `man_contact` |
| **Личные данные** | `personal_data`, `status_pd`, `candidate_consents` |
| **Компании** | `company`, `company_info`, `company_contact`, `company_email` |
| **Рекрутинг** | `recruiting`, `recruiting_log`, `recruiting_type`, `coexecutor` |
| **Планирование** | `userplan`, `userplanByMonth`, `dep_plan_quarter`, `planner` |
| **Справочники** | `position`, `dep`, `depatment`, `division`, `city`, `classifier`, `const` |
| **ИИ / чат** | `ai_action_logs`, `ai_dialog_scenarios`, `conversations`, `messages` |
| **Логи** | `log_actions`, `log_man`, `log_project`, `login_log`, `log_session` |
| **Views** | `man_contacts`, `man_login`, `ProjectByMan`, `v_project_buh_actual`, `ViewManCard` |

---

## Технические детали

### Доступ к БД
- Используется **Sql2o** (не Spring Data JPA) — нативные SQL-запросы
- DataSource настраивается в `AppConfig.java` и `DataSourceProvider.java`
- Driver: `com.microsoft.sqlserver.jdbc.SQLServerDriver`

### Безопасность
- Spring Security с кастомными хендлерами
- JWT-аутентификация (`JwtService`, `JwtAuthFilter`)
- Роли из таблицы `Login_Role`

### Почта
- SMTP через `office.altapersonnel.ru:587`
- Планировщик поздравлений с днём рождения (`SchedulerBirthdayService`)

### Сборка и запуск
```bash
# Сборка JAR
mvn clean package -DskipTests

# Запуск
java -jar target/thirdproj-0.0.1-SNAPSHOT.jar

# Порт: 8181
```

### Логирование
- Файл: `logs/application.log`
- Уровень root: INFO, пакет `ru.alta.thirdproj`: DEBUG

---

## Важные соглашения

- Репозитории используют **Sql2o**, а не JPA — запросы пишутся на чистом SQL
- Шаблоны лежат в `src/main/resources/template/` (не в стандартном `templates/`)
- Конфигурация Thymeleaf: `spring.thymeleaf.prefix=classpath:/template/`
- Ветки с фичами именуются по схеме `codex/<описание>`
- Excel-экспорт через Aspose (репозиторий `https://repository.aspose.com/repo/`)
