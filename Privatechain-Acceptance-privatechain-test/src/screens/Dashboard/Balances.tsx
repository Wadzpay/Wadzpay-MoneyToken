import { t } from "i18next";
import React, { useContext, useState, useEffect } from "react";
import { useGetExchangeRate } from "src/api/onRamp";
import { useUserBalances } from "src/api/user";
import {
  Asset,
  CryptoFullName,
  FiatAsset,
  TokenToAmount,
} from "src/constants/types";
import { MerchantContext } from "src/context/Merchant";
import { UserContext } from "src/context/User";
import useFormatCurrencyAmount from "src/helpers/formatCurrencyAmount";

interface AllCrypto {
  WTK: number;
  BTC: number;
  ETH: number;
  USDT: number;
  SAR: number;
}

function Balances(): JSX.Element {
  const { fiatAsset, user } = useContext(UserContext);
  const [boxesWidth, setBoxesWidth] = useState("100%");
  const { data: exchangeRatesData, isFetching: isFetchingExchangeRates } =
    useGetExchangeRate(fiatAsset);
  const { merchantDetails, institutionDetails } = useContext(MerchantContext);
  console.log("mer", merchantDetails, institutionDetails);
  const {
    data: balanceData,
    isFetching: isFetchingBalance,
    error: errorBalance,
  } = useUserBalances();

  const formatter = useFormatCurrencyAmount({
    locale:
      navigator.languages && navigator.languages.length
        ? navigator.languages[0]
        : navigator.language,
  });

  const currencyFormatter = (amount: number, asset: FiatAsset) =>
    formatter(amount, {
      fiatAsset: asset,
      currency: `${asset}`,
      currencyDisplay: "symbol",
      style: "currency",
    });

  const getAccountFiatBalance: (
    rates: TokenToAmount,
    balances: TokenToAmount
  ) => number = (rates, balances) => {
    let allCrypto;
    if (user && user.attributes.email == "ddf.pilot@wadzpay.com") {
      const obj: Partial<AllCrypto> = { ...balances };
      delete obj["BTC"];
      delete obj["WTK"];
      allCrypto = obj;
    } else {
      allCrypto = { ...balances };
    }
    const totalBalance = Object.entries(allCrypto).reduce(
      (total, [cryptoToken, amount]) =>
        total + amount / (rates[cryptoToken as Asset] || 0),
      0
    );
    const total = Number(totalBalance).toFixed(2);
    const num = Number(total);
    return !isNaN(num) ? num : 0;
  };

  const getCryptoBalance: (
    asset: Asset,
    balanceData?: TokenToAmount
  ) => number = (asset, balanceData) =>
    Number(balanceData && balanceData[asset] ? balanceData[asset] : 0);

  const getFiatBalance: (
    asset: Asset,
    rates?: TokenToAmount,
    balanceData?: TokenToAmount
  ) => number = (asset, rates, balanceData) =>
    Number(
      rates
        ? (
            (balanceData && balanceData[asset] ? balanceData[asset] : 0) /
            rates[asset]
          ).toFixed(2)
        : 0
    );

  type Card = {
    cryptoFullName: CryptoFullName;
    asset: Asset;
    cryptoBalance: number;
    fiatBalance: number;
  };
  const useGetData: (
    exchangeRatesData?: TokenToAmount,
    balanceData?: TokenToAmount
  ) => Card[] = (exchangeRatesData, balanceData) => {
    if (user && user.attributes.email == "ddf.pilot@wadzpay.com") {
      return [
        {
          cryptoFullName: "Ethereum",
          asset: "ETH",
          cryptoBalance: getCryptoBalance("ETH", balanceData),
          fiatBalance: getFiatBalance("ETH", exchangeRatesData, balanceData),
        },
        {
          cryptoFullName: "Tether",
          asset: "USDT",
          cryptoBalance: getCryptoBalance("USDT", balanceData),
          fiatBalance: getFiatBalance("USDT", exchangeRatesData, balanceData),
        },
      ];
    } else {
      return [
        {
          cryptoFullName: "SART",
          asset: "SART",
          cryptoBalance: getCryptoBalance("SART", balanceData),
          fiatBalance: getFiatBalance("SART", exchangeRatesData, balanceData),
        },
        /*
        {
          cryptoFullName: "Ethereum",
          asset: "ETH",
          cryptoBalance: getCryptoBalance("ETH", balanceData),
          fiatBalance: getFiatBalance("ETH", exchangeRatesData, balanceData)
        },
        {
          cryptoFullName: "Tether",
          asset: "USDT",
          cryptoBalance: getCryptoBalance("USDT", balanceData),
          fiatBalance: getFiatBalance("USDT", exchangeRatesData, balanceData)
        },
        {
          cryptoFullName: "USD Coin",
          asset: "USDC",
          cryptoBalance: getCryptoBalance("USDC", balanceData),
          fiatBalance: getFiatBalance("USDC", exchangeRatesData, balanceData)
        }  ,
        {
          cryptoFullName: "WTK",
          asset: "WTK",
          cryptoBalance: getCryptoBalance("WTK", balanceData),
          fiatBalance: getFiatBalance("WTK", exchangeRatesData, balanceData)
        } */
      ];
    }
  };

  useEffect(() => {
    if (user && user.attributes.email == "ddf.pilot@wadzpay.com") {
      setBoxesWidth("50%");
    } else {
      setBoxesWidth("100%");
    }
  }, [user]);

  const data = useGetData(exchangeRatesData, balanceData);
  const isLoading = isFetchingBalance && isFetchingExchangeRates;

  const renderItems = data.map((Card) => (
    <div
      data-testid="balances"
      className="wdz-card col-sm border p-2 m-2 text-center"
      key={Card.asset}
    >
      <div className="row">
        <div className="col col-lg-auto" style={{ marginTop: "11px" }}>
          <div className="mt-2 lh-1">
            {institutionDetails?.merchantCurrency
              ? institutionDetails?.merchantCurrency + " *"
              : ""}
          </div>
        </div>
        <div className="col col-lg fs-6 mt-2">
          {balanceData && !isLoading && !isNaN(Card.cryptoBalance) && (
            <>
              <div>
              {institutionDetails?.merchantCurrency
              ? institutionDetails?.merchantCurrency + " *"
              : ""}
                {`${formatter(Card.cryptoBalance, {
                  asset: Card.asset,
                })}`}
              </div>
              <div>
                {balanceData &&
                  exchangeRatesData &&
                  balanceData.SART.toFixed(2) + "" + "﷼."}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  ));

  return (
    <>
      {errorBalance && (
        <div className="alert alert-danger" role="alert">
          {errorBalance.message}
        </div>
      )}
      <div data-testid="account-balance">
        {t("Account Balance")}
        {!isFetchingBalance && !isFetchingExchangeRates && (
          <span className="fw-bolder ps-1">
            {balanceData &&
              exchangeRatesData &&
              balanceData.SART.toFixed(2) + "" + "﷼."}
          </span>
        )}
      </div>
      <div className="container-fluid p-0 mt-2">
        {/* <div className="row">{renderItems}</div> */}
        <div className="row" style={{ width: "30%" }}>
          {renderItems}
        </div>
      </div>
    </>
  );
}
export default Balances;
