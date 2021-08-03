mkdir .fox-cli
curl https://maven.ensolvers.com/scripts/bin/fox-cli-latest.jar --output .fox-cli/fox-cli-latest.jar
curl https://maven.ensolvers.com/scripts/fox-cli.env --output .fox-cli/fox-cli.env

echo "fox-cli installed successfully, run 'source .fox-cli/fox-cli.env' to invoke commands in this shell instance"

