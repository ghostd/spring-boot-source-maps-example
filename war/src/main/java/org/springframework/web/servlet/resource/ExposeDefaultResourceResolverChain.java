package org.springframework.web.servlet.resource;

import java.util.List;

/**
 * This class is a ugly hack to make {@link org.springframework.web.servlet.resource.DefaultResourceResolverChain}
 * public (which is in a package scope).
 * <p>(and i'm too lazy to copy/paste the original)</p>
 */
public class ExposeDefaultResourceResolverChain extends DefaultResourceResolverChain {
    public ExposeDefaultResourceResolverChain(List<? extends ResourceResolver> resolvers) {
        super(resolvers);
    }
}
