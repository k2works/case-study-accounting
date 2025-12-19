FROM schemaspy/schemaspy:latest

# Install Japanese fonts for Graphviz
USER root
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    fonts-noto-cjk \
    fonts-noto-cjk-extra \
    fontconfig && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Update font cache
RUN fc-cache -f -v

# Set locale
ENV LANG=ja_JP.UTF-8 \
    LC_ALL=ja_JP.UTF-8

USER java
