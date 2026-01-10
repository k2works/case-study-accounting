{ packages ? import <nixpkgs> {} }:
let
  baseShell = import ../../shells/shell.nix { inherit packages; };
in
packages.mkShell {
  inherit (baseShell) pure;
  buildInputs = baseShell.buildInputs ++ (with packages; [
    jdk25
    gradle
    postgresql
  ]);
  shellHook = ''
    ${baseShell.shellHook}
    echo "Backend (Spring Boot + Java + Gradle) development environment activated"
    echo "  - Java: $(java --version | head -1)"
    echo "  - Gradle: $(gradle --version | grep Gradle | head -1)"
  '';
}
