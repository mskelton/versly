import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { NavigationContainer } from '@react-navigation/native';
import { useTheme } from '../theme/ThemeContext';
import { ReadScreen } from '../screens/ReadScreen';
import { PlansScreen } from '../screens/PlansScreen';
import { SearchScreen } from '../screens/SearchScreen';

const Tab = createBottomTabNavigator();

export const AppNavigator: React.FC = () => {
  const { colors } = useTheme();

  return (
    <NavigationContainer>
      <Tab.Navigator
        screenOptions={{
          tabBarActiveTintColor: colors.primary,
          tabBarInactiveTintColor: colors.textSecondary,
          tabBarStyle: {
            backgroundColor: colors.surface,
            borderTopColor: colors.border,
          },
          headerStyle: {
            backgroundColor: colors.surface,
            borderBottomColor: colors.border,
          },
          headerTintColor: colors.text,
        }}
      >
        <Tab.Screen
          name="Read"
          component={ReadScreen}
          options={{
            tabBarLabel: 'Read',
            headerTitle: 'Versly',
          }}
        />
        <Tab.Screen
          name="Plans"
          component={PlansScreen}
          options={{
            tabBarLabel: 'Plans',
            headerTitle: 'Reading Plan',
          }}
        />
        <Tab.Screen
          name="Search"
          component={SearchScreen}
          options={{
            tabBarLabel: 'Search',
            headerTitle: 'Search',
          }}
        />
      </Tab.Navigator>
    </NavigationContainer>
  );
};
