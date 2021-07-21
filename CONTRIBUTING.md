# Contributing Guidelines

Thank you for your interest in contributing to the Android distribution
of the Amplify Framework. Whether it's a bug report, new feature,
correction, or additional documentation, the project maintainers at AWS
greatly value your feedback and contributions.

Please read through this document before submitting any issues or pull
requests. Doing so will help to ensure that the project maintainers have
all information necessary to effectively respond to your bug report or
contribution.

- [Contributing Guidelines](#contributing-guidelines)
  - [Getting Started](#getting-started)
    - [Consuming Development Versions of the Framework](#consuming-development-versions-of-the-framework)
  - [Tools](#tools)
  - [Workflows](#workflows)
    - [Build and Validate Your Work](#build-and-validate-your-work)
  - [Reporting Bugs/Feature Requests](#reporting-bugsfeature-requests)
  - [Contributing via Pull Requests](#contributing-via-pull-requests)
  - [Troubleshooting](#troubleshooting)
    - [Environment Debugging](#environment-debugging)
    - [Problems with the Build](#problems-with-the-build)
    - [Getting More Output](#getting-more-output)
  - [Related Repositories](#related-repositories)
  - [Finding Contributions to Make](#finding-contributions-to-make)
  - [Code of Conduct](#code-of-conduct)
  - [Security Issue Notifications](#security-issue-notifications)
  - [Licensing](#licensing)

## Getting Started

First, ensure that you have installed the latest stable version of Android
Studio / the Android SDK.

Configure your environment, so that the `ANDROID_HOME` and `JAVA_HOME`
environment variables are set. A convenient way of doing this is to add them
into `~/.bashrc`. On a Mac, the SDK and Java installation used by the SDK may
be found:

```shell
export ANDROID_HOME=~/Library/Android/sdk
export JAVA_HOME=/Applications/Android\ Studio.app/Contents/jre/jdk/Contents/Home
```
Note: JDK 11, 12, 13, etc. have known issues and are not supported.

Now, clone the Amplify Android Samples project from GitHub.

```shell
git clone git@github.com:aws-amplify/amplify-android-samples.git
```
Load this project into Android Studio by selecting File > Open, and choosing
the root directory of the project (`amplify-android`). Alternately, cd into this
top-level directory.

In Android Studio, build the project by clicking the Hammer icon, "Make
Project ⌘F9". If working on the command line, you can do the same thing
via:

```shell
./gradlew build
```

### Consuming Development Versions of the Framework

Once you've built the framework, you can manually install the Framework
by publishing its artifacts to your local Maven repository.

The local Maven repository is usually found in your home directory at
`~/.m2/repository`.

To publish the outputs of the build, execute the following command from
the root of the `amplify-android` project:

```shell
./gradlew publishToMavenLocal
```

After this, you can use the published development artifacts from an app.
To do so, specify `mavenLocal()` inside the app's top-level
`build.gradle(Project)` file:

```gradle
buildscript {
    repositories {
        mavenLocal() // this should ideally appear before other repositories
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:4.0.1'
    }
}

allprojects {
    repositories {
        mavenLocal() // this should ideally appear before other repositories
    }
}
```
Then, find the `VERSION_NAME` of the *library* inside `gradle.properties` file.

Use the above version to specify dependencies in your *app*'s `build.gradle (:app)` file:
```
dependencies {
    implementation 'com.amplifyframework:core:VERSION_NAME'
}
```

## Tools
[Gradle](https://gradle.org) is used for all [build and dependency management](https://developer.android.com/studio/build).

Some widely used dependencies are:

1. [org.json](https://developer.android.com/reference/org/json/JSONObject) is
   baked into Android, and is used for all modeling with JSON.
2. [Gson](https://github.com/google/gson/blob/master/README.md#gson) is used
   for serialization and deserialization.
3. [OkHttp](https://github.com/square/okhttp#okhttp) is used for network operations.

_Unit and component tests_, which run on your development machine, use:

1. [jUnit](https://github.com/junit-team/junit4/blob/r4.13/README.md#junit-4), to make assertions
2. [Mockito](https://javadoc.io/static/org.mockito/mockito-core/1.10.19/org/mockito/Mockito.html#1), to mock dependencies
3. [Robolectric](https://github.com/robolectric/robolectric/blob/master/README.md),
   to simulate an Android device when unit tests execute on your machine.

_Instrumentation tests_, which run on an Android device or emulator use
AndroidX test core, runner, and a jUnit extension. See Android's notes on
[using AndroidX for test](https://developer.android.com/training/testing/set-up-project).

## Workflows

### Build and Validate Your Work

This will perform a clean build, run Checkstyle, Android Lint, and all unit
tests. This must complete successfully before proposing a PR.

```shell
./gradlew clean build
```

Tip: Checkstyle specifies a specific ordering and spacing of import statements, maximum line length, and other rules.
To setup the Android Studio editor to automatically organize import according to the project checkstyle,  go to
Preferences > Editor > Code Style > Java.  Under Scheme, select "Project".

## Reporting Bugs/Feature Requests

We welcome you to use the GitHub issue tracker to report bugs or suggest
features.

When filing an issue, please check [existing open](https://github.com/awslabs/amplify-android/issues)
and [recently closed](https://github.com/awslabs/amplify-android/issues?utf8=%E2%9C%93&q=is%3Aissue%20is%3Aclosed%20)
issues to make sure somebody else hasn't already reported the issue.
Please try to include as much information as you can. Details like these
are useful:

* The version of the Framework you are using
* Details and configurations for any backend resources that are relevant
* A full exception trace of an error you observe
* A statement about what system behavior you _expect_, alongside the
  behavior you actually observe

## Contributing via Pull Requests

This is mostly the same as [GitHub's guide on creating a pull request](https://help.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request).

First, create a _fork_ of `amplify-android`. Clone it, and make changes to this _fork_.

```shell
git clone git@github.com:your_username/amplify-android.git 
```

After you have tested your feature/fix, by adding sufficient test coverage, and
validating Checkstyle, lint, and the existing test suites, you're ready to
publish your change.

The commit message should look like below. It started with a bracketed tag
stating which module has been the focus of the change. After a paragraph
describing what you've done, include links to useful resources. These might
include design documents, StackOverflow implementation notes, GitHub issues,
etc. All links must be publicly accessible.

```console
[aws-datatore] Add a 3-way merging component for network ingress

The Merger checks the state of the Mutation Outbox before applying
remote changes, locally. Subscriptions, Mutation responses, and
base/delta sync all enter the local storage through the Merger.

Resolves: https://github.com/amplify-android/issues/222
See also: https://stackoverflow.com/a/58662077/695787
```

Now, save your work to a new branch:

```shell
git checkout -B add_merger_to_datastore
```

To publish it:

```shell
git push -u origin add_merger_to_datastore
```

This last step will give you a URL to view a GitHub page in your browser.
Copy-paste this, and complete the workflow in the UI. It will invite you to
"create a PR" from your newly published branch.

Your PR must be reviewed by at least one repository maintainer, in order
to be considered for inclusion.

your PR must also pass the CircleCI workflow and LGTM validations. CircleCI
will run all build tasks (Checkstyle, Lint, unit tests).

Currently, CircleCI **DOES NOT** run instrumentation tests for PRs that come
from user forks. You should run these tests on your laptop before submitting
the PR.

## Troubleshooting

### Environment Debugging

Are you using the right versions of Gradle, Ant, Groovy, Kotlin, Java, Mac OS X?
```console
./gradlew -version

------------------------------------------------------------
Gradle 6.6
------------------------------------------------------------

Build time:   2020-08-10 22:06:19 UTC
Revision:     d119144684a0c301aea027b79857815659e431b9

Kotlin:       1.3.72
Groovy:       2.5.12
Ant:          Apache Ant(TM) version 1.10.8 compiled on May 10 2020
JVM:          1.8.0_242-release (JetBrains s.r.o 25.242-b3-6222593)
OS:           Mac OS X 10.15.6 x86_64
```

Do you have the Android SDK setup, and do you have a pointer to the Java environment?

```console
echo -e $ANDROID_HOME\\n$JAVA_HOME 
/Users/jhwill/Library/Android/sdk
/Applications/Android Studio.app/Contents/jre/jdk/Contents/Home
```

### Problems with the Build

If the build fails, and you can't figure out why from a Google search /
StackOverflow session, try passing options to Gradle:

```shell
./gradlew --stacktrace
```

The next flag will spit out lots of info. It's only useful if you pipe the
output to a file, and grep through it.

```shell
./gradlew --debug 2>&1 > debugging-the-build.log
```

### Getting More Output

The Amplify Android library emits logs while it is running on a device
or emulator. By default, debug and verbose logs are not output.
However, you can change the log threshold at runtime, by explicitly
configuring a logging plugin:
```kotlin
Amplify.addPlugin(AndroidLoggingPlugin(LogLevel.VERBOSE))
// ... Add more plugins only *after* setting the log plugin.
```

This project is part of the Amplify Framework, which runs on Android,
iOS, and numerous JavaScript-based web platforms. The Amplify CLI
provides an entry point to configure backend resources for all of these
platforms.

1. [AWS Amplify for Flutter](https://github.com/aws-amplify/amplify-flutter)
2. [AWS Amplify for iOS](https://github.com/aws-amplify/amplify-ios)
3. [AWS Amplify for JavaScript](https://github.com/aws-amplify/amplify-js)
4. [AWS Amplify CLI](https://github.com/aws-amplify/amplify-cli)

AWS Amplify plugins are built on top of "low-level" AWS SDKs. AWS SDKs are a
toolkit for interacting with AWS backend resources.

1. [AWS SDK for Android](https://github.com/aws-amplify/aws-sdk-android)
2. [AWS SDK for iOS](https://github.com/aws-amplify/aws-sdk-ios)
3. [AWS SDK for JavaScript](https://github.com/aws/aws-sdk-js-v3)

## Finding Contributions to Make
Looking at [the existing issues](https://github.com/aws-amplify/amplify-android/issues) is a
great way to find something to work on.

## Code of Conduct
This project has adopted the [Amazon Open Source Code of Conduct](https://aws.github.io/code-of-conduct).
For more information see the [Code of Conduct FAQ](https://aws.github.io/code-of-conduct-faq) or contact
opensource-codeofconduct@amazon.com with any additional questions or comments.

## Security Issue Notifications
If you discover a potential security issue in this project we ask that you notify AWS/Amazon Security via our
[vulnerability reporting page](http://aws.amazon.com/security/vulnerability-reporting/). Please
do **not** create a public GitHub issue.

## Licensing

See the
[LICENSE](https://github.com/awslabs/amplify-android/blob/master/LICENSE)
for more information. We will ask you to confirm the licensing of your
contribution.

We may ask you to sign a
[Contributor License Agreement (CLA)](http://en.wikipedia.org/wiki/Contributor_License_Agreement) for
larger changes.
