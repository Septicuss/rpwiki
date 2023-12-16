A static site generator software written in Java for a wiki.
[Live demo can be seen here](https://picpop-black-fog-1159.fly.dev/), hosted on [fly.io](https://fly.io/)

1) updates content based on commits to a [content repository](https://github.com/minepack/resourcepack-wiki)
2) generates the HTML content of the page
3) serves the static assets using a Javalin server

All of the above are split into their own modules which interact when needed.
More of a proof of concept, and first time writing a full website with Java.
