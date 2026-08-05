# Server build
FROM node:24.19.0-alpine3.23@sha256:244cc2b53f46f9e876304391d17682b0ddae9ac33491f4857e25e35a36ba7995 AS server-builder

WORKDIR /app

RUN corepack enable && corepack prepare pnpm@latest --activate

COPY ./server/pnpm-lock.yaml ./server/package.json ./
COPY ./server/tsconfig.json ./
COPY ./server/src ./src

RUN pnpm install --frozen-lockfile
RUN pnpm run build

# Server runtime
FROM node:24.19.0-alpine3.23@sha256:244cc2b53f46f9e876304391d17682b0ddae9ac33491f4857e25e35a36ba7995

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
