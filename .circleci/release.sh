#!/usr/bin/env bash

mvn -s .circleci/m2/settings.xml release:prepare \
    --batch-mode


mvn -s .circleci/m2/settings.xml release:perform \
    --batch-mode