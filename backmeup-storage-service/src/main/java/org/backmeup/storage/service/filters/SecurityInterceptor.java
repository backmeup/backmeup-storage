package org.backmeup.storage.service.filters;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.keyserver.model.KeyserverException;
import org.backmeup.keyserver.model.Token.Kind;
import org.backmeup.keyserver.model.dto.AuthResponseDTO;
import org.backmeup.keyserver.model.dto.TokenDTO;
import org.backmeup.storage.model.StorageUser;
import org.backmeup.storage.service.auth.StorageSecurityContext;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class SecurityInterceptor implements ContainerRequestFilter {
    private static final ServerResponse ACCESS_FORBIDDEN = new ServerResponse("Access forbidden", 403, new Headers<>());
    private static final ServerResponse ACCESS_DENIED = new ServerResponse("Access denied for this resource", 401, new Headers<>());
    private static final String AUTHORIZATION_PROPERTY = "Authorization";

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityInterceptor.class);

    @Inject
    private KeyserverClient keyserverClient;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) requestContext
                .getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
        Method method = methodInvoker.getMethod();

        if (!method.isAnnotationPresent(PermitAll.class)) {
            if (method.isAnnotationPresent(DenyAll.class)) {
                requestContext.abortWith(ACCESS_FORBIDDEN);
                return;
            }

            // Get authorization header
            final MultivaluedMap<String, String> headers = requestContext.getHeaders();
            final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

            // If no authorization header, deny access
            if (authorization == null || authorization.isEmpty()) {
                requestContext.abortWith(ACCESS_DENIED);
                return;
            }

            // Get token from header
            final String accessToken = authorization.get(0);

            // Verify token
            if (method.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAnnotation.value()));

                StorageUser user = resolveUser(accessToken);
                if (user == null) {
                    requestContext.abortWith(ACCESS_DENIED);
                    return;
                }

                if (!isUserAllowed(user, rolesSet)) {
                    requestContext.abortWith(ACCESS_DENIED);
                    return;
                }

                requestContext.setSecurityContext(new StorageSecurityContext(user));
            }
        }
    }

    private StorageUser resolveUser(final String accessToken) {
        try {
            TokenDTO token = new TokenDTO(Kind.INTERNAL, accessToken);
            AuthResponseDTO response = this.keyserverClient.authenticateWithInternalToken(token);
            String userId = response.getUsername();
            LOGGER.info("Resolved user with id: " + userId);

            return new StorageUser(Long.parseLong(userId), accessToken);
        } catch (KeyserverException ke) {
            LOGGER.info("", ke);
        }
        try {
            //try different way of parsing token
            AuthResponseDTO response = this.keyserverClient.authenticateWithInternalToken(TokenDTO.fromTokenString(accessToken));
            String userId = response.getUsername();
            LOGGER.info("Resolved user with id: " + userId);
            return new StorageUser(Long.parseLong(userId), accessToken);
        } catch (KeyserverException ke) {
            LOGGER.info("", ke);
        }
        return null;
    }

    private boolean isUserAllowed(final StorageUser user, final Set<String> rolesSet) {
        return true;
    }
}
