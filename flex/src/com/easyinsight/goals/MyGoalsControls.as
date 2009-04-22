package com.easyinsight.goals
{
    import mx.events.FlexEvent;
    import mx.binding.utils.BindingUtils;
	import com.easyinsight.genredata.AnalyzeEvent;
	
	import flash.events.MouseEvent;
	
	import mx.containers.HBox;
	import mx.controls.Button;

	public class MyGoalsControls extends HBox
	{
		[Embed(source="../../../../assets/navigate_cross.png")]
        public var deleteIcon:Class;
                
        [Embed(source="../../../../assets/branch_edit.png")]
        public var adminIcon:Class;
        
        [Embed(source="../../../../assets/media_play_green.png")]
        public var playIcon:Class;
        
        private var goalTree:GoalTreeDescriptor;

        private var _admin:Boolean = false;

		public function MyGoalsControls()
		{
			super();
            this.percentWidth = 100;
            setStyle("horizontalAlign", "center");
			var viewButton:Button = new Button();
			viewButton.setStyle("icon", playIcon);
			viewButton.toolTip = "View";
			viewButton.addEventListener(MouseEvent.CLICK, viewGoalTree);
			addChild(viewButton);
			var adminButton:Button = new Button();
			adminButton.setStyle("icon", adminIcon);
			adminButton.toolTip = "Administer";
			adminButton.addEventListener(MouseEvent.CLICK, adminGoalTree);
            BindingUtils.bindProperty(adminButton, "visible", this, "admin");
			addChild(adminButton);
			var deleteButton:Button = new Button();
			deleteButton.setStyle("icon", deleteIcon);
			deleteButton.toolTip = "Delete";
			deleteButton.addEventListener(MouseEvent.CLICK, deleteGoalTree);
            BindingUtils.bindProperty(deleteButton, "visible", this, "admin");
			addChild(deleteButton);			
		}

        [Bindable]        
        public function get admin():Boolean {
            return _admin;
        }
        public function set admin(val:Boolean):void {
            _admin = val;
            dispatchEvent(new FlexEvent(FlexEvent.DATA_CHANGE));
        }

        override public function set data(value:Object):void {
			this.goalTree = value as GoalTreeDescriptor;
            if (this.goalTree != null) {
                this.admin = this.goalTree.role == 1;
            } else {
                this.admin = false;
            }
		}
		
		override public function get data():Object {
			return this.goalTree;
		}
		
		private function viewGoalTree(event:MouseEvent):void {
            dispatchEvent(new AnalyzeEvent(new GoalDataAnalyzeSource(goalTree.id)));
		}
		
		private function adminGoalTree(event:MouseEvent):void {
            dispatchEvent(new AnalyzeEvent(new GoalTreeAdminAnalyzeSource(goalTree.id)));
		}
		
		private function deleteGoalTree(event:MouseEvent):void {
            dispatchEvent(new DeleteGoalTreeEvent(goalTree));
		}
	}
}