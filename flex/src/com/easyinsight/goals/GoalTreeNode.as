package com.easyinsight.goals
{
import com.easyinsight.filtering.FilterDefinition;
import com.easyinsight.analysis.AnalysisMeasure;
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.easyinsight.goals.GoalTreeNode")]
	public class GoalTreeNode
	{
        public var goalTreeNodeID:int;
		public var children:ArrayCollection;
		public var parent:GoalTreeNode;
		public var coreFeedID:int;
		public var coreFeedName:String;
        public var analysisMeasure:AnalysisMeasure;
        public var goalValue:Number;
        public var filterDefinition:FilterDefinition;
        public var highIsGood:Boolean;
		public var associatedFeeds:ArrayCollection = new ArrayCollection();
		public var associatedInsights:ArrayCollection = new ArrayCollection();
		public var associatedSolutions:ArrayCollection = new ArrayCollection();
		public var tags:ArrayCollection = new ArrayCollection();
		public var name:String;
		public var description:String;
        public var iconImage:String;
        public var newSolutions:ArrayCollection = new ArrayCollection();
		
		public function GoalTreeNode()
		{
		}

        public function clone():GoalTreeNode {
            var clonedNode:GoalTreeNode = new GoalTreeNode();
            // recurse children?
            clonedNode.analysisMeasure = this.analysisMeasure;
            clonedNode.coreFeedID = this.coreFeedID;
            clonedNode.coreFeedName = this.coreFeedName;
            clonedNode.goalValue = this.goalValue;
            clonedNode.filterDefinition = this.filterDefinition;
            clonedNode.highIsGood = this.highIsGood;
            clonedNode.name = this.name;
            clonedNode.description = this.description;
            clonedNode.iconImage = this.iconImage;
            var newChildren:ArrayCollection = new ArrayCollection();
            for each (var childNode:GoalTreeNode in children) {
                var clonedChild:GoalTreeNode = childNode.clone();
                clonedChild.parent = clonedNode;
                newChildren.addItem(clonedChild);
            }
            this.children = newChildren;
            return clonedNode;
        }
	}
}