FROM node:26.13.0-alpine3.23@sha256:44a3247313f9bc069a4ec25d01b66254ed7dc4d0408727d8720dc827f0753423

WORKDIR /app/server

COPY ./server/pnpm-lock.yaml ./server/package.json ./server/pnpm-workspace.yaml ./

RUN npm install --global --ignore-scripts pnpm@12

ENV PORT=8080

RUN pnpm install --prod --frozen-lockfile && \
    rm -rf /usr/local/lib/node_modules /usr/local/bin/npm /usr/local/bin/npx /usr/local/bin/pnpm /usr/local/bin/pnpx \
      /usr/local/bin/corepack /usr/local/bin/yarn /usr/local/bin/yarnpkg /opt/yarn-* /root/.local/share/pnpm /root/.cache /root/.npm

COPY ./server/src ./src
COPY ./server/public ./public

USER node

CMD ["node", "src/index.ts"]

EXPOSE ${PORT}
