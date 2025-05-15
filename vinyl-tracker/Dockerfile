FROM node:18 AS builder

WORKDIR /app
ENV NX_DAEMON=false

RUN npm install -g pnpm

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml nx.json tsconfig.base.json eslint.config.mjs ./
COPY frontend ./frontend

RUN pnpm install
RUN pnpm nx build frontend

FROM node:18

WORKDIR /app

RUN npm install -g serve

COPY --from=builder /app/dist/frontend ./dist

EXPOSE 4200

CMD ["serve", "-s", "dist", "-l", "4200"]