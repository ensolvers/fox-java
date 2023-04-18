# What is fox-java?

`fox-java` is a set of utilities and classes written in Java to simplify the development of enterprise applications

## How to use

In your `pom.xml`, simply add the following to enable access to our public Maven repositories

```
	<repositories>
		<repository>
			<id>ensolvers-java-fox</id>
			<name>Ensolvers java-fox</name>
			<url>https://maven.ensolvers.com/snapshot</url>
			<snapshots>
				<enabled>true</enabled>
				<updatePolicy>always</updatePolicy>
			</snapshots>
		</repository>
	</repositories>
```

Then, libraries can be referenced simply by its groupId, artifactId and version, for instance:

```
		<dependency>
			<groupId>com.ensolvers.fox-java</groupId>
			<artifactId>fox-s3</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
```

## Libraries included
- [`fox-alert`](modules/fox-alerts/README.md): Utils for creating notification alerts (using Slack)
- [`fox-cache`](modules/fox-cache/README.md): Memcached and Redis typed cache clients which simplify access to most common cache methods, serialization (using Jackson), and so on
- [`fox-chime`](modules/fox-chime/README.md): AWS Chime client
- [`fox-cli`](modules/fox-cli/README.md): Command-line application that allows to generate and send SonarQube reports to a specified Slack channel
- [`fox-quality`](modules/fox-quality/README.md): A utility class that allows to interact with the SonarQube API
- [`fox-cognito`](modules/fox-cognito/README.md): AWS Cognito client
- [`fox-email`](modules/fox-email/README.md): Utilities for email sending and processing
- [`fox-metrics`](modules/fox-metrics/README.md): Service which simplifies metric pushing to AWS Cloudwatch 
- [`fox-s3`](modules/fox-s3/README.md): A service which takes care of CRUD operations for objects from and to AWS S3
- [`fox-location`](modules/fox-location/README.md): IP2Location using MaxMind DB and utilities for IP handling
- [`fox-services`](modules/fox-services/README.md): Provides utility services and classes for logging and rate limiting.
- [`fox-ses`](modules/fox-ses/README.md): Utils for sending emails via AWS SES
- [`fox-sns`](modules/fox-sns/README.md): Utils for sending notifications via AWS SNS
- [`fox-spring`](modules/fox-spring/README.md): General utils for Spring and Spring Boot


## How to collaborate to this project

1. Ask for write access
2. Create a PR with the changes
3. After the PR is approved, ask for AWS credentials (since the repo is hosted in AWS)
4. Configure your AWS environment 
5. Run `deploy.sh`

If `deploy.sh` runs successfully, both the compiled jars and the source code should be upload to the repo