#!/bin/bash
set -e

# Скачиваем gradle если нужно
if [ ! -f gradle-8.2/bin/gradle ]; then
    curl -L https://services.gradle.org/distributions/gradle-8.2-bin.zip -o gradle.zip
    unzip -q gradle.zip
    rm gradle.zip
fi

# Запускаем
./gradle-8.2/bin/gradle "$@"
