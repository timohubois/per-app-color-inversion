{ pkgs ? import <nixpkgs> {} }:
let
  android = pkgs.androidenv.composeAndroidPackages {
    platformVersions = [ "35" ];
    buildToolsVersions = [ "34.0.0" "35.0.0" ];
  };
in pkgs.mkShell {
  packages = [ pkgs.gradle pkgs.jdk17 android.androidsdk ];
  ANDROID_HOME = "${android.androidsdk}/libexec/android-sdk";
  ANDROID_SDK_ROOT = "${android.androidsdk}/libexec/android-sdk";
}
