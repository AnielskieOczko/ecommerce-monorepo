import React from 'react';

const GlobalLoader: React.FC = () => {
  return (
    <div 
      id="global-loader" 
      className="fixed top-0 left-0 w-full z-50"
      style={{ display: 'none' }}
    >
      {/* Progress bar */}
      <div className="h-1 bg-blue-500">
        <div 
          className="h-full bg-blue-600 animate-[loading_2s_ease-in-out_infinite]"
        />
      </div>
    </div>
  );
};

// Add this animation to your tailwind.config.js
const tailwindConfig = {
  theme: {
    extend: {
      keyframes: {
        loading: {
          '0%': { width: '0%' },
          '50%': { width: '70%' },
          '100%': { width: '100%' }
        }
      }
    }
  }
};

export default GlobalLoader;