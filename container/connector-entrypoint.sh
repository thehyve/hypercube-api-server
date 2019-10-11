#!/usr/bin/env bash
set -e

# Error message and exit for missing environment variable
fatal() {
   cat << EndOfMessage
###############################################################################
!!!!!!!!!! FATAL ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
###############################################################################
            The variable with the name '$IT is unset.
            Please specify a value in this container environment using
            -e in docker run or the environment section in Docker Compose.
###############################################################################
EndOfMessage
    exit 1
}

# Check that Keycloak, Transmart and Variant store settings
# are configured via environment variables
[[ -n ${KEYCLOAK_SERVER_URL+x} ]] || fatal 'KEYCLOAK_SERVER_URL'
[[ -n ${KEYCLOAK_REALM+x} ]] || fatal 'KEYCLOAK_REALM'
[[ -n ${KEYCLOAK_CLIENT_ID+x} ]] || fatal 'KEYCLOAK_CLIENT_ID'

# Fixed values, not configurable by user
CERTS_PATH="/opt/connector/extra_certs.pem"

# Import custom certificates
[[ -f "${CERTS_PATH}" ]] && \
    keytool -importcert -trustcacerts -file "${CERTS_PATH}" -alias certificate-alias -keystore ${JAVACACERTDIR}/cacerts -storepass changeit -noprompt

# Run variant store connector
exec java \
     "-Dserver.port=${PORT}" \
     "-Dkeycloak.auth-server-url=${KEYCLOAK_SERVER_URL}/auth" \
     "-Dkeycloak.realm=${KEYCLOAK_REALM}" \
     "-Dkeycloak.resource=${KEYCLOAK_CLIENT_ID}" \
     -jar \
     /opt/connector/hypercube-api-server.jar
