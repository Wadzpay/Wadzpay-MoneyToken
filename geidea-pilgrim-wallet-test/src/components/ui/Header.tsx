import React, {useContext} from 'react';
import {
  StyleSheet,
  TouchableOpacity,
  View,
  Image,
  useWindowDimensions,
} from 'react-native';
import theme from './theme';
import Typography from './Typography';
import Icon from './Icon';
import Container from './Container';
import {IconName} from '~/icons';
import {UserContext} from '~/context';

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: theme.spacing.md,
    paddingVertical: theme.spacing.xs,
    height: theme.headerHeight,
  },
  title: {
    marginLeft: theme.spacing.sm,
  },
  logo: {
    // width: 121,
    // height: 33,
    alignSelf: 'flex-start',
    marginVertical: 10,
  },
});

type Props = {
  title?: string;
  useLogo?: boolean;
  leftIconName?: IconName;
  onLeftIconClick?: () => void;
  rightComponent?: React.ReactNode;
  onRightIconClick?: () => void;
  notificationComponent?: React.ReactNode;
  onNotificationIconClick?: () => void;
  showHeaderBg?: boolean;
};

const Header: React.FC<Props> = ({
  title,
  useLogo,
  leftIconName,
  onLeftIconClick,
  rightComponent,
  onRightIconClick,
  notificationComponent,
  onNotificationIconClick,
  showHeaderBg,
}: Props) => {
  const {instDetails, user} = useContext(UserContext);
  console.log("in header", user)
  const {width} = useWindowDimensions(); // Get dynamic screen width
  console.log("window width", width)
  const logoWidth = width * 0.5; // Adjust width (30% of screen width)
  const logoHeight = (logoWidth / 121) * 33; // Maintain aspect ratio
  return (
    <Container direction="column">
      <Container
        direction="row"
        justify="space-between"
        alignItems="center"
        noItemsStretch
        style={styles.container}>
        <Container direction="row" alignItems="flex-start" noItemsStretch>
          {leftIconName && (
            <Icon
              name={leftIconName}
              color={'black'}
              onPress={onLeftIconClick}
            />
          )}
          <View style={[leftIconName && styles.title]}>
            {instDetails?.institutionLogo ? (
              <Image
                // source={require(instDetails?instDetails?.institutionLogo: "")}
                source={{uri: 
                  instDetails.institutionLogo
                }}
                style={[
                  styles.logo,
                  {
                    width: 90,
                    height: logoHeight,
                    // alignSelf: 'flex-start',
                  },
                ]}
                resizeMode="contain"

              />
            ) : (
              // <Typography fontFamily="Rubik-Medium" variant="headerTitle">{title}</Typography>
              <Typography fontFamily="Rubik-Medium" variant="headerTitle">
                {instDetails?.institutionName}
              </Typography>
            )}
          </View>
        </Container>

        <Container
          direction="row"
          alignItems="center"
          noItemsStretch
          justify="space-evenly"
          spacing={1}>
          {notificationComponent && (
            <TouchableOpacity onPress={onNotificationIconClick}>
              {notificationComponent}
            </TouchableOpacity>
          )}
          {rightComponent && (
            <TouchableOpacity onPress={onRightIconClick}>
              {rightComponent}
            </TouchableOpacity>
          )}
        </Container>
      </Container>

      {showHeaderBg && (
        <View
          style={{
            borderBottomColor: theme.colors.gray.light,
            borderBottomWidth: StyleSheet.hairlineWidth,
            shadowColor: '#171717',
            shadowOffset: {width: -2, height: 140},
            shadowOpacity: 0.2,
            shadowRadius: 130,
            elevation: 20,
          }}
        />
      )}
    </Container>
  );
};

export default Header;
