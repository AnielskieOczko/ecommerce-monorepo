interface Review {
  id: string;
  rating: number;
  productName: string;
  comment: string;
  author: string;
}

const Reviews: React.FC = () => {
  return (
    <section className="py-16 bg-gray-50">
      <div className="container mx-auto px-4">
        <h2 className="text-3xl font-light text-center mb-4">Customer Reviews</h2>
        <p className="text-center text-gray-600 mb-12">
          Our customers rate us 4.8/5 based on 4320 reviews.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-8">
          {/* Review cards will be mapped here */}
          <ReviewCard />
          <ReviewCard />
          <ReviewCard />
          <ReviewCard />
          <ReviewCard />
        </div>
      </div>
    </section>
  );
};

const ReviewCard: React.FC = () => {
  return (
    <div className="text-center space-y-4">
      <div className="flex justify-center">
        {[1, 2, 3, 4, 5].map((star) => (
          <span key={star} className="text-yellow-400">★</span>
        ))}
      </div>
      <h3 className="font-medium">Product Name</h3>
      <p className="text-sm text-gray-600 line-clamp-3">
        Review comment goes here...
      </p>
      <p className="text-sm text-gray-500">John D.</p>
    </div>
  );
};

export default Reviews;