package org.fastutil.mainland.util;

import javax.servlet.*;
import java.io.IOException;

/**
 * 
 * @author fastutil
 *
 */
public class CharacterEncodingFilter implements Filter {

	private String encoding = "UTF-8";

	private boolean ignore = true;

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (ignore || (request.getCharacterEncoding() == null)) {
			request.setCharacterEncoding(encoding);
			response.setCharacterEncoding(encoding);
		}
		response.setContentType("text/html;charset=UTF-8");
		chain.doFilter(request, response);
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		encoding = filterConfig.getInitParameter("encoding");
		if (encoding == null) {
			encoding = "UTF-8";
		}

		String value = filterConfig.getInitParameter("ignore");
        ignore = value == null || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("ok");
	}
}
