/* eslint-disable @typescript-eslint/explicit-module-boundary-types, @typescript-eslint/no-explicit-any */
import { useState } from "react";
import {
  signInAsync,
  requestResetPasswordCodeAsync,
  submitResetPasswordCodeAsync,
  getIdTokenAsync,
} from "../auth/AuthManager";
import {
  Transaction,
  TransactionsData,
  RefundTransactionsData,
  BalancesData,
  MerchantData,
  MerchantDetailsData,
  UserDetailsData,
  VerifyEmailOTPData,
  P2pTransaction,
  AcceptRejectRefundableTransaction,
  InitiateWebLinkRefund,
  SubmitRefundFormData,
  GetRefundFormToken,
  SubmitRefundWithAuth,
  RecentTransaction,
  WalletWithdraw,
  UserData,
  RefundFormFields,
  GetFiatCurrencyList,
  SaveInstitutionDetails,
  GetIndustryTypeList,
  GetAggregatorList,
  GetInstitutionList,
  AggregatorTree,
  CreateInstitution,
  CreateMerchantGroup,
  CreateMerchant,
  GetMerchantList,
  CreateSubMerchant,
  Outlet,
  Pos,
  OutletDetails,
  Aggregator,
  CreateSubMerchantDetails,
  CreateMerchantRequest,
  CreateMerchantGroupRequest,
  PosCreate,
  Level,
  RmModule,
  UserCreate,
  UserUpdate,
  DeActivateUser,
  GetDepartments,
  GetRoles,
  GetRolesByUsers,
} from "./models";
import { EndpointKey, useApi } from "./constants";
import { useFile, useGet, useSet } from "./index";
import env from "src/env";

const useApiCallStates = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  const reset = () => {
    setIsSuccess(false);
    setIsLoading(false);
    setError(null);
  };
  return {
    isLoading,
    setIsLoading,
    isSuccess,
    setIsSuccess,
    error,
    setError,
    reset,
  };
};

export const useGenericApiCall = (
  apiToCall: (...params: any) => Promise<any>
) => {
  const {
    isLoading,
    setIsLoading,
    isSuccess,
    setIsSuccess,
    error,
    setError,
    reset,
  } = useApiCallStates();

  return {
    mutate: async (...params: any) => {
      try {
        setIsLoading(true);
        const response = await apiToCall(...params);
        setIsSuccess(true);
        return response;
      } catch (e: any | null) {
        setError(e);
      } finally {
        setIsLoading(false);
      }
    },
    isLoading,
    isSuccess,
    error,
    reset,
  };
};

export const useSignIn = () => {
  return useGenericApiCall(signInAsync);
};

export const useUserDetailsAndEmailOTP = () =>
  useSet<UserDetailsData>(
    EndpointKey.USER_DETAILS_SEND_EMAIL_OTP,
    useApi().userDetailsAndEmailOTP(),
    "POST",
    {},
    EndpointKey.USER_DETAILS_SEND_EMAIL_OTP
  );

export const useMerchant = () =>
  useSet<MerchantData>(
    EndpointKey.MERCHANT,
    useApi().merchant(),
    "POST",
    {},
    EndpointKey.USER_DETAILS_SEND_EMAIL_OTP
  );

export const useVerifyEmailOTPAndCreateUser = () =>
  useSet<VerifyEmailOTPData>(
    EndpointKey.VERIFY_EMAIL_OTP_CREATE_USER,
    useApi().verifyEmailOTPAndCreateUser(),
    "POST",
    {},
    EndpointKey.VERIFY_EMAIL_OTP_CREATE_USER
  );

export const useRequestResetPasswordCode = () => {
  return useGenericApiCall(requestResetPasswordCodeAsync);
};

export const useSubmitResetPasswordCode = () => {
  return useGenericApiCall(submitResetPasswordCodeAsync);
};

export const useMerchantTransaction = (id: string) =>
  useGet<Transaction>(
    [EndpointKey.MERCHANT_TRANSACTION, id],
    useApi().merchantTransaction(id),
    { keepPreviousData: true }
  );

export const useMerchantTransactions = (query?: string) =>
  useGet<TransactionsData>(
    [EndpointKey.MERCHANT_TRANSACTIONS, query],
    useApi().merchantTransactions(query || ""),
    { keepPreviousData: true },
    "uuid"
  );
