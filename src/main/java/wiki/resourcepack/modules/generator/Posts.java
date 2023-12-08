package wiki.resourcepack.modules.generator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import dev.dejvokep.boostedyaml.YamlDocument;
import wiki.resourcepack.Bootstrap;
import wiki.resourcepack.modules.generator.model.Post;
import wiki.resourcepack.modules.generator.model.Version;
import wiki.resourcepack.utils.FileUtils;
import wiki.resourcepack.utils.markdown.Markdown;

public class Posts {

	private Set<Post> posts;
	
	private Posts() {
		this(new HashSet<>());
	}
	
	private Posts(Set<Post> posts) {
		this.posts = posts;
	}
	
	public static Posts load(Groups groups, File contentFolder) {
		
		var sourcePath = Bootstrap.mainframe().config().getString("generator.source");
		Set<Post> posts = new HashSet<>();
		
		for (var group : groups.groups()) {
			
			File groupFolder = new File(contentFolder, group.id());
			
			if (!groupFolder.exists())
				continue;
			
			List<File> files = FileUtils.filesInDirectory(groupFolder);
			
			for (File postFile : files) {
				
				String fileName = postFile.getName();
				String id = fileName.substring(0, fileName.indexOf(".md"));
				
				var postBuilder = Post.post(id)
										.group(group.id(), group.slug());
				
				String markdown = FileUtils.readFile(postFile);
				
				if (markdown == null)
					markdown = "";
				
				parsePostsMarkdown(postBuilder, markdown);
				
				String path = postFile.getPath().replace(File.separator, "/");
				String githubPath = "https://github.com/minepack/resourcepack-wiki/blob/main" + path.replaceFirst(sourcePath, "");
				postBuilder.context("github", githubPath);
				
				posts.add(postBuilder.build());
				
			}
		}
		
		return new Posts(posts);
	}
	
	public Optional<Post> get(String id) {
		return posts.stream().filter(post -> post.id().equals(id)).findFirst();
	}
	
	public Set<Post> posts() {
		return Collections.unmodifiableSet(posts);
	}
	
	public Set<Post> postsByGroup(String group) {
		return postsFiltered(post -> post.group().equals(group));
	}

	public Set<Post> postsFiltered(Predicate<Post> filter) {
		return posts.stream().filter(filter).collect(Collectors.toUnmodifiableSet());
	}
	
	private static void parsePostsMarkdown(Post.Builder postBuilder, String markdown) {
		
		markdown = replaceExternal(markdown);
		markdown = escapeUnicode(markdown);
		markdown = convertCodeBlocks(markdown);
		
		Map<String, ArrayList<Version>> sections = extractSections(markdown);
		ArrayList<String> versions = new ArrayList<>();
		sections.values().forEach(list -> {
			list.forEach(version -> {
				if (!versions.contains(version.name()))
					versions.add(version.name());
			});
		});
		
		markdown = replaceWithCounter(markdown);

		// Defaults
		final String postId = postBuilder.id();
		
		postBuilder
			.slug(postId)
			.title(postId)
			.description("");

		YamlDocument frontMatter = readFrontMatter(markdown);
		
		if (frontMatter != null)
			postBuilder
				.slug(frontMatter.getString("slug", postId))
				.title(frontMatter.getString("title", postId))
				.description(frontMatter.getString("description", ""))
				.context("relocate", frontMatter.getString("relocate", null))
				.hidden(frontMatter.getBoolean("hidden", false))
				.tags(frontMatter.getStringList("tags", new ArrayList<>()));
		
		markdown = removeFrontMatter(markdown);
		
		String html = Markdown.markdownToHtml(markdown);

		postBuilder
			.context("content", html)
			.context("versions", versions)
			.context("sections", sections);
		
	}
	
	private static String replaceExternal(String markdown) {
		return markdown.replace("{external}", "{target=\"_blank\"}").replace("{ext}", "{target=\"_blank\"}");
	}
	
