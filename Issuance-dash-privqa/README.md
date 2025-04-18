# System requirements

- Node 16
...
# Run the project locally

## Install node

https://nodejs.org/en/ and make sure you can use the `npm` package manager with command:

```bash
npm --version
```

## Install yarn package manager

```bash
npm install --global yarn
```

## Install packages

```bash
yarn install
```

## Create an environment

```bash
yarn copy-envs
```

This will create an `env.ts` file, where you can specify environment specific variables or values that help with development and testing like:

```
DEFAULT_USER_EMAIL
DEFAULT_USER_PASSWORD
DEFAULT_USER_PHONE_NUMBER
```

There are 3 environments:

1. `dev`
2. `testing`
3. `prod`

Each has a git branch and should be merged into in this order `dev` -> `testing` -> `prod`.

## Run development server

```bash
yarn start
```

## Run tests

```bash
yarn test
```

## Build (development)

```
yarn build
```

Builds the app for production to the `build` folder.\
It correctly bundles React in production mode and optimizes the build for the best performance.

## Build testing environment

This will use the test api https://api.test.wadzpay.com/

```
yarn build:test
```

## Build production environment

This will use the production api https://api.privatechain-dev.wadzpay.com/

```
yarn build:dev
```