export const useMerchantTransactionsNoPage = (query?: string) =>
  useGet<TransactionsData>(
    [EndpointKey.MERCHANT_TRANSACTIONS, query],
    useApi().merchantTransactions(query || ""),
    { keepPreviousData: true },
    "uuid"
  );

export const useRefundTransactions = (query?: string) =>
  useGet<RefundTransactionsData>(
    [EndpointKey.REFUND_TRANSACTIONS, query],
    useApi().refundTransactions(query || ""),
    { keepPreviousData: true },
    "uuid"
  );
export const useRefundTransactionsNoPage = (query?: string) =>
  useGet<RefundTransactionsData>(
    [EndpointKey.REFUND_TRANSACTIONS, query],
    useApi().refundTransactions(query || ""),
    { keepPreviousData: true },
    "uuid"
  );

export const useRefundAcceptRefundTrx = () =>
  useSet<AcceptRejectRefundableTransaction>(
    EndpointKey.REFUND_ACCEPT_REJECT_TRANSACTIONS,
    useApi().refundAcceptRejectTransactions(),
    "POST",
    {},
    EndpointKey.REFUND_ACCEPT_REJECT_TRANSACTIONS
  );

export const useInitiateWebLinkRefund = () =>
  useSet<InitiateWebLinkRefund>(
    EndpointKey.REFUND_INITIATE_WEBLINK,
    useApi().refundInitiateWebLink(),
    "POST",
    {},
    EndpointKey.REFUND_INITIATE_WEBLINK
  );

export const useSubmitRefundformWithAuth = () =>
  useSet<SubmitRefundWithAuth>(
    EndpointKey.REFUND_SUBMIT_WITHAUTH,
    useApi().refundSubmitFormWithAuth(),
    "POST",
    {},
    EndpointKey.REFUND_SUBMIT_WITHAUTH
  );

export const useUserBalances = () =>
  useGet<BalancesData>(EndpointKey.USER_BALANCES, useApi().userBalances(), {});

export const useP2pTransaction = () =>
  useSet<P2pTransaction>(
    EndpointKey.P2P_TRANSACTIONS,
    useApi().p2pTransaction(),
    "POST",
    {},
    EndpointKey.P2P_TRANSACTIONS
  );

export const useMerchantDetails = () =>
  useGet<MerchantDetailsData>(
    EndpointKey.MERCHANT_DETAILS,
    useApi().merchantDetails(),
    {}
  );

export const useSubmitRefundFormData = () =>
  useSet<SubmitRefundFormData>(
    EndpointKey.SUBMIT_REFUND_FORM_DATA,
    useApi().submitRefundForm(),
    "POST",
    {},
    EndpointKey.SUBMIT_REFUND_FORM_DATA
  );

export const useRefundFormTokenDetails = () =>
  useSet<GetRefundFormToken>(
    EndpointKey.GET_REFUND_FORM,
    useApi().getRefundForm(),
    "POST",
    {},
    EndpointKey.GET_REFUND_FORM
  );

export const useRecentPayment = () =>
  useSet<RecentTransaction>(
    EndpointKey.RECENT_PAYMENT,
    useApi().recentPayment(),
    "POST",
    {},
    EndpointKey.RECENT_PAYMENT
  );

export const useWalletWithdraw = () =>
  useSet<WalletWithdraw>(
    EndpointKey.ATM_WITHDRAW,
    useApi().atmWithDraw(),
    "POST",
    {},
    EndpointKey.ATM_WITHDRAW
  );

export const useUserVerify = (query: string) =>
  useGet<UserData>(
    [EndpointKey.USER_VERIFY, query],
    useApi().userVerify(query),
    { enabled: query != "" }
  );

export const useUserVerifyReset = (query: string) =>
  useGet<UserData>(
    [EndpointKey.USER_VERIFY_RESET, query],
    useApi().userVerifyReset(query),
    { enabled: query != "" }
  );

export const useRefundFormFields = (query: string) =>
  useGet<RefundFormFields>(
    [EndpointKey.REFUND_FORM_FIELDS, query],
    useApi().refundFormFields(query),
    { enabled: query != "" }
  );

export const useSaveAggregatorDetails = () =>
  useSet<SaveInstitutionDetails>(
    EndpointKey.SAVE_AGGREGATOR_DETAILS,
    useApi().saveAggregatorDetails(),
    "POST",
    {}
  );
