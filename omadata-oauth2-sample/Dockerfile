# Server build
FROM node:22.14.0-alpine3.21@sha256:9bef0ef1e268f60627da9ba7d7605e8831d5b56ad07487d24d1aa386336d1944 AS server-builder

WORKDIR /app

RUN corepack enable && corepack prepare pnpm@latest --activate

COPY ./server/pnpm-lock.yaml ./server/package.json ./
COPY ./server/tsconfig.json ./
COPY ./server/src ./src

RUN pnpm install --frozen-lockfile
RUN pnpm run build

# Server runtime
FROM node:22.14.0-alpine3.21@sha256:9bef0ef1e268f60627da9ba7d7605e8831d5b56ad07487d24d1aa386336d1944

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
