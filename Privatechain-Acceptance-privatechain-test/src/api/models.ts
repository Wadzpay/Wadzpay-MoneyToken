import {
  Asset,
  FiatAsset,
  TransactionStatus,
  TransactionType,
  TransactionDirection,
} from "src/constants/types";
import { TokenToAmount } from "src/constants/types";

export type SignInData = {
  email: string;
  password: string;
};

export type SendPhoneOTPData = {
  phoneNumber: string;
};

export type UserDetailsData = {
  email: string;
  phoneNumber: string;
  password: string;
};

export type MerchantData = {
  name: string;
  countryOfRegistration: string;
  registrationCode: string;
  primaryContactFullName: string;
  primaryContactEmail: string;
  companyType: string;
  industryType: string;
  primaryContactPhoneNumber: string;
  merchantId: string;
  defaultRefundableFiatValue: number;
  tnc: string;
};

export type VerifyPhoneOTPData = {
  phoneNumber: string;
  code: string;
};

export type VerifyEmailOTPData = {
  email: string;
  phoneNumber: string;
  code: string;
  password: string;
  isMerchantAdmin: boolean;
};

export type Transaction = {
  id: string; // uuid
  reference: string;
  description: string;
  senderName: string;
  receiverName: string;
  asset: Asset;
  fiatAmount: number;
  fiatAsset: FiatAsset;
  status: TransactionStatus;
  transactionType: TransactionType;
  amount: number;
  totalAmount: number;
  feePercentage: number;
  feeAmount: number;
  createdAt: Date;
  direction: TransactionDirection;
  uuid: string;
  blockchainAddress: string;
  totalDigitalCurrencyReceived: number;
  blockchainTxId: string;
  totalFiatReceived: number;
  posId: string;
  extPosTransactionId: string;
  extPosId: string;
  extPosSequenceNo: string;
  orderId: string;
  paymentReceivedDate: string;
  transactionId: string;
  PosTransactionId: string;
  refundFiatAmount: number;
  refundType: string;
  refundStatus: string;
  refundUserName: string;
  refundUserMobile: string;
  refundUserEmail: string;
  refundReason: string;
  refundWalletAddress: string;
  refundMode: string;
  refundAcceptanceComment: string;
  refundApprovalComment: string;
  refundAmountDigital: number;
  requestedDigitalAmount: number;
  extPosActualDate: Date;
  extPosLogicalDate: Date;
  extPosShift: number;
  extPosActualTime: string;
  refundDateTime: Date;
  walletAddressMatch: boolean;
  balanceAmountFiat: number;
  numberOfRefunds: number;
  extPosLogicalDateRefund: Date;
  extPosActualDateRefund: Date;
  extPosActualTimeRefund: Date;
  extPosShiftRefund: Date;
  extPosSequenceNoRefund: String;
  extPosTransactionIdRefund: String;
  refundTransactionID: String;
  refundOrigin: String;
  sourceWalletAddress: String;
  refundableAmountFiat: number;
  txnMode: String;
  issuerName: String;
};

export type TransactionsData = Transaction[];

export type RefundTransactionsData = Transaction[];

export type UserData = {
  email: string;
  phoneNumber: string;
  cognitoUsername: string;
  merchant: MerchantData;
};

export type RefundFormFields = {
  txnReference: boolean;
  customerName: boolean;
  mobile: boolean;
  email: boolean;
  digitalAmt: boolean;
  digitalName: boolean;
  refundAmountInFiat: boolean;
  refundAmountInCrypto: boolean;
  reason: boolean;
  srcWalletAddr: boolean;
  walletAddr: boolean;
  confirmWalletAddr: boolean;
};

export type InviteUserData = {
  email: string;
  role: string;
};

export type BalancesData = TokenToAmount;

export type P2pTransaction = {
  amount: string;
  asset: Asset;
  receiverUsername?: string;
  receiverEmail?: string;
  receiverPhone?: string;
  description: string;
};

export type AcceptRejectRefundableTransaction = {
  txn_uuid: string;
  status: string;
  type: string;
  rejectReason: string;
};

export type InitiateWebLinkRefund = {
  refundUserMobile?: string | null;
  refundUserEmail?: string | null;
  refundDigitalType: string;
  refundFiatType: string;
  refundAmountFiat: number;
  balanceAmountFiat: number;
  transactionId: string;
  refundMode: string;
  refundCustomerFormUrl?: string;
  refundAmountDigital: number;
  isReInitiateRefund: boolean | false;
  refundTransactionID: String | null;
  sourceWalletAddress: String;
  refundWalletAddress: String;
};

