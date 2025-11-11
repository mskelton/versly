import { Icon, Label, NativeTabs } from 'expo-router/unstable-native-tabs'

export default function RootLayout() {
  return (
    <NativeTabs>
      <NativeTabs.Trigger name="index">
        <Icon
          sf="book"
          androidSrc={require('../assets/icons/book_2_24px.svg')}
        />
        <Label>Read</Label>
      </NativeTabs.Trigger>

      <NativeTabs.Trigger name="plans">
        <Icon
          sf="list.bullet"
          androidSrc={require('../assets/icons/library_add_check_24px.svg')}
        />
        <Label>Plans</Label>
      </NativeTabs.Trigger>

      <NativeTabs.Trigger name="settings">
        <Icon
          sf="magnifyingglass"
          androidSrc={require('../assets/icons/search_24px.svg')}
        />
        <Label>Search</Label>
      </NativeTabs.Trigger>
    </NativeTabs>
  )
}
