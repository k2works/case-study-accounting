# ビルドステージ
FROM gradle:8-jdk21 AS builder
WORKDIR /app
COPY apps/backend/ ./
RUN gradle build -x test --no-daemon

# 実行ステージ
# OpenJDK 公式は Docker Hub での更新を停止しているため eclipse-temurin を使用
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Heroku では EXPOSE は無視されるが、ドキュメントとして残す
EXPOSE 8080

# $PORT 変数をシェル形式で渡す
CMD java -Dserver.port=$PORT -jar app.jar