const Hero: React.FC = () => {
  return (
    <div className="relative h-[80vh] bg-gray-50">
      <div className="absolute inset-0 flex items-center justify-center text-center">
        <div className="max-w-3xl px-4">
          <h1 className="text-4xl md:text-6xl font-light mb-4">
            Już jest
            <span className="block text-5xl md:text-7xl mt-4">
              Twoj nowy produkt
            </span>
            <span className="block text-3xl md:text-5xl mt-4">
              Vodka Bols vol. 40
            </span>
          </h1>
        </div>
      </div>
    </div>
  );
};

export default Hero; 