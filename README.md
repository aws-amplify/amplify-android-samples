# amplify-android-samples
This repository contains sample applications to demonstrate usage of Amplify Android.
* `todo`: Demonstrates DataStore with a Todo schema

## Setup Using Amplify Admin UI
Note: You may need to create an [AWS account](https://docs.amplify.aws/lib/project-setup/prereq/q/platform/android) and configure it before testing the backend.<br />

* Clone the sample GitHub repo under your preferred directory.
* Change directory to root folder of whichever language you want to use. For Java: `cd getting-started/todo/java`. For Kotlin: `cd getting-started/todo/kotlin`.
* Follow the [Instructions](https://docs.amplify.aws/start/getting-started/installation/q/integration/android) to provision Amplify and run this application. (Skip "Set up fullstack project" and "Integrate in your app" and any instructions that changes code)

## Setup using Amplify CLI

1. Please follow the [instructions](https://docs.amplify.aws/lib/project-setup/prereq/q/platform/android/) to sign up for an AWS Account and setup the Amplify CLI.

2. Initialize Amplify in your project by running the following command from your project directory:

```
amplify init
```
Provide the responses shown after each of the following prompts.
```
? Enter a name for the environment
    `dev`
? Choose your default editor:
    `Android Studio`
? Where is your Res directory:
    `app/src/main/res`
? Select the authentication method you want to use: 
    `AWS profile`
? Please choose the profile you want to use 
    `default`
```
Wait until provisioning is finished. Upon successfully running `amplify init`, you will see a configuration file created in `./app/src/main/res/raw/` called `amplifyconfiguration.json`. This file will be bundled into your application so that the Amplify libraries know how to reach your provisioned backend resources at runtime.

3. Configure DataStore category

From your project directory, run the following command to add the Amplify API category and create a new GraphQL API:
```
amplify add api
```
Provide the responses shown after each of the following prompts.
```
? Please select from one of the below mentioned services: 
    `GraphQL`
? Provide API name: 
    `ToDoAPI`
? Choose the default authorization type for the API 
    `Amazon Cognito User Pool`
? Do you want to configure advanced settings for the GraphQL API 
    `Yes, I want to make some additional changes.`
? Configure additional auth types? 
    `No`
? Enable conflict detection? 
    `Yes`
? Select the default resolution strategy 
    `Auto Merge`
? Do you have an annotated GraphQL schema? 
    `Yes`
? Provide your schema file path: 
    `schema.graphql`
```
This will configure the Amplify API category to provision a GraphQL service with CRUD operations to persist data locally and automatically synchronize local data to the cloud with Amplify DataStore. 

The GraphQL schema is defined as follows: 

```
enum Priority {
    LOW
    NORMAL
    HIGH
}

type Todo @model @auth(rules: [{allow: public}]) {
    id: ID!
    name: String!
    priority: Priority!
    completedAt: AWSDateTime
}
```

4. Generate model files
   
   From your project directory, run `amplify codegen models`. This will generate model files from the GraphQL schema. You'll find the generated files under `amplify-android-samples/getting-started/todo/java/app/src/main/java/com/amplifyframework/datastore/generated/model/`(if you are using Java version) or `amplify-android-samples/getting-started/todo/kotlin/app/src/main/java/com/amplifyframework/datastore/generated/model/`(if you are using Kotlin version) directory.
   
5. Once finished, run `amplify push` to publish your changes. Provide the responses shown after each of the following prompts.
```
? Are you sure you want to continue?
    `Yes`
? Do you want to generate code for your newly created GraphQL API?
    `No`
```

Upon completion, `amplifyconfiguration.json` should be updated to reference these provisioned backend resources.

## Run the app

- Build and run the project on an emulator or Android device in Android Studio.