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

import org.fedoraproject.candlepin.model.Consumer;
import org.fedoraproject.candlepin.model.Entitlement;
import org.fedoraproject.candlepin.model.EntitlementCertificate;
import org.fedoraproject.candlepin.model.Owner;
import org.fedoraproject.candlepin.model.Pool;

/**
 * AccessControlValidator
 */
public class AccessControlValidator {
    
    private AccessControlValidator() {
    }
    
    public static boolean shouldGrantAccess(Pool accessed, Consumer consumer) {
        return accessed.getOwner().getId().equals(consumer.getOwner().getId());
    }
    
    public static boolean shouldGrantAccess(Pool accessed, Owner owner) {
        return accessed.getOwner().getKey().equals(owner.getKey());
    }

    public static boolean shouldGrantAccess(Consumer accessed, Consumer consumer) {
        return accessed.getUuid().equals(consumer.getUuid());
    }
    
    public static boolean shouldGrantAccess(Consumer accessed, Owner owner) {
        return accessed.getOwner().getKey().equals(owner.getKey());
    }
    
    public static boolean shouldGrantAccess(EntitlementCertificate c, Consumer consumer) {
        return consumer.getUuid().equals(c.getEntitlement().getConsumer().getUuid());
    }
    
    public static boolean shouldGrantAccess(EntitlementCertificate c, Owner owner) {
        return owner.getKey().equals(c.getEntitlement().getOwner().getKey());
    }
    
    public static boolean shouldGrantAccess(Entitlement e, Consumer consumer) {
        return consumer.getUuid().equals(e.getConsumer().getUuid());
    }
    
    public static boolean shouldGrantAccess(Entitlement e, Owner owner) {
        return owner.getKey().equals(e.getOwner().getKey());
    }
}