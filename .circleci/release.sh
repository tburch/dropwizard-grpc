#!/usr/bin/env bash

mvn -s .circleci/m2/settings.xml release:prepare \
    -Prelease \
    --batch-mode


mvn -s .circleci/m2/settings.xml release:perform \
    -Prelease \
    --batch-mode