#!/bin/bash

IMAGE=$(curl -s https://hub.docker.com/v2/repositories/library/amazoncorretto/tags\?page_size\=1\&page\=1\&ordering\=last_updated\&name\=17-alpine | jq -r '.results.[] | "amazoncorretto:" + .name + "@" + .digest')

echo "$IMAGE"

mv Dockerfile Dockerfile.orig
sed "s|^FROM .*|FROM $IMAGE|" < Dockerfile.orig > Dockerfile
rm Dockerfile.orig
