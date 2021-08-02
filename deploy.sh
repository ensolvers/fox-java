mvn deploy -DskipTests
aws s3 sync --acl public-read scripts s3://maven.ensolvers.com/scripts
aws cloudfront create-invalidation --distribution-id E107AULWENVHP1 --paths "/*"