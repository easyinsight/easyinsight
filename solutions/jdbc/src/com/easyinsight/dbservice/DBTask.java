package com.easyinsight.dbservice;

import com.easyinsight.dbservice.validated.*;

import java.util.*;
import java.net.URL;
import java.text.MessageFormat;

/**
 * User: James Boe
 * Date: Jan 16, 2009
 * Time: 10:24:35 PM
 */
public class DBTask extends TimerTask {

    private String eiHost = System.getProperty("ei.target", "www.easy-insight.com");
    private IStorage storage;

    public DBTask() {
        String storageMechanism = System.getProperty("ei.storage", "xml");
        if ("database".equals(storageMechanism)) {
            LogClass.info("Using jdbc storage...");
            storage = new DerbyBackedStorage();
        } else {
            LogClass.info("Using xml storage...");
            storage = new XMLBackedStorage();
        }
        run();
    }

    public void run() {
        try {
            List<QueryConfiguration> queryConfigs = null;
            try {
                queryConfigs = storage.getQueryConfigurations();
                EIConfiguration eiConfiguration = storage.getEIConfiguration();
                DBConfiguration dbConfiguration = storage.getDBConfiguration();
                if (eiConfiguration != null && dbConfiguration != null) {
                    URL url = new URL(MessageFormat.format(DBRemote.VALIDATED_ENDPOINT, eiHost));
                    BasicAuthValidatedPublish service = new BasicAuthValidatingPublishServiceServiceLocator().getBasicAuthValidatingPublishServicePort(url);
                    ((BasicAuthValidatingPublishServiceServiceSoapBindingStub)service).setUsername(eiConfiguration.getUserName());
                    ((BasicAuthValidatingPublishServiceServiceSoapBindingStub)service).setPassword(eiConfiguration.getPassword());
                    for (QueryConfiguration queryConfiguration : queryConfigs) {
                        QueryValidatedPublish publish = new QueryValidatedPublish(queryConfiguration, service);
                        LogClass.info("Running " + queryConfiguration.getName());
                        publish.execute(dbConfiguration);
                    }
                }
            } catch (NoDatabaseException e) {
                // ignore
            }
        } catch (Exception e) {
            LogClass.error(e);
        }
    }

    
}
