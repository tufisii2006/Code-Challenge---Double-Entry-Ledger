
I used Kafka as a system to notify all the other services.
RabbitMQ was used for the payment system so it can be checked different from the Kafka, basically to simulate the existing of two different systems.

# How to run locally
Have Docker and JDK 17 installed. Run the docker compose so you have all the containers up and running.
cd in terminal and use a 

docker-compose up -d
We need to create the schema
docker exec ledger-mysql mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS ledger; GRANT ALL PRIVILEGES ON ledger.* TO 'ledger'@'%'; FLUSH PRIVILEGES;"

Liquibase should create the tables afterward. 

Swagger is available to test the APIs: http://localhost:8080/swagger-ui/index.html

# Minor improvements
For transaction create,
I should use when checking the accounts the method findAllForUpdate instead of findAllById, so it is thread safe. The lock is acquired before any sold read.
A mapper between entities and DTOs, I did not avoid them, I just wanted to create more business logic rather than mapping fields.
Clean-up the OpenAPIs yaml files of some redundant schema objects. I focused on EUR account mostly, and there are still some missing validations in the code, and many things that could be improved.
I simply had no more time for tests in the code, I am sorry, we can talk about this when we have the meeting.

# Challenge Questions:

Say what a consumer can rely on: can it miss a transaction, can it see
one twice, in what order do transactions arrive, and what happens if the service crashes right after
recording one?

Can it miss a transaction? Yes. The event is published after the database commit, but
the publish is not retried and is not stored anywhere. If the service crashes after the commit and
before the message reaches Kafka, that event is lost. A Kafka failure during publish is logged,
not retried. 

Can it see one transaction twice? Yes. Producer retries can duplicate a message. Consumers must
deduplicate by transactionId.

In what order do transactions arrive? No order is guaranteed. Messages are keyed by
transactionId, so transactions touching the same account can land on different partitions.

What happens if the service crashes right after recording one? The transaction is stored and
its balances are updated, but the announcement can be lost.

# Sample JSON models
**Create an account:**

`{
"currency": "EUR",
"overdraftLimit": 10000
}`

`{
"currency": "EUR",
"overdraftLimit": 0
}`

**Create a transaction:**

`{
"description": "Radu → Rares, 100 EUR",
"entries": [
{
"accountId": "8f33a04b-2eec-48e7-bc5c-860c1f83df46",
"direction": "DEBIT",
"amount": "100.00"
},
{
"accountId": "99081836-f09c-4ae8-bf13-82ad53a0afbb",
"direction": "CREDIT",
"amount": "100.00"
}
]
}`

**Revert a transaction:**
Needs a transactionId and idempotencyKey

# How to use the rabbitmq script in order to test that transactions are working from payment settlement.
Rabbitmq interface can be accesed here: http://localhost:15672 with legder/ledger credentials
cd to the scripts directory and use:

chmod +x publish-payment-settled.sh

Change the line ~~~`~MERCHANT_ACCOUNT_ID="${2:-7f3c9a2e-4b1d-4e8a-9c63-0d5b7e214a98}"~`~~~ in the script with a valid UUID

### Default message: EUR, 125.50, a random paymentId
./publish-payment-settled.sh

Edit the UUIDs with valid ones from Database. 
### Explicit values
./publish-payment-settled.sh p-test-001 7f3c9a2e-4b1d-4e8a-9c63-0d5b7e214a98 125.50 EUR

### Redelivery test: same paymentId, new eventId. The second run must not create a transaction.
./publish-payment-settled.sh p-test-001 7f3c9a2e-4b1d-4e8a-9c63-0d5b7e214a98 125.50 EUR

### Dead-letter test: unsupported currency, should end up in payments.settled.dlq
./publish-payment-settled.sh p-test-usd 7f3c9a2e-4b1d-4e8a-9c63-0d5b7e214a98 10.00 USD
You can check the rabbit interface to find the entry in payments.settled.dlq