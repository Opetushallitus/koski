#!/usr/bin/env python3
# coding=UTF-8

import subprocess
import json

default_uri_values = {
    "OWNER": "opetushallitus",
    "REPO": "koski"
}

def github(*command):
    command = ['gh', *command]
    res = subprocess.run(command, stdout=subprocess.PIPE)
    if res.returncode != 0:
        exit(res.returncode)
    return res.stdout.decode("utf8")

def github_api(api_version, *args):
    response = github(
        "api",
        "-H", "Accept: application/vnd.github+json",
        "-H", "X-GitHub-Api-Version: {}".format(api_version),
        *args
    )
    return json.loads(response)

def uri(template, **values):
    for key, value in (default_uri_values | values).items():
        template = template.replace(key, str(value))
    return template

def filter_failures(xs):
    return filter(lambda x: x["conclusion"] == "failure", xs)

def list_failed_workflow_runs(workflow_id):
    return github_api(
        "2022-11-28",
        uri("/repos/OWNER/REPO/actions/workflows/WORKFLOW_ID/runs?status=failure",
            WORKFLOW_ID = workflow_id))["workflow_runs"]

def list_jobs_for_workflow_run_attempt(run_id, attempt_number):
    return github_api(
        "2022-11-28",
        uri("/repos/OWNER/REPO/actions/runs/RUN_ID/attempts/ATTEMPT_NUMBER/jobs",
            RUN_ID = run_id,
            ATTEMPT_NUMBER = attempt_number))["jobs"]

def analyze_failed_workflow_runs(workflow_id):
    for run in list_failed_workflow_runs(workflow_id):
        for job in filter_failures(list_jobs_for_workflow_run_attempt(run["id"], run["run_attempt"])):
            # Filteröidään pois ajot, jotka eivät koskaan feilanneet steppitasolla. Yleensä Githubin itsensä ongelma.
            failed_steps = list(filter_failures(job["steps"]))
            if len(failed_steps) > 0:
                print(job["name"].ljust(24, " "), run["created_at"], "\t", job["html_url"])

analyze_failed_workflow_runs("run_koski_tests_continuously_on_master.yml")
