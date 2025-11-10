import React from 'react'
import { View, Text } from 'react-native'
import { useTheme } from '../theme/ThemeContext'

export const SearchScreen: React.FC = () => {
  const { colors } = useTheme()

  return (
    <View
      className="flex-1 justify-center items-center"
      style={{ backgroundColor: colors.background }}
    >
      <Text className="text-base" style={{ color: colors.textSecondary }}>
        Search functionality coming soon
      </Text>
    </View>
  )
}
