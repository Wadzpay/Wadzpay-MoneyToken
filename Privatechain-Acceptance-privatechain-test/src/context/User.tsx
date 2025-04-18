/* eslint-disable no-console */
/* eslint-disable @typescript-eslint/no-empty-function */
import React, { PropsWithChildren, useEffect, useState } from "react";
import { Hub, HubCapsule } from "@aws-amplify/core";
import { getCurrentUserAsync, User } from "../auth/AuthManager";
import { FiatAsset } from "../constants/types";
import { saveToken, verifyToken } from "src/api/user";
import CryptoJS from 'crypto-js';

type UserContextType = {
  user: User;
  isLoading: boolean;
  fiatAsset: FiatAsset;
  setFiatAsset: (value: FiatAsset) => void;
  setUser: (value: User|null) => void;
  error:string,
  verified:boolean

};

export const UserContext = React.createContext<UserContextType>({
  user: null,
  isLoading: true,
  fiatAsset: "AED",
  setFiatAsset: () => {},
  setUser:()=>{},
  error:"",
  verified:false
});

type Props = PropsWithChildren<{}>;

export const UserContextProvider: React.FC<Props> = ({ children }: Props) => {
  const [user, setUser] = useState<User>();
  const [error, setError] = useState("")
  const [verified, setVerified] = useState(false)
  const [isLoading, setIsLoading] = useState(true);
  const [fiatAsset, setFiatAsset] = useState<FiatAsset>("AED");

  const listener = async (data: HubCapsule) => {
    switch (data.payload.event) {
      case "signIn":{
        let userLogged = await getCurrentUserAsync();
        let isSessionTokenVerified=false 
        const sessionStorageToken=sessionStorage.getItem('currentToken')
        let userVerfied:any       
        if(sessionStorageToken!=null){
        console.log("sessionStorageToken",sessionStorageToken)
       var decrypted = CryptoJS.AES.decrypt(sessionStorageToken!!, "EntitledPowerUserCanReadThisWithKey@Random001020122");
       let respone=await verifyToken(decrypted.toString())
       try{
       userVerfied=await respone.json()
       }
       catch(e){
        console.log("something went wrong json processing",e)
       }
       console.log(userVerfied)
       isSessionTokenVerified=userVerfied?.verified}
       if((sessionStorageToken==null||isSessionTokenVerified)&&((sessionStorage.getItem('currentUser')==null)||(sessionStorage.getItem('currentUser')?.toLowerCase()==userVerfied?.username.toLowerCase()))){
        console.log("user ==", userLogged);
        setVerified(true)
        sessionStorage.setItem("verified","true")
        let clientId=userLogged?.pool?.clientId
        let accessTokenKey=`CognitoIdentityServiceProvider.${clientId}.${userLogged?.username}.accessToken`
        setUser(userLogged);
        sessionStorage.setItem("currentUser", user?.username!!);
        console.log(accessTokenKey)
        console.log("userLogged?.storage[accessTokenKey]",userLogged?.storage[accessTokenKey])
        var encrypted = CryptoJS.AES.encrypt(userLogged?.storage[accessTokenKey], "EntitledPowerUserCanReadThisWithKey@Random001020122");
        sessionStorage.setItem("currentToken", encrypted.toString());
        saveToken(userLogged?.storage[accessTokenKey]);
      }
        else{
         setError("Error user not matched with the current session")
          throw Error("Error An user already  exists in session")
        }}
        break;
      case "signUp":
        setUser(await getCurrentUserAsync());
        break;
      case "signOut":
        setUser(null);
        sessionStorage.removeItem("currentUser");
        sessionStorage.removeItem("currentToken");
        sessionStorage.removeItem("verified")
        break;
      case "signIn_failure":
        break;
      case "tokenRefresh":
        break;
      case "tokenRefresh_failure":
        setUser(null);
        break;
      case "configured":
        console.log("The Auth module is configured");
    }
  };

  useEffect(() => {
    const defaultCurrency = localStorage.getItem("default-currency");
    if (defaultCurrency) {
      setFiatAsset(JSON.parse(defaultCurrency || "AED"));
    } else {
      setFiatAsset("AED");
    }
  }, [fiatAsset]);

  useEffect(() => {
    const bootstrapUser = async () => {
      setUser(await getCurrentUserAsync());
      setIsLoading(false);
    };

    Hub.listen("auth", listener);
    bootstrapUser();
    return () => {
      Hub.remove("auth", listener);
    };
  }, []);

  return (
    <UserContext.Provider value={{ user, isLoading, fiatAsset, error,verified,setFiatAsset ,setUser}}>
      {children}
    </UserContext.Provider>
  );
};
