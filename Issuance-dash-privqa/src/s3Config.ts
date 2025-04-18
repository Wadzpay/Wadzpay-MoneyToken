import S3 from "react-aws-s3-typescript"

import { isDev, isTest, isProd } from "./utils"

let bucketName = ""
const dirName = "multilingual"
const region = "me-south-1"
let accessKeyId = ""
let secretAccessKey = ""

if (isDev) {
  bucketName = "***************************"
  accessKeyId = "*************************"
  secretAccessKey = "*******************************"
}

if (isTest) {
  bucketName = "***************************"
  accessKeyId = "*************************"
  secretAccessKey = "*******************************"
}

if (isProd) {
  bucketName = "***************************"
  accessKeyId = "*************************"
  secretAccessKey = "*******************************"
}

const s3Config = {
  bucketName,
  dirName,
  region,
  accessKeyId,
  secretAccessKey
}

const s3 = new S3(s3Config)

export default s3
