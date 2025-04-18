export const LOCAL_ENV_PUBLIC_API_URL = "http://localhost:8080/"

export enum ENV {
  DEV = "dev",
  TESTING = "testing",
  UAT = "uat",
  PROD = "prod",
  GEIDEADEV = "geideadev",
  GEIDEATEST = "geideatest",
  GEIDEAPROD = "geideaprod",
  GEIDEAUAT = "geideauat"
}

type CongnitoConfig = {
  aws_project_region: string
  aws_cognito_region: string
  aws_user_pools_id: string
  aws_user_pools_web_client_id: string
  oauth: { [key in string]: string }
}

type ENV_VALUES = {
  TYPE: ENV
  PUBLIC_API_URL: string
  COGNITO_CONFIG: CongnitoConfig
  DEFAULT_COUNTRY: string
  DEFAULT_USER_EMAIL?: string
  DEFAULT_USER_PASSWORD?: string
  DEFAULT_USER_PHONE_NUMBER?: string
}

const congnitoConfig: { [key in ENV]: CongnitoConfig } = {
  dev: {
    aws_project_region: "eu-central-1",
    aws_cognito_region: "eu-central-1",
    aws_user_pools_id: "eu-central-************************************",
    aws_user_pools_web_client_id: "************************************",
    oauth: {}
  },
 
  testing: {
    aws_project_region: "ap-southeast-1",
    aws_cognito_region: "ap-southeast-1",
    aws_user_pools_id: "ap-southeast-************************************",
    aws_user_pools_web_client_id: "************************************",
    oauth: {}
  },
  uat: {
    aws_project_region: "ap-southeast-1",
    aws_cognito_region: "ap-southeast-1",
    aws_user_pools_id: "ap-southeast-************************************",
    aws_user_pools_web_client_id: "************************************",
    oauth: {}
  },
  prod: {
    aws_project_region: "ap-southeast-1",
    aws_cognito_region: "ap-southeast-1",
    aws_user_pools_id: "ap-southeast-************************************",
    aws_user_pools_web_client_id: "************************************",
    oauth: {}
  },
  geideadev: {
    aws_project_region: "me-south-1",
    aws_cognito_region: "me-south-1",
    aws_user_pools_id: "me-south-************************************",
    aws_user_pools_web_client_id: "************************************",
    oauth: {}
  },
  geideatest: {
    aws_project_region: "me-south-1",
    aws_cognito_region: "me-south-1",
    aws_user_pools_id: "me-south-************************************",
    aws_user_pools_web_client_id: "************************************",
    oauth: {}
  },
  geideaprod: {
    aws_project_region: "me-south-1",
    aws_cognito_region: "me-south-1",
    aws_user_pools_id: "me-south-************************************",
    aws_user_pools_web_client_id: "************************************",
    oauth: {}
  },
  geideauat: {
  aws_project_region: "me-south-1",
  aws_cognito_region: "me-south-1",
  aws_user_pools_id: "me-south-************************************",
  aws_user_pools_web_client_id: "************************************",
  oauth: {}
  }
}

const envsMap: { [key in ENV]: ENV_VALUES } = {
  dev: {
    TYPE: ENV.DEV,
    PUBLIC_API_URL: "https://api.dev.************************************.com/",
    COGNITO_CONFIG: congnitoConfig.dev,
    DEFAULT_COUNTRY: "AE"
  },
 
  testing: {
    TYPE: ENV.TESTING,
    PUBLIC_API_URL: "https://api.test.************************************.com/",
    COGNITO_CONFIG: congnitoConfig.testing,
    DEFAULT_COUNTRY: "AE"
  },
  uat: {
    TYPE: ENV.UAT,
    PUBLIC_API_URL: "https://api.uat.************************************.com/",
    COGNITO_CONFIG: congnitoConfig.uat,
    DEFAULT_COUNTRY: "AE"
  },
  prod: {
    TYPE: ENV.PROD,
    PUBLIC_API_URL: "https://api.************************************.com/",
    COGNITO_CONFIG: congnitoConfig.prod,
    DEFAULT_COUNTRY: "AE"
  },
  geideadev: {
    TYPE: ENV.GEIDEADEV,
    PUBLIC_API_URL: "https://api.geidea-dev.************************************.com/",
    COGNITO_CONFIG: congnitoConfig.geideadev,
    DEFAULT_COUNTRY: "AE"
  },
  geideatest: {
    TYPE: ENV.GEIDEATEST,
     PUBLIC_API_URL: "https://api.privatechain-test.************************************.com/",
    //  PUBLIC_API_URL: "http://192.168.3.88:8080/",
     COGNITO_CONFIG: congnitoConfig.geideatest,
    DEFAULT_COUNTRY: "AE"
  },
  geideaprod: {
    TYPE: ENV.GEIDEAPROD,
    PUBLIC_API_URL: "https://api.geidea.************************************.com/",
    COGNITO_CONFIG: congnitoConfig.geideaprod,
    DEFAULT_COUNTRY: "AE"
  },
  geideauat: {
    TYPE: ENV.GEIDEAUAT,
    PUBLIC_API_URL: "https://api.geidea-uat.************************************.com/",
    COGNITO_CONFIG: congnitoConfig.geideauat,
    DEFAULT_COUNTRY: "AE"
  }
}

const getEnvVars = (env: string = ENV.DEV) => {
  if (!env) return envsMap[ENV.DEV]
  if (env.indexOf(ENV.DEV) !== -1) return envsMap[ENV.DEV]
  if (env.indexOf(ENV.TESTING) !== -1) return envsMap[ENV.TESTING]
  if (env.indexOf(ENV.UAT) !== -1) return envsMap[ENV.UAT]
  if (env.indexOf(ENV.PROD) !== -1) return envsMap[ENV.PROD]
  if (env.indexOf(ENV.GEIDEADEV) !== -1) return envsMap[ENV.GEIDEADEV]
  if (env.indexOf(ENV.GEIDEATEST) !== -1) return envsMap[ENV.GEIDEATEST]
  if (env.indexOf(ENV.GEIDEAPROD) !== -1) return envsMap[ENV.GEIDEAPROD]
  if (env.indexOf(ENV.GEIDEAUAT) !== -1) return envsMap[ENV.GEIDEAUAT]
  return envsMap[ENV.GEIDEATEST]
}

const env = envsMap[ENV.GEIDEATEST]


export default env
