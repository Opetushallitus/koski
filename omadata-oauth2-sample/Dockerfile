# Server build
FROM node:24.14.1-alpine3.23@sha256:01743339035a5c3c11a373cd7c83aeab6ed1457b55da6a69e014a95ac4e4700b AS server-builder

WORKDIR /app

RUN corepack enable && corepack prepare pnpm@latest --activate

COPY ./server/pnpm-lock.yaml ./server/package.json ./
COPY ./server/tsconfig.json ./
COPY ./server/src ./src

RUN pnpm install --frozen-lockfile
RUN pnpm run build

# Server runtime
FROM node:24.14.1-alpine3.23@sha256:01743339035a5c3c11a373cd7c83aeab6ed1457b55da6a69e014a95ac4e4700b

ENV PORT=8080

WORKDIR /app

RUN corepack enable && corepack prepare pnpm@latest --activate

COPY --from=server-builder /app/dist ./server/dist
COPY --from=server-builder /app/pnpm-lock.yaml ./server/
COPY --from=server-builder /app/package.json ./server/
COPY ./server/public ./server/dist/public

WORKDIR /app/server

RUN pnpm install --prod --frozen-lockfile

WORKDIR /app/server/dist

CMD ["node", "index.js"]

EXPOSE ${PORT}
