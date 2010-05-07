/**
 * Copyright (c) 2009 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.fedoraproject.candlepin.auth.interceptor;

import java.util.HashSet;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.fedoraproject.candlepin.auth.Principal;
import org.fedoraproject.candlepin.auth.Role;
import org.fedoraproject.candlepin.exceptions.ForbiddenException;
import org.xnap.commons.i18n.I18n;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Interceptor for enforcing role based access to REST API methods.
 * 
 * This interceptor deals with coarse grained access, it only answers the question of
 * can a principal with these roles access this method. It does not support any paramaters
 * such as verifying the call is being made on a visible consumer/owner, this is handled
 * instead by the filtering mechanism.
 */
public class SecurityInterceptor implements MethodInterceptor {

    @Inject private Provider<Principal> principalProvider;
    @Inject private Provider<I18n> i18nProvider;
    
    private static Logger log = Logger.getLogger(SecurityInterceptor.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Principal currentUser = this.principalProvider.get();
        log.debug("Invoked.");
        
        Set<Role> allowedRoles = new HashSet<Role>();
        
        // Super admins can access any URL:
        allowedRoles.add(Role.SUPER_ADMIN);
        log.debug(invocation.getClass().getName());
        log.debug(invocation.getClass().getAnnotations().length);
        
        
        AllowRoles annotation = invocation.getMethod().getAnnotation(AllowRoles.class);
        log.debug("Method annotation: " + annotation);
        if (annotation != null) {
            for (Role allowed : annotation.roles()) {
                log.debug("   allowing role: " + allowed);
                allowedRoles.add(allowed);
            }
        }
        
        boolean foundRole = false;
        for (Role allowed : allowedRoles) {
            if (currentUser.hasRole(allowed)) {
                foundRole = true;
                if (log.isDebugEnabled()) {
                    log.debug("Granting access for " + currentUser + " due to role: " + 
                        allowed);
                }
                break;
            }
        }
        
        if (!foundRole) {
            log.warn("Refusing principal: " + currentUser + " access to: " + 
                invocation.getMethod().getName());
            I18n i18n = this.i18nProvider.get();
            String error = "Insufficient permission";
            throw new ForbiddenException(i18n.tr(error));
        }
        
        return invocation.proceed();
    }
    
}