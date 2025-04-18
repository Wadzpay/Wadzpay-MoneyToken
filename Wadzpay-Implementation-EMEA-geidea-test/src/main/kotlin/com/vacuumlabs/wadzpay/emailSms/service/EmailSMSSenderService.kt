package com.vacuumlabs.wadzpay.emailSms.service

import com.vacuumlabs.wadzpay.configuration.AppConfig
import com.vacuumlabs.wadzpay.configuration.AwsConfiguration
import com.vacuumlabs.wadzpay.ledger.CurrencyUnit
import com.vacuumlabs.wadzpay.ledger.model.Transaction
import com.vacuumlabs.wadzpay.ledger.model.TransactionRefundDetails
import com.vacuumlabs.wadzpay.ledger.service.GetAcceptApproveRejectRequest
import com.vacuumlabs.wadzpay.merchant.model.FiatCurrencyUnit
import com.vacuumlabs.wadzpay.services.TwilioService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.mail.Message
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Service
class EmailSMSSenderService(val awsConfiguration: AwsConfiguration, @Lazy val twilioService: TwilioService, val appConfig: AppConfig) {

    fun sendEmail(subject: String, text: String, targetEmail: String, fromEmail: String) {

        val fromEmailId = "do-not-reply@wadzpay.com"
        val formName = "Merchant"
        var isWadzPayDomain = false

       /* if (fromEmail.endsWith("@wadzpay.com")) {
            fromEmailId = fromEmail
            isWadzPayDomain = true
        }*/

        val sessionConfig = awsConfiguration.getEmailSession()

        val msg = MimeMessage(sessionConfig.session)
        msg.setFrom(InternetAddress(fromEmailId, formName))
        msg.setRecipient(Message.RecipientType.TO, InternetAddress(targetEmail))
        /*  TODO: When mail server is upgraded

         if (!isWadzPayDomain) {
               msg.addRecipients(Message.RecipientType.CC, fromEmail)
           }
           */

        msg.subject = subject
        msg.setContent(text, "text/html")

        val transport: Transport = sessionConfig.session.transport

        try {
            transport.connect(
                sessionConfig.HOST,
                sessionConfig.authDetails.username,
                sessionConfig.authDetails.password
            )
            transport.sendMessage(msg, msg.allRecipients)
        } catch (ex: Exception) {
            println(ex.message)
            throw ex
        } finally {
            // Close and terminate the connection.
            transport.close()
        }
    }
    fun getRefundSuccessEmailBody(
        customerName: String,
        walletAddress: String,
        merchantMobileNo: String,
        merchantEmailId: String,
        request: GetAcceptApproveRejectRequest,
        transaction: Transaction,
        defaultTimeZone: String,
        refundTransaction: TransactionRefundDetails
    ): String {
        val strRefundDate = refundDate() + ""
//        val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
       /* val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("GMT" + defaultTimeZone)
        val strRefundDate = formatter.format(Date.from(Instant.now()))*/
        if (customerName == null || customerName == "") {
            customerName == "Customer"
        }
        return "<p>Dear \n" + customerName + "," +
            "<br>" +
            "<br>" +
            "<p>We are happy to inform that your refund request Transaction ID <b> ${request.txn_uuid} </b> on $strRefundDate for ${transaction.fiatAsset} ${refundTransaction.refundAmountFiat?.stripTrailingZeros()?.toPlainString()} has been successfully processed.\n" +
            "<br>" +
            "Your refund amount is transferred to your Wallet ID $walletAddress \n" +
            "<br>" +
            "For any queries, you may reach us on 800-4443 or email us at <a href = \"mailto: customer.service@merchant.ae\">customer.service@merchant.ae</a> by mentioning your Transaction ID." +
            "\n" +
            "           <br> <br>Thanks,\n" +
            "\n" +
            "            <br> <b> Merchant </b>\n" +
            "</p>"
    }

