{ packages ? import <nixpkgs> {} }:
packages.mkShell {
  buildInputs = with packages; [
    git
    curl
    wget
    vim
    tmux
    zip
    unzip
  ];
  # ホスト環境から完全に分離する
  pure = true;
  shellHook = ''
    echo "Welcome to the common development environment"
  '';
}
