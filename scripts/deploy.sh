#!/bin/bash

set -e

if ! gh --help &> /dev/null; then
    echo "This script requires gh. See https://cli.github.com/ for installation instructions."
    exit 1
fi

ENVIRONMENT="$1"

if [ -z "$ENVIRONMENT" ]; then
    echo "Usage: $0 dev/qa/prod [commit hash]"
    exit 64
fi

COMMIT=${2:-$(git log "origin/$(git rev-parse --abbrev-ref HEAD)" --pretty=format:%H -n 1)}

echo "Deploy to $ENVIRONMENT following commit:"
echo
git --no-pager log "$COMMIT" -n 1
echo

read -p "Are you sure? " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    gh workflow run deploy.yml -f environment="$ENVIRONMENT" -f commithash="$COMMIT"
    echo
    echo OK! Waiting 5 seconds before starting watching...
    sleep 5
    GA_DEPLOY_WORKFLOW_ID=$(gh run list --workflow=deploy.yml --json databaseId -q '.[0]'.databaseId)
    gh run watch "$GA_DEPLOY_WORKFLOW_ID"
fi