    fun getRefundSuccessEmailBodyQAR(
        customerName: String,
        walletAddress: String,
        merchantMobileNo: String,
        merchantEmailId: String,
        request: GetAcceptApproveRejectRequest,
        transaction: Transaction,
        defaultTimeZone: String,
        refundTransaction: TransactionRefundDetails
    ): String {
        val strRefundDate = refundDate() + ""
//        val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        /* val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
         formatter.timeZone = TimeZone.getTimeZone("GMT" + defaultTimeZone)
         val strRefundDate = formatter.format(Date.from(Instant.now()))*/
        if (customerName == null || customerName == "") {
            customerName == "Customer"
        }
        return "<p>Dear \n" + customerName + "," +
            "<br>" +
            "<br>" +
            "<p>We are happy to inform that your refund request Transaction ID <b> ${request.txn_uuid} </b> on $strRefundDate for QAR ${refundTransaction.refundAmountFiat?.stripTrailingZeros()?.toPlainString()} has been successfully processed.\n" +
            "<br>" +
            "Your refund amount is transferred to your Wallet ID $walletAddress \n" +
            "<br>" +
            "For any queries, you may reach us on 800-4443 or email us at <a href = \"mailto: customer.service@merchant.ae\">customer.service@merchant.ae</a> by mentioning your Transaction ID." +
            "\n" +
            "           <br> <br>Thanks,\n" +
            "\n" +
            "            <br> <b> Merchant </b>\n" +
            "</p>"
    }
    fun getMerchantEmailBody(
        merchantName: String,
        webUrl: String,
        refundTransaction: TransactionRefundDetails,
        transaction: Transaction
    ): String {
        val strRefundDate = refundDate() + ""
       /* val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("GMT" + (transaction.receiver.account.owner as Merchant).defaultTimeZone.toString())
        val strRefundDate = formatter.format(Date.from(Instant.now()))*/

        return "<html>\n" +
            "\n" +
            "<style type=\"text/css\">\n" +
            "\n" +
            "    p {\n" +
            "\n" +
            "        border: 1px solid black;\n" +
            "\n" +
            "        padding: 20px;\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "\n" +
            "    div {\n" +
            "\n" +
            "        display: flex;\n" +
            "\n" +
            "        justify-content: center;\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "</style>\n" +
            "\n" +
            "\n" +
            "\n" +
            "<body>\n" +
            "\n" +
            "    <div>\n" +
            "\n" +
            "        <p>Dear Customer,\n" +
            "\n" +
            "\n" +

            "\n" +
            "            <br />\n" +
            "\n" +
            "            <br />\n" +
            "\n" +
            "            Please click on the below link to complete the refund request for the transaction made @ Dubai Duty Free on ${posActualDate(transaction)} for an amount of ${refundTransaction.refundFiatType} ${refundTransaction.refundAmountFiat?.stripTrailingZeros()?.toPlainString()}." +
            "\n" +
            "\n" +

            "\n" +
            "            <br />\n" +
            "\n" +
            "            <br />\n" +
            "\n" +
            "            URL:-  <a href=\"" + webUrl + "\"> $webUrl </a> " +
            "\n" +
            "            <br />\n" +
            "\n" +
            "            <br>Thanks,\n" +
            "\n" +
            "            <br> <br> <b> Dubai Duty Free </b>\n" +
            "\n" +
            "        </p>\n" +
            "\n" +
            "    </div>\n" +
            "\n" +
            "</body>\n" +
            "\n" +
            "\n" +
            "\n" +
            "</html>"
    }

