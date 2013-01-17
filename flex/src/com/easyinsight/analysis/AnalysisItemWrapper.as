package com.easyinsight.analysis
{
import flash.events.Event;
import flash.events.EventDispatcher;

import mx.collections.ArrayCollection;
import mx.collections.Sort;
import mx.collections.SortField;

public class AnalysisItemWrapper extends EventDispatcher
	{
		private var _feedNode:FeedNode;
		private var _displayName:String;
        private var _children:ArrayCollection;
		
		public function AnalysisItemWrapper(feedNode:FeedNode)	{
			this._feedNode = feedNode;
			displayName = feedNode.display;
            _children = new ArrayCollection();
            for each (var child:FeedNode in feedNode.children) {
                _children.addItem(new AnalysisItemWrapper(child));
            }
            if (_children.length > 0) {
                var sort:Sort = new Sort();
                sort.fields = [ new SortField("sortType"), new SortField("displayName") ];
                _children.sort = sort;
                _children.refresh();
            }
            //_children.sort = null;
		}

    public function get sortType():int {
        return isAnalysisItem() ? 1 : 0;
    }

    public function get fieldType():String {
        if (analysisItem.getType() == AnalysisItemTypes.DATE) {
            return "Date;"
        } else if (analysisItem.getType() == AnalysisItemTypes.MEASURE) {
            return "Measure";
        } else {
            return "Grouping";
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


    [Bindable(event="displayNameChanged")]
    public function get displayName():String {
        return _displayName;
    }

    public function set displayName(value:String):void {
        if (_displayName == value) return;
        _displayName = value;
        dispatchEvent(new Event("displayNameChanged"));
    }

    public function get feedNode():FeedNode {
        return _feedNode;
    }

    public function set feedNode(value:FeedNode):void {
        _feedNode = value;
    }
}
}