package org.bladerunnerjs.plugin.bundlers.cssresource;

import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.bladerunnerjs.api.Asset;
import org.bladerunnerjs.api.BRJS;
import org.bladerunnerjs.api.memoization.MemoizedFile;
import org.bladerunnerjs.api.plugin.AssetDiscoveryInitiator;
import org.bladerunnerjs.api.plugin.base.AbstractAssetPlugin;
import org.bladerunnerjs.model.AssetContainer;
import org.bladerunnerjs.model.FileAsset;

public class CssResourceAssetPlugin extends AbstractAssetPlugin {
	
	FileFilter assetFileFilter = new SuffixFileFilter( Arrays.asList(".jpg",".jpeg",".bmp",".png",".gif",".svg",".ico",".cur",".eot",".ttf",".woff") );
	
	@Override
	public void setBRJS(BRJS brjs) {
		// do nothing
	}

	@Override
	public List<Asset> discoverAssets(AssetContainer assetContainer, MemoizedFile dir, String requirePrefix, List<Asset> implicitDependencies, AssetDiscoveryInitiator assetDiscoveryInitiator)
	{
		List<Asset> assets = new ArrayList<>();
		for (MemoizedFile assetFile : dir.listFiles(assetFileFilter)) {
			Asset asset = new FileAsset(assetFile, assetContainer, "css_resource"+"!"+requirePrefix);
			assets.add(asset);
			if (!assetDiscoveryInitiator.hasRegisteredAsset(asset.getPrimaryRequirePath())) {
				assetDiscoveryInitiator.registerAsset( asset );
			}
		}
		return assets;
	}
	
}
