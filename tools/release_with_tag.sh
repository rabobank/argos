#!/bin/bash

set -e

usage() {
    echo "Usage: ./release_with_tag.sh  <release version>"
    echo
    echo "This will force a release of the project with the <release version>"
}

push() {
    git tag -f ${VERSION}
    git push -f origin refs/tags/${VERSION}
}

if [ -z "$1" ]; then
    usage
    exit 8
fi

VERSION=$1

echo "This will force a release of the project with the version ${VERSION}"
echo "with a force push of the tag ${VERSION}"
while true; do
    read -p "Are you sure you wish to do this [y/n]: " yn
    case $yn in
        [Yy]* ) push; break;;
        [Nn]* ) echo "Abort..."; exit;;
        * ) echo "Please answer yes(y) or no(n).";;
    esac
done
