package wiki.resourcepack.modules.generator.model;

/**
 * Describes an error page
 */
public record ErrorPage(int id, String title, String content) implements WebPage {

	@Override
	public String href() {
		return String.valueOf(id);
	}

	@Override
	public String filePath() {
		return id + ".html";
	}
}
