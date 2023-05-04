This is a sample abstract rabbitmq adapter.
In current state is not intended for run, serves as rabbitmq config and workflow demonstration.
Once properly configured for real environment, it is fully functional.

Implemented features:
1. RabbiqMQ queue consumption.
2. Remote REST service call as step of message processing.
3. Retry mechanism using Resilience4j in case remote service (2) returned 4xx or 5xx status code.
4. Rabbit Listener registry that handles listeners lifecycle.
5. Remote service (2) availability monitor, that starts when all retries have been exausted, preliminarily stopping rabbit listener using Rabbit Listener registry (4).
6. Publishing data to RabbitMQ exchange after message processing.
