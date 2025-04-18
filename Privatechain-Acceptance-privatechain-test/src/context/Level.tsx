import { PropsWithChildren ,createContext, useState} from "react";

type LevelType  = {
    levelNumber: number;  
    setLevelNumber: (value: number) => void;
    error:string
  };
  
export const LevelContext = createContext<LevelType>({
    levelNumber:0,
    setLevelNumber:()=>{},
    error:""
  });
  type Props = PropsWithChildren<{}>;
  export const LevelContextProvider: React.FC<Props> = ({ children }: Props) => {
    const [levelNumber, setLevelNumber] = useState<number>(0);
    const [error, setError] = useState("")
    return (
        <LevelContext.Provider value={{ levelNumber, setLevelNumber,error}}>
          {children}
        </LevelContext.Provider>
      );
    
  }