package org.bladerunnerjs.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bladerunnerjs.model.engine.Node;
import org.bladerunnerjs.model.engine.RootNode;
import org.bladerunnerjs.plugin.AssetPlugin;

public class DeepAssetLocation extends AbstractShallowAssetLocation {
	public DeepAssetLocation(RootNode rootNode, Node parent, File dir, AssetLocation assetLocation) {
		super(rootNode, parent, dir, assetLocation);
		
		// TODO: understand why removing this line doesn't break any tests
		// TODO: we should never call registerInitializedNode() from a non-final class
		registerInitializedNode();
	}
	
	public DeepAssetLocation(RootNode rootNode, Node parent, File dir) {
		super(rootNode, parent, dir);
		
		registerInitializedNode();
	}
	
	@Override
	public <A extends Asset> A obtainAsset(Class<? extends A> assetClass, File dir, String assetName) throws AssetFileInstantationException {
		return assetLocationUtility.obtainAsset(assetClass, dir, assetName);
	}
	
	@Override
	public <A extends Asset> List<A> obtainMatchingAssets(AssetFilter assetFilter, Class<A> assetListClass, Class<? extends A> assetClass) throws AssetFileInstantationException {
		List<A> assets = new ArrayList<>();
		
		if (dir.isDirectory()) {
			addAllMatchingAssets(dir, assetFilter, assetClass, assets);
		}
		
		return assets;
	}
	
	private <A extends Asset> void addAllMatchingAssets(File dir, AssetFilter assetFilter, Class<? extends A> assetClass, List<A> assets) throws AssetFileInstantationException {
		addMatchingAssets(dir, assetFilter, assetClass, assets);
		
		for(File childDir : root().getFileInfo(dir).dirs()) {
			addAllMatchingAssets(childDir, assetFilter, assetClass, assets);
		}
	}
	
	
	private Map<String, Asset> cachedAssets = new HashMap<>();
	@Override
	public List<Asset> _getAssets(AssetPlugin assetPlugin) {
		FileInfo dirInfo = root().getFileInfo(dir());
		return(dirInfo.exists()) ? __getAssets(assetPlugin, this, dirInfo.nestedFiles(), cachedAssets) : new ArrayList<>();
	}

	@Override
	protected List<File> getCandidateFiles() {
		return dirInfo.nestedFiles();
	}
}