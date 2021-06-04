mvn deploy -DskipTests 
aws cloudfront create-invalidation --distribution-id E107AULWENVHP1 --paths "/*"