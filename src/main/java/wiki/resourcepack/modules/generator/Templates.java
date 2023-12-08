package wiki.resourcepack.modules.generator;

import java.util.Locale;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.FileLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;

public class Templates {

	public static PebbleEngine ENGINE = new PebbleEngine.Builder()
			.loader(new FileLoader())
			.defaultLocale(Locale.US)
			.build();

	public static PebbleTemplate POST_TEMPLATE;
	public static PebbleTemplate GROUP_TEMPLATE;
	public static PebbleTemplate ERROR_TEMPLATE;

	public static void load(Section config) {
		
		ENGINE = new PebbleEngine.Builder()
				.loader(new FileLoader())
				.defaultLocale(Locale.US)
				.build();
		
		String sourcePath = config.getString("generator.source");
		Section templatesSection = config.getSection("generator.templates");
		
		POST_TEMPLATE = ENGINE.getTemplate(getFullPath("post", sourcePath, templatesSection));
		GROUP_TEMPLATE = ENGINE.getTemplate(getFullPath("group", sourcePath, templatesSection));
		ERROR_TEMPLATE = ENGINE.getTemplate(getFullPath("error", sourcePath, templatesSection));
		
	}
	
	private static String getFullPath(String template, String sourcePath, Section templatesSection) {
		
		if (sourcePath.endsWith("/"))
			sourcePath = sourcePath.substring(0, sourcePath.length() - 1);
		
		return (sourcePath + "/" + templatesSection.getString(template));
		
	}
	

}
