import axios from 'axios';

const BASE_URL = 'https://versly.mskelton.dev/api/';

export interface TranslationInfo {
  id: string;
  name: string;
  lastUpdated: string;
}

class VerslyService {
  async getTranslations(): Promise<TranslationInfo[]> {
    const response = await axios.get<TranslationInfo[]>(`${BASE_URL}translations`);
    return response.data;
  }

  async downloadTranslation(translation: string): Promise<string[]> {
    const response = await axios.get(`${BASE_URL}download/${translation}`, {
      responseType: 'text',
    });

    return response.data.split('\n').filter((line: string) => line.trim());
  }
}

export const api = new VerslyService();
