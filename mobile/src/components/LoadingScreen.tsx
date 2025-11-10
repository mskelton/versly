import React from 'react'
import { View, ActivityIndicator, Text } from 'react-native'
import { useTheme } from '../theme/ThemeContext'

interface LoadingScreenProps {
  message?: string
}

export const LoadingScreen: React.FC<LoadingScreenProps> = ({
  message = 'Loading...',
}) => {
  const { colors } = useTheme()

  return (
    <View
      className="flex-1 justify-center items-center"
      style={{ backgroundColor: colors.background }}
    >
      <ActivityIndicator size="large" color={colors.primary} />
      <Text className="mt-4 text-base" style={{ color: colors.text }}>
        {message}
      </Text>
    </View>
  )
}
