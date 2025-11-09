import React, { useState, useEffect } from 'react';
import { View, ScrollView, Text, StyleSheet } from 'react-native';
import { useTheme } from '../theme/ThemeContext';
import { useAppStore } from '../store/appStore';
import { ReaderNode } from '../components/ReaderNode';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { PassageId, Passage } from '../models/types';
import { database } from '../services/database';
import { format } from 'date-fns';

interface ReadingPlan {
  date: string;
  passages: PassageId[];
}

export const PlansScreen: React.FC = () => {
  const { colors } = useTheme();
  const { currentPassage } = useAppStore();
  const [passages, setPassages] = useState<Passage[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadTodaysPlan();
  }, [currentPassage?.translation]);

  const loadTodaysPlan = async () => {
    try {
      setIsLoading(true);

      const planData = require('../assets/plan.json') as ReadingPlan[];
      const today = format(new Date(), 'yyyy-MM-dd');
      const todaysPlan = planData.find(p => p.date === today);

      if (todaysPlan && currentPassage) {
        const loadedPassages = await Promise.all(
          todaysPlan.passages.map(p =>
            database.getPassage(p.book, p.chapter, currentPassage.translation)
          )
        );
        setPassages(loadedPassages);
      } else {
        setPassages([]);
      }
    } catch (error) {
      console.error('Failed to load reading plan:', error);
      setPassages([]);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <View style={[styles.container, { backgroundColor: colors.background }]}>
        <LoadingSpinner />
      </View>
    );
  }

  if (passages.length === 0) {
    return (
      <View style={[styles.container, styles.centered, { backgroundColor: colors.background }]}>
        <Text style={[styles.emptyText, { color: colors.textSecondary }]}>
          No reading plan for today
        </Text>
      </View>
    );
  }

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <ScrollView style={styles.scrollView} contentContainerStyle={styles.content}>
        <View style={styles.header}>
          <Text style={[styles.headerText, { color: colors.text }]}>
            {passages.map(p => p.bookAbbreviation).join(' • ')}
          </Text>
        </View>

        {passages.map((passage, index) => (
          <View key={`${passage.book}-${passage.chapter}`}>
            {passage.nodes.map((node) => (
              <ReaderNode key={node.id} node={node} />
            ))}
            {index < passages.length - 1 && (
              <View style={[styles.divider, { backgroundColor: colors.border }]} />
            )}
          </View>
        ))}
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  centered: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  scrollView: {
    flex: 1,
  },
  content: {
    padding: 16,
    paddingBottom: 32,
  },
  header: {
    paddingVertical: 16,
    alignItems: 'center',
  },
  headerText: {
    fontSize: 16,
    fontWeight: '600',
  },
  emptyText: {
    fontSize: 16,
  },
  divider: {
    height: 2,
    marginVertical: 32,
  },
});
