mvn clean deploy -DskipTests

# deploy fox-cli directly
# TODO this should be done directly using GH actions in the near future
fox_cli_build_dir=$(mktemp -d)
cp modules/fox-cli/target/fox*jar-with-dependencies.jar $fox_cli_build_dir/fox-cli-latest.jar
aws s3 cp --acl public-read $fox_cli_build_dir/fox-cli-latest.jar s3://maven.ensolvers.com/scripts/bin/fox-cli-latest.jar

# sync scripts and other stuff to our distro
aws s3 sync --acl public-read scripts s3://maven.ensolvers.com/scripts

# invalidate CDN distribution
aws cloudfront create-invalidation --distribution-id E107AULWENVHP1 --paths "/*"