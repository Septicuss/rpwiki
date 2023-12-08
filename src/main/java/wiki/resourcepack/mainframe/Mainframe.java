package wiki.resourcepack.mainframe;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;

import com.google.gson.Gson;

import dev.dejvokep.boostedyaml.YamlDocument;
import wiki.resourcepack.modules.WikiModule;

public interface Mainframe {
	
	static Mainframe create() {
		return new MainframeImpl();
	}
	
	public void runModule(String id);

	public void stopModule(String id);
	
	public Set<String> moduleIds();
	
	public Optional<WikiModule> module(String id);
	
	public void register(String id, WikiModule module);
	
	public YamlDocument config();
	
	public Logger logger(String id);
	
	public Gson gson();
	
}
