# Como compilar

## 1. Instalar o NDK e o CMake

No Android Studio: **Settings → Languages & Frameworks → Android SDK → aba SDK Tools**. Marque **NDK (Side by side)** na versão **25.1.8937393** e **CMake**, e aplique.

A versão do NDK precisa ser exatamente essa. As bibliotecas nativas incluídas no projeto foram compiladas contra ela.

## 2. Baixar o OpenCV

Baixe o **OpenCV 4.11.0 Android SDK** em [opencv.org/releases](https://opencv.org/releases/) e extraia em uma pasta de caminho fixo, por exemplo `C:\SDKs\OpenCV-android-sdk`.

Evite deixar na pasta Downloads: se ela for limpa, o projeto para de compilar.

## 3. Apontar o projeto para o OpenCV

Abra o arquivo `gradle.properties` na raiz do projeto e ajuste a última linha para o caminho onde você extraiu o SDK:

```properties
opencvsdk=C:\\SDKs\\OpenCV-android-sdk
```

No Windows, use barras invertidas duplas. No Linux e no macOS, barras simples:

```properties
opencvsdk=/home/usuario/OpenCV-android-sdk
```

Esse caminho é lido pelo `settings.gradle.kts`, que inclui o módulo `:opencv` a partir dele. É o único ajuste manual necessário — sem ele, o sync falha com erro de projeto não encontrado.

## 4. Ajustar o build do módulo OpenCV

O `build.gradle` que acompanha o OpenCV, em `OpenCV-android-sdk/sdk/`, precisa de duas mudanças para conviver com o AGP 8.2.2:

- Remover os blocos `externalNativeBuild`, `prefab` e `publishing`
- Desativar `dataBinding` e `viewBinding` em `buildFeatures`

Sem isso, o sync falha com `NoSuchMethodError: Project.exec` ou com `Cannot mutate the dependencies of configuration`.
