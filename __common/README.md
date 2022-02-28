# __common

Where the "shared" resources lie.


## The static nonsense

Due to "issues" (namely, sveltekit), there's zero actual way of including *additional* static resources in your project.
So \_\_common/static is just copy pasted between the projects as needed to keep them up to date. Ugh.


## Ok, but what is __common.mjs?

This is a script that gets imported in the root \_\_layout.svelte files to handle themeing.  
Consisiency is key. *Chef's kiss*
