interface BlogPost {
  id: string;
  title: string;
  excerpt: string;
  image: string;
  date: string;
}

const BlogSection: React.FC = () => {
  return (
    <section className="py-16 bg-white">
      <div className="container mx-auto px-4">
        <h2 className="text-3xl font-light text-center mb-12">Ph. Doctor BLOG</h2>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* Blog cards will be mapped here */}
          <BlogCard />
          <BlogCard />
          <BlogCard />
        </div>

        <div className="text-center mt-12">
          <button className="bg-transparent border border-black px-6 py-2 hover:bg-black hover:text-white transition-colors">
            GO TO BLOG
          </button>
        </div>
      </div>
    </section>
  );
};

const BlogCard: React.FC = () => {
  return (
    <article className="group">
      <div className="aspect-[4/3] overflow-hidden">
        <img
          src="/blog-image.jpg"
          alt="Blog post"
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
        />
      </div>
      <div className="mt-4 space-y-2">
        <h3 className="text-xl font-light">Blog Post Title</h3>
        <p className="text-gray-600 line-clamp-2">
          Short excerpt from the blog post...
        </p>
        <p className="text-sm text-gray-500">31. July 2024</p>
      </div>
    </article>
  );
}; 

export default BlogSection;