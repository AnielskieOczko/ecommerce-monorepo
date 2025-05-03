const Footer: React.FC = () => {
  return (
    <footer className="bg-black text-white py-16">
      <div className="container mx-auto px-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-12">
          {/* Shop Info */}
          <div>
            <h3 className="text-lg font-medium mb-4">SHOP</h3>
            <ul className="space-y-2">
              <li><a href="#" className="hover:text-gray-300">About Us</a></li>
              <li><a href="#" className="hover:text-gray-300">Contact</a></li>
              <li><a href="#" className="hover:text-gray-300">Knowledge Base</a></li>
              <li><a href="#" className="hover:text-gray-300">FAQ</a></li>
            </ul>
          </div>

          {/* Information */}
          <div>
            <h3 className="text-lg font-medium mb-4">INFORMATION</h3>
            <ul className="space-y-2">
              <li><a href="#" className="hover:text-gray-300">Terms & Conditions</a></li>
              <li><a href="#" className="hover:text-gray-300">Privacy Policy</a></li>
              <li><a href="#" className="hover:text-gray-300">Shipping & Payment</a></li>
              <li><a href="#" className="hover:text-gray-300">Returns</a></li>
            </ul>
          </div>

          {/* Newsletter */}
          <div>
            <h3 className="text-lg font-medium mb-4">NEWSLETTER</h3>
            <p className="mb-4">Subscribe to receive updates and special offers</p>
            <form className="space-y-4">
              <input
                type="email"
                placeholder="Your email"
                className="w-full p-2 bg-transparent border border-white"
              />
              <button className="w-full bg-white text-black py-2 hover:bg-gray-200 transition-colors">
                SUBSCRIBE
              </button>
            </form>
          </div>
        </div>

        <div className="mt-12 pt-8 border-t border-gray-800 text-center text-sm">
          <p>© 2024 Your Brand. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer; 