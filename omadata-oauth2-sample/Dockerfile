# Client build
FROM node:22.9.0-alpine3.19@sha256:83b4d7bcfc3d4a40faac3e73a59bc3b0f4b3cc72b9a19e036d340746ebfeaecb AS client-builder

WORKDIR /app
COPY ./client/package*.json ./
COPY ./client/tsconfig.json ./
COPY ./client/src ./src
COPY ./client/public ./public

RUN npm ci
RUN npm run build

# Server build
FROM node:22.9.0-alpine3.19@sha256:83b4d7bcfc3d4a40faac3e73a59bc3b0f4b3cc72b9a19e036d340746ebfeaecb AS server-builder

WORKDIR /app

COPY ./server/package*.json ./
COPY ./server/tsconfig.json ./
COPY ./server/src ./src

RUN npm ci
RUN npm run build

# Server run
FROM node:22.9.0-alpine3.19@sha256:83b4d7bcfc3d4a40faac3e73a59bc3b0f4b3cc72b9a19e036d340746ebfeaecb

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

