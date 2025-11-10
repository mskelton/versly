import React, { useState, useEffect } from 'react'
import { ActivityIndicator, View } from 'react-native'
import { useTheme } from '../theme/ThemeContext'

export const LoadingSpinner: React.FC = () => {
  const { colors } = useTheme()
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    const timer = setTimeout(() => setVisible(true), 200)
    return () => clearTimeout(timer)
  }, [])

  if (!visible) return null

  return (
    <View className="p-5 items-center">
      <ActivityIndicator size="large" color={colors.primary} />
    </View>
  )
}
