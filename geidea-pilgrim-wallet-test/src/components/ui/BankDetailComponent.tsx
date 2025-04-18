import React, { useContext, useEffect, useState } from "react"
import { StyleSheet, Text, TouchableOpacity, View } from "react-native"
import Container from "./Container"
import theme from "./theme"
import Typography from "./Typography"






type BankDetailsComponentProps = {
    savingAccountNumber?: String,
    bankHolderName: String,
    type: String
    selectedBank?:(value:boolean)=>void,
    isBankSelect:boolean
    isDisabled?:boolean
  }

const BankDetailsComponent: React.FC<BankDetailsComponentProps> = ({ 
        savingAccountNumber,
        bankHolderName,
        type,
        selectedBank,
        isDisabled = false ,
        isBankSelect = false}: BankDetailsComponentProps) => {

    const [isSelected, setIsSelected] = useState(false);

    const onRadioBtnClick = () => {
        selectedBank(!isSelected)
      };

    return (
        <Container alignItems="center" justify="center" >
            <TouchableOpacity disabled={isDisabled} onPress={() => {onRadioBtnClick()}}>
                <View style={[{
                    backgroundColor: theme.colors.white,
                    borderColor: theme.colors.gray.light,
                    borderRadius: 6,
                    borderWidth: 0.5,
                    padding: 10,
                    marginHorizontal: 20,
                    marginTop: 20,
                }]}>
                    <View style={styles.radioButtonContainer}>
                    <View style={[styles.radioButton ,{ borderColor:  isBankSelect ? theme.colors.orange  : isDisabled ?  theme.colors.gray.light: theme.colors.gray.medium }]}>
                    { isBankSelect ? <View style={styles.radioButtonIcon}/>  : undefined }
                        </View>
                        <Typography style={{ paddingVertical: 5 }} variant="heading" color={isDisabled ? "grayLight":"midDarkToneGray"} fontFamily="Rubik-Medium"  textAlign="left">
                           {type}
                        </Typography>
                    </View>
                    <View style={{ marginLeft: 30 }}>
                        { savingAccountNumber && <Typography style={{ paddingVertical: 5 }} variant="heading" color={isDisabled ? "grayLight":"darkBlackBold"} fontFamily="Rubik-Regular" textAlign="left">
                            Savings Ac.No. - {savingAccountNumber}
                        </Typography>}
                        { !isDisabled ? <Typography style={{ paddingVertical: 5 }} variant="heading" fontFamily="Rubik-Regular" color={isDisabled ? "grayLight":"darkBlackBold"} textAlign="left">
                            Name : {bankHolderName}
                        </Typography> : undefined}
                    </View>
                </View>
            </TouchableOpacity>
        </Container>

    )
    }

    export default BankDetailsComponent

    const styles = StyleSheet.create({
        radioButtonContainer: {
            flexDirection: "row",
            alignItems: "center",
            marginRight: 45
          },
          radioButton: {
            marginRight:10,
            height: 18,
            width: 18,
            backgroundColor: theme.colors.white,
            borderRadius: 10,
            borderWidth: 1,
            alignItems: "center",
            justifyContent: "center"
          },
          radioButtonIcon: {
            height: 12,
            width: 12,
            borderRadius: 7,
            backgroundColor: theme.colors.orange
          },
          radioButtonText: {
            fontSize: 16,
            marginLeft: 16
          }
    })