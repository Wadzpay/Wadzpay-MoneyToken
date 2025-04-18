import React, { useContext } from "react"
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs"
import { useTranslation } from "react-i18next"
import { TFunction } from "i18next"

import MenuStackNavigator from "./MenuStackNavigator"
import TransactionStackNavigator from "./TransactionStackNavigator"
import SendFundsStackNavigator from "./SendFundsStackNavigator"

import { Container, CustomTabBarButton, Icon, theme, Typography } from "~/components/ui"
import { Home, ReceiveFunds } from "~/screens"
import { UserContext } from "~/context"
import { BlockedCountries } from "~/constants/blockedNumber"
import SendFundSelector from "~/screens/SendFundSelector"
import RecieveFundsStackNavigator from "./RecieveFundsStackNavigator"
import InternalTransferNavigator from "./InternalTransferNavigator"
import HomeStackNavigator from "./HomeStackNavigator"
import { Text, View } from "react-native"
import { getFocusedRouteNameFromRoute } from "@react-navigation/native"

type TabRouteName =
  | "Home"
  | "Transactions"
  | "SendFunds"
  | "ReceiveFunds"
  | "Menu"

const getRouteLabel = (routeName: TabRouteName, t: TFunction) => {
  const routeLabelMap: { [key in TabRouteName]: string } = {
    Home: t("Dashboard"),
    Transactions: t("Transactions"),
    SendFunds: t("Scan to Pay"),
    ReceiveFunds: t("Share QR"),
    Menu: t("More")
  }
  return routeLabelMap[routeName]
}

const getTabBarIcon = (routeName: TabRouteName, focused: boolean) => {
  return (
    <Icon name={routeName} size="lg" color={focused ? "focused" : "regular"} />
  )
}

export type AppTabNavigatorParamList = {
  Home: undefined
  Transactions: undefined
  SendFunds: undefined
  ReceiveFunds: undefined
  Menu: undefined
 
}

const Tab = createBottomTabNavigator<AppTabNavigatorParamList>()

const AppTabNavigator: React.FC = () => {
  const { t } = useTranslation()
  const { user } = useContext(UserContext)

  const getTabBarVisibility = (route) => {
    
    const routeName = getFocusedRouteNameFromRoute(route) ?? 'Home';
    if (routeName === 'TopUpScreen'||
      routeName === 'RefundScreen' ||
      routeName === 'VerifyPhoneOtp' ||
      routeName === 'User' ||
      routeName === 'PasscodeScreen' ||
      routeName === 'Contacts' ||
      routeName === 'SelectContact' ||
      routeName === 'SendViaQRCode' ||
      routeName === 'TransactionDetail'||
      routeName === 'PaymentSuccess' ||
      routeName === 'TransactionConfirmationScreenTopUp'||
      routeName === 'Notification'

    ) {
      return false;
    }
  
    return true;
  }

  return (
    <Tab.Navigator
      screenOptions={{headerShown: false}}
      tabBarOptions={{
        showLabel: false,
        activeTintColor: theme.colors.gray.dark,
        inactiveTintColor: theme.colors.gray.medium,
        style: {
          position: 'absolute',
          bottom: 0,
          right: 0,
          left: 0,
          backgroundColor: theme.colors.white,
          borderRadius: 10,
          ...theme.appTabBar,
          ...theme.shadow.tabBar,
        }
      }}>
      {user?.attributes?.phone_number?.startsWith(
        BlockedCountries.SINGAPORE,
      ) === false ? (
        <>
          <Tab.Screen
            name="Home"
            component={HomeStackNavigator}
            options={({route}) => ({
              tabBarVisible: getTabBarVisibility(route),
              
             // unmountOnBlur: true,
              tabBarIcon: ({focused}) => (
                <Container
                  justify="center"
                  alignItems="center"
                  spacing={0.5}
                  noItemsStretch>
                  <Icon
                    name={'Home'}
                    size="xl"
                    color={focused ? 'black' : 'regular'}
                  />
                  {/* <Typography variant="chip" color={focused ? "iconFocused" : "grayLight"}>{t("Dashboard")}</Typography> */}
                </Container>
              ),
              tabBarStyle:
                getTabBarVisibility(route) === false
                  ? {display: 'none'}
                  : undefined,
            })}
          />
          <Tab.Screen
            name="InternalTransfer"
            component={InternalTransferNavigator}
            options={({route}) => ({
              unmountOnBlur: true,
              tabBarVisible: getTabBarVisibility(route),
              tabBarIcon: ({focused}) => (
                <Container
                  justify="center"
                  alignItems="center"
                  spacing={0.5}
                  noItemsStretch>
                  <Icon
                    name={'Share'}
                    size="md"
                    color={focused ? 'black' : 'regular'}
                  />
                </Container>
              ),
              tabBarStyle:
                getTabBarVisibility(route) === false
                  ? {display: 'none'}
                  : undefined,
            })}
          />

          <Tab.Screen
            name="SendFunds"
            component={SendFundsStackNavigator}
            options={({route}) => ({
              tabBarVisible: getTabBarVisibility(route),
              unmountOnBlur: true,
              tabBarIcon: ({focused}) => (
                <Container
                  justify="center"
                  alignItems="center"
                  spacing={0.5}
                  noItemsStretch>
                  <Icon name={'Scantopay'} size="md" color={'white'} />
                </Container>
              ),
              tabBarButton: props => <CustomTabBarButton {...props} />,
              tabBarStyle:
                getTabBarVisibility(route) === false
                  ? {display: 'none'}
                  : undefined,
            })}
          />

          <Tab.Screen
            name="Transactions"
            component={TransactionStackNavigator}
            options={({route}) => ({
              tabBarVisible: getTabBarVisibility(route),
             // unmountOnBlur: true,
              tabBarIcon: ({focused}) => (
                <Container
                  justify="center"
                  alignItems="center"
                  spacing={0.5}
                  noItemsStretch>
                  <Icon
                    name={'Transactions'}
                    size="md"
                    color={focused ? 'black' : 'regular'}
                  />
                  {/* <Typography variant="chip"  color={focused ? "iconFocused" : "grayLight"}>{t("Transactions")}</Typography> */}
                </Container>
              ),
              tabBarStyle:
                getTabBarVisibility(route) === false
                  ? {display: 'none'}
                  : undefined,
            })
          }
          />
          <Tab.Screen
            name="Menu"
            component={MenuStackNavigator}
            options={({route}) => ({
              tabBarVisible: getTabBarVisibility(route),
              tabBarIcon: ({focused}) => (
                <Container
                  justify="center"
                  alignItems="center"
                  spacing={0.5}
                  noItemsStretch>
                  <Icon
                    name={'Settings'}
                    size="md"
                    color={focused ? 'black' : 'regular'}
                  />
                  {/* <Typography variant="chip" color={focused ? "iconFocused" : "grayLight"}>{t("Menu")}</Typography> */}
                </Container>
              ),
              tabBarStyle:
                getTabBarVisibility(route) === false
                  ? {display: 'none'}
                  : undefined,
            })}
            
          />
        </>
      ) : (
        <>
          <Tab.Screen name="Home" component={HomeStackNavigator} />
          <Tab.Screen
            name="Transactions"
            component={TransactionStackNavigator}
          />
          <Tab.Screen name="SendFunds" component={SendFundsStackNavigator} />
          <Tab.Screen
            name="ReceiveFunds"
            component={RecieveFundsStackNavigator}
          />
          <Tab.Screen name="Menu" component={MenuStackNavigator} />
        </>
      )}
    </Tab.Navigator>
  );
}

export default AppTabNavigator
