#!/usr/bin/env bash

here=$(dirname "${0}")
HYPERCUBE_API_SERVER_VERSION=$(mvn -q -f "${here}/.." -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)
export HYPERCUBE_API_SERVER_VERSION
buildah unshare "${here}/../container/buildah.sh" && \
podman login -u "$DOCKER_USERNAME" -p "$DOCKER_PASSWORD" docker.io && \
podman push "thehyve/hypercube-api-server:${HYPERCUBE_API_SERVER_VERSION}"
