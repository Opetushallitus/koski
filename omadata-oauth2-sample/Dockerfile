# Client build
FROM node:22.14.0-alpine3.21@sha256:9bef0ef1e268f60627da9ba7d7605e8831d5b56ad07487d24d1aa386336d1944 AS client-builder

WORKDIR /app
COPY ./client/package*.json ./
COPY ./client/tsconfig.json ./
COPY ./client/src ./src
COPY ./client/public ./public

RUN npm ci
RUN npm run build

# Server build
FROM node:22.14.0-alpine3.21@sha256:9bef0ef1e268f60627da9ba7d7605e8831d5b56ad07487d24d1aa386336d1944 AS server-builder

WORKDIR /app

COPY ./server/package*.json ./
COPY ./server/tsconfig.json ./
COPY ./server/src ./src

RUN npm ci
RUN npm run build

# Server run
FROM node:22.14.0-alpine3.21@sha256:9bef0ef1e268f60627da9ba7d7605e8831d5b56ad07487d24d1aa386336d1944

ENV PORT=8080

WORKDIR /app

COPY --from=client-builder /app/build ./client/build
COPY --from=server-builder /app/dist ./server/dist
COPY --from=server-builder /app/package*.json ./server/

WORKDIR /app/server

RUN npm ci

WORKDIR /app/server/dist

CMD ["node", "index.js"]

EXPOSE ${PORT}

