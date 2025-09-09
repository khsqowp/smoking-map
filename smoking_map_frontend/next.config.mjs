// next.config.mjs

/** @type {import('next').NextConfig} */
const nextConfig = {
  rewrites() {
    // For local development, INTERNAL_API_URL might not be set.
    // Fallback to the local backend server address.
    const internalApiUrl = process.env.INTERNAL_API_URL || 'http://localhost:8080';

    return [
        {
            source: "/api/:path*",
            destination: `${internalApiUrl}/api/:path*`,
        },
        {
            source: "/oauth2/:path*",
            destination: `${internalApiUrl}/oauth2/:path*`,
        },
        {
            source: "/logout",
            destination: `${internalApiUrl}/logout`
        }
    ];
  },

  headers() {
    return [
      {
        source: '/:path*',
        headers: [
          // Content-Security-Policy is temporarily removed for debugging.
          {
            key: 'Strict-Transport-Security',
            value: 'max-age=63072000; includeSubDomains; preload'
          },
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff'
          },
          {
            key: 'X-Frame-Options',
            value: 'DENY'
          },
          {
            key: 'Referrer-Policy',
            value: 'origin-when-cross-origin'
          }
        ],
      },
    ];
  },
};

export default nextConfig;