export const useSaveAggregatorDraft = () =>
  useSet<SaveInstitutionDetails>(
    EndpointKey.SAVE_AGGREGATOR_DRAFT,
    useApi().saveAggregatorDraft(),
    "POST",
    {}
  );
export const useGetAggregatorDetails = () =>
  useSet<GetAggregatorList[]>(
    EndpointKey.GET_AGGREGATOR_DETAILS,
    useApi().getAggregatorDetails(),
    "POST",
    {}
  );

export const useUpdateAggregatorDetails = () =>
  useSet<SaveInstitutionDetails>(
    EndpointKey.UPDATE_AGGREGATOR_DETAILS,
    useApi().updateAggregatorDetails(),
    "POST",
    {}
  );
export const useUpdateAggregatorDraft = () =>
  useSet<SaveInstitutionDetails>(
    EndpointKey.UPDATE_AGGREGATOR_DRAFT,
    useApi().updateAggregatorDraft(),
    "POST",
    {}
  );

export const useDeleteAggregator = () =>
  useSet<any>(
    EndpointKey.DELETE_AGGREGATOR,
    useApi().deleteAggregator(),
    "POST",
    {}
  );
export const useFileAggregator = () =>
  useFile<any>(
    EndpointKey.AGGREGATOR_UPLOAD,
    useApi().uploadAggregator(),
    "POST",
    {}
  );

export const SaveAggregatorFromFile = async (formData: any, uri: string) => {
  return fetch(env.PUBLIC_API_URL + uri, {
    method: "POST",
    headers: {
      Accept: "*",
      Host: env.PUBLIC_API_URL,
      "Access-Control-Allow-Origin": "*",
      authorization: `Bearer ${await getIdTokenAsync()}`,
    },
    body: formData,
  });
};

export const verifyToken = async (token: String) => {
  return fetch(env.PUBLIC_API_URL + "user/account/verifyToken", {
    method: "POST",
    headers: {
      Accept: "*",
      Host: env.PUBLIC_API_URL,
      "Access-Control-Allow-Origin": "*",
      authorization: `Bearer ${await getIdTokenAsync()}`,
    },
  });
};
export const saveToken = async (token: String) => {
  return fetch(env.PUBLIC_API_URL + "user/account/saveToken", {
    method: "POST",
    headers: {
      Accept: "*",
      Host: env.PUBLIC_API_URL,
      "Access-Control-Allow-Origin": "*",
      authorization: `Bearer ${await getIdTokenAsync()}`,
    },
  });
};

function parseJSON(response: any) {
  return new Promise((resolve) =>
    response.json().then((json: any) =>
      resolve({
        status: response.status,
        ok: response.ok,
        json,
      })
    )
  );
}

/**
 * Requests a URL, returning a promise
 *
 * @param  {string} url       The URL we want to request
 * @param  {object} [options] The options we want to pass to "fetch"
 *
 * @return {Promise}           The request promise
 */
export default function request(formData: any, url: string) {
  return new Promise(async (resolve, reject) => {
    fetch(env.PUBLIC_API_URL + url, {
      method: "POST",
      headers: {
        Accept: "*",
        Host: env.PUBLIC_API_URL,
        "Access-Control-Allow-Origin": "*",
        authorization: `Bearer ${await getIdTokenAsync()}`,
      },
      body: formData,
    })
      .then(parseJSON)
      .then((response: any) => {
        if (response.ok) {
          return resolve(response.blob);
        }
        // extract the error from the server's json
        return reject(response.json);
      })
      .catch((error) =>
        reject({
          networkError: error.message,
        })
      );
  });
}

export async function requestBlob(
  formData: any,
  url: string
): Promise<Response> {
  // Make an HTTP request to the API endpoint
  return fetch(env.PUBLIC_API_URL + url, {
    method: "POST",
    headers: {
      Accept: "*",
      // 'Content-Type': 'text/csv',
      Host: env.PUBLIC_API_URL,
      "Access-Control-Allow-Origin": "*",
      authorization: `Bearer ${await getIdTokenAsync()}`,
    },
    body: formData,
  });
}

export function downloadFile(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob);
  // Create an anchor element
  const a = document.createElement("a");
  a.href = url;
  a.download = `${fileName}.csv`; // Set the file name

  // Programmatically click the anchor element to trigger the download
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);

  // Revoke the URL to release the resources
  URL.revokeObjectURL(url);
}

