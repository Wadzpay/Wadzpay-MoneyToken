package com.vacuumlabs.wadzpay.services

import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.configuration.AwsConfiguration
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File

@Service
class S3Service(val awsConfiguration: AwsConfiguration, val appConfig: AppConfig) {
    fun writeTransactions(filename: String, transactions: ByteArray) {
        val putRequest = PutObjectRequest.builder()
            .bucket(appConfig.s3.appBucket)
            .key("transaction-reports-csv/$filename")
            .build()

        awsConfiguration.s3Client().putObject(putRequest, RequestBody.fromBytes(transactions))
    }

    fun uploadImage(filename: String?, imageByteArray: ByteArray?) {
        val fileName = "TestCase Failed.png"
        val filePath = "C:\\Users\\Admin\\OneDrive - Wadzpay Technology India Private Limited\\Pictures\\CBD Snipped/$fileName"
        println("filePath ==> $filePath")
       /* val client: S3Client = S3Client.builder().build()

        val request = PutObjectRequest.builder()
            .bucket(bucketName).key(fileName).build()

        client.putObject(request, RequestBody.fromFile(File(filePath))) */
        try {

            val putRequest = PutObjectRequest.builder()
                .bucket(appConfig.s3.appBucket)
                .key("institution logos/$fileName")
                .build()

            val s3Client = awsConfiguration.s3Client().putObject(putRequest, RequestBody.fromFile(File(filePath)))
            println("s3Client ==> $s3Client")
        } catch (e: Exception) {
            println("e: ==> $e")
        }
    }
}
