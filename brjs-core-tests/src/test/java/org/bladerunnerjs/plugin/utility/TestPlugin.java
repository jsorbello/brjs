package org.bladerunnerjs.plugin.utility;

import java.util.ArrayList;
import java.util.List;

import org.bladerunnerjs.api.BRJS;
import org.bladerunnerjs.api.plugin.OrderedPlugin;
import org.bladerunnerjs.api.plugin.base.AbstractPlugin;

public class TestPlugin extends AbstractPlugin implements OrderedPlugin {
	protected List<String> pluginsThatMustAppearBeforeThisPlugin = new ArrayList<>();
	protected List<String> pluginsThatMustAppearAfterThisPlugin = new ArrayList<>();
	
	@Override
	public void setBRJS(BRJS brjs) {
		// do nothing
	}
	
	@Override
	public List<String> getPluginsThatMustAppearBeforeThisPlugin() {
		return pluginsThatMustAppearBeforeThisPlugin;
	}
	
	@Override
	public List<String> getPluginsThatMustAppearAfterThisPlugin() {
		return pluginsThatMustAppearAfterThisPlugin;
	}
}
