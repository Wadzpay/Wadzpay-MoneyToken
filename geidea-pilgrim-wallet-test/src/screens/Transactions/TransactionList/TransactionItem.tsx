import React, { useContext } from 'react';
import {useNavigation, useRoute} from '@react-navigation/native';
import {StyleSheet, TouchableOpacity, View} from 'react-native';

import {
  Container,
  Icon,
  spacing,
  theme,
  Typography,
  UserAvatar,
  Chip,
} from '~/components/ui';
import {Transaction} from '~/api/models';
import {
  formatAMPM,
  getDirection,
  getOtherPartyName,
  getTransactionType,
  sartTxt,
  showAssetAmount,
  showFromFeildValue,
  shownames,
} from '~/utils';
import { UserContext } from '~/context';

const styles = StyleSheet.create({
  container: {
    height: '100%',
    paddingHorizontal: theme.spacing.md,
  },
  transactionRow: {
    paddingVertical: 15,
    backgroundColor: theme.colors.white,
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.5,
    elevation: 2,
    paddingRight: 10,
  },
  avatar: {
    padding: spacing(0.5),
  marginRight: theme.spacing.xs,
   marginLeft: 5,
  },
  separator: {
    height: 1,
    backgroundColor: '#ECECEC',
    shadowColor: '#171717',
    shadowOffset: {width: 1, height: 140},
    shadowOpacity: 0.2,
    elevation: 20,
  },
  shadow: {
    marginVertical: 8,
    shadowColor: theme.shadow.tabBar.shadowColor,
    shadowOffset: {
      width: 0,
      height: 2,
    },
    height: 1,
    shadowOpacity: 0.25,
    shadowRadius: 3.5,
    elevation: 5,
  },
});

type Props = {
  item: Transaction;
  showDate?: boolean;
};

const TransactionItem: React.FC<Props> = ({item, showDate = false}: Props) => {
  const navigation = useNavigation();
  const route = useRoute();
  const {user,instDetails} = useContext(UserContext);
   
  const date = item ? item.transactionType === "POS" ? new Date(item.paymentReceivedDate) : new Date(item.createdAt) : new Date();


  const handleNavigation = () => {
    route.name === 'Transactions'
      ? navigation.navigate('TransactionDetail', {transactionId: item.id})
      : navigation.navigate('Transactions', {
          screen: 'TransactionDetail',
          initial: false,
          params: {transactionId: item.id},
        });
  };
 // console.log("Number(showAssetAmount(item)) ", showAssetAmount(item).toLocaleString("en-US", { maximumFractionDigits: 2, minimumFractionDigits: 2 }))
 // console.log("Number(showAssetAmount(item) ** ", typeof showAssetAmount(item) , parseFloat(showAssetAmount(item)))
  return (
    <TouchableOpacity
      onPress={handleNavigation}>
      <Container
        direction="row"
        justify="space-between"
        alignItems="flex-start"
        noItemsStretch
        style={styles.transactionRow}>
        <Container direction="row" alignItems="center" noItemsStretch>
          <Container style={styles.avatar}>
          <Icon
            name={getDirection(item, 'name')}
            size="lg"
            color={getDirection(item, 'color')}
          />
            {/* <UserAvatar name={shownames(item)} /> */}
          </Container>
          <Container spacing={0.5} alignItems={'center'} style={{width: 220}}>
            <Typography
              fontFamily="Rubik-Regular"
              textAlign="left"
              variant="label"
              numberOfLines={1}
              ellipsizeMode={'tail'}>
             {getTransactionType(item,item.transactionType)}
            </Typography>

            <Typography
              fontFamily={'Rubik-Regular'}
              variant="chip"
              textAlign="left"
              color="dateChipColor">
             {getOtherPartyName(item,  user?.attributes?.email || '')}
              {/* {new Date(item.createdAt).toLocaleTimeString()} */}
            </Typography>

            {/* For Date */}
            <Typography
              fontFamily={'Rubik-Regular'}
              variant="chip"
              textAlign="left"
              color="dateChipColor">
              {!showDate
                ? `${date.toLocaleTimeString('en-GB', {
                  day: 'numeric', month: 'short', year: 'numeric', hour:"2-digit",minute:'2-digit', hour12: true
                })}`
                : `${date.toLocaleTimeString('en-GB', {
                  day: 'numeric', month: 'short', year: 'numeric', hour:"2-digit",minute:'2-digit', hour12: true
                })}`}
              {/* {new Date(item.createdAt).toLocaleTimeString()} */}
            </Typography>
          </Container>
        </Container>

        <Container
          direction="row"
          alignItems="center"
          noItemsStretch
          spacing={1}
          style={{marginRight: 5}}>
          <Container noItemsStretch direction={"row"}>
            <Typography
              variant="label"
              color="darkBlackBold"
              fontFamily="Rubik-Regular"
              textAlign="left">
              {/* {sartTxt} */}
              {instDetails?.issuingCurrency ? instDetails?.issuingCurrency+ "* " : ""}
            </Typography>
            <Typography
              color="darkBlackBold"
              fontFamily="Rubik-Regular"
              variant="label">
              {showAssetAmount(item)}
            </Typography>

            {/*  NOT in updated designs
            {item.status !== 'SUCCESSFUL' &&
              item.status !== 'OVERPAID' &&
              item.status !== 'UNDERPAID' && (
                <Chip
                  text={tsProps[item.status].title}
                  color={tsProps[item.status].color}
                />
              )} */}
          </Container>
          {/* <Icon
            name={getDirection(item, 'name')}
            size="xxs"
            color={getDirection(item, 'color')}
          /> */}
        </Container>
      </Container>
      <View style={styles.separator} />
    </TouchableOpacity>
  );
};
export default TransactionItem;
