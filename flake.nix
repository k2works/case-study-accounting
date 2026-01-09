{
  description = "Development environments managed with Nix for claude-code-booster assets";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        packages = nixpkgs.legacyPackages.${system};
      in
      {
        devShells = {
          default = import ./ops/nix/shells/shell.nix { inherit packages; };
          node = import ./ops/nix/environments/node/shell.nix { inherit packages; };
          python = import ./ops/nix/environments/python/shell.nix { inherit packages; };
        };
      }
    );
}
