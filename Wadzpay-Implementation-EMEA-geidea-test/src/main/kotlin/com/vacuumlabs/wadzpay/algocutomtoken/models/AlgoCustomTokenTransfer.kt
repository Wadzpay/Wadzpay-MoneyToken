package com.vacuumlabs.wadzpay.algocutomtoken.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigInteger

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoCustomTokenTransferResponse(
    val transactionId: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoCustomTokenTransferRequest(
    val Amount: String,
    val ReceiverAddress: String,
    val SendrMnemonic: String
)

// val PrivateKey: String,
@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoCustomTokenToAccountRequest(
    val AccountAddress: String,
    val Amount: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoCustomTokenToAccountResponse(
    val transactionId: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoRefundUnspendTokenRequest(
    val AccountMnemonic: String,
    val Amount: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoRefundUnspendTokenResponse(
    val transactionId: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoCustomTokenAccountResponse(
    val Address: String,
    val passphrase: String

)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoTokenBalanceReq(
    val AddressMnemonic: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoTokenBalanceResponse(
    val AlgoBalance: String,
    val AssetBalance: String

)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountInfoFromMnuRequest(
    val Mnemonic: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AccountInfoFromMnuResponse(
    val Address: String,
    val AlgoBalance: String,
    val AssetBalance: String

)
@JsonIgnoreProperties(ignoreUnknown = true)
data class BcTransactionInfo(
    @JsonProperty("receiver-rewards")
    val receiverRewards: BigInteger,
    @JsonProperty("round-time")
    val roundTime: BigInteger,
    @JsonProperty("note")
    val note: String = "",
    @JsonProperty("signature")
    val signature: Signature,
    @JsonProperty("fee")
    val fee: BigInteger,
    @JsonProperty("confirmed-round")
    val confirmedRound: BigInteger,
    @JsonProperty("tx-type")
    val txType: String = "",
    @JsonProperty("sender-rewards")
    val senderRewards: BigInteger,
    @JsonProperty("closing-amount")
    val closingAmount: BigInteger,
    @JsonProperty("asset-transfer-transaction")
    val assetTransferTransaction: AssetTransferTransaction,
    @JsonProperty("genesis-hash")
    val genesisHash: String = "",
    @JsonProperty("intra-round-offset")
    val intraRoundOffset: BigInteger,
    @JsonProperty("sender")
    val sender: String = "",
    @JsonProperty("first-valid")
    val firstValid: BigInteger,
    @JsonProperty("id")
    val id: String = "",
    @JsonProperty("close-rewards")
    val closeRewards: BigInteger,
    @JsonProperty("genesis-id")
    val genesisId: String = "",
    @JsonProperty("last-valid")
    val lastValid: BigInteger
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BcTransInfo(
    @JsonProperty("current-round")
    val currentRound: BigInteger,
    @JsonProperty("transaction")
    val transaction: Transaction
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Signature(
    @JsonProperty("sig")
    val sig: String = ""
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AssetTransferTransaction(
    @JsonProperty("close-amount")
    val closeAmount: BigInteger,
    @JsonProperty("amount")
    val amount: BigInteger,
    @JsonProperty("receiver")
    val receiver: String = "",
    @JsonProperty("asset-id")
    val assetId: BigInteger
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Transaction(
    @JsonProperty("receiver-rewards")
    val receiverRewards: BigInteger,
    @JsonProperty("round-time")
    val roundTime: BigInteger,
    @JsonProperty("note")
    val note: String = "",
    @JsonProperty("signature")
    val signature: Signature,
    @JsonProperty("fee")
    val fee: BigInteger,
    @JsonProperty("confirmed-round")
    val confirmedRound: BigInteger,
    @JsonProperty("tx-type")
    val txType: String = "",
    @JsonProperty("sender-rewards")
    val senderRewards: BigInteger,
    @JsonProperty("closing-amount")
    val closingAmount: BigInteger,
    @JsonProperty("asset-transfer-transaction")
    val assetTransferTransaction: AssetTransferTransaction,
    @JsonProperty("genesis-hash")
    val genesisHash: String = "",
    @JsonProperty("intra-round-offset")
    val intraRoundOffset: BigInteger,
    @JsonProperty("sender")
    val sender: String = "",
    @JsonProperty("first-valid")
    val firstValid: BigInteger,
    @JsonProperty("id")
    val id: String = "",
    @JsonProperty("close-rewards")
    val closeRewards: BigInteger,
    @JsonProperty("genesis-id")
    val genesisId: String = "",
    @JsonProperty("last-valid")
    val lastValid: BigInteger
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AssetsInfo(
    @JsonProperty("assets")
    var assets: List<AssetsInfoParam> = listOf()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SingleAssetInfo(
    @JsonProperty("asset")
    var asset: AssetsInfoParam
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AssetsInfoParam(
    @JsonProperty("index")
    var index: Int,
    var params: AssetsInfoParamDetail
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AssetsInfoParamDetail(
    @JsonProperty("name")
    var name: String,
    @JsonProperty("unit-name")
    var unitName: String,
    @JsonProperty("decimals")
    var decimals: Int,
    @JsonProperty("creator")
    var creator: String
)
data class AssetsInfoOnAlgoNt(
    var index: Int,
    var name: String,
    var unitName: String,
    var decimals: Int
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoTokenAllBalanceResponse(
    val AlgoBalance: String,
    val Balances: List<AlgoTokenAllBalanceItemResponse> = listOf()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoTokenAllBalanceItemResponse(
    val Assetname: String,
    val Assetbalance: String
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoCustomTokenOptInRequest(
    val Address: String,
    val AssetName: String
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoCustomTokenCreateRequest(
    val AssetName: String,
    val Creator: String,
    val Decimals: Int,
    val TotalIssuance: BigInteger,
    val UnitName: String
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoCustomTokenCreateResponse(
    @JsonProperty("AssetId")
    val AssetId: Int
)
@JsonIgnoreProperties(ignoreUnknown = true)
data class AlgoCustomTokenTransferRequestById(
    val Amount: String,
    val AssetId: String,
    val ReceiverAddress: String,
    val SendrAddress: String
)
