package wiki.resourcepack.modules.generator.model.sidebar;

public enum LinkType {
	LINK("link"),
	GROUP("group");
	
	private String slug;
	
	private LinkType(String slug) {
		this.slug = slug;
	}
	
	public String slug() {
		return slug;
	}
}
