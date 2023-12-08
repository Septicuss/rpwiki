package wiki.resourcepack.modules.generator;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import wiki.resourcepack.modules.generator.model.Group;
import wiki.resourcepack.modules.generator.model.Tag;

public class Groups {

	private static final String DEFAULT_TAG_COLOR = "#fffff";
	private final LinkedHashMap<String, Group> groups;
	
	private Groups() {
		this(new LinkedHashMap<>());
	}
	
	private Groups(LinkedHashMap<String, Group> groups) {
		this.groups = groups;
	}
	
	public static Groups load(Section config) {
		
		LinkedHashMap<String, Group> groups = new LinkedHashMap<>();
		var groupsSection = config.getSection("groups");
		
		for (var groupId : groupsSection.getRoutesAsStrings(false)) {
			var groupSection = groupsSection.getSection(groupId);
			
			Group.Builder groupBuilder = 
					Group.group(groupId)
						.title(groupSection.getString("title", groupId))
						.slug(groupSection.getString("slug", groupId));
			
			if (groupSection.contains("tags")) {
				var tagsSection = groupSection.getSection("tags");
				for (String tagId : tagsSection.getRoutesAsStrings(false)) {
					var tagSection = tagsSection.getSection(tagId);
					
					groupBuilder.tag(
						Tag.tag(tagId)
							.title(tagSection.getString("title", tagId))
							.backgroundColor(tagSection.getString("background", DEFAULT_TAG_COLOR))
							.textColor(tagSection.getString("text-color", DEFAULT_TAG_COLOR))
							.build()
					);
				}
			}
			
			Group group = groupBuilder.build();
			groups.put(group.id(), group);
		}
		
		return new Groups(groups);
		
	}
	
	public Optional<Group> get(String id) {
		return Optional.ofNullable(groups.get(id));
	}
	
	public Optional<Group> getBySlug(String slug) {
		for (Group group : groups.values())
			if (group.slug().equals(slug))
				return Optional.of(group);
		return Optional.empty();
	}
	
	public Collection<Group> groups() {
		return groups.values();
	}
	
}
