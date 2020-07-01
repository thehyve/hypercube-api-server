#!/usr/bin/env bash
set -e

# Error message and exit for missing environment variable
fatal() {
  var_name=$1
   cat >&2 << EndOfMessage
-------------------------------------------------------------
Configuration error!
-------------------------------------------------------------
The variable with the name '${var_name}' is not set.
Please specify a value in the container environment using -e
in 'docker run' or the environment section in docker-compose.
EndOfMessage
    exit 1
}

# Check that Keycloak settings are configured via environment variables
if test -z "${KEYCLOAK_SERVER_URL}"; then fatal 'KEYCLOAK_SERVER_URL'; fi
if test -z "${KEYCLOAK_REALM}"; then fatal 'KEYCLOAK_REALM'; fi
if test -z "${KEYCLOAK_CLIENT_ID}"; then fatal 'KEYCLOAK_CLIENT_ID'; fi

# Fixed values, not configurable by user
CERTS_PATH="/opt/connector/extra_certs.pem"

# Import custom certificates
[[ -f "${CERTS_PATH}" ]] && \
    keytool -importcert -trustcacerts -file "${CERTS_PATH}" -alias certificate-alias -keystore "${JAVACACERTDIR}/cacerts" -storepass changeit -noprompt

# Run variant store connector
exec java \
     "-Dserver.port=${PORT}" \
     "-Dspring.profiles.active=prod" \
     -jar \
     /opt/hypercube/hypercube-api-server.jar
