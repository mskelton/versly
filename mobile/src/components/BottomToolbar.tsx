import React from 'react'
import { View, TouchableOpacity, Text } from 'react-native'
import { useTheme } from '../theme/ThemeContext'

interface BottomToolbarProps {
  onPrevious: () => void
  onNext: () => void
  onBookPicker: () => void
  onChapterPicker: () => void
  bookTitle: string
  chapter: string
}

export const BottomToolbar: React.FC<BottomToolbarProps> = ({
  onPrevious,
  onNext,
  onBookPicker,
  onChapterPicker,
  bookTitle,
  chapter,
}) => {
  const { colors } = useTheme()

  return (
    <View
      className="border-t pb-5"
      style={{ backgroundColor: colors.surface, borderTopColor: colors.border }}
    >
      <View className="flex-row items-center justify-between px-4 py-3">
        <TouchableOpacity
          className="p-2 min-w-[48px] items-center"
          onPress={onPrevious}
        >
          <Text className="text-2xl" style={{ color: colors.text }}>
            ←
          </Text>
        </TouchableOpacity>

        <View className="flex-1 items-center">
          <TouchableOpacity onPress={onBookPicker}>
            <Text
              className="text-lg font-semibold"
              style={{ color: colors.text }}
            >
              {bookTitle}
            </Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={onChapterPicker}>
            <Text
              className="text-sm mt-0.5"
              style={{ color: colors.textSecondary }}
            >
              Chapter {chapter}
            </Text>
          </TouchableOpacity>
        </View>

        <TouchableOpacity
          className="p-2 min-w-[48px] items-center"
          onPress={onNext}
        >
          <Text className="text-2xl" style={{ color: colors.text }}>
            →
          </Text>
        </TouchableOpacity>
      </View>
    </View>
  )
}
