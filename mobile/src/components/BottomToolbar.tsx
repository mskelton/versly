import React from 'react';
import { View, TouchableOpacity, Text, StyleSheet } from 'react-native';
import { useTheme } from '../theme/ThemeContext';

interface BottomToolbarProps {
  onPrevious: () => void;
  onNext: () => void;
  onBookPicker: () => void;
  onChapterPicker: () => void;
  bookTitle: string;
  chapter: string;
}

export const BottomToolbar: React.FC<BottomToolbarProps> = ({
  onPrevious,
  onNext,
  onBookPicker,
  onChapterPicker,
  bookTitle,
  chapter,
}) => {
  const { colors } = useTheme();

  return (
    <View style={[styles.container, { backgroundColor: colors.surface, borderTopColor: colors.border }]}>
      <View style={styles.content}>
        <TouchableOpacity style={styles.button} onPress={onPrevious}>
          <Text style={[styles.buttonText, { color: colors.text }]}>←</Text>
        </TouchableOpacity>

        <View style={styles.center}>
          <TouchableOpacity onPress={onBookPicker}>
            <Text style={[styles.title, { color: colors.text }]}>{bookTitle}</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={onChapterPicker}>
            <Text style={[styles.chapter, { color: colors.textSecondary }]}>
              Chapter {chapter}
            </Text>
          </TouchableOpacity>
        </View>

        <TouchableOpacity style={styles.button} onPress={onNext}>
          <Text style={[styles.buttonText, { color: colors.text }]}>→</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    borderTopWidth: 1,
    paddingBottom: 20,
  },
  content: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  button: {
    padding: 8,
    minWidth: 48,
    alignItems: 'center',
  },
  buttonText: {
    fontSize: 24,
  },
  center: {
    flex: 1,
    alignItems: 'center',
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
  },
  chapter: {
    fontSize: 14,
    marginTop: 2,
  },
});
