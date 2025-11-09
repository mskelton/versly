import React, { useState, useEffect } from 'react';
import { View, ScrollView, StyleSheet } from 'react-native';
import { useTheme } from '../theme/ThemeContext';
import { useAppStore } from '../store/appStore';
import { ReaderNode } from '../components/ReaderNode';
import { BottomToolbar } from '../components/BottomToolbar';
import { BookPicker } from '../components/BookPicker';
import { ChapterPicker } from '../components/ChapterPicker';
import { LoadingSpinner } from '../components/LoadingSpinner';

export const ReadScreen: React.FC = () => {
  const { colors } = useTheme();
  const {
    passage,
    books,
    currentPassage,
    isLoading,
    setCurrentPassage,
    goToNextChapter,
    goToPreviousChapter,
  } = useAppStore();

  const [showBookPicker, setShowBookPicker] = useState(false);
  const [showChapterPicker, setShowChapterPicker] = useState(false);

  const currentBook = books.find(b => b.id === currentPassage?.book);

  const handleBookSelect = (bookId: string) => {
    if (currentPassage) {
      setCurrentPassage({
        ...currentPassage,
        book: bookId,
        chapter: '1',
      });
    }
  };

  const handleChapterSelect = (chapter: string) => {
    if (currentPassage) {
      setCurrentPassage({
        ...currentPassage,
        chapter,
      });
    }
  };

  if (isLoading) {
    return (
      <View style={[styles.container, { backgroundColor: colors.background }]}>
        <LoadingSpinner />
      </View>
    );
  }

  if (!passage) {
    return <View style={[styles.container, { backgroundColor: colors.background }]} />;
  }

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <ScrollView style={styles.scrollView} contentContainerStyle={styles.content}>
        {passage.nodes.map((node) => (
          <ReaderNode key={node.id} node={node} />
        ))}
      </ScrollView>

      <BottomToolbar
        onPrevious={goToPreviousChapter}
        onNext={goToNextChapter}
        onBookPicker={() => setShowBookPicker(true)}
        onChapterPicker={() => setShowChapterPicker(true)}
        bookTitle={passage.bookTitle}
        chapter={passage.chapter}
      />

      <BookPicker
        visible={showBookPicker}
        books={books}
        onSelect={handleBookSelect}
        onClose={() => setShowBookPicker(false)}
      />

      <ChapterPicker
        visible={showChapterPicker}
        chapterCount={currentBook?.chapterCount || 1}
        onSelect={handleChapterSelect}
        onClose={() => setShowChapterPicker(false)}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollView: {
    flex: 1,
  },
  content: {
    padding: 16,
    paddingBottom: 32,
  },
});
