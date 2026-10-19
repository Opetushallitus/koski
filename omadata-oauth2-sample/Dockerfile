FROM node:24.22.0-alpine3.23@sha256:e44c87c7dd212720eb00469aeac89504ca2be77b8f0d6dbffb30665d5fcc59e1

WORKDIR /app/server

COPY ./server/pnpm-lock.yaml ./server/package.json ./server/pnpm-workspace.yaml ./

RUN npm install --global --ignore-scripts pnpm@12

ENV PORT=8080

RUN pnpm install --prod --frozen-lockfile

COPY ./server/src ./src
COPY ./server/public ./public

USER node

CMD ["node", "src/index.ts"]

EXPOSE ${PORT}
