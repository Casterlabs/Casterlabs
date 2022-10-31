import adapter from '@sveltejs/adapter-static';

/** @type {import('@sveltejs/kit').Config} */
const config = {
    kit: {
        trailingSlash: 'never',
        prerender: { default: true },
        adapter: adapter({
            fallback: '__fallback.html'
        }),
        files: {
            lib: 'src/lib'
        }
    }
};

export default config;