package com.easyinsight.framework {
import com.easyinsight.analysis.CredentialFulfillment;

import flash.display.DisplayObject;

import flash.events.EventDispatcher;

import mx.collections.ArrayCollection;

public class WebCredentialsCache extends EventDispatcher implements ICredentialsCache {
    public function WebCredentialsCache() {
    }

    private var cache:Object = new Object();

    public function addCredentials(dataSourceID:int, credentials:Credentials):void {
        cache[String(dataSourceID)] = credentials;
    }

    public function getCredentials(dataSourceID:int):Credentials {
        return cache[String(dataSourceID)];
    }

    public function createCredentials():ArrayCollection {
        var creds:ArrayCollection = new ArrayCollection();
        for (var dataSourceID:String in cache) {
            var credentialFulfillment:CredentialFulfillment = new CredentialFulfillment();
            credentialFulfillment.dataSourceID = int(dataSourceID);
            credentialFulfillment.credentials = cache[dataSourceID];
            creds.addItem(credentialFulfillment);
        }
        return creds;
    }

    public function obtainCredentials(displayObject:DisplayObject, credentials:ArrayCollection, successFunction:Function,
            ... callbackParams):void {
        var credentialRequirementState:CredentialRequirementState = new CredentialRequirementState(displayObject, credentials,
                successFunction, callbackParams);
        credentialRequirementState.act();
    }    
}
}