import React, { useEffect } from 'react';
import { StatusBar, useColorScheme } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { ThemeProvider } from './src/theme/ThemeContext';
import { AppNavigator } from './src/navigation/AppNavigator';
import { LoadingScreen } from './src/components/LoadingScreen';
import { useAppStore } from './src/store/appStore';

function AppContent() {
  const isDarkMode = useColorScheme() === 'dark';
  const { init, isInitialized, isLoading, error } = useAppStore();

  useEffect(() => {
    init();
  }, []);

  if (error) {
    return <LoadingScreen message={`Error: ${error}`} />;
  }

  if (!isInitialized || isLoading) {
    return <LoadingScreen message="Initializing Versly..." />;
  }

  return (
    <>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <AppNavigator />
    </>
  );
}

function App() {
  return (
    <SafeAreaProvider>
      <ThemeProvider>
        <AppContent />
      </ThemeProvider>
    </SafeAreaProvider>
  );
}

export default App;