export type SubmitRefundWithAuth = {
  transactionId: string;
  refundUserName: string;
  refundUserMobile: string | null;
  refundUserEmail: string | null;
  refundWalletAddress: string;
  sourceWalletAddress: string;
  refundDigitalType: string;
  refundFiatType: string;
  refundAmountFiat: number;
  refundAmountDigital: number;
  balanceAmountFiat: number;
  reasonForRefund: string;
  refundMode: string;
  isReInitiateRefund: boolean;
  refundTransactionID: string;
};

export type GenerateAPIKey = {
  username?: string;
  password?: string;
  basicKey?: string;
};

export type MerchantDetailsData = {
  merchant?: MerchantData;
  role?: string;
};

export type EnableDisableUserData = {
  email: string;
};

export type MerchantUserData = {
  userAccount: UserData;
  isActive: boolean;
};

export type SubmitRefundFormData = {
  transactionId: string;
  refundUserName: string;
  refundUserMobile: string;
  refundUserEmail: string;
  sourceWalletAddress: string;
  refundWalletAddress: string;
  refundDigitalType: string;
  refundFiatType: string;
  refundAmountFiat: number;
  refundAmountDigital: number;
  reasonForRefund: string;
  refundMode: string;
  refundToken: string;
};

export type GetRefundFormToken = {
  refundUUID: string;
};

export type RecentTransaction = {
  requesterUserName: string;
  requesterEmailAddress: string;
  requesterMobileNumber: string;
  fiatAsset: string;
  fiatAmount: number;
  digitalAsset: string;
  externalOrderId: string;
};

export type WalletWithdraw = {
  walletAddress: string;
  amount: number;
  asset: string;
};

export type GetFiatCurrencyList = {
  key: string;
};

export type AggregatorTree = {
  aggregatorPreferenceId: string;
  aggregatorName: string;
  aggregatorLogo: string;
  institutions: GetInstitutionList[];
  aggregatorStatus: string;
  outlets?: Outlet[];
};

export type GetAllAggregators = {
  aggregatorPreferenceId: string;
  aggregatorName: string;
  aggregatorStatus: string;
  aggregatorId: number;
};

export type GetAggregatorList = {
  aggregatorPreferenceId: string;
  aggregatorName: string;
  aggregatorStatus: string;
  aggregatorLogo: string;
  aggregatorId: number;
  institutions: GetInstitutionList[];
};

export type GetInstitutionList = {
  aggregatorPreferenceId: string;
  institutionPreferenceId: string;
  institutionLogo: string;
  aggregatorLogo: string;
  institutionId: string;
  institutionName: string;
  institutionStatus: string;
  systemGenerated: boolean;
  merchantGroup: GetMerchantGroupList[];
};

export type GetMerchantGroupList = {
  merchantGroupName: string;
  merchantGroupPreferenceId: string;
  merchantGroupStatus: string;
  merchantGroupImage: string;
  systemGenerated: boolean;
  merchants: GetMerchantList[];
};

export type GetMerchantList = {
  merchantName: string;
  merchantPreferenceId: string;
  merchantStatus: string;
  merchantImage: string;
  systemGenerated: boolean;
  subMerchants: GetSubMerchantList[];
};

export type GetSubMerchantList = {
  merchantName: string;
  merchantPreferenceId: string;
  merchantStatus: string;
  merchantImage: string;
  systemGenerated: boolean;
  outlets: OutletOnly[];
};

export type Address = {
  entityAddressAddressLine1: string;
  entityAddressAddressLine2: string;
  entityAddressAddressLine3: string;
  entityAddressCity: string;
  entityAddressState: string;
  entityAddressCountry: string;
  entityAddressPostalCode: string;
};

export type AdminDetails = {
  entityAdminDetailsFirstName: string;
  entityAdminDetailsMiddleName: string;
  entityAdminDetailsLastName: string;
  entityAdminDetailsEmailId: string;
  entityAdminDetailsMobileNumber: string;
  entityAdminDetailsDepartment: string;
};

