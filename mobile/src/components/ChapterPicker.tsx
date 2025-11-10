import React from 'react'
import { View, Text, TouchableOpacity, Modal, FlatList } from 'react-native'
import { useTheme } from '../theme/ThemeContext'

interface ChapterPickerProps {
  visible: boolean
  chapterCount: number
  onSelect: (chapter: string) => void
  onClose: () => void
}

export const ChapterPicker: React.FC<ChapterPickerProps> = ({
  visible,
  chapterCount,
  onSelect,
  onClose,
}) => {
  const { colors } = useTheme()

  const chapters = Array.from({ length: chapterCount }, (_, i) =>
    (i + 1).toString(),
  )

  return (
    <Modal
      visible={visible}
      animationType="slide"
      transparent={true}
      onRequestClose={onClose}
    >
      <View className="flex-1 bg-black/50 justify-end">
        <View
          className="max-h-[80%] rounded-t-[20px]"
          style={{ backgroundColor: colors.surface }}
        >
          <View
            className="flex-row justify-between items-center p-4 border-b"
            style={{ borderBottomColor: colors.border }}
          >
            <Text className="text-xl font-bold" style={{ color: colors.text }}>
              Select Chapter
            </Text>
            <TouchableOpacity onPress={onClose}>
              <Text className="text-base" style={{ color: colors.primary }}>
                Close
              </Text>
            </TouchableOpacity>
          </View>
          <FlatList
            data={chapters}
            numColumns={6}
            keyExtractor={(item) => item}
            renderItem={({ item }) => (
              <TouchableOpacity
                className="flex-1 aspect-square m-1 border rounded-lg justify-center items-center min-w-[50px]"
                style={{ borderColor: colors.border }}
                onPress={() => {
                  onSelect(item)
                  onClose()
                }}
              >
                <Text
                  className="text-lg font-medium"
                  style={{ color: colors.text }}
                >
                  {item}
                </Text>
              </TouchableOpacity>
            )}
            contentContainerClassName="p-4"
          />
        </View>
      </View>
    </Modal>
  )
}