export const useCreateInstitution = () =>
  useSet<CreateInstitution>(
    EndpointKey.CREATE_INSTITUTION,
    useApi().createInstitution(),
    "POST",
    {}
  );
export const useSaveInstitution = () =>
  useSet<CreateInstitution>(
    EndpointKey.SAVE_INSTITUTION,
    useApi().saveInstitution(),
    "POST",
    {}
  );

export const useUpdateInstitutionDetails = () =>
  useSet<CreateInstitution>(
    EndpointKey.UPDATE_INSTITUTION_DETAILS,
    useApi().updateInstitutionDetails(),
    "POST",
    {}
  );
export const useUpdateInstitutionDraft = () =>
  useSet<CreateInstitution>(
    EndpointKey.UPDATE_INSTITUTION_DRAFT,
    useApi().updateInstitutionDraft(),
    "POST",
    {}
  );

export const useDeleteInstitution = () =>
  useSet<any>(
    EndpointKey.DELETE_INSTITUTION,
    useApi().institutionDelete(),
    "POST",
    {}
  );

export const useCreateMerchantGroup = () =>
  useSet<CreateMerchantGroupRequest>(
    EndpointKey.CREATE_MERCHANT_GROUP,
    useApi().createMerchantGroup(),
    "POST",
    {}
  );
export const useSaveMerchantGroup = () =>
  useSet<CreateMerchantGroupRequest>(
    EndpointKey.SAVE_MERCHANT_GROUP,
    useApi().saveMerchantGroup(),
    "POST",
    {}
  );
export const useUpdateMerchantGroup = () =>
  useSet<CreateMerchantGroupRequest>(
    EndpointKey.UPDATE_MERCHANT_GROUP,
    useApi().updateMerchantGroup(),
    "POST",
    {}
  );
export const useUpdateMerchantGroupDraft = () =>
  useSet<CreateMerchantGroupRequest>(
    EndpointKey.UPDATE_MERCHANT_GROUP_DRAFT,
    useApi().updateMerchantGroupDraft(),
    "POST",
    {}
  );
export const useDeleteMerchantGroup = () =>
  useSet<any>(
    EndpointKey.DELETE_MERCHANT_GROUP,
    useApi().merchantGroupDelete(),
    "POST",
    {}
  );

export const useGetInstitutionList = () =>
  useSet<GetInstitutionList[]>(
    EndpointKey.GET_INTITUTION_LIST,
    useApi().getInstitutionList(),
    "POST",
    {}
  );

export const useAggregator = (aggregatorId: String) =>
  useGet<Aggregator>(
    [EndpointKey.GET_AGGREGATOR],
    useApi().getAggregatorById(aggregatorId),
    {
      keepPreviousData: true,
      enabled:
        aggregatorId !== null &&
        aggregatorId !== undefined &&
        aggregatorId !== "",
    }
  );
export const useMerchantGroup = (
  merchanttGroupId: String,
  mgroupEnabled: boolean
) =>
  useGet<CreateMerchantGroup>(
    [EndpointKey.GET_MERCHANT_GROUP],
    useApi().getMerchantGroupById(merchanttGroupId),
    { keepPreviousData: true, enabled: mgroupEnabled }
  );
export const useInstitution = (institutionId: String, instEnabled: boolean) =>
  useGet<CreateInstitution>(
    [EndpointKey.GET_INSTITUTION],
    useApi().getInstitutionById(institutionId),
    { keepPreviousData: true, enabled: instEnabled }
  );
export const useMerchantAcquirer = (
  merchantId: String,
  merchantEnabled: boolean
) =>
  useGet<CreateMerchant>(
    [EndpointKey.GET_MERCHANT],
    useApi().getMerchantById(merchantId),
    { keepPreviousData: true, enabled: merchantEnabled }
  );
export const useSubMerchantAcquirer = (
  merchantId: String,
  merchantEnabled: boolean
) =>
  useGet<CreateSubMerchant>(
    [EndpointKey.GET_SUB_MERCHANT],
    useApi().getSubMerchantById(merchantId),
    { keepPreviousData: true, enabled: merchantEnabled }
  );

export const useOutlet = (outletId: String, merchantEnabled: boolean) =>
  useGet<Outlet>([EndpointKey.GET_OUTLET], useApi().getOutletById(outletId), {
    keepPreviousData: true,
    enabled: merchantEnabled,
  });

