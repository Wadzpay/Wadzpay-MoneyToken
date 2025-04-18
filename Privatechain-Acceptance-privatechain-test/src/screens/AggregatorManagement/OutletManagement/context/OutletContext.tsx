import React, { PropsWithChildren, useState } from "react";

type OutletContextType = {
  isSavePos: boolean;
  isListing:boolean;
  setIsListing:(value:boolean) => void
  setIsSavePos: (value: boolean) => void;
};

export const OutletContext = React.createContext<OutletContextType>({
  isSavePos: false,
  isListing:false,
  setIsSavePos: () => {},
  setIsListing:()=>{}
});

type Props = PropsWithChildren<{}>;

export const OutletContextProvider: React.FC<Props> = ({ children }: Props) => {
  const [isSavePos, setIsSavePos] = useState(false);
  const [isListing, setIsListing] = useState(false);
console.log("isSavePos",isSavePos)
  return (
    <OutletContext.Provider value={{ isSavePos, setIsSavePos ,isListing,setIsListing}}>
      {children}
    </OutletContext.Provider>
  );
};
