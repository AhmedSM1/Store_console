#!/bin/bash

SONAR_URL="http://localhost:9000"
ADMIN_AUTH="admin:admin"
# List your project folders here
PROJECTS=("products" "authorization")

echo "Waiting for SonarQube to be ready..."
until $(curl --output /dev/null --silent --head --fail $SONAR_URL); do
    printf '.'
    sleep 5
done
echo -e "\nSonarQube is UP."


# Navigate to the project root directory (one level up from the scripts folder)
cd "$(dirname "$0")/.." || exit

for DIR in "${PROJECTS[@]}"; do
    echo ">>> Analyzing Project: $DIR"
    if [ ! -d "$DIR" ]; then
        echo "Error: Directory $DIR not found in $(pwd)"
        continue
    fi
    cd "$DIR" || exit
    chmod +x mvnw
    
    # 1. Create Project
    curl -u $ADMIN_AUTH -X POST "$SONAR_URL/api/projects/create?name=$DIR&project=$DIR"
    
    # 2. Generate Token
    TOKEN=$(curl -u $ADMIN_AUTH -X POST "$SONAR_URL/api/user_tokens/generate?name=token-$DIR-$(date +%s)" | grep -o '"token":"[^"]*' | grep -o '[^"]*$')

    # 3. Run Scan (All on one line to prevent "No such file" errors)
    ./mvnw clean verify sonar:sonar -Dsonar.projectKey="$DIR" -Dsonar.host.url="$SONAR_URL" -Dsonar.token="$TOKEN" -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml

    cd ..
done

echo "All projects scanned! View them at $SONAR_URL"