    fun getReceivePaymentEmailBody(customerName: String, webUrl: String): String {
        return "<html>\n" +
            "\n" +
            "<style type=\"text/css\">\n" +
            "\n" +
            "    p {\n" +
            "\n" +
            "        border: 1px solid black;\n" +
            "\n" +
            "        padding: 20px;\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "\n" +
            "    div {\n" +
            "\n" +
            "        display: flex;\n" +
            "\n" +
            "        justify-content: center;\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "</style>\n" +
            "\n" +
            "\n" +
            "\n" +
            "<body>\n" +
            "\n" +
            "    <div>\n" +
            "\n" +
            "        <p>Dear " + customerName + ",\n" +
            "\n" +
            "\n" +
            "\n" +
            "            <br />\n" +
            "\n" +
            "            <br />\n" +
            "\n" +
            "            <br />\n" +
            "            Please pay to Amber Lounge using the below link :\n" +
            "\n" +
            "            <br />\n" +
            "            <a href=\"" + webUrl + "\">Click Here</a><br>\n" +
            "\n" +
            "            <br />\n" +
            "\n" +
            "            <br>Thanks\n" +
            "\n" +
            "            <br> <b> " + "Amber Lounge" + " </b>\n" +
            "            <br />\n" +
            "<img src=\"https://icons-wadzpay.s3.eu-central-1.amazonaws.com/amber_lounge_text.png\" alt=\"Image Unavailable\" width=\"150\" height=\"50\">" +
            "        </p>\n" +
            "\n" +
            "    </div>\n" +
            "\n" +
            "</body>\n" +
            "\n" +
            "\n" +
            "\n" +
            "</html>"
    }
    fun getRefundAckSlipEmailBody(
        ackNumber: String,
        transactionNumber: String,
        amount: BigDecimal?,
        fiatCurrencyUnit: FiatCurrencyUnit?,
        refundAmountDigital: BigDecimal?,
        currencyUnit: CurrencyUnit?,
        instant: Instant,
        defaultTimeZone: String
    ): String {
        val strRefundDate = refundDate() + ""
        /*val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("GMT" + defaultTimeZone)
        val strDate = formatter.format(Date.from(instant))*/

        return "<html>\n" +
            "\n" +
            "<style type=\"text/css\">\n" +
            "\n" +
            "    p {\n" +
            "\n" +
            "        border: 1px solid black;\n" +
            "\n" +
            "        padding: 20px;\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "\n" +
            "    div {\n" +
            "\n" +
            "        display: flex;\n" +
            "\n" +
            "        justify-content: center;\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "</style>\n" +
            "\n" +
            "\n" +
            "\n" +
            "<body>\n" +
            "\n" +
            "    <div>\n" +
            "\n" +
            "        <p>Message:\n" +
            "\n" +
            "\n" +
            "\n" +
            "            <br />\n" +
            "\n" +
            "              Acknowledgement No:<b> $ackNumber </b>" +
            "            <br />\n" +
            "              Date: " + refundInitiateDate(Instant.now()) +
            "            <br />\n" +
            "\n" +
            "              We acknowledge the refund request initiated for transaction no. <b> $transactionNumber </b> for an amount of $fiatCurrencyUnit ${amount?.stripTrailingZeros()?.toPlainString()} " +
            "\n" +
            "            <br>\n" +
            "\n" +
            "\n" +
            "            <br />\n" +
            "\n" +
            "            <br>Once approved, the refunded value will be credited in your designated wallet.\n" +
            "\n" +
            "\n" +
            "<br/><br/>For any queries, you may reach us on 800-4443 or email us at <a href = \"mailto: customer.service@merchant.ae\">customer.service@merchant.ae</a> by mentioning your Acknowledgment No." +
            "<br/>" +
            "<br/>" +
            "<br/>Thanks," +
            "<br> <b> Merchant </b>" +
            "        </p>\n" +
            "\n" +
            "    </div>\n" +
            "\n" +
            "</body>\n" +
            "\n" +
            "\n" +
            "\n" +
            "</html>"
    }

    fun getRefundAcceptanceSlipEmailBody(
        ackNumber: String,
        transactionNumber: String,
        amount: BigDecimal?,
        fiatCurrencyUnit: FiatCurrencyUnit?,
        refundAmountDigital: BigDecimal?,
        currencyUnit: CurrencyUnit?,
        instant: Instant,
        defaultTimeZone: String,
        walletAddress: String
    ): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone(defaultTimeZone)
        val strDate = formatter.format(Date.from(instant))

