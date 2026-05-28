# Server build
FROM node:24.16.0-alpine3.23@sha256:2bdb65ed1dab192432bc31c95f94155ca5ad7fc1392fb7eb7526ab682fa5bf14 AS server-builder

WORKDIR /app

RUN corepack enable && corepack prepare pnpm@latest --activate

COPY ./server/pnpm-lock.yaml ./server/package.json ./
COPY ./server/tsconfig.json ./
COPY ./server/src ./src

RUN pnpm install --frozen-lockfile
RUN pnpm run build

# Server runtime
FROM node:24.16.0-alpine3.23@sha256:2bdb65ed1dab192432bc31c95f94155ca5ad7fc1392fb7eb7526ab682fa5bf14

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
