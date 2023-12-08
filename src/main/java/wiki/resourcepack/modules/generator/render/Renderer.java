package wiki.resourcepack.modules.generator.render;

import java.io.Writer;
import java.util.Map;

import wiki.resourcepack.modules.generator.Groups;
import wiki.resourcepack.modules.generator.Posts;

public interface Renderer<T> {

	public static GroupRenderer group(Posts posts) {
		return new GroupRenderer(posts);
	}
	
	public static PostRenderer post(Groups groups) {
		return new PostRenderer(groups);
	}
	
	public static ErrorRenderer error() {
		return new ErrorRenderer();
	}
	
	public void render(T object, Map<String, Object> context, Writer writer);
	
}
