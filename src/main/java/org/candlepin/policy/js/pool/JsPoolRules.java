/**
 * Copyright (c) 2009 - 2012 Red Hat, Inc.
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
package org.candlepin.policy.js.pool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.candlepin.config.Config;
import org.candlepin.controller.PoolManager;
import org.candlepin.exceptions.IseException;
import org.candlepin.model.Pool;
import org.candlepin.model.Subscription;
import org.candlepin.policy.PoolRules;
import org.candlepin.policy.js.JsRules;
import org.candlepin.policy.js.ProductCache;

import com.google.inject.Inject;

/**
 *
 */
public class JsPoolRules implements PoolRules {

    private static Logger log = Logger.getLogger(JsPoolRules.class);
    protected Logger rulesLogger = null;

    private JsRules jsRules;
    private PoolManager poolManager;
    private ProductCache productCache;
    private Config config;


    @Inject
    public JsPoolRules(JsRules jsRules, PoolManager poolManager,
        ProductCache productCache, Config config) {
        this.jsRules = jsRules;
        this.poolManager = poolManager;
        this.productCache = productCache;
        this.config = config;
        this.rulesLogger = Logger.getLogger(
            JsPoolRules.class.getCanonicalName() + ".rules");
        jsRules.init("pool_name_space");
    }

    @Override
    public List<Pool> createPools(Subscription sub) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("sub", sub);
        args.put("attributes", jsRules.getFlattenedAttributes(sub.getProduct()));
        args.put("helper", new PoolHelper(this.poolManager,
            this.productCache, null));
        args.put("standalone", config.standalone());
        args.put("log", rulesLogger);
        List<Pool> poolsCreated = null;
        try {
            poolsCreated = jsRules.invokeMethod("createPools", args);
        }
        catch (NoSuchMethodException e) {
            log.error("Unable to find javascript method: createPools");
            log.error(e);
            throw new IseException("Unable to create pools.");
        }
        return poolsCreated;
    }

    @Override
    public List<PoolUpdate> updatePools(Subscription sub, List<Pool> existingPools) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("sub", sub);
        args.put("pools", existingPools);
        args.put("attributes", jsRules.getFlattenedAttributes(sub.getProduct()));
        args.put("log", log);
        args.put("helper", new PoolHelper(this.poolManager, this.productCache, null));
        args.put("standalone", config.standalone());
        List<PoolUpdate> poolsUpdated = null;
        try {
            poolsUpdated = jsRules.invokeMethod("updatePools", args);
        }
        catch (NoSuchMethodException e) {
            log.error("Unable to find javascript method: updatePools");
            log.error(e);
            throw new IseException("Unable to update pools.");
        }
        return poolsUpdated;
    }

}
