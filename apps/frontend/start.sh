#!/bin/sh

# 環境変数をデフォルト値で設定
: "${PORT:=80}"
: "${API_URL:=http://localhost:8080/api}"

# nginx.conf の環境変数を置換
envsubst '${PORT} ${API_URL}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf

# nginx を起動
exec nginx -g 'daemon off;'
