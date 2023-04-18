## fox-services

Provides utility services and classes for logging and rate limiting.

- [`Logger`](./src/main/java/com/ensolvers/fox/services/logging/Logger.java): A generic interface for describing a Logger
- [`NewRelicLogger`](./src/main/java/com/ensolvers/fox/services/logging/NewRelicLogger.java): A implementation for using the New Relic logger to log directly to the NR API.
- [`JsonMap`](./src/main/java/com/ensolvers/fox/services/util/JsonMap.java): A utility class used to create, build, navigate, alter, read and write Json Strings.
- [`SimpleRateLimiter`](./src/main/java/com/ensolvers/fox/services/util/SimpleRateLimiter.java): A utility class to implement a rate limiter