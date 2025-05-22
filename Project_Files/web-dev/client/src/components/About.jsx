const contributors = [
  {
    name: "Atishay Gangwal",
    linkedin: "https://www.linkedin.com/in/atishay-gangwal-238137168/",
  },
  { name: "Aadarsh", linkedin: "https://www.linkedin.com/in/aadarshg087/" },
  {
    name: "Mayank Agarwal",
    linkedin: "https://www.linkedin.com/in/mayank1008agarwal/",
  },
  { name: "Pritpal Singh", linkedin: "https://linkedin.com/in/alicejohnson" },
];

export default function About() {
  return (
    <footer className="bg-gray-900 text-white py-6">
      <div className="container mx-auto px-6">
        <h2 className="text-lg font-semibold text-center mb-4">Contributors</h2>

        <div className="flex flex-wrap justify-center gap-6">
          {contributors.map((contributor, index) => (
            <div key={index} className="text-center">
              <p className="font-medium">{contributor.name}</p>
              <a
                href={contributor.linkedin}
                target="_blank"
                rel="noopener noreferrer"
                className="text-blue-400 hover:text-blue-500"
              >
                LinkedIn
              </a>
            </div>
          ))}
        </div>

        <p className="text-center text-gray-400 mt-4">
          © {new Date().getFullYear()} All rights reserved.
        </p>
      </div>
    </footer>
  );
}
