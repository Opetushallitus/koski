# Client build
FROM node:22-alpine AS client-builder

WORKDIR /app
COPY ./client/package*.json ./
COPY ./client/tsconfig.json ./
COPY ./client/src ./src
COPY ./client/public ./public

RUN npm install # TODO: npm ci?
RUN npm run build

# Server build
FROM node:22-alpine AS server-builder

WORKDIR /app

COPY ./server/package*.json ./
COPY ./server/tsconfig.json ./
COPY ./server/src ./src

RUN npm install # TODO: npm ci?
RUN npm run build

# Server run
FROM node:22-alpine

ENV PORT=8080

WORKDIR /app

COPY --from=client-builder /app/build ./client/build
COPY --from=server-builder /app/dist ./server/dist
COPY --from=server-builder /app/package*.json ./server/

WORKDIR /app/server

RUN npm install

WORKDIR /app/server/dist

CMD ["node", "index.js"]

EXPOSE ${PORT}

