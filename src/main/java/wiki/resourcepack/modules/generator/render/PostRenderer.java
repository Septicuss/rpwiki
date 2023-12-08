package wiki.resourcepack.modules.generator.render;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import wiki.resourcepack.modules.generator.Groups;
import wiki.resourcepack.modules.generator.Templates;
import wiki.resourcepack.modules.generator.model.Post;
import wiki.resourcepack.modules.generator.model.breadcrumb.Breadcrumb;
import wiki.resourcepack.modules.generator.model.breadcrumb.Crumb;

public class PostRenderer implements Renderer<Post> {

	private Groups groups;

	protected PostRenderer(Groups groups) {
		this.groups = groups;
	}

	@Override
	public void render(Post object, Map<String, Object> context, Writer writer) {

		Map<String, Object> postContext = object.context();
		postContext.putAll(context);

		Breadcrumb breadcrumb = new Breadcrumb();

		String href = object.href();
		String[] routes = href.split("/");
		
		if (!href.isBlank()) {
			
			for (int i = 0; i < routes.length - 1; i++) {
				String route = routes[i];
				String title = route;
				
				if (route.isBlank())
					continue;
				
				var group = groups.getBySlug(route);
				
				if (group.isPresent())
					title = group.get().title();
				
				breadcrumb.add(new Crumb(title, "/" + route));
			}
			
			breadcrumb.add(new Crumb(object.title(), "/" + href));
		
		}
		
		boolean smallBreadcrumb = breadcrumb.crumbs().isEmpty() || breadcrumb.crumbs().size() <= 1;
		
		if (!smallBreadcrumb)
			postContext.put("breadcrumb", breadcrumb);

		postContext.put("post", object);

		try {
			Templates.POST_TEMPLATE.evaluate(writer, postContext);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
