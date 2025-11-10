import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  ReactNode,
} from 'react'
import { useColorScheme } from 'react-native'
import { colors } from './colors'

type Theme = 'light' | 'dark'

interface ThemeContextType {
  theme: Theme
  colors: typeof colors.light
  toggleTheme: () => void
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined)

export const ThemeProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const systemColorScheme = useColorScheme()
  const [theme, setTheme] = useState<Theme>(
    systemColorScheme === 'dark' ? 'dark' : 'light',
  )

  useEffect(() => {
    setTheme(systemColorScheme === 'dark' ? 'dark' : 'light')
  }, [systemColorScheme])

  const toggleTheme = () => {
    setTheme((prev) => (prev === 'light' ? 'dark' : 'light'))
  }

  const value = {
    theme,
    colors: colors[theme],
    toggleTheme,
  }

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>
}

export const useTheme = () => {
  const context = useContext(ThemeContext)
  if (!context) {
    throw new Error('useTheme must be used within ThemeProvider')
  }
  return context
}
