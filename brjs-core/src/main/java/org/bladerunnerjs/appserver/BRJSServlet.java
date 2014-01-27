package org.bladerunnerjs.appserver;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BladerunnerUri;
import org.bladerunnerjs.model.exception.request.BundlerProcessingException;
import org.bladerunnerjs.model.exception.request.MalformedRequestException;
import org.bladerunnerjs.model.exception.request.ResourceNotFoundException;
import org.eclipse.jetty.servlet.DefaultServlet;


public class BRJSServlet extends DefaultServlet
{
	private static final long serialVersionUID = 1964608537461568895L;
	
	private App app;
	private ServletContext servletContext;
	
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		
		servletContext = config.getServletContext();
		ServletModelAccessor.initializeModel(servletContext);
		
		try {
			BRJS brjs = ServletModelAccessor.aquireModel();
			app = brjs.locateAncestorNodeOfClass(new File(servletContext.getRealPath(".")), App.class);
		}
		finally {
			ServletModelAccessor.releaseModel();
		}
	}
	
	@Override
	public void destroy()
	{
		ServletModelAccessor.destroy();
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try {
			BRJS brjs = ServletModelAccessor.aquireModel();
			app.handleLogicalRequest(new BladerunnerUri(brjs, servletContext, request), response.getOutputStream());
		} catch (MalformedRequestException | ResourceNotFoundException | BundlerProcessingException e) {
			throw new ServletException(e);
		}
		finally {
			ServletModelAccessor.releaseModel();
		}
	}
}
