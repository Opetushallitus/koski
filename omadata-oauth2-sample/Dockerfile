# Shared base
FROM node:24.21.0-alpine3.23@sha256:159fe64649038c30f8cc1ec4be3af3a6e93e3648678c31294e2c5058dbeb99f3 AS base

WORKDIR /app/server

COPY ./server/pnpm-lock.yaml ./server/package.json ./server/pnpm-workspace.yaml ./

RUN npm install --global --ignore-scripts pnpm@10

# Server build
FROM base AS server-builder

RUN pnpm install --frozen-lockfile

COPY ./server/tsconfig.json ./
COPY ./server/src ./src

RUN pnpm run build

# Server runtime
FROM base

ENV PORT=8080

RUN pnpm install --prod --frozen-lockfile

COPY --from=server-builder /app/server/dist ./dist
COPY ./server/public ./dist/public

WORKDIR /app/server/dist

CMD ["node", "index.js"]

EXPOSE ${PORT}
