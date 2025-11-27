# Koski Development Container

This directory contains the development container configuration for the Koski project. The dev container provides a consistent, reproducible development environment with all necessary tools pre-installed.

## What's Included

### Development Tools
- **JDK 11** - Java Development Kit
- **Maven 3.9** - Build tool for the Scala/Java backend
- **Node.js 22** - JavaScript runtime for frontend
- **pnpm** - Fast, disk space efficient package manager
- **Scala 2.12** - Via Maven plugins
- **Git** - Version control
- **Docker** - For running services (docker-outside-of-docker)

### Shells
- **bash** - Default shell
- **zsh** - Configured as default, with persistent history
- **fish** - Available for use

### Browser Support
- **Xvfb** - Virtual framebuffer for headless browser testing
- **Chromium** - Installed via Playwright for integration tests
- Configured for Claude Code to run browser-based tests

### Additional Tools
- **Claude Code CLI** - AI-powered coding assistant (requires authentication on first use)

## Prerequisites

### For IntelliJ IDEA Ultimate (Recommended for this project)
1. [IntelliJ IDEA Ultimate](https://www.jetbrains.com/idea/) version 2024.1 or later
2. [Docker Desktop](https://www.docker.com/products/docker-desktop/)
3. Dev Container support is built-in (no additional plugins needed)

### For VS Code (Alternative)
1. [Visual Studio Code](https://code.visualstudio.com/)
2. [Dev Containers extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers)
3. [Docker Desktop](https://www.docker.com/products/docker-desktop/)

## Getting Started

### Using IntelliJ IDEA Ultimate

1. Open the Koski project in IntelliJ IDEA Ultimate
2. IDEA should detect the `.devcontainer` configuration automatically
3. Click the notification or go to **File → Remote Development → Dev Containers**
4. Select **"Clone Sources"** when prompted (recommended for security)
   - **Clone Sources**: Source code cloned into container (isolated, secure)
   - **Mount Sources**: Source code shared from host (less secure, not recommended)
5. IDEA will build the container (first time takes 5-10 minutes)
6. Once ready, IDEA will reconnect and you'll be developing inside the container

**Why Clone Sources?**
- ✅ Source code lives inside the container (isolated from host)
- ✅ Prevents malicious code in container from modifying files on your Mac
- ✅ Better security when running AI tools like Claude Code
- ✅ Native Linux filesystem performance (faster builds)
- ⚠️ Must use git operations inside container to commit/push changes

### Using VS Code

1. Open the Koski project folder in VS Code
2. Click the notification "Reopen in Container" or press `F1` → "Dev Containers: Reopen in Container"
3. VS Code will build and start the container
4. Once ready, your terminal will be inside the container

### Using CLI

```bash
# From the project root
devcontainer up --workspace-folder .

# Then attach to the container
devcontainer exec --workspace-folder . /bin/zsh
```

## What Happens on First Start

The `post-create.sh` script automatically runs and:

1. Installs all frontend dependencies for `web/` and `valpas-web/`
2. Downloads Playwright Chromium browser
3. Sets up command history files
4. Starts the database and OpenSearch services via docker-compose

## Available Services

The dev container automatically starts these services (via `docker-compose.yaml` in the project root):

- **PostgreSQL** - Port 5432
- **OpenSearch** - Port 9200
- **DynamoDB Local** - Port 8000
- **LocalStack (S3)** - Port 4566

Access these from within the container at `localhost:<port>`.

## Building and Running Koski

Inside the container, use the standard make commands:

```bash
# Build the entire project
make build

# Run the application
make run

# Watch frontend changes
make watch

# Run tests
make test

# Run integration tests
make fronttest
```

The Koski application will be available at http://localhost:7021/koski/virkailija

## Using Claude Code

Claude Code CLI is pre-installed in the container:

```bash
# Start Claude Code
claude

# First time: authenticate when prompted
# Subsequent times: just runs
```

Claude Code can run browser-based tests thanks to the headless Chromium setup.

## Shell History Persistence

Your command history is persisted across container rebuilds in a Docker named volume:

- **bash**: `/commandhistory/.bash_history`
- **zsh**: `/commandhistory/.zsh_history`
- **fish**: `/commandhistory/.fish_history`

This means your command history survives even if you rebuild the container.

## Switching Shells

```bash
# Switch to zsh (default)
zsh

# Switch to fish
fish

# Switch to bash
bash
```

## Troubleshooting

### Container won't start
- Ensure Docker Desktop is running
- Check Docker has enough resources (recommended: 8GB RAM, 4 CPUs)
- Try rebuilding: **File → Remote Development → Dev Containers → Rebuild Container**

### Services not accessible
- Verify services are running: `docker compose ps`
- Restart services: `docker compose restart`
- Check logs: `docker compose logs <service-name>`

### Build fails
- Clean build artifacts: `make clean`
- Rebuild container from scratch
- Check Docker disk space: `docker system df`

### Claude Code authentication issues
- Run `claude logout` then `claude` to re-authenticate
- Ensure you have a valid Anthropic API key

## Customization

### Adding more tools

Edit `.devcontainer/Dockerfile` to install additional tools.

### Changing ports

Edit `.devcontainer/devcontainer.json` in the `forwardPorts` section.

### Modifying services

Edit the root `docker-compose.yaml` to add/modify services.

## Notes

- The dev container uses **host networking** to easily access services
- Browser operations use **2GB shared memory** for stability
- Shell history is stored in a **Docker named volume** (not in your host filesystem)
- The container runs as user `developer` (UID 1000, GID 1000)
- **Security**: Source code is isolated inside the container (Clone Sources mode)
- Maven and pnpm caches are persisted in Docker volumes for faster rebuilds

## Persisted Data

The following data is stored in Docker named volumes (survives container rebuilds):

- `koski-workspace` - Your cloned source code
- `koski-maven-cache` - Maven dependencies (~/.m2)
- `koski-pnpm-cache` - pnpm package cache
- `koski-shell-history` - Command history for bash/zsh/fish
- `koski-claude-config` - Claude Code configuration

To back up or inspect these volumes:
```bash
docker volume ls | grep koski
docker volume inspect koski-workspace
```

## More Information

- [Dev Containers Documentation](https://containers.dev/)
- [IntelliJ IDEA Remote Development](https://www.jetbrains.com/help/idea/remote-development-overview.html)
- [IntelliJ Dev Containers Support](https://www.jetbrains.com/help/idea/connect-to-devcontainer.html)
- [Koski Project README](../README.md)
