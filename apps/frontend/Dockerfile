# ビルドステージ
FROM node:22-alpine AS builder
WORKDIR /app

# ビルド時の環境変数
ARG VITE_DEMO_MODE=false
ARG VITE_DEMO_USERNAME=
ARG VITE_DEMO_PASSWORD=

COPY package*.json ./
RUN npm ci
COPY . .

# ビルド時に環境変数ファイルを生成（.env.production.local は .env.production より優先される）
RUN echo "VITE_DEMO_MODE=${VITE_DEMO_MODE}" >> .env.production.local && \
    echo "VITE_DEMO_USERNAME=${VITE_DEMO_USERNAME}" >> .env.production.local && \
    echo "VITE_DEMO_PASSWORD=${VITE_DEMO_PASSWORD}" >> .env.production.local

RUN npm run api:generate
RUN npm run build

# 実行ステージ
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf.template
COPY start.sh /start.sh
RUN sed -i 's/\r$//' /start.sh && chmod +x /start.sh
EXPOSE 80
CMD ["/bin/sh", "/start.sh"]
