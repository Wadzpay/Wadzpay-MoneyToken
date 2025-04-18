import React, {useRef, useState} from 'react';
import {
  Dimensions,
  StyleSheet,
  TouchableWithoutFeedback,
  View,
} from 'react-native';
//import RNPickerSelect from "react-native-picker-select"
import {Picker} from '@react-native-picker/picker';
import {useTranslation} from 'react-i18next';
import {FieldError, useController} from 'react-hook-form';

import Icon from './Icon';
import theme, {spacing} from './theme';
import Typography from './Typography';
import Container from './Container';
import ErrorMessage from './ErrorMessage';

import {IconName} from '~/icons';
import {FieldControl, FieldName, WADZPAY_WALLET} from '~/constants';
import {isIOS} from '~/utils';

const {width} = Dimensions.get('window');
const createStyles = (hasIcon: boolean, hasError: boolean) =>
  StyleSheet.create({
    container: {
      height: spacing(7.5),
      flexDirection: 'row',
      paddingHorizontal: theme.spacing.xs,
      paddingVertical: theme.spacing.xs,
      borderRadius: theme.borderRadius.md,
      borderWidth: theme.borderWidth.sm,
      backgroundColor: '#F1F1F1',
      borderColor: hasError ? theme.colors.error : '#E8E8E8',
    },
    label: {
      marginLeft: hasIcon ? spacing(0.5) : 0,
      marginRight: spacing(0.5),
    },
    icon: {
      margin: 4,
    },
  });

export type SelectOptionItem = {
  label: string;
  value: string;
};

type Props = {
  name: FieldName;
  items: SelectOptionItem[];
  control: FieldControl;
  label?: string;
  error?: FieldError;
  iconName?: IconName;
  placeholder?: string;
  initalCountryName?: string;
  onChange?: (value: string) => void;
};

const SelectField: React.FC<Props> = ({
  label,
  name,
  items,
  control,
  error,
  iconName,
  placeholder,
  initalCountryName,
  onChange,
}: Props) => {
  const {field} = useController({control, name});
  const [selectedLanguage, setSelectedLanguage] = useState(initalCountryName);
  const pickerRef = useRef();
  const styles = createStyles(!!iconName, !!error);
  const {t} = useTranslation();

  const _onChange = (value: string) => {
    onChange && onChange(value || '');
    field.onChange(value || '');
    setSelectedLanguage(value)
  };
  // function open() {
  //   pickerRef.current.focus();
  // }

  // function close() {
  //   pickerRef.current.blur();
  // }

 // console.log("field.value ", field.value,"selectedLanguage" ,selectedLanguage)
  return (
    <TouchableWithoutFeedback>
      <View>
        <Typography
          fontWeight="bold"
          textAlign="left"
          color="grayDark"
          style={{marginBottom: 5}}>
          {label}
        </Typography>

      <Container alignItems='center'
      justify='center'
      style ={{marginTop:8,
        borderColor: theme.colors.gray.dark,
        borderWidth:0.5,
        borderRadius:6,
      backgroundColor: theme.colors.gray.light}}>
        <Picker
          ref={pickerRef}
          selectedValue={selectedLanguage}
          value={selectedLanguage}
          onValueChange={(itemValue, itemIndex) => _onChange(itemValue)}
          items={items}>
            <Picker.Item label={initalCountryName} value={""} />
          {items.filter((item) => initalCountryName !== item.label)
          .map(item => (
                  <Picker.Item label={item.label} value={item.value} />
          ))}
             
        </Picker>
        </Container>
        <ErrorMessage text={error?.message} />
      </View>
    </TouchableWithoutFeedback>
  );
};

export default SelectField;
