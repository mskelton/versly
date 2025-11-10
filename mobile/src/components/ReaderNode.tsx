import React from 'react'
import { View, Text, StyleSheet } from 'react-native'
import { Node } from '../models/types'
import { useTheme } from '../theme/ThemeContext'

interface ReaderNodeProps {
  node: Node
}

export const ReaderNode: React.FC<ReaderNodeProps> = ({ node }) => {
  const { colors } = useTheme()
  const data = node.data

  if (!Array.isArray(data) || data.length === 0) return null

  const type = data[0]

  const renderChildren = (startIndex: number = 1) => {
    const elements: React.ReactNode[] = []

    for (let i = startIndex; i < data.length; i++) {
      const item = data[i]

      if (typeof item === 'string') {
        elements.push(<Text key={i}>{item}</Text>)
      } else if (Array.isArray(item)) {
        elements.push(renderInlineNode(item, i))
      }
    }

    return elements
  }

  const renderInlineNode = (item: any[], key: number): React.ReactNode => {
    if (!Array.isArray(item) || item.length === 0) return null

    const inlineType = item[0]
    const content = item.slice(1)

    switch (inlineType) {
      case 'v':
        return (
          <Text
            key={key}
            style={[styles.verse, { color: colors.textSecondary }]}
          >
            {content[0]}{' '}
          </Text>
        )

      case 'wj':
        return (
          <Text key={key} style={{ color: colors.wordsOfJesus }}>
            {content.map((c, i) =>
              typeof c === 'string' ? c : renderInlineNode(c, i),
            )}
          </Text>
        )

      case 'em':
      case 'bd':
        return (
          <Text key={key} style={styles.bold}>
            {content.map((c, i) =>
              typeof c === 'string' ? c : renderInlineNode(c, i),
            )}
          </Text>
        )

      case 'it':
      case 'bk':
      case 'qt':
      case 'sig':
      case 'sls':
      case 'tl':
        return (
          <Text key={key} style={styles.italic}>
            {content.map((c, i) =>
              typeof c === 'string' ? c : renderInlineNode(c, i),
            )}
          </Text>
        )

      case 'nd':
      case 'sc':
        return (
          <Text key={key} style={styles.smallCaps}>
            {content
              .map((c, i) =>
                typeof c === 'string' ? c : renderInlineNode(c, i),
              )
              .join('')
              .toUpperCase()}
          </Text>
        )

      default:
        return (
          <Text key={key}>
            {content.map((c, i) =>
              typeof c === 'string' ? c : renderInlineNode(c, i),
            )}
          </Text>
        )
    }
  }

  switch (type) {
    case 'zc':
      return (
        <View style={styles.chapterHeader}>
          <Text style={[styles.chapterTitle, { color: colors.text }]}>
            {data[1]} {data[2]}
          </Text>
        </View>
      )

    case 's1':
    case 's2':
    case 's3':
    case 'ms':
      return (
        <View style={styles.heading}>
          <Text style={[styles.headingText, { color: colors.text }]}>
            {renderChildren()}
          </Text>
        </View>
      )

    case 'd':
      return (
        <View style={styles.description}>
          <Text style={[styles.italic, { color: colors.text }]}>
            {renderChildren()}
          </Text>
        </View>
      )

    case 'p':
      return (
        <View style={styles.paragraph}>
          <Text style={[styles.bodyText, { color: colors.text }]}>
            {'  '}
            {renderChildren()}
          </Text>
        </View>
      )

    case 'm':
      return (
        <View style={styles.paragraph}>
          <Text style={[styles.bodyText, { color: colors.text }]}>
            {renderChildren()}
          </Text>
        </View>
      )

    case 'q1':
      return (
        <View style={styles.poetry1}>
          <Text style={[styles.bodyText, { color: colors.text }]}>
            {renderChildren()}
          </Text>
        </View>
      )

    case 'q2':
      return (
        <View style={styles.poetry2}>
          <Text style={[styles.bodyText, { color: colors.text }]}>
            {renderChildren()}
          </Text>
        </View>
      )

    case 'q3':
      return (
        <View style={styles.poetry3}>
          <Text style={[styles.bodyText, { color: colors.text }]}>
            {renderChildren()}
          </Text>
        </View>
      )

    case 'q4':
      return (
        <View style={styles.poetry4}>
          <Text style={[styles.bodyText, { color: colors.text }]}>
            {renderChildren()}
          </Text>
        </View>
      )

    case 'li1':
    case 'li2':
    case 'li3':
    case 'li4':
      const indent = parseInt(type.slice(-1)) || 1
      return (
        <View style={[styles.listItem, { paddingLeft: indent * 16 }]}>
          <Text style={[styles.bodyText, { color: colors.text }]}>
            • {renderChildren()}
          </Text>
        </View>
      )

    case 'b':
      return <View style={styles.blank} />

    case 'pi1':
    case 'pi2':
    case 'pi3':
      const piIndent = parseInt(type.slice(-1)) || 1
      return (
        <View style={[styles.paragraph, { paddingLeft: piIndent * 16 }]}>
          <Text style={[styles.bodyText, { color: colors.text }]}>
            {'  '}
            {renderChildren()}
          </Text>
        </View>
      )

    default:
      return (
        <View style={styles.paragraph}>
          <Text style={[styles.bodyText, { color: colors.text }]}>
            {renderChildren()}
          </Text>
        </View>
      )
  }
}

const styles = StyleSheet.create({
  chapterHeader: {
    paddingVertical: 24,
    alignItems: 'center',
  },
  chapterTitle: {
    fontSize: 24,
    fontWeight: 'bold',
  },
  heading: {
    paddingTop: 32,
    paddingBottom: 24,
  },
  headingText: {
    fontSize: 20,
    fontWeight: 'bold',
    lineHeight: 28,
  },
  description: {
    paddingBottom: 24,
  },
  paragraph: {
    paddingBottom: 16,
  },
  bodyText: {
    fontSize: 18,
    lineHeight: 32,
  },
  verse: {
    fontSize: 14,
    lineHeight: 24,
  },
  bold: {
    fontWeight: 'bold',
  },
  italic: {
    fontStyle: 'italic',
  },
  smallCaps: {
    fontSize: 16,
    letterSpacing: 1,
  },
  poetry1: {
    paddingBottom: 16,
  },
  poetry2: {
    paddingBottom: 16,
    paddingLeft: 16,
  },
  poetry3: {
    paddingBottom: 16,
    paddingLeft: 32,
  },
  poetry4: {
    paddingBottom: 16,
    paddingLeft: 48,
  },
  listItem: {
    paddingBottom: 8,
  },
  blank: {
    height: 16,
  },
})
