#!/usr/bin/env bash

mvn -Dgpg.passphrase=${env.GPG.PASSPHRASE} \
    -s .circleci/m2/settings.xml release:prepare \
    --batch-mode


mvn -Dgpg.passphrase=${env.GPG.PASSPHRASE} \
    -s .circleci/m2/settings.xml release:perform \
    --batch-mode