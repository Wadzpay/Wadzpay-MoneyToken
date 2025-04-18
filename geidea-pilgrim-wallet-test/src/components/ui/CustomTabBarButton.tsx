import React from 'react';
import {StyleSheet, Text, View, TouchableOpacity} from 'react-native';
import Svg, {Path} from 'react-native-svg';
import theme from './theme';
import Typography from './Typography';

const CustomTabBarButton = props => {
  const {children, onPress, accessibilityState} = props;
   return(
    <TouchableOpacity
    onPress={onPress}
    style={{
        top:0,
        justifyContent:"center",
        alignItems:"center"
    }}>
        <View style={{
            width:38,
            height:38,
            borderRadius:19,
            margin:30,
            backgroundColor: theme.colors.orange,
        }}>
            {children}
        </View>
    </TouchableOpacity>
   )
};

export default CustomTabBarButton;

const styles = StyleSheet.create({
    shadow: {
    shadowColor: theme.shadow.tabBar.shadowColor,
    shadowOffset:{
        width:0,
        height:10
    },
    shadowOpacity:0.25,
    shadowRadius:3.5,
    elevation: 5,
  }
});