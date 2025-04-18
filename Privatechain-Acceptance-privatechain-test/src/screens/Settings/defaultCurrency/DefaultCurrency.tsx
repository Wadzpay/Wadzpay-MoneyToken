import React, { useEffect, useState, useContext } from "react";
import { Form } from "react-bootstrap";
import { Link } from "react-router-dom";
import { UserContext } from "src/context/User";
import { MerchantContext } from "src/context/Merchant";
import { RouteType } from "src/constants/routeTypes";
import { t } from "i18next";

const DefaultCurrency: React.FC = () => {
  const { setFiatAsset } = useContext(UserContext);
  const defaultCurrency = () => {
    if (localStorage.getItem("default-currency") !== null) {
      const currency = JSON.parse(
        localStorage.getItem("default-currency") || ""
      );
      return currency;
    }
    return "AED";
  };
  const [currenctCurrency, setCurrentCurrency] = useState(defaultCurrency);
  const { institutionDetails } = useContext(MerchantContext);

  const selectCurrency = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const currency: string = event.currentTarget.value;
    localStorage.setItem("default-currency", JSON.stringify(currency));
    setFiatAsset(JSON.parse(localStorage.getItem("default-currency") || ""));
    setCurrentCurrency(currency);
  };

  return (
    <>
      <div className="table-responsive" style={{ overflowX: "visible" }}>
        <div className="col-md-3 mt-4 ml-2">
          <Form.Group controlId="custom-select">
            <Form.Label>Default Currency</Form.Label>
            <Form.Select
              as="select"
              value={currenctCurrency}
              onChange={(evt) => selectCurrency(evt as any)}
            >
              {[
                institutionDetails?.merchantCurrency
                  ? institutionDetails?.merchantCurrency
                  : "",
              ].map((option) => (
                <option key={option}>{option}</option>
              ))}
            </Form.Select>
          </Form.Group>
        </div>
      </div>
    </>
  );
};

export default DefaultCurrency;