        return "<p>Acknowledgment Number:-\n" + ackNumber +
            "<p>Date: \n" + strDate +
            "<br>" +
            "<br>" +
            "<br>" +
            "Acceptance accepted your refund request for transaction number:-\n" + transactionNumber +
            "for amount: \n" + amount + " " + fiatCurrencyUnit + "<br>" +
            "You will receive your refund to your mentioned wallet " + walletAddress + " in a few days. \n" +
            "In case of any queries please call on our call centre by\n" +
            "mentioning your Approved No." +
            "</p>"
    }

    fun getRefundRejectEmailBody(
        customerName: String,
        rejectReason: String?,
        ackNumber: String,
        transactionId: UUID,
        refundInitiateDate: Instant?,
        refundAmountFiat: BigDecimal?
    ): String {
        return "<p>Dear " + customerName + "," +
            "<br>" +
            "<br>" +
            "Acknowledgment No: " + ackNumber + "\n" +
            "<br>" +
            "Date: " + refundInitiateDate(refundInitiateDate!!) +
            "<br>" +
            "<br>" +
            "We regret to inform you that your Refund Request for \n" +
            "Transaction ID - " + transactionId + " for amount " +
            refundAmountFiat?.stripTrailingZeros()?.toPlainString() + " has been Rejected.\n" +
            "<br>" +
            "Reject Reason: " + rejectReason + "\n" +
            "<br>" +
            "<br>" +
            "For any queries, you may reach us on 800-4443 or email us at <a href = \"mailto: customer.service@merchant.ae\">customer.service@merchant.ae</a> by mentioning your Acknowledgment No.\n" +
            "<br>" +
            "</p>"
    }

    fun getRefundFailedEmailBody(
        customerName: String,
        rejectReason: String?,
        ackNumber: String,
        transactionId: UUID,
        refundInitiateDate: Instant?,
        refundAmountFiat: BigDecimal?
    ): String {
        if (customerName == null || customerName == "") {
            customerName == "Customer"
        }
        return "<p>Dear " + customerName + "," +
            "<br>" +
            "<br>" +
            "Acknowledgment No: " + ackNumber + "\n" +
            "<br>" +
            "Date: " + refundInitiateDate(refundInitiateDate!!) +
            "<br>" +
            "<br>" +
            "We regret to inform you that your Refund Request for \n" +
            "Transaction ID - " + transactionId + " for amount " +
            refundAmountFiat?.stripTrailingZeros()?.toPlainString() + " has got Failed from Block chain.\n" +
            "<br>" +
            "Reject Reason: Transaction failed from Blockchain. Kindly contact us.\n" +
            "<br>" +
            "<br>" +
            "For any queries, you may reach us on 800-4443 or email us at <a href = \"mailto: customer.service@merchant.ae\">customer.service@merchant.ae</a> by mentioning your Acknowledgment No.\n" +
            "<br>" +
            "</p>"
    }

    fun userOfflineAlertEmailBody(
        customerName: String,
        reason: String?,
        uuid: UUID?,
        attemptDate: Instant?
    ): String {
        if (customerName == null || customerName == "") {
            customerName == "Customer"
        }
        return "<p>Dear Customer" + "," +
            "<br>" +
            "<br>" +
            "We regret to inform you that your Passcode Invalid Attempt with below details \n" +
            "<br>" +
            "<br>" +
            "UUID - " + uuid + ",\n" +
            "<br>" +
            "Date: " + refundInitiateDate(attemptDate!!) +
            "<br>" +
            "<br>" +
            "</p>"
    }
    fun sendSms(mobileNumber: String, messageBody: String): Boolean {

        val result = awsConfiguration.snsClient().publish(
            PublishRequest.builder().message(messageBody).phoneNumber(mobileNumber).messageAttributes(
                mutableMapOf(
                    Pair(
                        "AWS.SNS.SMS.SMSType",
                        MessageAttributeValue.builder().stringValue("Transactional").dataType("String").build()
                    )
                )
            ).build()
        )
        return result.messageId() != null
    }

    fun sendMobileSMS(toString: String, message: String) {
        twilioService.sendMobileSMS(toString, message)
    }
    private fun refundDate(): String? {
        val date = Date()
        val dateFmt = SimpleDateFormat("dd-MM-yyyy")
        return dateFmt.format(date)
    }

    fun getInviteEmailBody(
        email: String
    ): String {
        return "<p>Dear Customer's\n" + email +
            "<br>" +
            "<br>" +
            "Your Invite URL -  https://merchant.dev.wadzpay.com/sign-in \n" +
            "<br>" +
            "</p>"
    }

    fun sendEmailWadzpay(subject: String, text: String, targetEmail: String, fromEmail: String) {

        val formName = "Wadzpay"
        var isWadzPayDomain = false

        /* if (fromEmail.endsWith("@wadzpay.com")) {
             fromEmailId = fromEmail
             isWadzPayDomain = true
         }*/

        val sessionConfig = awsConfiguration.getEmailSession()

        val msg = MimeMessage(sessionConfig.session)
        msg.setFrom(InternetAddress(fromEmail, formName))
        msg.setRecipient(Message.RecipientType.TO, InternetAddress(targetEmail))
        /*  TODO: When mail server is upgraded

         if (!isWadzPayDomain) {
               msg.addRecipients(Message.RecipientType.CC, fromEmail)
           }
           */

        msg.subject = subject
        msg.setContent(text, "text/html")

        val transport: Transport = sessionConfig.session.transport

        try {
            transport.connect(
                sessionConfig.HOST,
                sessionConfig.authDetails.username,
                sessionConfig.authDetails.password
            )
            transport.sendMessage(msg, msg.allRecipients)
        } catch (ex: Exception) {
            println(ex.message)
            throw ex
        } finally {
            // Close and terminate the connection.
            transport.close()
        }
    }

    //    start of issuance email
    fun getIssuanceEmailBody(email: String, webUrl: String): String {
        return "<html>\n" +
            "\n" +
            "<style type=\"text/css\">\n" +
            "\n" +
            "    p {\n" +
            "\n" +
            "        border: 1px solid black;\n" +
            "\n" +
            "        padding: 20px;\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "\n" +
            "    div {\n" +
            "\n" +
            "        display: flex;\n" +
            "\n" +
            "        justify-content: center;\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "</style>\n" +
            "\n" +
            "\n" +
            "\n" +
            "<body>\n" +
            "\n" +
            "    <div>\n" +
            "\n" +
            "        <p><b>Dear Customer,</b>\n" +
            "\n" +
            "\n" +

            "\n" +
            "            <br />\n" +
            "\n" +
            "            <br />\n" +
            "\n" +
            "            Your account has been created." +
            "\n\n" +
            "            Please click <a href=\"" + webUrl + "\"> here</a> to set up your password" +
            ". \n\n" +
            "\n" +
            "\n" +
            "\n" +
            "            <br />\n" +
            "            <br />\n" +
            "\n" +

            "            <br>Thanks,\n" +
            "\n" +
            "            <br> <br> <b></b>\n" +
            "\n" +
            "        </p>\n" +
            "\n" +
            "    </div>\n" +
            "\n" +
            "</body>\n" +
            "\n" +
            "\n" +
            "\n" +
            "</html>"
    }
//    end of issuance email

    fun posActualDate(transaction: Transaction): String? {
        val dateFmt = SimpleDateFormat("dd-MM-yyyy")
        return dateFmt.format(transaction.extPosActualDate)
    }

    fun refundInitiateDate(date: Instant): String? {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault())
        return formatter.format(date)
    }
}
