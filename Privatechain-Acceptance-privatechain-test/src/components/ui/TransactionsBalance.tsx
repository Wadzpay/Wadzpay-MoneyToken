import { t } from "i18next";
import React, { useState, useContext, useEffect } from "react";
import { MerchantContext } from "src/context/Merchant";
import { UserContext } from "src/context/User";

const TransactionsBalances = (props: any): JSX.Element => {
  
  const [cryptos, setCryptos] = useState([
    /* {
      cryptoFullName: "Bitcoin",
      asset: "BTC"
    },
    {
      cryptoFullName: "Ethereum",
      asset: "ETH"
    },
    {
      cryptoFullName: "Tether",
      asset: "USDT"
    },
    {
      cryptoFullName: "USD Coin",
      asset: "USDC"
    },  ,
    {
      cryptoFullName: "WTK",
      asset: "WTK"
    } */
    {
      cryptoFullName: "SART",
      asset: "SART",
    },
  ]);
  const [boxesWidth, setBoxesWidth] = useState("100%");

  const { user } = useContext(UserContext);
  const { institutionDetails } = useContext(MerchantContext);

  const checkFiatTypeAmount = (fiatTypeCheck: string) => {
    const amount = () => {
      let total = 0;
      props.trxdata.map((obj: any) => {
        if (obj.asset == fiatTypeCheck) {
          total = total + obj.amount;
        }
      });
      return total.toFixed(2);
    };
    const feeamount = () => {
      let total = 0;
      props.trxdata.map((obj: any) => {
        if (obj.asset == fiatTypeCheck) {
          total = total + obj.feeAmount;
        }
      });
      return total.toFixed(8);
    };
    const totalAmount = () => {
      let total = 0;
      props.trxdata.map((obj: any) => {
        if (obj.asset == fiatTypeCheck) {
          if (obj.status == "IN_PROGRESS" || obj.status == "FAILED") {
            return;
          } else if (obj.status == null) {
            return;
          } else {
            total = total + obj.totalAmount;
          }
        }
      });
      return total.toFixed(2);
    };
    return (
      <>
        {/* <h5>{amount()}</h5> */}
        {/* <p>Fee Amount : {feeamount()}</p>*/}
        <p>{totalAmount()}</p>
      </>
    );
  };

  const calculateAmount = (fiatType: string) => {
    switch (fiatType) {
      case "Bitcoin":
        return checkFiatTypeAmount("BTC");
        break;
      case "Ethereum":
        return checkFiatTypeAmount("ETH");
        break;
      case "Tether":
        return checkFiatTypeAmount("USDT");
        break;
      case "WTK":
        return checkFiatTypeAmount("WTK");
        break;
      case "USD Coin":
        return checkFiatTypeAmount("USDC");
      case "SART":
        return checkFiatTypeAmount("SART");
        break;
      default:
        return null;
        break;
    }
  };

  useEffect(() => {
    if (user && user.attributes.email == "ddf.pilot@wadzpay.com") {
      setBoxesWidth("50%");
    } else {
      setBoxesWidth("100%");
    }
  }, [user]);

  const renderItems = cryptos.map(function (Card: any) {
    if (user && user.attributes.email == "ddf.pilot@wadzpay.com") {
      if (Card.asset !== "BTC" && Card.asset !== "WTK") {
        return (
          <div
            data-testid="balances"
            className="wdz-card col-sm border p-2 m-2 text-center"
            key={Card.asset}
          >
            <div className="row trxbalances">
              <div className="col col-lg-auto" style={{ marginTop: "11px" }}>
                <div>{institutionDetails?.merchantCurrency ? institutionDetails?.merchantCurrency+" *" : ""}</div>
              </div>
              <div className="col col-lg fs-5 customFlex">
                {/* <div>{Card.cryptoFullName}</div> */}
                <div className="trxbalancesData">
                  {props.trxdata && calculateAmount(Card.cryptoFullName)}
                </div>
                <span className="fs-6" style={{ marginLeft: "5px" }}>
                  {institutionDetails?.merchantCurrency ? institutionDetails?.merchantCurrency+" *" : ""}
                </span>
              </div>
            </div>
          </div>
        );
      }
    } else {
      return (
        <div
          data-testid="balances"
          className="wdz-card col-sm border p-2 m-2 text-center"
          key={Card.asset}
        >
          <div className="row trxbalances">
            <div className="col col-lg-auto">
              <div>{institutionDetails?.merchantCurrency ? institutionDetails?.merchantCurrency+" *" : ""}</div>
            </div>
            <div className="col col-lg fs-5 customFlex">
              {/* <div>{Card.cryptoFullName}</div> */}
              <span className="fs-6" style={{ marginRight: "5px" }}>
                {(props.trxdata && Card.asset) == "SART" ? `${institutionDetails?.merchantCurrency ? institutionDetails?.merchantCurrency+" *" : ""}` : Card.asset}
              </span>
              <div
                className="trxbalancesData"
                style={{ marginBottom: "20px", marginTop: "20px" }}
              >
                {props.trxdata && calculateAmount(Card.cryptoFullName)}
              </div>
            </div>
          </div>
        </div>
      );
    }
  });

  return (
    <>
      {/* <div data-testid="account-balance">
        {t("Total Transactions Balance")}{" "}
        {props.trxdata &&
          props.trxdata.length > 0 &&
          props.trxdata
            .reduce((sum: any, obj: any) => {
              return sum + obj.amount
            }, 0)
            .toFixed(9)}
      </div> */}
      <div className="container-fluid p-0 mt-2" style={{ display: "contents" }}>
        <h4>{t("Total Amount")}</h4>
        <div
          className="row"
          style={{ marginBottom: "20px", marginTop: "20px", width: "30%" }}
        >
          {renderItems}
        </div>
      </div>
    </>
  );
};
export default TransactionsBalances;
