# amplify-android-samples
This repository contains sample applications to demonstrate usage of Amplify Android.
* `todo`: Demonstrates DataStore with a Todo schema

## Setup
Note: You may need to create an [AWS account](https://docs.amplify.aws/lib/project-setup/prereq/q/platform/android) and configure it before testing the backend.<br />
[Instructions](https://docs.amplify.aws/lib/datastore/getting-started/q/platform/android#option-2-use-amplify-cli) to install and run this application:
```
cd todo/app
amplify init
amplify add api
```
Edit the `schema.graphql`:
```
enum Priority {
  LOW
  NORMAL
  HIGH
}

type Todo @model {
  id: ID!
  name: String!
  priority: Priority!
  completedAt: AWSDateTime
}
```
Create the backend:
`amplify push`