export type BankDetails = {
  entityBankDetailsBankName: string;
  entityBankDetailsBankAccountNumber: string;
  entityBankDetailsBankHolderName: string;
  entityBankDetailsBranchCode: string;
  entityBankDetailsBranchLocation: string;
};

export type ContactDetails = {
  entityContactDetailsFirstName: string;
  entityContactDetailsMiddleName: string;
  entityContactDetailsLastName: string;
  entityContactDetailsEmailId: string;
  entityContactDetailsMobileNumber: string;
  entityContactDetailsDesignation: string;
  entityContactDetailsDepartment: string;
};

export type Info = {
  entityInfoAbbrevation: string;
  entityInfoDescription: string;
  entityInfoLogo: string;
  entityInfoRegion: string;
  entityInfoTimezone: string;
  entityInfoType: string;
  entityInfoDefaultDigitalCurrency: string;
  entityInfoBaseFiatCurrency: string;
};

export type Others = {
  entityOthersCustomerOfflineTxn: string;
  entityOthersMerchantOfflineTxn: string;
  entityOthersApprovalWorkFlow: string;
  entityOthersActivationDate: string;
  entityOthersExpiryDate?: string;
};

export type SaveInstitutionDetails = {
  aggregatorPreferenceId: string;
  clientAggregatorPreferenceId: string | undefined;
  aggregatorName: string;
  aggregatorStatus: string;
  aggregatorLogo: string;
  address: Address;
  adminDetails: AdminDetails;
  bankDetails: BankDetails;
  contactDetails: ContactDetails;
  info: Info;
  others: Others;
};

export type Aggregator = {
  aggregatorPreferenceId: string;
  aggregatorName: string;
  aggregatorStatus: string;
  aggregatorLogo: string;
  address: Address;
  adminDetails: AdminDetails;
  bankDetails: BankDetails;
  contactDetails: ContactDetails;
  info: Info;
  others: Others;
};

export type CreateInstitution = {
  aggregatorPreferenceId: string;
  institutionId: string;
  insitutionPreferenceId: string;
  insitutionName: string;
  isParentBlocked?: string;
  institutionLogo: string;
  insitutionStatus?: string;
  address: Address;
  adminDetails: AdminDetails;
  bankDetails: BankDetails;
  contactDetails: ContactDetails;
  info: Info;
  others: Others;
};

export type CreateMerchantGroup = {
  aggregatorPreferenceId: string;
  insitutionPreferenceId: string;
  merchantGroupPreferenceId: string;
  merchantGroupName: string;
  merchantGroupStatus: string;
  address: Address;
  adminDetails: AdminDetails;
  bankDetails: BankDetails;
  contactDetails: ContactDetails;
  info: Info;
  others: Others;
};
export type CreateMerchantGroupRequest = {
  merchantGroup: CreateMerchantGroup;
  isDirect?: boolean;
  parentType?: string;
};
export type CreateMerchant = {
  merchantAcquirerId: string;
  aggregatorPreferenceId: string | undefined;
  insitutionPreferenceId: string | undefined;
  merchantGroupPreferenceId: string | undefined;
  merchantAcquirerName: string;
  merchantAcquirerStatus: string;
  address: Address;
  adminDetails: AdminDetails;
  bankDetails: BankDetails;
  contactDetails: ContactDetails;
  info: Info;
  others: Others;
};
export type CreateMerchantRequest = {
  merchant: CreateMerchant;
  isDirect?: boolean;
  parentType?: string;
};
export type CreateSubMerchant = {
  aggregatorPreferenceId: string | undefined;
  insitutionPreferenceId: string | undefined;
  merchantGroupPreferenceId: string | undefined;
  merchantAcquirerPreferenceId: string | undefined;
  subMerchantAcquirerId: string;
  subMerchantAcquirerName: string;
  clientSubMerchantAcquirerId: String | undefined;
  subMerchantAcquirerStatus: string;
  subMerchantAcquirerLogo: string;
  address: Address;
  adminDetails: AdminDetails;
  bankDetails: BankDetails;
  contactDetails: ContactDetails;
  info: Info;
  others: Others;
};
export type CreateSubMerchantDetails = {
  subMerchant: CreateSubMerchant;
  isDirect?: boolean;
  parentType?: string;
};
export type OutletOnly = {
  outletId: string;
  outletName: string;
  outletStatus: string;
  outletLogo: string;
  posList: Pos[];
};
export type Pos = {
  posKey?: string;
  posId: string;
  posMacAddress?: string;
  posIPAddress?: string;
  posFirmwareVersion: string;
  posSerialNum?: string;
  posManufacturer: string;
  posModel: string;
  status: "active" | "inactive";
  outletPreferenceId?: string;
};
export type OutletDetails = {
  isDirect?: boolean;
  parentType?: string;
  aggregatorName?: string;
  insitutionName?: string;
  merchantGroupName?: string;
  merchantAcquirerName?: string;
  outlet: Outlet;
};

