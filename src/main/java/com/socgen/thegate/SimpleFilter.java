package com.socgen.thegate;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Created by Abderrazak BOUADMA
 * on 27/12/2017.
 */
@Component
public class SimpleFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(SimpleFilter.class);

    @Autowired
    private RouteLocator routeLocator;

    @Override
    public String filterType() {
        return "route";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext cc = RequestContext.getCurrentContext();
        log.info("Request Path {}", cc.getRequest().getPathInfo());
        final String requestURI = cc.getRequest().getRequestURI();
        final HttpMethod httpMethod = HttpMethod.valueOf(cc.getRequest().getMethod());
        final Route matchingRoute = routeLocator.getMatchingRoute("/api");
        if (matchingRoute != null) {
            Swagger swagger = new SwaggerParser().read("swagger-location");
            if (swagger == null) {
                return routingError(cc);
            }
            if (invalidRequest(requestURI, httpMethod, swagger)) {
                return routingError(cc);
            }
        }
        return null;
    }

    private Object routingError(RequestContext requestContext) {
        log.error("Request {} cancelled by gateway", requestContext.getRequest().getRequestURI());
        requestContext.unset();
        requestContext.getResponse().setContentType(MediaType.TEXT_HTML_VALUE);
        requestContext.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
        requestContext.setSendZuulResponse(false);
        return null;
    }

    private boolean invalidRequest(String requestURI, HttpMethod httpMethod, Swagger swagger) {
        AtomicBoolean foundValidApiRoute = new AtomicBoolean(false);
        Set<String> apiPaths = swagger.getPaths().keySet();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        apiPaths.forEach(path -> {
            Path p = swagger.getPath(path);
            List<String> httpMethodNames = p.getOperationMap().keySet().stream().map(Enum::name).collect(Collectors.toList());
            if (pathMatcher.match(path, requestURI) && httpMethodNames.contains(httpMethod.name())) {
                foundValidApiRoute.set(true);
            }
        });
        return foundValidApiRoute.get();
    }
}
