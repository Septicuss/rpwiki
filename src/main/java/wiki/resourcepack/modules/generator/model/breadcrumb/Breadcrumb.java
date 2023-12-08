package wiki.resourcepack.modules.generator.model.breadcrumb;

import java.util.LinkedHashSet;

public class Breadcrumb {
	
	private LinkedHashSet<Crumb> crumbs = new LinkedHashSet<>();
	
	public LinkedHashSet<Crumb> crumbs() {
		return crumbs;
	}
	
	public void add(Crumb crumb) {
		this.crumbs.add(crumb);
	}
	
}
