# Building and Testing Argos

This document describes how to set up your development environment to build and test Argos.
It also explains the basic mechanics of using `git`, `maven`.

* [Prerequisite Software](#prerequisite-software)
* [Getting the Sources](#getting-the-sources)
* [Building](#building)
* [Running Tests Locally](#running-tests-locally)

See the [contribution guidelines](https://github.com/rabobank/argos/blob/master/CONTRIBUTING.md)
if you'd like to contribute to Argos.

## Prerequisite Software

Before you can build and test Argos, you must install and configure the
following products on your development machine:

* [Git](http://git-scm.com) and/or the **GitHub app** (for [Mac](http://mac.github.com) or
  [Windows](http://windows.github.com)); [GitHub's Guide to Installing
  Git](https://help.github.com/articles/set-up-git) is a good source of information.

* [Maven](https://maven.apache.org).

* [Lombok](https://projectlombok.org).

* [Docker](https://www.docker.com).


## Getting the Sources

Fork and clone the Argos repository:

1. Login to your GitHub account or create one by following the instructions given
   [here](https://github.com/signup/free).
2. [Fork](http://help.github.com/forking) the [main Argos
   repository](https://github.com/rabobank/argos).
3. Clone your fork of the Argos repository and define an `upstream` remote pointing back to
   the Argos repository that you forked in the first place.

```shell
# Clone your GitHub repository:
git clone git@github.com:<github username>/argos.git

# Go to the Argos directory:
cd argos

# Add the main Argos repository as an upstream remote to your repository:
git remote add upstream https://github.com/rabobank/argos.git
```

## Building

To build Argos run:

```shell
mvn clean install
```

* Results are put in the diverse `target` folders.

## Running Tests Locally

You should execute all test suites before submitting a PR to GitHub:

```shell
mvn -q clean install
cd argos-test
mvn -q clean verify -Pregression-test-drone

```

**Note**: The first test run will be much slower than future runs. This is because future runs will
benefit from Bazel's capability to do incremental builds.

All the tests are executed on our Continuous Integration infrastructure. PRs can only be
merged if the code is formatted properly and all tests are passing.

<a name="clang-format"></a>
## Formatting your source code

Argos uses [clang-format](http://clang.llvm.org/docs/ClangFormat.html) to format the source code.
If the source code is not properly formatted, the CI will fail and the PR cannot be merged.

A better way is to set up your IDE to format the changed file on each file save.

