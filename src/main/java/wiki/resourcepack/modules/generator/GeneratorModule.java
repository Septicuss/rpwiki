package wiki.resourcepack.modules.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import dev.dejvokep.boostedyaml.YamlDocument;
import wiki.resourcepack.Bootstrap;
import wiki.resourcepack.modules.WikiModule;
import wiki.resourcepack.modules.generator.model.ErrorPage;
import wiki.resourcepack.modules.generator.model.WebPage;
import wiki.resourcepack.modules.generator.model.sidebar.Sidebar;
import wiki.resourcepack.modules.generator.render.ErrorRenderer;
import wiki.resourcepack.modules.generator.render.GroupRenderer;
import wiki.resourcepack.modules.generator.render.PostRenderer;
import wiki.resourcepack.modules.generator.render.Renderer;
import wiki.resourcepack.utils.FileUtils;

/**
 * Module responsible for generating the static wiki website and all of its
 * pages
 */
public class GeneratorModule implements WikiModule {

	private static final String DEFAULT_DESTINATION_FOLDER = "generated";
	private static final String DEFAULT_SOURCE_FOLDER = "source";
	private static final String PUBLIC_PATH = "public";
	private static final String ASSETS_PATH = "content/assets";

	private YamlDocument contentConfig;
	private File destinationFolder;
	private File sourceFolder;
	private Logger logger;

	@Override
	public void run() {

		var mainframe = Bootstrap.mainframe();
		this.logger = mainframe.logger("Generator");

		/**
		 * Reading & setting required folders & configs
		 */

		var config = mainframe.config();
		var generatorSection = config.getSection("generator");

		var destinationPath = generatorSection.getString("destination", DEFAULT_DESTINATION_FOLDER);
		var sourcePath = generatorSection.getString("source", DEFAULT_SOURCE_FOLDER);

		this.destinationFolder = new File(destinationPath);
		this.sourceFolder = new File(sourcePath);

		if (!sourceFolder.exists()) {
			logger.error("Missing source directory at:");
			logger.error(sourceFolder.getAbsolutePath());
			return;
		}

		if (!destinationFolder.exists())
			destinationFolder.mkdirs();

		var contentConfigFile = new File(sourceFolder, "content/config.yml");

		if (!contentConfigFile.exists()) {
			logger.error("Missing config.yml in the content directory:");
			logger.error(contentConfigFile.getAbsolutePath());
			return;
		}

		try {
			this.contentConfig = YamlDocument.create(contentConfigFile);
		} catch (IOException e) {
			logger.error("Failed to load content config.yml:");
			e.printStackTrace();
			return;
		}

		/**
		 * Log information
		 */

		logger.info("Generator setup successful:");
		logger.info("> Source:");
		logger.info("     " + sourceFolder.getAbsolutePath());
		logger.info("> Destination: ");
		logger.info("     " + destinationFolder.getAbsolutePath());

	}

	@Override
	public void stop() {

		logger.info("Generator finished.");

	}

	public void generate() {
		
		long start = System.currentTimeMillis();
		logger.info("Generating...");

		Templates.load(Bootstrap.mainframe().config());

		setupDestination();
		
		File contentFolder = new File(sourceFolder, "content");

		Groups groups = Groups.load(contentConfig);
		Posts posts = Posts.load(groups, contentFolder);
		Sidebar sidebar = Sidebar.load(contentConfig, groups, posts);
		Set<ErrorPage> errors = Set.of(
				new ErrorPage(404, "Not Found", "Try choosing another page on the sidebar")
		);
		
		copyPublicFiles();
		copyContentAssets();

		Map<String, Object> context = new HashMap<>();
		context.put("links", sidebar.links());
		
		GroupRenderer groupRenderer = Renderer.group(posts);
		PostRenderer postRenderer = Renderer.post(groups);
		ErrorRenderer errorRenderer = Renderer.error();
		
		render(groupRenderer, context, groups.groups());
		render(postRenderer, context, posts.posts());
		render(errorRenderer, context, errors);
		
		logger.info("Generation successful, took " + (System.currentTimeMillis() - start) + "ms");
		
	}
	
	private void setupDestination() {
		FileUtils.clearDirectory(destinationFolder.toPath());
		destinationFolder.mkdirs();
	}
	
	private <T extends WebPage, G extends Renderer<T>> void render(G renderer, Map<String, Object> context, Collection<T> pages) {
		
		for (T page : pages) {
			
			File pageFile = new File(destinationFolder, page.filePath());
			FileUtils.create(pageFile);
			
			try (FileWriter writer = new FileWriter(pageFile)) {
				renderer.render(page, context, writer);
			} catch (IOException e) {
				continue;
			}
			
		}
		
	}

	private void copyPublicFiles() {
		File publicFolder = new File(sourceFolder, PUBLIC_PATH);
		
		if (!publicFolder.exists())
			return;

		FileUtils.copyFilesFrom(publicFolder, destinationFolder);
	}

	private void copyContentAssets() {
		File assetsFolder = new File(sourceFolder, ASSETS_PATH);

		if (!assetsFolder.exists())
			return;

		FileUtils.copyFilesFrom(assetsFolder, destinationFolder);
	}

}
