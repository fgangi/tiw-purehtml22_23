package it.polimi.tiw.playlist.utils;

import javax.servlet.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

public class TemplateHandler {
	
	public static TemplateEngine getTemplateEngine(ServletContext context) {
		TemplateEngine result = new TemplateEngine();
		ServletContextTemplateResolver template = new ServletContextTemplateResolver(context); 
		template.setTemplateMode(TemplateMode.HTML);
		result.setTemplateResolver(template);
		template.setSuffix(".html");
		return result;
	}
	
}