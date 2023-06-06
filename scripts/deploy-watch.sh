#!/bin/bash

if ! gh --help &> /dev/null; then
    echo "This script requires gh. See https://cli.github.com/ for installation instructions."
    exit 1
fi

GA_DEPLOY_WORKFLOW_ID=$(gh run list --workflow=deploy.yml --json databaseId -q '.[0]'.databaseId)
gh run watch "$GA_DEPLOY_WORKFLOW_ID"
