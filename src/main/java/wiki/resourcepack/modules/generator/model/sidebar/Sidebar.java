package wiki.resourcepack.modules.generator.model.sidebar;

import java.util.ArrayList;
import java.util.List;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import wiki.resourcepack.modules.generator.Groups;
import wiki.resourcepack.modules.generator.Posts;
import wiki.resourcepack.modules.generator.model.Group;
import wiki.resourcepack.modules.generator.model.Post;

public final record Sidebar(List<String> featuredGroups, List<String> featuredPosts, List<Link> links) {
	
	public static Sidebar load(Section config, Groups groups, Posts posts) {
		
		var sidebarSection = config.getSection("sidebar");
		
		List<String> featuredGroups = new ArrayList<>();
		List<String> featuredPosts = new ArrayList<>();
		List<Link> links = new ArrayList<>();
		
		var featuredSection = sidebarSection.getSection("featured");
		
		for (var featuredGroup : featuredSection.getRoutesAsStrings(false)) {
			featuredGroups.add(featuredGroup);
			
			featuredSection.getStringList(featuredGroup).forEach(postId -> {
				featuredPosts.add(postId);
			});
		}
		
		for (Group group : groups.groups()) {
			
			if (!featuredGroups.contains(group.id()))
				continue;
			
			// title: Group Title
			// href: /group-slug
			links.add(new Link(group.title(), "/" + group.slug(), LinkType.GROUP));
			
			for (String featuredPost : featuredPosts) {
				for (Post post : posts.posts()) {
					if (!post.id().equals(featuredPost))
						continue;

					if (!post.group().equals(group.id()))
						continue;
					
					// title: Post Title
					// href: /group-slug/post-slug
					links.add(new Link(post.title(), "/" + group.slug() + "/" + post.slug(), LinkType.LINK));
				}
			}
			
			
		}
		
		return new Sidebar(featuredGroups, featuredPosts, links);
		
	}

}
