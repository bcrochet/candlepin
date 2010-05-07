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
package org.fedoraproject.candlepin.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.fedoraproject.candlepin.auth.Role;
import org.fedoraproject.candlepin.auth.interceptor.AllowRoles;
import org.fedoraproject.candlepin.resource.AdminResource;
import org.fedoraproject.candlepin.resource.CertificateResource;
import org.fedoraproject.candlepin.resource.ConsumerResource;
import org.fedoraproject.candlepin.resource.ConsumerTypeResource;
import org.fedoraproject.candlepin.resource.EntitlementResource;
import org.fedoraproject.candlepin.resource.OwnerResource;
import org.fedoraproject.candlepin.resource.PoolResource;
import org.fedoraproject.candlepin.resource.ProductResource;
import org.fedoraproject.candlepin.resource.RulesResource;
import org.fedoraproject.candlepin.resource.StatusResource;
import org.fedoraproject.candlepin.resource.SubscriptionResource;
import org.fedoraproject.candlepin.resource.SubscriptionTokenResource;
import org.junit.Test;

/**
 * ApiCrawler
 */
public class ApiCrawlerTest {
    private List<Class> resourceClasses;
    
    public ApiCrawlerTest() {
        resourceClasses = new LinkedList<Class>();
        resourceClasses.add(AdminResource.class);
        resourceClasses.add(CertificateResource.class);
        resourceClasses.add(ConsumerResource.class);
        resourceClasses.add(ConsumerTypeResource.class);
        resourceClasses.add(EntitlementResource.class);
        resourceClasses.add(OwnerResource.class);
        resourceClasses.add(PoolResource.class);
        resourceClasses.add(ProductResource.class);
        resourceClasses.add(RulesResource.class);
        resourceClasses.add(StatusResource.class);
        resourceClasses.add(SubscriptionResource.class);
        resourceClasses.add(SubscriptionTokenResource.class);
    }
    
    @Test
    public void testApiCrawler() {
        List<RestApiCall> allApiCalls = new LinkedList<RestApiCall>();
        for (Class c : resourceClasses) {
            allApiCalls.addAll(processClass(c));
        }
        
        // Now print the final results:
        for (RestApiCall call : allApiCalls) {
            call.print();
        }
    }
    
    private List<RestApiCall> processClass(Class c) {
        Path a = (Path) c.getAnnotation(Path.class);
        String parentUrl = a.value();
        List<RestApiCall> classApiCalls = new LinkedList<RestApiCall>();
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers())) {
                classApiCalls.add(processMethod(parentUrl, m));
            }
        }
        return classApiCalls;
    }
    
    private RestApiCall processMethod(String rootPath, Method m) {
        RestApiCall apiCall = new RestApiCall();
        
        Path subPath = (Path) m.getAnnotation(Path.class);
        if (subPath != null) {
            apiCall.setUrl(rootPath + "/" + subPath.value());
        }
        else {
            apiCall.setUrl(rootPath);
        }
        
        processHttpVerb(m, apiCall);
        processAllowedRoles(m, apiCall);
        processQueryParams(m, apiCall);
        
        return apiCall;
    }
    
    private void processHttpVerb(Method m, RestApiCall apiCall) {
        GET get = m.getAnnotation(GET.class);
        if (get != null) {
            apiCall.addHttpVerb("GET");
        }
        PUT put = m.getAnnotation(PUT.class);
        if (put != null) {
            apiCall.addHttpVerb("PUT");
        }
        POST post = m.getAnnotation(POST.class);
        if (post != null) {
            apiCall.addHttpVerb("POST");
        }
        DELETE delete = m.getAnnotation(DELETE.class);
        if (delete != null) {
            apiCall.addHttpVerb("DELETE");
        }
    }

    private void processAllowedRoles(Method m, RestApiCall apiCall) {
        AllowRoles allowRoles = m.getAnnotation(AllowRoles.class);
        if (allowRoles != null) {
            for (Role allow : allowRoles.roles()) {
                apiCall.addRole(allow);
            }
        }
    }
    
    private void processQueryParams(Method m, RestApiCall apiCall) {
        for (int i = 0; i < m.getParameterAnnotations().length; i++) {
            for (Annotation a : m.getParameterAnnotations()[i]) {
                if (a instanceof QueryParam) {
                    apiCall.addQueryParam(((QueryParam) a).value());
                }
            
            }
        }
    }
    
    class RestApiCall {
        private String url;
        private List<Role> allowedRoles;
        private List<String> httpVerbs;
        private List<String> queryParams;
        
        public RestApiCall() {
            allowedRoles = new LinkedList<Role>();
            allowedRoles.add(Role.SUPER_ADMIN); // assumed to always have access
            httpVerbs = new LinkedList<String>();
            queryParams = new LinkedList<String>();
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public void addHttpVerb(String verb) {
            this.httpVerbs.add(verb);
        }
        
        public void addRole(Role role) {
            allowedRoles.add(role);
        }
        
        public void addQueryParam(String param) {
            queryParams.add(param);
        }
        
        public void print() {
            System.out.println(getFormattedHttpVerbs() + " " + this.url);
            System.out.print("  Allowed roles:");
            for (Role allowed : allowedRoles) {
                System.out.print(" " + allowed);
            }
            System.out.print("\n");
            System.out.println("   Query params: " + queryParams);
        }
        
        private String getFormattedHttpVerbs() {
            String verbs = "";
            for (String verb : httpVerbs) {
                verbs = verbs + " " + verb;
            }
            return verbs;
        }
    }
}