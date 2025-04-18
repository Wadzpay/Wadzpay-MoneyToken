import React, { PropsWithChildren, useState } from "react";

type SaveContextType = {
  isSave: boolean;
  isSaveAndClose: boolean;
  setIsSave: (value: boolean) => void;
  setIsSaveAndClose: (value: boolean) => void;
};

export const SaveContext = React.createContext<SaveContextType>({
  isSave: false,
  isSaveAndClose: false,
  setIsSave: () => {},
  setIsSaveAndClose: () => {},
});

type Props = PropsWithChildren<{}>;

export const SaveContextProvider: React.FC<Props> = ({ children }: Props) => {
  const [isSave, setIsSave] = useState(false);
  const [isSaveAndClose, setIsSaveAndClose] = useState(false);
  return (
    <SaveContext.Provider
      value={{ isSave, isSaveAndClose, setIsSave, setIsSaveAndClose }}
    >
      {children}
    </SaveContext.Provider>
  );
};
