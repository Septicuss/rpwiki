package wiki.resourcepack.mainframe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import wiki.resourcepack.modules.WikiModule;

public class MainframeImpl implements Mainframe {

	private ConcurrentHashMap<String, WikiModule> modules = new ConcurrentHashMap<>();
	private File configFile = new File("data/config.yml");

	private Map<String, Logger> loggers = new HashMap<>();
	private YamlDocument config;
	private Gson gson;

	@Override
	public void runModule(String id) {
		var optionalModule = module(id);
		optionalModule.ifPresent(WikiModule::run);
	}

	@Override
	public void stopModule(String id) {
		var optionalModule = module(id);
		optionalModule.ifPresent(WikiModule::stop);
	}

	@Override
	public Set<String> moduleIds() {
		return Collections.unmodifiableSet(modules.keySet());
	}

	@Override
	public Optional<WikiModule> module(String id) {
		return Optional.ofNullable(modules.get(id));
	}

	@Override
	public void register(String id, WikiModule module) {
		modules.put(id, module);
	}

	@Override
	public YamlDocument config() {
		if (config == null) {
			try {

				InputStream defaultConfigStream = MainframeImpl.class.getResourceAsStream("/config.yml");
				YamlDocument defaultConfig = YamlDocument.create(defaultConfigStream);
				config = YamlDocument.create(configFile, defaultConfigStream);
				
				// Copy missing routes if any
				for (Route route : defaultConfig.getRoutes(true)) {
					if (config.contains(route))
						continue;
					
					config.set(route, defaultConfig.get(route));
				}
				
				config.save();
				
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return config;
	}

	@Override
	public Logger logger(String id) {
		if (!loggers.containsKey(id))
			loggers.put(id, LoggerFactory.getLogger(id));
		return loggers.get(id);
	}

	@Override
	public Gson gson() {
		if (gson == null)
			gson = new GsonBuilder().create();
		return gson;
	}

}