export const useGetIndustryTypeList = () =>
  useGet<GetIndustryTypeList>(
    [EndpointKey.GET_INDUSTRY_TYPE_LIST],
    useApi().getIndustryTypeList(),
    { keepPreviousData: true }
  );

export const useGetFiatCurrencyList = () =>
  useGet<GetFiatCurrencyList>(
    [EndpointKey.GET_FIAT_CURRENCY_LIST],
    useApi().getFiatCurrencyList(),
    { keepPreviousData: true }
  );

export const useGetMerchantGroupList = () =>
  useSet<any>(
    EndpointKey.GET_MERCHANT_GROUP_LIST,
    useApi().getMerchantGroupList(),
    "POST",
    {}
  );

export const useGetAggregatorTree = () =>
  useGet<AggregatorTree[]>(
    [EndpointKey.GET_AGGREGATOR_TREE],
    useApi().getAggregatorTree(),
    { keepPreviousData: true }
  );

export const useCreateMerchantAcquirer = () =>
  useSet<CreateMerchantRequest>(
    EndpointKey.CREATE_MERCHANT_ACQUIRER,
    useApi().createMerchantAcquirer(),
    "POST",
    {}
  );

export const useSaveMerchantAcquirer = () =>
  useSet<CreateMerchantRequest>(
    EndpointKey.SAVE_MERCHANT_ACQUIRER,
    useApi().saveMerchantAcquirer(),
    "POST",
    {}
  );
export const useCreateSubMerchantAcquirer = () =>
  useSet<CreateSubMerchantDetails>(
    EndpointKey.CREATE_SUB_MERCHANT_ACQUIRER,
    useApi().createSubMerchantAcquirer(),
    "POST",
    {}
  );
export const useSaveSubMerchantAcquirer = () =>
  useSet<CreateSubMerchantDetails>(
    EndpointKey.SAVE_SUB_MERCHANT_ACQUIRER,
    useApi().saveSubMerchantAcquirer(),
    "POST",
    {}
  );
export const useUpdateSubMerchantAcquirer = () =>
  useSet<CreateSubMerchantDetails>(
    EndpointKey.UPDATE_SUB_MERCHANT_ACQUIRER,
    useApi().updateSubMerchantAcquirer(),
    "POST",
    {}
  );
export const useUpdateSubMerchantAcquirerDraft = () =>
  useSet<CreateSubMerchantDetails>(
    EndpointKey.UPDATE_SUB_MERCHANT_ACQUIRER_DRAFT,
    useApi().updateSubMerchantAcquirerDraft(),
    "POST",
    {}
  );

export const useGetSubMerchantGroupList = () =>
  useSet<any>(
    EndpointKey.GET_SUB_MERCHANT_ACQUIRER_LIST,
    useApi().getSubMerchantAcquirerList(),
    "POST",
    {}
  );

export const useUpdateMerchantAcquirer = () =>
  useSet<CreateMerchantRequest>(
    EndpointKey.UPDATE_MERCHANT_ACQUIRER,
    useApi().updateMerchantAcquirer(),
    "POST",
    {}
  );
export const useUpdateMerchantAcquirerDraft = () =>
  useSet<CreateMerchantRequest>(
    EndpointKey.UPDATE_MERCHANT_ACQUIRER,
    useApi().updateMerchantAcquirerDraft(),
    "POST",
    {}
  );
export const useGetMerchantAcquirerList = () =>
  useSet<GetMerchantList>(
    EndpointKey.GET_MERCHANT_ACQUIRER_LIST,
    useApi().getMerchantAcquirerList(),
    "POST",
    {}
  );

export const useDeleteMerchant = () =>
  useSet<any>(
    EndpointKey.DELETE_MERCHANT,
    useApi().merchantDelete(),
    "POST",
    {}
  );

export const useDeleteSubMerchant = () =>
  useSet<any>(
    EndpointKey.DELETE_SUB_MERCHANT,
    useApi().subMerchantDelete(),
    "POST",
    {}
  );
export const useDeleteOutlet = () =>
  useSet<any>(EndpointKey.DELETE_OUTLET, useApi().outletDelete(), "POST", {});
export const useDeletePos = () =>
  useSet<any>(EndpointKey.DELETE_POS, useApi().deletePos(), "POST", {});
