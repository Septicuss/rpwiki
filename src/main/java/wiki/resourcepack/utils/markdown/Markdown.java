package wiki.resourcepack.utils.markdown;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class Markdown {
	
	private static MutableDataSet option = new MutableDataSet();
	static {
		option.set(Parser.EXTENSIONS, Arrays.asList(AttributesExtension.create(), AnchorLinkExtension.create()));
	}
	private static Parser parser = Parser.builder(option).build();
	private static HtmlRenderer renderer = HtmlRenderer.builder(option).build();

	
	private Markdown() {
	}

	public static String markdownToHtml(String md) {
		return renderer.render(parser.parse(md));
	}

	public static String markdownToHtml(Reader reader) {
		try {
			return renderer.render(parser.parseReader(reader));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

}
