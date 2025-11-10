import React, { useState, useEffect } from 'react'
import { View, ScrollView, Text } from 'react-native'
import { useTheme } from '../theme/ThemeContext'
import { useAppStore } from '../store/appStore'
import { ReaderNode } from '../components/ReaderNode'
import { LoadingSpinner } from '../components/LoadingSpinner'
import { PassageId, Passage } from '../models/types'
import { database } from '../services/database'
import { format } from 'date-fns'

interface ReadingPlan {
  date: string
  passages: PassageId[]
}

export const PlansScreen: React.FC = () => {
  const { colors } = useTheme()
  const { currentPassage } = useAppStore()
  const [passages, setPassages] = useState<Passage[]>([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    loadTodaysPlan()
  }, [currentPassage?.translation])

  const loadTodaysPlan = async () => {
    try {
      setIsLoading(true)

      const planData = require('../assets/plan.json') as ReadingPlan[]
      const today = format(new Date(), 'yyyy-MM-dd')
      const todaysPlan = planData.find((p) => p.date === today)

      if (todaysPlan && currentPassage) {
        const loadedPassages = await Promise.all(
          todaysPlan.passages.map((p) =>
            database.getPassage(p.book, p.chapter, currentPassage.translation),
          ),
        )
        setPassages(loadedPassages)
      } else {
        setPassages([])
      }
    } catch (error) {
      console.error('Failed to load reading plan:', error)
      setPassages([])
    } finally {
      setIsLoading(false)
    }
  }

  if (isLoading) {
    return (
      <View className="flex-1" style={{ backgroundColor: colors.background }}>
        <LoadingSpinner />
      </View>
    )
  }

  if (passages.length === 0) {
    return (
      <View
        className="flex-1 justify-center items-center"
        style={{ backgroundColor: colors.background }}
      >
        <Text className="text-base" style={{ color: colors.textSecondary }}>
          No reading plan for today
        </Text>
      </View>
    )
  }

  return (
    <View className="flex-1" style={{ backgroundColor: colors.background }}>
      <ScrollView className="flex-1" contentContainerClassName="p-4 pb-8">
        <View className="py-4 items-center">
          <Text
            className="text-base font-semibold"
            style={{ color: colors.text }}
          >
            {passages.map((p) => p.bookAbbreviation).join(' • ')}
          </Text>
        </View>

        {passages.map((passage, index) => (
          <View key={`${passage.book}-${passage.chapter}`}>
            {passage.nodes.map((node) => (
              <ReaderNode key={node.id} node={node} />
            ))}
            {index < passages.length - 1 && (
              <View
                className="h-0.5 my-8"
                style={{ backgroundColor: colors.border }}
              />
            )}
          </View>
        ))}
      </ScrollView>
    </View>
  )
}
