import React from 'react'
import { View, Text, TouchableOpacity, Modal, ScrollView } from 'react-native'
import { useTheme } from '../theme/ThemeContext'
import { BookMetadata } from '../models/types'

interface BookPickerProps {
  visible: boolean
  books: BookMetadata[]
  onSelect: (bookId: string) => void
  onClose: () => void
}

export const BookPicker: React.FC<BookPickerProps> = ({
  visible,
  books,
  onSelect,
  onClose,
}) => {
  const { colors } = useTheme()

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
              Select Book
            </Text>
            <TouchableOpacity onPress={onClose}>
              <Text className="text-base" style={{ color: colors.primary }}>
                Close
              </Text>
            </TouchableOpacity>
          </View>
          <ScrollView className="p-2">
            {books.map((book) => (
              <TouchableOpacity
                key={book.id}
                className="p-4 border-b"
                style={{ borderBottomColor: colors.border }}
                onPress={() => {
                  onSelect(book.id)
                  onClose()
                }}
              >
                <Text
                  className="text-lg font-medium mb-1"
                  style={{ color: colors.text }}
                >
                  {book.title}
                </Text>
                <Text
                  className="text-sm"
                  style={{ color: colors.textSecondary }}
                >
                  {book.chapterCount}{' '}
                  {book.chapterCount === 1 ? 'chapter' : 'chapters'}
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>
      </View>
    </Modal>
  )
}
