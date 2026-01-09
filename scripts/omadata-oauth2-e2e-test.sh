#!/bin/bash
set -eou pipefail
echo "Running OmaDataOAuth2 Sample Playwright tests, shard $2 of $3, using backend on port $1"

cd $(dirname "$0")/../omadata-oauth2-sample

# Start Java sample in background (needs Java 21+, use Java 23 from CI)
cd java
JAVA_HOME="${JAVA_HOME_23_X64:-$JAVA_HOME}" KOSKI_BACKEND_HOST="http://localhost:$1" ./mvnw spring-boot:run &
JAVA_PID=$!
cd ..

# Wait for Java sample to be ready
echo "Waiting for Java sample..."
until curl -sf http://localhost:7052/ > /dev/null 2>&1; do sleep 1; done
echo "Java sample ready"

# Run tests
cd server
KOSKI_BACKEND_PORT="$1" JAVA_SAMPLE_URL="http://localhost:7052" pnpm run playwright:test --shard=$2/$3
TEST_EXIT=$?

kill $JAVA_PID 2>/dev/null || true
exit $TEST_EXIT