	/**
	 * Convert all 
	 * 
	 * ```LANG 
	 * Code
	 * ```
	 * 
	 * in the markdown into
	 * 
	 * <pre>
	 * <code class="language-LANG">
	 * Code
	 * </code>
	 * </pre>
	 * 
	 * (Prism.js supported format)
	 * 
	 */
	private static String convertCodeBlocks(String markdown) {
		String regex = "```(\\w+)([\\s\\S]*?)```";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(markdown);
		StringBuilder result = new StringBuilder();

		while (matcher.find()) {
			String language = matcher.group(1);
			String codeBlockContent = matcher.group(2);

			// Escape < and & inside <code> elements
			codeBlockContent = codeBlockContent.replace("&", "&amp;").replace("<", "&lt;");

			// Double-escape Unicode characters
//			codeBlockContent = codeBlockContent.replaceAll("\\\\u", "\\\\\\\\u");

			// Replace with <pre><code class="language-xxxx">...</code></pre> without
			// newlines and indentation
			String replacement = "<pre><code class=\"language-" + language + "\">" + codeBlockContent.trim()
					+ "</code></pre>";

			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
		}

		// Append the rest of the input
		matcher.appendTail(result);

		return result.toString();
	}
	
	private static String escapeUnicode(String markdown) {
	    return markdown.replace("\\uF", "\\\\uF");
	}

	
	private static Map<String, ArrayList<Version>> extractSections(String md) {
		Map<String, ArrayList<Version>> sections = new LinkedHashMap<>();

		Pattern versionPattern = Pattern.compile("<versions>(.*?)</versions>", Pattern.DOTALL);
		Pattern versionInfoPattern = Pattern.compile("---\\s*([^\\n]+?)\\s*\n(.*?)(?=(\\n---|$))", Pattern.DOTALL);

		Matcher versionMatcher = versionPattern.matcher(md);

		int index = 1;

		while (versionMatcher.find()) {
			String key = "section_" + index;
			ArrayList<Version> versions = new ArrayList<>();
			String versionContent = versionMatcher.group(1);

			Matcher versionInfoMatcher = versionInfoPattern.matcher(versionContent);

			while (versionInfoMatcher.find()) {
				String versionName = versionInfoMatcher.group(1);
				String content = versionInfoMatcher.group(2);

				versions.add(new Version(versionName, Markdown.markdownToHtml(content.trim())));
			}

			sections.put(key, versions);
			index += 1;
		}

		return sections;
	}
	
	/**
	 * Replaces all
	 * <versions>
	 * 		[VARIOUS VERSIONS]
	 * </version>
	 * 
	 * with
	 * <div id="section_x"></div>
	 * 
	 * for later dynamic replacement in JavaScript
	 * 
	 */
	private static String replaceWithCounter(String input) {
		Pattern versionsPattern = Pattern.compile("<versions>(.*?)</versions>", Pattern.DOTALL);
		Matcher matcher = versionsPattern.matcher(input);

		StringBuilder resultBuilder = new StringBuilder();
		int counter = 1;

		while (matcher.find()) {
			String replacement = "<div id=\"section_" + counter + "\"></div>";
			matcher.appendReplacement(resultBuilder, replacement);
			counter++;
		}

		matcher.appendTail(resultBuilder);

		return resultBuilder.toString();
	}
	
	private static YamlDocument readFrontMatter(String md) {
		if (md.startsWith("---")) {
			int nextSeparatorIndex = md.indexOf("---", 3);

			final var yamlString = md.substring(3, nextSeparatorIndex);
			try {
				return loadYamlFromString(yamlString);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return null;
	}
	
	private static YamlDocument loadYamlFromString(String yamlString) throws IOException {
		return YamlDocument.create(new ByteArrayInputStream(yamlString.getBytes()));
	}
	
	private static String removeFrontMatter(String md) {
		Pattern versionPattern = Pattern.compile("---(.*?)---", Pattern.DOTALL);
		Matcher matcher = versionPattern.matcher(md);

		if (matcher.find()) {
			return md.substring(matcher.end()).trim();
		}

		return md;
	}
	
	
}
