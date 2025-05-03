/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: 'rgb(var(--color-primary))',
        secondary: 'rgb(var(--color-secondary))',
        text: {
          primary: 'rgb(var(--color-text-primary))',
          secondary: 'rgb(var(--color-text-secondary))',
          disabled: 'rgb(var(--color-text-disabled))',
        },
        bg: {
          primary: 'rgb(var(--color-bg-primary))',
          secondary: 'rgb(var(--color-bg-secondary))',
          tertiary: 'rgb(var(--color-bg-tertiary))',
        },
        status: {
          success: 'rgb(var(--color-success))',
          error: 'rgb(var(--color-error))',
          warning: 'rgb(var(--color-warning))',
          info: 'rgb(var(--color-info))',
        },
      },
      animation: {
        'fade-in': 'fadeIn 0.2s ease-in-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0', transform: 'translateY(-10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
    },
  },
  plugins: [],
};
