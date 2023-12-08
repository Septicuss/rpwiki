package wiki.resourcepack.modules.generator.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final record Post(
		String id, 
		String group, 
		String href, 
		String title, 
		String slug, 
		String description, 
		boolean hidden,
		List<String> tags, 
		Map<String, Object> context) 
implements Comparable<Post>, WebPage {

	public static Builder post(String id) {
		return new Builder(id);
	}

	public Map<String, Object> context() {
		return this.context;
	}
	
	public String href() {
		return this.href;
	}
	
	public String filePath() {
		return this.href + ".html";
	}

	@Override
	public int compareTo(Post o) {
		return this.title.compareTo(o.title());
	}
	
	public static class Builder {
		
		private String id;
		private String group;
		private String groupSlug;
		private String title;
		private String slug;
		private String description;
		private boolean hidden;
		private List<String> tags = new ArrayList<>();
		private Map<String, Object> context = new HashMap<>();
		
		private Builder(String id) {
			this.id = id;
		}
		
		public String id() {
			return this.id;
		}
		
		public Builder group(String group, String groupSlug) {
			this.group = group;
			this.groupSlug = groupSlug;
			return this;
		}
		
		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder slug(String slug) {
			this.slug = slug;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder tag(String tag) {
			this.tags.add(tag);
			return this;
		}

		public Builder tags(Collection<String> tags) {
			for (var tag : tags)
				this.tags.add(tag);
			return this;
		}
		
		public Builder context(String key, Object object) {
			this.context.put(key, object);
			return this;
		}
		
		public Builder hidden(boolean hidden) {
			this.hidden = hidden;
			return this;
		}
		
		
		public String href() {
			
			String href = groupSlug + "/" + slug;
			
			if (context.get("relocate") != null) {
				String relocation = (String) context.get("relocate");
				
				if (relocation.endsWith("/"))
					relocation = relocation.substring(0, relocation.length() - 1);
				
				href = relocation.isBlank() ? slug : relocation + "/" + slug;
			} 
			
			return href;
		}
		
		public Post build() {
			
			if (title == null)
				title = id;

			if (slug == null)
				slug = id;
			
			if (description == null)
				description = new String();
			
			String href = href();
			
			return new Post(id, group, href, title, slug, description, hidden, tags, context);
			
		}
		
	}
	
}