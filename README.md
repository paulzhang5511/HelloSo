# rustlib


## 使用的库

* [cargo ndk](https://github.com/bbqsrc/cargo-ndk)
* [android toolchain](https://doc.rust-lang.org/nightly/rustc/platform-support.html)


## 运行 

```
cargo ndk -t arm64-v8a -o ../app/src/main/jniLibs/  build
```

## config.toml

```toml
[target.aarch64-linux-android]
linker = 'D:\software\android\sdk\ndk\26.3.11579264\toolchains/llvm/prebuilt/windows-x86_64\bin\aarch64-linux-android24-clang.cmd'
rustflags = [
    "-Clink-arg=-landroid",
    "-Clink-arg=-llog",
    "-Clink-arg=-lOpenSLES",
]

[target.armv7-linux-androideabi]
linker = 'D:\software\android\sdk\ndk\26.3.11579264\toolchains/llvm/prebuilt/windows-x86_64\bin\armv7a-linux-androideabi24-clang.cmd'
rustflags = [
    "-Clink-arg=-landroid",
    "-Clink-arg=-llog",
    "-Clink-arg=-lOpenSLES",
]

[target.i686-linux-android]
linker = 'D:\software\android\sdk\ndk\26.3.11579264\toolchains/llvm/prebuilt/windows-x86_64\bin\i686-linux-android24-clang.cmd'
rustflags = [
    "-Clink-arg=-landroid",
    "-Clink-arg=-llog",
    "-Clink-arg=-lOpenSLES",
]

[target.x86_64-linux-android]
linker = 'D:\software\android\sdk\ndk\26.3.11579264\toolchains/llvm/prebuilt/windows-x86_64\bin\x86_64-linux-android24-clang.cmd'
rustflags = [
    "-Clink-arg=-landroid",
    "-Clink-arg=-llog",
    "-Clink-arg=-lOpenSLES",
]

```