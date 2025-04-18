import React from 'react';

import {StyleSheet, ViewStyle, TouchableOpacity} from 'react-native';

import Typography from './Typography';

import theme, {
  ChipColorVariant,
  TypographyColorVariant,
  TypographyVariant,
} from './theme';

import Container from './Container';

import Icon from './Icon';

import {
  Asset,
  TransactionDirection,
  TransactionStatus,
  TransactionType,
} from '~/constants/types';

import {useTranslationItems} from '~/constants/translationItems';

import {IconName} from '~/icons';

export const useTransactionDirectionChipProps: () => {
  [key in TransactionDirection]: {
    title: string;

    color: ChipColorVariant;

    iconName: IconName;
  };
} = () => {
  const {direction} = useTranslationItems();

  return {
    INCOMING: {
      title: direction.INCOMING,

      color: 'success',

      iconName: 'ArrowDown',
    },

    OUTGOING: {
      title: direction.OUTGOING,

      color: 'error',

      iconName: 'ArrowUp',
    },

    UNKNOWN: {
      title: direction.UNKNOWN,

      color: 'orange',

      iconName: 'ArrowLeft',
    },
  };
};

export const useTransactionTypeChipProps: () => {
  [key in TransactionType]: {title: string; color: ChipColorVariant};
} = () => {
  const {type} = useTranslationItems();

  return {
    DEPOSIT: {title: type.DEPOSIT, color: 'info'},
    WITHDRAW: {title: type.WITHDRAW, color: 'info'},
    PEER_TO_PEER: {title: type.PEER_TO_PEER, color: 'info'},
    EXTERNAL_SEND: {title: type.EXTERNAL_SEND, color: 'info'},
    EXTERNAL_RECEIVE: {title: type.EXTERNAL_RECEIVE, color: 'info'},
    POS: {title: type.POS, color: 'info'},
    OTHERS: {title: type.POS, color: 'info'},
    REFUND: {title: type.REFUND, color: 'info'},
  };
};

export const useTransactionStatusChipProps: () => {
  [key in TransactionStatus]: {title: string; color: ChipColorVariant};
} = () => {
  const {status} = useTranslationItems();

  return {
    SUCCESSFUL: {title: status.SUCCESSFUL, color: 'success'},

    IN_PROGRESS: {title: status.IN_PROGRESS, color: 'orange'},

    FAILED: {title: status.FAILED, color: 'error'},

    OVERPAID: {title: status.OVERPAID, color: 'error'},

    UNDERPAID: {title: status.UNDERPAID, color: 'error'},
  };
};

export const useAssetChipProps: () => {
  [key in Asset]: {title: string; color: ChipColorVariant};
} = () => {
  const {asset} = useTranslationItems();

  return {
    WTK: {title: asset.WTK, color: 'black'},

    BTC: {title: asset.BTC, color: 'black'},

    ETH: {title: asset.ETH, color: 'black'},

    USDT: {title: asset.USDT, color: 'black'},
  };
};

const chipColorsMap: {[key in ChipColorVariant]: string} = {
  black: theme.colors.black,

  orange: theme.colors.orange,

  success: theme.colors.success,

  error: theme.colors.error,

  info: theme.colors.info,
};

const getStyles = (
  color: ChipColorVariant,

  hasLeftIcon: boolean,

  hasRightIcon: boolean,
) =>
  StyleSheet.create({
    chip: {
      backgroundColor: `${chipColorsMap[color]}25`,

      borderRadius: theme.spacing.xs,
    },

    text: {
      paddingLeft: hasLeftIcon ? 0 : theme.spacing.xs,

      paddingRight: hasRightIcon ? 0 : theme.spacing.xs,

      paddingVertical: theme.spacing.xs / 2,
    },

    icon: {
      marginHorizontal: theme.spacing.xs / 2,
    },
  });

type Props = {
  variant?: TypographyVariant;

  text: string;

  color: ChipColorVariant;

  leftIconName?: IconName;

  rightIconName?: IconName;

  onPress?: (...args: unknown[]) => void;

  style?: ViewStyle;
};

const Chip: React.FC<Props> = ({
  variant = 'chip',

  text,

  color,

  leftIconName,

  rightIconName,

  onPress,

  style,
}: Props) => {
  const styles = getStyles(color, !!leftIconName, !!rightIconName);

  const chip = text ? (
    <Container
      direction="row"
      justify="center"
      alignItems="center"
      noItemsStretch
      style={[styles.chip, style]}>
      {leftIconName && (
        <Icon
          name={leftIconName}
          size="xxs"
          color={color}
          style={styles.icon}
        />
      )}

      <Typography
        variant={variant}
        color={color as TypographyColorVariant}
        style={styles.text}>
        {text}
      </Typography>

      {rightIconName && (
        <Icon
          name={rightIconName}
          size="xxs"
          color={color}
          style={styles.icon}
        />
      )}
    </Container>
  ) : null;

  return onPress ? (
    <TouchableOpacity onPress={onPress}>{chip}</TouchableOpacity>
  ) : (
    chip
  );
};

export default Chip;
