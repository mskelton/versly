/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./App.{js,jsx,ts,tsx}",
    "./src/**/*.{js,jsx,ts,tsx}"
  ],
  theme: {
    extend: {
      colors: {
        wordsOfJesus: {
          light: '#FF6467',
          dark: '#C10007',
        },
      },
      fontFamily: {
        rubik: ['Rubik'],
      },
    },
  },
  plugins: [],
}

