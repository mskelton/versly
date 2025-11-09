import AsyncStorage from '@react-native-async-storage/async-storage';
import { PassageId } from '../models/types';

const KEYS = {
  DESTINATION: 'selected_destination',
  BOOK: 'selected_book',
  CHAPTER: 'selected_chapter',
  TRANSLATION: 'selected_translation',
};

const DEFAULTS = {
  DESTINATION: 0,
  BOOK: 'JHN',
  CHAPTER: '1',
  TRANSLATION: 'ESV',
};

class AppPreferences {
  async getDestination(): Promise<number> {
    const value = await AsyncStorage.getItem(KEYS.DESTINATION);
    return value ? parseInt(value) : DEFAULTS.DESTINATION;
  }

  async setDestination(destination: number): Promise<void> {
    await AsyncStorage.setItem(KEYS.DESTINATION, destination.toString());
  }

  async getPassage(): Promise<PassageId> {
    const [book, chapter, translation] = await Promise.all([
      AsyncStorage.getItem(KEYS.BOOK),
      AsyncStorage.getItem(KEYS.CHAPTER),
      AsyncStorage.getItem(KEYS.TRANSLATION),
    ]);

    return {
      book: book || DEFAULTS.BOOK,
      chapter: chapter || DEFAULTS.CHAPTER,
      translation: translation || DEFAULTS.TRANSLATION,
    };
  }

  async setPassage(passageId: PassageId): Promise<void> {
    await Promise.all([
      AsyncStorage.setItem(KEYS.BOOK, passageId.book),
      AsyncStorage.setItem(KEYS.CHAPTER, passageId.chapter),
      AsyncStorage.setItem(KEYS.TRANSLATION, passageId.translation),
    ]);
  }
}

export const preferences = new AppPreferences();
