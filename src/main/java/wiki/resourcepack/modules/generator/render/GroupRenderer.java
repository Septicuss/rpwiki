package wiki.resourcepack.modules.generator.render;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import wiki.resourcepack.modules.generator.Posts;
import wiki.resourcepack.modules.generator.Templates;
import wiki.resourcepack.modules.generator.model.Group;
import wiki.resourcepack.modules.generator.model.Post;
import wiki.resourcepack.modules.generator.model.Tag;
import wiki.resourcepack.utils.Utils;

public class GroupRenderer implements Renderer<Group> {

	private Posts posts;
	
	protected GroupRenderer(Posts posts) {
		this.posts = posts;
	}
	
	@Override
	public void render(Group object, Map<String, Object> context, Writer writer) {
		
		Map<String, Object> groupContext = new HashMap<>();
		groupContext.putAll(context);
		
		ArrayList<String> postJsons = constructPostJsons(object);
		ArrayList<String> tagJsons = constructTagJsons(object);
		
		groupContext.put("group", object);
		groupContext.put("posts", postJsons);
		groupContext.put("tags", tagJsons);
		
		try {
			Templates.GROUP_TEMPLATE.evaluate(writer, groupContext);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * { title: "Post Title", description: "Post Description", href: "post-slug", tags: ["Tag", "Tag"] },
	 */
	private ArrayList<String> constructPostJsons(Group group) {

		ArrayList<String> postJsons = new ArrayList<>();
		Set<Post> groupPosts = posts.postsByGroup(group.id());
		Set<Post> sortedPosts = new TreeSet<>(groupPosts);
		
		for (Post post : sortedPosts) {
			
			if (post.hidden())
				continue;
			
			JsonObject object = new JsonObject();
			
			object.addProperty("title", post.title());
			object.addProperty("description", post.description());
			object.addProperty("href", "/" + group.slug() + "/" + post.slug());
			
			JsonArray tagArray = new JsonArray();
			
			for (String tag : post.tags())
				tagArray.add(tag);
			
			object.add("tags", tagArray);
			
			String json = Utils.gson.toJson(object);
			postJsons.add(json + ",");
		}
		
		return postJsons;
		
	}
	
	/**
	 * tag: { title: "Tag Title", backgroundColor: "color", textColor: "color" },
	 */
	private ArrayList<String> constructTagJsons(Group group) {
		
		ArrayList<String> tagJsons = new ArrayList<>();
		
		for (Map.Entry<String, Tag> tags : group.tags().entrySet()) {
			
			String id = tags.getKey();
			Tag tag = tags.getValue();
			
			JsonObject object = new JsonObject();
			
			object.addProperty("title", tag.title());
			object.addProperty("backgroundColor", tag.backgroundColor());
			object.addProperty("textColor", tag.textColor());
			
			tagJsons.add(id + ": " + Utils.gson.toJson(object) + ",");
		}
		
		return tagJsons;
				
	}

}
