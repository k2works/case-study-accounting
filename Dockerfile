# ベースイメージとして Ubuntu 24.04 を使用
FROM ubuntu:24.04 AS base

# 環境変数の設定
ENV DEBIAN_FRONTEND=noninteractive \
    LANG=ja_JP.UTF-8 \
    LC_ALL=ja_JP.UTF-8 \
    LC_CTYPE=ja_JP.UTF-8 \
    NODE_VER=22

# ユーザーの設定
ARG USERNAME=developer
ARG USER_UID=1000
ARG USER_GID=$USER_UID

# 既存の ubuntu ユーザーを developer にリネーム、または新規作成
RUN if id ubuntu &>/dev/null; then \
        usermod -l $USERNAME ubuntu && \
        usermod -d /home/$USERNAME -m $USERNAME && \
        groupmod -n $USERNAME ubuntu; \
    else \
        groupadd --gid $USER_GID $USERNAME && \
        useradd --uid $USER_UID --gid $USER_GID -m $USERNAME; \
    fi \
    && apt-get update \
    && apt-get install -y sudo \
    && echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME \
    && chmod 0440 /etc/sudoers.d/$USERNAME

# ロケールのセットアップ
RUN apt-get update && apt-get install -y \
    language-pack-ja-base \
    language-pack-ja \
    && update-locale LANG=ja_JP.UTF-8 LANGUAGE=ja_JP:ja \
    && rm -rf /var/lib/apt/lists/*

# 基本的なパッケージのインストール
RUN apt-get update && \
    apt-get install -y \
        build-essential \
        zip \
        unzip \
        git \
        curl \
        wget \
        vim \
        tmux \
        ca-certificates \
        gnupg \
        && apt-get clean \
        && rm -rf /var/lib/apt/lists/*

# Python 3 のインストール（MkDocs 用）
RUN apt-get update && \
    apt-get install -y \
        python3 \
        python3-pip \
        python3-venv \
        && apt-get clean \
        && rm -rf /var/lib/apt/lists/*

# GitHub CLI のインストール
RUN curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg \
    && chmod go+r /usr/share/keyrings/githubcli-archive-keyring.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | tee /etc/apt/sources.list.d/github-cli.list > /dev/null \
    && apt-get update \
    && apt-get install -y gh \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Heroku CLI のインストール
RUN curl https://cli-assets.heroku.com/install-ubuntu.sh | sh

# Java 25 (SDKMAN!) のインストール準備
RUN apt-get update && \
    apt-get install -y \
        fontconfig \
        && apt-get clean \
        && rm -rf /var/lib/apt/lists/*

# Node.js のインストール（nvm 経由）
RUN mkdir -p /home/$USERNAME/.nvm \
    && chown -R $USERNAME:$USERNAME /home/$USERNAME/.nvm \
    && echo '#!/bin/bash\n\
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash\n\
export NVM_DIR="/home/'$USERNAME'/.nvm"\n\
[ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"\n\
nvm install "'$NODE_VER'"\n\
nvm use "'$NODE_VER'"\n\
nvm alias default "'$NODE_VER'"' > /tmp/nvm_install.sh \
    && chmod +x /tmp/nvm_install.sh \
    && su - $USERNAME -c /tmp/nvm_install.sh \
    && rm /tmp/nvm_install.sh

# SDKMAN! と Java 25 のインストール
RUN echo '#!/bin/bash\n\
curl -s "https://get.sdkman.io" | bash\n\
source "/home/'$USERNAME'/.sdkman/bin/sdkman-init.sh"\n\
sdk install java 25-open\n\
sdk default java 25-open' > /tmp/sdkman_install.sh \
    && chmod +x /tmp/sdkman_install.sh \
    && su - $USERNAME -c /tmp/sdkman_install.sh \
    && rm /tmp/sdkman_install.sh

# Gemini CLI と Claude Code のインストール（nvm 経由の npm を使用）
RUN echo '#!/bin/bash\n\
export NVM_DIR="/home/'$USERNAME'/.nvm"\n\
[ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"\n\
npm install -g @google/gemini-cli\n\
npm install -g @anthropic-ai/claude-code' > /tmp/npm_global_install.sh \
    && chmod +x /tmp/npm_global_install.sh \
    && su - $USERNAME -c /tmp/npm_global_install.sh \
    && rm /tmp/npm_global_install.sh

# すべてのインストールが完了した後、ユーザーのホームディレクトリの所有権を確保
RUN chown -R $USERNAME:$USERNAME /home/$USERNAME

# 作業ディレクトリの設定
WORKDIR /srv

# ユーザーを設定したユーザーに切り替える
USER $USERNAME

# デフォルトのシェルを bash に設定
SHELL ["/bin/bash", "-c"]