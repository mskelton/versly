import React from 'react';
import { View, Text, TouchableOpacity, Modal, StyleSheet, FlatList } from 'react-native';
import { useTheme } from '../theme/ThemeContext';

interface ChapterPickerProps {
  visible: boolean;
  chapterCount: number;
  onSelect: (chapter: string) => void;
  onClose: () => void;
}

export const ChapterPicker: React.FC<ChapterPickerProps> = ({
  visible,
  chapterCount,
  onSelect,
  onClose,
}) => {
  const { colors } = useTheme();

  const chapters = Array.from({ length: chapterCount }, (_, i) => (i + 1).toString());

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
            <Text style={[styles.title, { color: colors.text }]}>Select Chapter</Text>
            <TouchableOpacity onPress={onClose}>
              <Text style={[styles.closeButton, { color: colors.primary }]}>Close</Text>
            </TouchableOpacity>
          </View>
          <FlatList
            data={chapters}
            numColumns={6}
            keyExtractor={(item) => item}
            renderItem={({ item }) => (
              <TouchableOpacity
                style={[styles.chapterItem, { borderColor: colors.border }]}
                onPress={() => {
                  onSelect(item);
                  onClose();
                }}
              >
                <Text style={[styles.chapterText, { color: colors.text }]}>{item}</Text>
              </TouchableOpacity>
            )}
            contentContainerStyle={styles.grid}
          />
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
  grid: {
    padding: 16,
  },
  chapterItem: {
    flex: 1,
    aspectRatio: 1,
    margin: 4,
    borderWidth: 1,
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    minWidth: 50,
  },
  chapterText: {
    fontSize: 18,
    fontWeight: '500',
  },
});