export const useCreateOutlet = () =>
  useSet<OutletDetails>(
    EndpointKey.CREATE_OUTLET,
    useApi().createOutlet(),
    "POST",
    {}
  );
export const useSaveOutlet = () =>
  useSet<OutletDetails>(
    EndpointKey.SAVE_OUTLET,
    useApi().saveOutlet(),
    "POST",
    {}
  );
export const useUpdateOutletDraft = () =>
  useSet<OutletDetails>(
    EndpointKey.UPDATE_OUTLET_DRAFT,
    useApi().updateOutletDraft(),
    "POST",
    {}
  );
export const useUpdateOutlet = () =>
  useSet<OutletDetails>(
    EndpointKey.UPDATE_OUTLET,
    useApi().updateOutlet(),
    "POST",
    {}
  );
export const useUpdatePos = () =>
  useSet<any>(EndpointKey.UPDATE_POS, useApi().updatePos(), "POST", {});
type Pagination = {
  current_page: number;
  total_records: number;
  total_pages: number;
};

export const useGetOutletList = () =>
  useSet<any>(EndpointKey.GET_OUTLET_LIST, useApi().outletList(), "POST", {});
export const useGetPosListing = () =>
  useSet<{
    totalCount: number;
    posList: PosCreate[];
    pagination: Pagination;
  }>(EndpointKey.POS_LIST_DETAILS, useApi().posListDetails(), "POST", {});
export const useCreateLevel = () =>
  useSet<Level>(EndpointKey.CREATE_LEVEL, useApi().createLevel(), "POST", {});
export const useUpdateLevel = () =>
  useSet<Level>(EndpointKey.UPDATE_LEVEL, useApi().updateLevel(), "POST", {});
export const useGetPosList = () =>
  useSet<any>([EndpointKey.POS_LIST], useApi().posList(), "POST");
export const useGetLevelList = () =>
  useSet<Level[]>([EndpointKey.LEVEL_LIST], useApi().levelList(), "POST");

export const useGetModuleList = () =>
  useSet<RmModule[]>([EndpointKey.MODULE_LIST], useApi().moduleList(), "POST");
export const useGetModuleListTree = () =>
  useSet<RmModule[]>(
    [EndpointKey.MODULE_LIST_TREE],
    useApi().moduleListTree(),
    "POST"
  );
export const useCreateModule = () =>
  useSet<RmModule>(
    EndpointKey.CREATE_MODULE,
    useApi().createModule(),
    "POST",
    {}
  );

export const useUpdateModule = () =>
  useSet<RmModule>(
    EndpointKey.UPDATE_MODULE,
    useApi().updateModule(),
    "POST",
    {}
  );

export const useEditPos = () =>
  useSet<PosCreate>(EndpointKey.UPDATE_POS, useApi().updatePos(), "POST", {});

export const useCreatePos = () =>
  useSet<PosCreate>(EndpointKey.CREATE_POS, useApi().createPos(), "POST", {});

export const useCreateUser = () =>
  useSet<UserCreate>(
    EndpointKey.CREATE_USER,
    useApi().createUser(),
    "POST",
    {}
  );

export const useGetUserManagementList = () =>
  useSet(
    [EndpointKey.USER_MANAGEMENT_LIST],
    useApi().userManagementList(),
    "POST"
  );

export const useUpdateUser = () =>
  useSet<UserUpdate>(
    EndpointKey.UPDATE_USER,
    useApi().updateUser(),
    "POST",
    {}
  );

export const useDeActivateUser = () =>
  useSet<DeActivateUser>(
    EndpointKey.DE_ACTIVATE_USER_BY_USERID,
    useApi().deActivateUser(),
    "POST",
    {}
  );

export const useGetDepartments = () =>
  useGet<GetDepartments>(
    EndpointKey.GET_DEPARTMENTS,
    useApi().getDepartments(),
    {}
  );

export const useGetRoles = (query?: string, enabled?: boolean) =>
  useGet<GetRoles>([EndpointKey.GET_ROLES, query], useApi().getRoles(query), {
    keepPreviousData: true,
    enabled,
  });

export const useGetRolesByUsers = (query?: string, enabled?: boolean) =>
  useGet<GetRolesByUsers>(
    [EndpointKey.GET_ROLES_BY_USERS, query],
    useApi().getRolesByUsers(query),
    {
      keepPreviousData: true,
      enabled,
    }
  );
