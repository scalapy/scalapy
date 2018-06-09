#!/bin/bash

set -e # exit with nonzero exit code if anything fails

openssl aes-256-cbc -K $encrypted_876a1fdcb9d7_key -iv $encrypted_876a1fdcb9d7_iv -in secrets.tar.enc -out secrets.tar -d

tar xvf secrets.tar

sbt publishSigned sonatypeRelease
