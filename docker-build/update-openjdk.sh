#!/bin/bash

IMAGE=$(curl -s https://hub.docker.com/v2/repositories/adoptopenjdk/openjdk11/tags\?page_size\=1\&page\=1\&ordering\=last_updated\&name\=alpine-slim | jq -r '.results.[] | "adoptopenjdk/openjdk11:" + .name + "@" + .digest')

echo "$IMAGE"

mv Dockerfile Dockerfile.orig
sed "s|^FROM .*|FROM $IMAGE|" < Dockerfile.orig > Dockerfile
rm Dockerfile.orig
