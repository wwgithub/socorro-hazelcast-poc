/*
 * Copyright (c) 2008-2010, Hazel Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hazelcast.socorro.persistence;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;
import com.hazelcast.impl.ThreadContext;
import com.hazelcast.socorro.CrashReport;
import com.sleepycat.je.*;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class BerkeleyMapStore implements MapStore<Long, CrashReport>, MapLoaderLifecycleSupport {

    volatile Database db = null;
    final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);
        Environment exampleEnv = new Environment(new File("/dev/shm"), envConfig);
        Transaction txn = exampleEnv.beginTransaction(null, null);
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(true);
        dbConfig.setAllowCreate(true);
        dbConfig.setSortedDuplicates(true);
        db = exampleEnv.openDatabase(txn,
                "/" + mapName,
                dbConfig);
        txn.commit();
    }

    @Override
    public void destroy() {
        db.close();
    }

    @Override
    public void store(Long key, CrashReport value) {
        ThreadContext tc = ThreadContext.get();
        db.put(null, new DatabaseEntry(tc.toByteArray(key)), new DatabaseEntry(tc.toByteArray(value)));
    }

    @Override
    public void storeAll(Map<Long, CrashReport> crashReportMap) {
        logger.info(Thread.currentThread().getId() + ": Storing " + crashReportMap.size() + " entries ");
        long current = System.currentTimeMillis();
        ThreadContext tc = ThreadContext.get();
        for (Map.Entry<Long, CrashReport> entry : crashReportMap.entrySet()) {
            OperationStatus os = db.put(null, new DatabaseEntry(tc.toByteArray(entry.getKey())), new DatabaseEntry(tc.toByteArray(entry.getValue())));
            if(os != OperationStatus.SUCCESS) {
                throw new RuntimeException("No Success");


            }
        }
        logger.info(Thread.currentThread().getId() + ": Stored " + crashReportMap.size() + " entries in " + (System.currentTimeMillis() - current) + " ms");
    }

    @Override
    public void delete(Long key) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteAll(Collection<Long> keys) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CrashReport load(Long key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<Long, CrashReport> loadAll(Collection<Long> keys) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
