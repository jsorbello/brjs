package org.bladerunnerjs.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.InvalidNameException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.bladerunnerjs.core.plugin.bundler.BundlerPlugin;
import org.bladerunnerjs.model.engine.NamedNode;
import org.bladerunnerjs.model.engine.Node;
import org.bladerunnerjs.model.engine.NodeItem;
import org.bladerunnerjs.model.engine.NodeMap;
import org.bladerunnerjs.model.engine.RootNode;
import org.bladerunnerjs.model.exception.ConfigException;
import org.bladerunnerjs.model.exception.modelupdate.ModelUpdateException;
import org.bladerunnerjs.model.utility.NameValidator;


public class JsLib extends AbstractBRJSNode implements AssetContainer, NamedNode
{
	private final NodeItem<DirNode> src = new NodeItem<>(DirNode.class, "src");
	private final NodeItem<DirNode> resources = new NodeItem<>(DirNode.class, "resources");
	private String name;
	private JsLibConf libConf;
	private final AssetContainerResources assetContainerResources;
	
	final Map<File, AssetLocation> assetLocations = new HashMap<>();
	
	public JsLib(RootNode rootNode, Node parent, File dir, String name)
	{
		this.name = name;
		init(rootNode, parent, dir);
		
		assetContainerResources = new AssetContainerResources(this, src().dir(), resources().dir());
	}
	
	public JsLib(RootNode rootNode, Node parent, File dir)
	{
		// TODO: can we avoid having to have a null name for a NamedNode that is available as a single item through the model
		this(rootNode, parent, dir, null);
	}
	
	public static NodeItem<JsLib> createSdkNodeItem()
	{
		return new NodeItem<>(JsLib.class, "sdk/libs/javascript/caplin");
	}
	
	public static NodeMap<JsLib> createAppNodeSet()
	{
		NodeMap<JsLib> appNodeSet = new NodeMap<>(JsLib.class, "libs", null);
		appNodeSet.addAdditionalNamedLocation("caplin", "../../sdk/libs/javascript/caplin");
		
		return appNodeSet;
	}
	
	@Override
	public App getApp() {
		return (App) parent;
	}
	
	@Override
	public void addTemplateTransformations(Map<String, String> transformations) throws ModelUpdateException
	{
		try {
			transformations.put("libns", libConf().getLibNamespace());
		}
		catch(ConfigException e) {
			throw new ModelUpdateException(e);
		}
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public boolean isValidName()
	{
		return NameValidator.isValidDirectoryName(name);
	}
	
	@Override
	public void assertValidName() throws InvalidNameException
	{
		NameValidator.assertValidDirectoryName(this);
	}
	
	public App parentApp()
	{
		return (App) parent;
	}
	
	public DirNode src()
	{
		return item(src);
	}
	
	public DirNode resources()
	{
		return item(resources);
	}
	
	public JsLibConf libConf() throws ConfigException
	{
		if(libConf == null) {
			libConf = new JsLibConf(this);
		}
		
		return libConf ;
	}
	
	public void populate(String libNamespace) throws InvalidNameException, ModelUpdateException
	{
		NameValidator.assertValidRootPackageName(this, libNamespace);
		
		try {
			libConf().setLibNamespace(libNamespace);
			populate();
			libConf().write();
		}
		catch (ConfigException e) {
			if(e.getCause() instanceof InvalidNameException) {
				throw (InvalidNameException) e.getCause();
			}
			else {
				throw new ModelUpdateException(e);
			}
		}
	}
	
	@Override
	public String getRequirePrefix() {
		return "/" + getName();
	}
	
	@Override
	public List<SourceFile> sourceFiles() {
		List<SourceFile> sourceFiles = new LinkedList<SourceFile>();
			
		for(BundlerPlugin bundlerPlugin : ((BRJS) rootNode).bundlerPlugins()) {
			for (AssetLocation assetLocation : getAllAssetLocations())
			{
				sourceFiles.addAll(bundlerPlugin.getAssetFileAccessor().getSourceFiles(assetLocation));
			}
		}
		
		return sourceFiles;
	}
	
	
	@Override
	public SourceFile sourceFile(String requirePath) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<AssetLocation> getAllAssetLocations() {
		List<AssetLocation> allAssetLocations = new ArrayList<AssetLocation>();
		allAssetLocations.add( getAssetLocation(resources().dir(), false) );
		
		File srcDir = src().dir();
		if (srcDir.isDirectory())
		{
    		Iterator<File> fileIterator = FileUtils.iterateFilesAndDirs(srcDir, FalseFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    		while (fileIterator.hasNext())
    		{
    			File dir = fileIterator.next();
    			if (!dir.equals(dir()))
    			{
    				allAssetLocations.add( getAssetLocation(dir) );
    			}
    		}
		}
		return allAssetLocations;
	}
	
	@Override
	public AssetLocation getAssetLocation(File dir) {
		return getAssetLocation(dir, true);
	}
	
	
	
	
	public AssetLocation getAssetLocation(File dir, boolean createShallowAssetLocation) 
	{
		AssetLocation assetLocation = assetLocations.get(dir);
		if (assetLocation == null) {
			assetLocation = (createShallowAssetLocation) ? new ShallowAssetLocation(this, dir) : new DeepAssetLocation(this, dir);
			assetLocations.put(dir, assetLocation);
		}
		return assetLocation;
		
	}
	
}
