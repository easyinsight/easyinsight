package com.easyinsight.analysis
{
import flash.events.EventDispatcher;

import mx.collections.ArrayCollection;
import mx.events.FlexEvent;

public class AnalysisItemWrapper extends EventDispatcher
	{
		private var _feedNode:FeedNode;
		private var _displayName:String;
        private var _children:ArrayCollection;
		
		public function AnalysisItemWrapper(feedNode:FeedNode)	{
			this._feedNode = feedNode;
			this._displayName = feedNode.display;
            _children = new ArrayCollection();
            for each (var child:FeedNode in feedNode.children) {
                _children.addItem(new AnalysisItemWrapper(child));
            }
		}


    public function get children():ArrayCollection {
        return _children;
    }

    public function get analysisItem():AnalysisItem {
        if (isAnalysisItem()) {
            var node:AnalysisItemNode = _feedNode as AnalysisItemNode;
            return node.analysisItem;
        }
        return null;
    }

    public function invalidateItem():void {
        dispatchEvent(new ItemWrapperInvalidationEvent());
    }

    public function set analysisItem(item:AnalysisItem):void {
        if (isAnalysisItem()) {
            var node:AnalysisItemNode = _feedNode as AnalysisItemNode;
            node.analysisItem = item;
        }
    }

    public function isAnalysisItem():Boolean {
        return _feedNode is AnalysisItemNode;
    }

    public function get hidden():Boolean {
        if (isAnalysisItem()) {
            var node:AnalysisItemNode = _feedNode as AnalysisItemNode;
            return node.analysisItem.hidden;
        }
        return false;
    }

        public function get keyName():String {
            if (_feedNode is AnalysisItemNode) {
                var analysisItemNode:AnalysisItemNode = _feedNode as AnalysisItemNode;
                return analysisItemNode.analysisItem.key.createString();
            }
            return null;
        }
		
		[Bindable]
		public function set displayName(displayName:String):void {
			this._displayName = displayName;
            dispatchEvent(new FlexEvent(FlexEvent.DATA_CHANGE));
		}

		public function get displayName():String {
			return this._displayName;
		}


    public function get feedNode():FeedNode {
        return _feedNode;
    }

    public function set feedNode(value:FeedNode):void {
        _feedNode = value;
    }
}
}