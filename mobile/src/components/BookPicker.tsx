import React from 'react';
import { View, Text, TouchableOpacity, Modal, ScrollView, StyleSheet } from 'react-native';
import { useTheme } from '../theme/ThemeContext';
import { BookMetadata } from '../models/types';

interface BookPickerProps {
  visible: boolean;
  books: BookMetadata[];
  onSelect: (bookId: string) => void;
  onClose: () => void;
}

export const BookPicker: React.FC<BookPickerProps> = ({ visible, books, onSelect, onClose }) => {
  const { colors } = useTheme();

  return (
    <Modal
      visible={visible}
      animationType="slide"
      transparent={true}
      onRequestClose={onClose}
    >
      <View style={styles.modalOverlay}>
        <View style={[styles.modalContent, { backgroundColor: colors.surface }]}>
          <View style={[styles.header, { borderBottomColor: colors.border }]}>
            <Text style={[styles.title, { color: colors.text }]}>Select Book</Text>
            <TouchableOpacity onPress={onClose}>
              <Text style={[styles.closeButton, { color: colors.primary }]}>Close</Text>
            </TouchableOpacity>
          </View>
          <ScrollView style={styles.scrollView}>
            {books.map((book) => (
              <TouchableOpacity
                key={book.id}
                style={[styles.bookItem, { borderBottomColor: colors.border }]}
                onPress={() => {
                  onSelect(book.id);
                  onClose();
                }}
              >
                <Text style={[styles.bookTitle, { color: colors.text }]}>{book.title}</Text>
                <Text style={[styles.bookInfo, { color: colors.textSecondary }]}>
                  {book.chapterCount} {book.chapterCount === 1 ? 'chapter' : 'chapters'}
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    maxHeight: '80%',
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    borderBottomWidth: 1,
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
  },
  closeButton: {
    fontSize: 16,
  },
  scrollView: {
    padding: 8,
  },
  bookItem: {
    padding: 16,
    borderBottomWidth: 1,
  },
  bookTitle: {
    fontSize: 18,
    fontWeight: '500',
    marginBottom: 4,
  },
  bookInfo: {
    fontSize: 14,
  },
});
