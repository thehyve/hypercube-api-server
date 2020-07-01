#!/usr/bin/env bash
set -ex
here=$(dirname "${0}")
if test -z "$HYPERCUBE_API_SERVER_VERSION"; then
  echo "Variable \$HYPERCUBE_API_SERVER_VERSION is not set." >&2; exit 1
else
  echo "Building container for hypercube-api-server:${HYPERCUBE_API_SERVER_VERSION} ..."
fi
export BUILDAH_FORMAT=docker
export MAVEN_VERSION="3.6.1-jdk-11"
export REPO_URL="https://repo.thehyve.nl/content/repositories/releases/"
export JAVA_VERSION="11-jre"
export JAVACACERTDIR="/usr/local/openjdk-11/lib/security"
export PORT=9090
NEWCONTAINER=$(buildah from openjdk:${JAVA_VERSION})
SCRATCHMNT=$(buildah mount "${NEWCONTAINER}")
buildah run "${NEWCONTAINER}" useradd -b /opt -m -r hypercube
podman --cgroup-manager=cgroupfs run --rm -v "${SCRATCHMNT}":/mnt:rw --systemd=false maven:${MAVEN_VERSION} mvn dependency:get -DremoteRepositories="${REPO_URL}" -Dartifact="nl.thehyve:hypercube-api-server:${HYPERCUBE_API_SERVER_VERSION}:jar" -Dtransitive=false -Ddest=/mnt/opt/hypercube/hypercube-api-server.jar
buildah config --env JAVACACERTDIR=${JAVACACERTDIR} "${NEWCONTAINER}"
buildah run "${NEWCONTAINER}" chown hypercube /opt/hypercube/hypercube-api-server.jar ${JAVACACERTDIR}/cacerts ${JAVACACERTDIR}
buildah run "${NEWCONTAINER}" chmod 644 ${JAVACACERTDIR}/cacerts
buildah copy --chown hypercube:hypercube "${NEWCONTAINER}" "${here}/entrypoint.sh" '/opt/hypercube'
buildah config --label name=hypercube-api-server "${NEWCONTAINER}"
buildah config --env PORT=${PORT} "${NEWCONTAINER}"
buildah config --workingdir /opt/hypercube "${NEWCONTAINER}"
buildah config --port ${PORT} "${NEWCONTAINER}"
buildah config --user hypercube "${NEWCONTAINER}"
buildah config --entrypoint /opt/hypercube/entrypoint.sh "${NEWCONTAINER}"
buildah unmount "${NEWCONTAINER}"
buildah commit "${NEWCONTAINER}" hypercube-api-server
buildah tag hypercube-api-server docker.io/thehyve/hypercube-api-server:latest
buildah tag hypercube-api-server "docker.io/thehyve/hypercube-api-server:${HYPERCUBE_API_SERVER_VERSION}"
