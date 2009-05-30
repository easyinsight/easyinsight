package com.easyinsight.framework {
import com.easyinsight.notifications.RefreshEventInfo;

import flash.events.EventDispatcher;

import mx.controls.Alert;
import mx.messaging.Consumer;
import mx.messaging.events.MessageEvent;
import mx.messaging.events.MessageFaultEvent;

public class EIMessageListener extends EventDispatcher {

    private var consumer:Consumer;

    public function EIMessageListener() {
        consumer = new Consumer();
        consumer.destination = "generalNotifications";
        consumer.addEventListener(MessageEvent.MESSAGE, handleMessage);
        consumer.addEventListener(MessageFaultEvent.FAULT, handleFault);
        User.getEventNotifier().addEventListener(LoginEvent.LOGIN, onLogin);
        User.getEventNotifier().addEventListener(LoginEvent.LOGOUT, onLogout);
    }

    private function handleFault(event:mx.messaging.events.MessageFaultEvent):void {
        Alert.show(event.message.faultString);
    }

    private function onLogin(event:LoginEvent):void {
        consumer.subscribe();
    }

    private function onLogout(event:LoginEvent):void {
        consumer.unsubscribe();
    }

    private function handleMessage(event:mx.messaging.events.MessageEvent):void {
        var info:RefreshEventInfo = event.message.body as RefreshEventInfo;
        dispatchEvent(new AsyncInfoEvent(info));
    }
}
}