export type Outlet = {
  aggregatorPreferenceId: string | undefined;
  insitutionPreferenceId: string | undefined;
  merchantGroupPreferenceId: string | undefined;
  merchantAcquirerPreferenceId: string | undefined;
  subMerchantPreferenceId: string | undefined;
  clientOutletId: string | undefined;
  outletId: string;
  outletName: string;
  outletStatus: string;
  outletLogo: string;
  address: Address;
  adminDetails: AdminDetails;
  bankDetails: BankDetails;
  contactDetails: ContactDetails;
  info: Info;
  others: Others;
};
export type Level = {
  levelId?: string | undefined;
  levelName: string;
  levelNumber: number;
  imageUrl: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  status: boolean;
};

export type RmModule = {
  moduleId?: string | undefined;
  moduleName: string;
  moduleType: string;
  moduleUrl?: string;
  imageUrl: string;
  parent?: RmModule;
  parentName?: String;
  sorting?: number;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
  status: boolean;
};
export type RmModuleTree = {
  moduleId?: string | undefined;
  moduleName: string;
  moduleType: string;
  moduleUrl?: string;
  imageUrl: string;
  parent?: RmModule;
  parentName?: String;
  sorting?: number;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
  key: string;
  name: string;
  children: RmModuleTree[];
  status: boolean;
};

export type PosCreate = {
  aggregatorPreferenceId: string | undefined;
  insitutionPreferenceId: string | undefined;
  merchantGroupPreferenceId: string | undefined;
  merchantAcquirerPreferenceId: string | undefined;
  subMerchantPreferenceId: string | undefined;
  outletPreferenceId: string | undefined;
  posId: string;
  posModel: string;
  posManufacturer: string;
  posSerialNum: string;
  posMacAddress: string;
  posIPAddress: String;
  posFirmwareVersion: String;
  status: String;
};
export type RoleCreate = {
  role: {
    roleId?: number;
    roleName: string;
    levelId: number;
    aggregatorId: string;
    users?: number;
    roleComments: string;
  };
  module: number[];
};
export type RoleList = {
  role: {
    roleId: number;
    roleName: string;
    levelId: number;
    roleComments: string;
    updatedBy: string;
    status: boolean;
  };
  module: number[];
};

export type UserCreate = {
  currentLevel: number;
  userName: string;
  userPreferenceId: string;
  countryCode: string;
  mobileNo: string;
  emailId: string;
  designation: string;
  departmentId: number;
  roleId: number;
  comment: string;
};

export type UserUpdate = {
  userId: number;
  currentLevel: number;
  userName: string;
  userPreferenceId: string;
  countryCode: string;
  mobileNo: string;
  emailId: string;
  designation: string;
  departmentId: number;
  roleId: number;
  comment: string;
};

export type UserList = {
  user: {
    userName: string;
    userPreferenceId: string;
    userEmail: string;
    assignedRole: string;
    requestedBy: string;
    approveReject: string;
    updatedBy: string;
    status: boolean;
  };
  module: number[];
};

export type DeActivateUser = {};

export type CreateAggregatorDetails = {
  key: string;
};

export type GetIndustryTypeList = {
  key: string;
};

export type GetDepartments = [
  {
    departmentId: number;
    departmentName: string;
    status: boolean;
    createdAt: string;
    updatedAt: string;
  }
];

export type GetRoles = [
  {
    userName: null | string;
    roleId: number;
    roleName: string;
    roleModuleList: [
      {
        moduleName: string;
        moduleId: number;
      },
      {
        moduleName: string;
        moduleId: number;
      },
      {
        moduleName: string;
        moduleId: number;
      }
    ];
  }
];

export type GetRolesByUsers = [
  {
    userName: string;
    roleId: number;
    roleName: string;
    roleModuleList: [
      {
        moduleName: string;
        moduleId: number;
      }
    ];
  }
];
