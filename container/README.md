# Running the Hypercube API server in a container

## Container tools

This project relies on [Container Tools](https://github.com/containers) project.
Check its page for details.

[Buildah](https://buildah.io/) and [Podman](https://podman.io/) are used for building and running a container for Variant store connector.

Fedora and RHEL (CentOS) distros have these tools in standard repositories.

For installing them in Ubuntu, check [Project Atomic PPA](https://launchpad.net/~projectatomic/+archive/ubuntu/ppa).

After installation, check if you have the `/etc/containers/registries.conf` file, with some content similar to:
```
[registries.search]
registries = ['docker.io', 'registry.fedoraproject.org', 'quay.io', 'registry.access.redhat.com', 'registry.centos.org']
```

## Baking your own image

To build your own container image run following command:
```bash
buildah unshare ./buildah.sh
```

To copy the image to the local Docker repository, run:
```bash
skopeo copy containers-storage:docker.io/thehyve/hypercube-api-server:latest docker-daemon:thehyve/hypercube-api-server:latest
```

## Getting pre-built images

We have already [pre-built images at Docker Hub](https://hub.docker.com/r/thehyve/hypercube-api-server).
You can get one by running one of following commands:
* Podman
    ```bash
    podman pull docker.io/thehyve/hypercube-api-server:0.0.2
    ```
* Docker
    ```bash
    docker pull thehyve/hypercube-api-server:0.0.2
    ```

## Running a container

To run an instance of variant store container run one of following commands:
* Podman
    ```bash
    podman run -p 9090:9090 -it --rm --env KEYCLOAK_SERVER_URL=https://keycloak.example.com --env KEYCLOAK_REALM=transmart --env KEYCLOAK_CLIENT_ID=transmart-client docker.io/thehyve/hypercube-api-server:0.0.2
    ```
* Docker
    ```bash
    docker run -p 9090:9090 -it --rm --env KEYCLOAK_SERVER_URL=https://keycloak.example.com --env KEYCLOAK_REALM=transmart --env KEYCLOAK_CLIENT_ID=transmart-client thehyve/hypercube-api-server:0.0.2
    ```
