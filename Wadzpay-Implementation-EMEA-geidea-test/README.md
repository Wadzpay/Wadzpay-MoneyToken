# Start the service 

To start the service, we have a docker-compose set up. To run it, simply run following command in terminal in the root folder of the project:
```
docker-compose up
``

This will build the images and create the whole environment. If you switch branches,
you need to rebuild the docker-compose too, otherwise you will run the environment from the previous branch.
To do this run following command:
```
docker-compose up --build
```

You can also specify which containers should be started, e.g. if you run backend from IDE:
```
docker-compose up database redis
``

# API documentation

Start backend server and visit
[http://localhost:8080/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config](http://localhost:8080/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config)

## Cognito JWT token generation

Install `aws-cli` first. Then call
```
aws configure
```
Enter info and credentials which you can find in `application.yml` file.
Then call
```
aws cognito-idp admin-initiate-auth --auth-flow ADMIN_USER_PASSWORD_AUTH --auth-parameters USERNAME=your-cognito-username,PASSWORD=your-password --client-id=1o2t4m41cbiivb907515tuk7r3 --user-pool-id eu-central-1_YHGC0AdSw
```
Copy `IdToken` from the output, and you will be able to perform BE only authentication with Swagger or by manually inserting token into header.

# Calling 3rd party services through REST API

Inside [com.vacuumlabs.wadzpay.configuration.RestTemplateConfiguration](/src/main/kotlin/com/vacuumlabs/wadzpay/configuration/RestTemplateConfiguration.kt) you can define
your own Bean with qualifier and configurations that you need. Always use getRequestFactory() method, because it loads our keystore.
When developing import the ssl certificate into [/src/main/resource/wadzpay-test.jks](/src/main/resources/wadzpay-test.jks) with
this command:

```
keytool -import -trustcacerts -alias {alias} -keystore wadzpay-dev.jks -file {certicate}
```
Here the alias should be the name of the domain (e.g. seon.io, app.bitgo-test.com) and the certificate can be obtained
with browser.

For most common keytool commands visit [this site](https://www.sslshopper.com/article-most-common-java-keytool-keystore-commands.html)

If you are done with development and need to add the certificate to a test or production environments,
contact Michal Zan (michal.zan@vacuumlabs.com) or devops (jan.toth@vacuumlabs.com, viliam.pucik@vacuumlabs.com)

# Cognito

We have user pools for dev, testing and production environments. Dev user pool is used both for dev and local environments.

# Migrations
We use [Flyway](https://flywaydb.org/) for migrations.
It supports pure SQL and Java-based migrations.
It scans `src/main/resources/db/migration` folder at the start of the application to search for the new migrations.
Whenever you change the schema, you need to write the migration to reflect these changes.
If you won't do this, Hibernate will fail to validate DB and application won't start.
You should create the migration with version prefix, which will be higher be one level higher than the current version of schema.

You may want to disable Flyway locally and let Hibernate update schema on its own. You need to open `application.yml` and set
```
flyway.enabled: false
hibernate.ddl-auto: update
```

# Troubleshooting

## BigDecimal
- We want to store Decimal as VARCHAR for this purpose we use `@Convert(converter = BigDecimalAttributeConverter::class)` othervise we will loose precision
- If you forgot to specify it column will be created for NUMERIC
- If you add it afterwards column won't be migrated automatically and you will encounter following error: `org.postgresql.util.PSQLException: ERROR: column "amount" is of type numeric but expression is of type character varying`
- You need to recreate the DB or migrate the column to solve this issue

# Onramp flow

We use Onramper and BitGo API for on-ramp and flow is described in [this document](docs/onramp_docs.docx).
Onramper API is very unstable and for now it works only with Moonpay and this is only due to multiple fixes on our side.
There are following problems:
- Absence of 0x prefix for ETH
- JSON arriving as string
- Multiple webhooks with the same data
- Late webhooks
- No retry policy
- No `txHash` field
- Invalid transaction amounts
- Undocumented behaviour
- Incorrect behaviour
- Can't test it without using the real money

# Merchant API

Merchant API is described in [this document](docs/merchant_api.docx)

# Tests

We use test docker containers for the database. Make sure to allow running docker without root permissions if you want to run tests locally.

# Push notifications

We use Expo notification library for the push notifications. We notify users about transactions.

# Authentication

## User role

We use AWS OAuth for the User authentication.
Front-end includes AWS JWT ID Token in `Authorization: Bearer` header together with every server request.
Our back-end verifies it and if it is valid, we let user in.

## Merchant role

We store merchant password hash on our side.
We generate passwords for merchant on our own.
If merchant wants to perform requests, he has to include Base64 encoded credentials into `Authorization: Basic` header.
We verify if credentials are valid and if they are, we let merchant it.

# Control panel controller

We have control panel controller which is available only on dev and testing environments.
This controller can be quite handy for local development, and we highly recommend using it.
It features the following endpoints:
- `/addFakeUsers` - adds fake users to DB. You can omit the tedious registration process, if you use this endpoint.
- `/addFakeTransaction` - adds fake transaction and this money can be used later
- Other useful and simple endpoints

# VUBA package

VUBA stands for Vacuumlabs Unified Universal Unorthodox Banking Architecture. It contains ledger implementation, and it is not recommended modifying it.
We recommend developers to look at VUBA tests for code examples. Also `LedgerService` and `LedgerConventions` files contain most of the VUBA-related logic from our side.
We don't use `ResponseHistory`, `Status`, `StatusEntry`, `StatusType` and related entities in our code.

VUBA `Subaccount` represents account balance in particular asset.

VUBA `Account` represents some account which is linked together with multiple VUBA `Subaccount` entities.
So VUBA `Account` can be used to represent bank account of some user with assets in different currencies.

# Wadzpay entities

## Subaccount entity

Wadzpay `Subaccount` entity is similar to VUBA `Subaccount` entity and is linked together with it via `reference` field.
`Subaccount` balance is stored into the related VUBA `Subaccount` entity.
Wadzpay `Subaccount` also refers to lists of corresponding ingoing and outgoing `Transaction` entities, which are described in this readme later.

## Account entity

Wadzpay `Account` entity has reference to `AccountOwner` entity which is described later in this readme
Also it has references to related Wadzpay `Subaccount` entities.
And it is connected to VUBA `Account` entity via `reference` field.

## AccountOwner entity

`AccountOwner` represents owner of Wadzpay `Account` entity.
It can be either `UserAccount` or `Merchant`.
It contains references to `Order` entities, which are part of Merchant API and will be described later.

## UserAccount entity

`UserAccount` extends `AccountOwner` and represents the regular user of the application.

## Merchant entity

`Merchant` extends `AccountOwner` and represents some server which places `Order` entities in our application.
`UserAccount` can pay for these orders using our web popout and `Merchant` `Account` balance will be updated.
We will send the webhook to the server `Merchant` represents with the confirmation of the `Order`.
Merchant API is documented in the other document.

## Transaction entity

`Transaction` is entity which represents money transfer from one `Account` to another.
`TransactionType` can be
1. `ONRAMP`. It represents getting money into the system using Onramper widget.
2. `PEER_TO_PEER`. It represents peer to peer transfer. Additional 0.5% of transfer goes to fee collection account.
3. `MERCHANT`. Represents paying for merchant order.
4. `OFFRAMP`. Not used yet.
5. `OTHER`. Used in tests.

`TransactionStatus` is usually `SUCCESSFUL`, but also `NEW`, `FAILED`, `IN_PROGRESS` statuses are used in on-ramping flow.
We also have endpoint for transactions export to CSV.

## Unmatched transaction entity

It is used only in on-ramp flow, and it represents transaction for which BitGo confirmation comes faster than Onramper confirmation.

## Contact entity

It is used to add users to list of contacts of the current user.
These contacts can later be used to send peer to peer transactions.

# Packages and classes

## Account Owner Package

Account owner package features two classes `AccountOwnerController` and `AccountOwnerService`.
`AccountOwner` class is super class for two other classes `UserAccount` and `Merchant`.
This class means entity which posses Ledger account.

### AccountOwnerController class

It features endpoints which are the same for `UserAccount` and `Merchant`.
`getOrder`, `getTransactions`, `getBalances` can be used for both `UserAccount` and `Merchant`.

### AccountOwnerService class

It has only one method, which extracts `UserAccount` or `Merchant` entity from `javax.security.Principal` object depending on principal authorities.

## Auth package

### Role class

Features roles which are used in our project.
`User` and `Merchant` roles are described above.
And `Merchant` role is not used.

### HmacHelper class

It is used for verifying the signature in `Onramper` webhook payload.

### BasicAuth class

It is used for authenticating `Merchant`.

## BitGo package

### BitGoWallet class

It is used for querying the BitGo API about particular transfer.

### BitGoWebhookController class

It is the receiver for webhook with BitGo notifications.
We only support BitGo transfer notifications for now.
When we receive such notification, we call method in `WalletService` to confirm transfer and update corresponding `Transaction` status to `SUCCESSFUL` or create `UnmatchedTransaction`.

### WalletService class

It asks `BitGoWallet` class to get transfer.
If successful, we are verifying on-ramp with the help of `LedgerService`.

## Cognito package

### AwsCognitoIdTokenProcessor class

Has `getAuthentication` function.
It checks if `Cognito` id token is valid and if it is creates `Authentication` object.

### AwsCognitoJwtAuthentication class

Has `verifyToken` function, which goes to `AwsCognitoIdTokenProcessor`.
If no exception is thrown, `SecurityContextHolder.authentication` is set to `AwsCognitoIdTokenProcessor.getAuthentication`.

### JwtAuthenticationProvider and JwtUserAuthentication class

Don't feature anything special at all.

## Common package

Doesn't contain any heavy code. It should be easy to follow.

## Configuration package

Contains a lot of config beans.
Most of them are extremely simple, so here we will focus only on complicated ones.

### JwtAutoConfiguration

Has required configuration for the `JwtProcessor` to parse `Cognito` id tokens.

### ConfigController

It is used to return configuration from BE to be used on FE.
We have this controller because we don't want to change the configuration on FE every time it changes on BE.
We want just to query the endpoint and get it from BE.

### RestTemplateConfiguration

It has some complicated code inside, but its main purpose is to intercept reuqests to BitGo and put API key in there.


### SecurityConfiguration

It should be readable on its own but there we decide which endpoints should be open to which roles.

### SwaggerConfiguration

This file is used to set up authentication in Swagger UI.

### WadzpayExceptionHandlers

When exception is thrown and not caught anywhere in the code `WadzpayExceptionHandlers` come into play.
We are using them to handle exceptions, when service methods are called from endpoints and return appropriate HTTP request return codes.

## Control package

### ControlPanelController class

It is the controller which is hidden on production, but has very useful in dev and testing environments.
These methods are described in this README file above.
Also, you can see their documentation if you open the class or in Swagger.

## Exchange package

### Exchange service

It calls CryptoCompare API to get the exchange rates for fiat and crypto.
It also caches recent exchange rates in Redis so that we don't call API too often.

## Ledger package

### Account class

This class is used to connect WP accounts together with VUBA accounts. `Account` entity is already described above.

### Subaccount, Transaction, UnmatchedTransaction classes

These classes are described above.
`UnmatchedTransaction` is described in the corresponding on-ramp document.
`Transaction` file has also many easy specifications inside and some constraints validator.

### TransactionService class

Has method for showing push notifications using notification service.
Also features methods to get single transaction, list of transactions, list of transaction view models.
You can do reach filtering on transactions list.

### LedgerConventions file

It defines some VUBA ledger reference format (`ReferenceConventions`).
Also, it defines list of available cryptos in `CurrencyUnit` enum.
It also has methods for initializing Ledger.
This method creates WP `omnibus` and `feeCollection` accounts and initializes VUBA ledger with cryptos and required accounts.

### LedgerService class

LedgerService has methods used in on-ramp flow:
`startOnramp`, `tryVerifyOnramp`, `verifyOnramp`, `declineOnramp`, `createNewOnrampTransaction`, `createUnmatchedTransaction`.
Usage of these methods is in the document, link to which is above, and they are called from different services and only `verifyOnramp` commits transaction to Ledger.
General flow is:
1. `createNewOnrampTransaction`
2. `startOnramp`
3. `tryVerifyOnramp`
4. `verifyOnramp`

But due to huge amount of inconsistencies in Onramper API we have to be prepared to every possible flow.
So they are designed a little tricky to ensure the data, system, and flow integrity regardless of flow.
You can read about it in the document on the Google Drive.

Also `LedgerService` features the `createMerchantTransaction`, `createP2PTransaction`, `createTransaction` methods.
`createMerchantTransaction` and `createP2PTransaction` just calls the `createTransaction` method with slightly different parameters.
`P2P` transactions has 0.5% fee.

It also has `createAccount` and `getBalances` methods which are rather simple.

## Merchant package

### CsvHeaderMappingHelper class

Just a copy-paste from the internet to be able to specify both order and names of the columns in CSV documents.

### TransactionCSVExportService class

Export transactions returns `ByteArray` which can be written to Amazon S3 storage or returned as an attachment in
`HttpResponse`.

### Merchant class

Has merchant name and reference to valid and invalid API keys for the merchant.

### MerchantApiKey class

Has a link to `MerchantClass` and string field defined as `apiKeySecretHash`.
Note that we don't store API keys in DB, we only store their hash,
and it is not possible to restore API key from the hash.
We don't store API key directly and don't provide options to restore API key in case it is lost, because it is not secure.
We only allow new API key to be issued, and we also allow setting present API keys state to invalid.

### Order class

Order class is a part of merchant API which is described in the document above.

### MerchantController class

It has methods for issuing and invalidating API keys. Also, it has methods for creating merchant, merchant API method and method to get transaction reports in CSV.

### MerchantService class

Just a helper for the merchant controller. You can see how the API key is issued there and what is stored in DB.

### MerchantTestController class

Is hidden in production and is used for testing our webhooks, which we send as a part of merchant API.

### OrderService class

Helper service to get order of `OPEN` type (not `PROCESSED` and not `EXPIRED`), to get order of any type and to create order.

## Notification package

### ExpoPushNotificationToken class

Contains string representation of `ExpoPushNotificationToken` and link to related `UserAccount`.
This token is a part `ExpoPushNotfication` library.

### NotificationService class

Contains CRUD methods for expo push notification tokens. Also contains method to send push notification.
Currently, we only send notifications about the transactions.

## Ramping package

### RampController class

Receiver for Onramper webhooks. Parses inconsistencies in Onramper webhook and handover payload to `RampService`.

### RampService class

Processes Onramper webhook. It calls `LedgerService` from above depending on payload type.

## Services package

### CognitoService class

Has method to register the user on Cognito.
Also has method to delete user on Cognito.
Also has methods to check if email or phone is available.

### RedisService class

Has methods to work with Redis storage.
Currently, we cache registration data in Redis.
Also, we cache exchange rates there.

### SeonService class

Service to call Seon API and check if user is not fraudulent.

### TwilioService class

Email and phone OTP service for registration.

## User package

### Contact class

Is used to represent user contact. This class is used for sending P2P transactions.

### ContactService class

Is used for CRUD operations for contact entity.

### RegistrationService class

Methods from these service are called from `UserAccountController` so they are documented there.
Registration flow consists of 4 endpoint calls.
1. You have to specify phone. Check if phone is not used and if not send OTP to that phone.
2. You have to specify phone and OTP received on the step 1. If everything is okay we allow you to go to step 3.
3. You have to specify phone and email. If email is not taken and phone is allowed on the previous step, we send the OTP code to your email.
4. If OTP code is valid, we register the user.

Cached phones and emails are stored in Redis storage for convenient moving between steps.
They are always removed from Redis storage at some point of time regardless of registration success.

### UserAccountService class

Has user data and also links to contact collections and expo tokens.

### UserAccountController class

Has a lot of endpoints.
1. To register a user.
2. CRUD endpoints expo tokens.
3. CRUD endpoint for contacts.
4. Methods for creating P2P transactions and to work with merchant API.

### UserAccountService class

CRUD methods for user. Also checks if emails or phones is taken in our DB

### UserInitializerService class

It is a helper service for `ControlPanelController`.

### UserAccountTestController class

Method for deleting user. Not available on production.

## Utils package

### CleanerService class

Service to clean the DB completely. Is used a lot in tests.

## ViewModels package

### TransactionViewModel class

Has method to create transaction view model from transaction.
Also has annotations to denote what and in what order to export to CSV from transaction view model.
Also contains methods to create push notification from transaction view model.

## Webhook package

### OrderWebhook class

Entity which contains info about webhook created for some order as a part of merchant API.

### OrderWebhookLog class

Entity which contains information about success rate of the last webhooks.
Can be used for debugging.

### WebhookService class

Helper service for sending and creating webhooks as a part of Merchant API.
We switched from `webhooks-io` to our own webhook implementation a moment ago, so theoretically there can be some problems with it.

## Test package

We have the test package where you can not only test the method calls,
but also can test the endpoints itself as we created environment with test containers containing embedded Postgres and Redis.
You should also configure your system to run Docker containers without root permissions if you want tests to be working.
We will not describe the tests in this README file as they are simple, and you should understand what's going on there if you look into the code.
We also recommend you to look on the code to see how to call our API and to understand possible API calls sequences.



