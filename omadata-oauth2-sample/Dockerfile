# Shared base
FROM node:24.21.0-alpine3.23@sha256:9ec4a2e289874ed0d722e1772ec2de45d2801541db8612f3638b26f128c69ac2 AS base

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
