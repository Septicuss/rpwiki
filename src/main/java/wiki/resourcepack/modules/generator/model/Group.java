package wiki.resourcepack.modules.generator.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final record Group(String id, String title, String slug, Map<String, Tag> tags) implements WebPage {

	public static Builder group(String id) {
		return new Builder(id);
	}
	
	public boolean hasTags() {
		return !this.tags.isEmpty();
	}

	@Override
	public Map<String, Tag> tags() {
		return Collections.unmodifiableMap(this.tags);
	}

	@Override
	public String href() {
		return slug;
	}

	@Override
	public String filePath() {
		return slug + ".html";
	}
	
	public static class Builder {
		
		private String id;
		private String title;
		private String slug;
		private LinkedHashMap<String, Tag> tags = new LinkedHashMap<>();
		
		private Builder(String id) {
			this.id = id;
		}
		
		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder slug(String slug) {
			this.slug = slug;
			return this;
		}

		public Builder tag(Tag tag) {
			this.tags.put(tag.id(), tag);
			return this;
		}

		public Builder tags(Collection<Tag> tags) {
			for (var tag : tags)
				this.tags.put(tag.id(), tag);
			return this;
		}
		
		public Group build() {
			
			if (title == null)
				title = id;
			
			if (slug == null)
				slug = id;
			
			return new Group(id, title, slug, tags);
			
		}
		
		
	}
	
}
