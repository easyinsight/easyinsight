package com.easyinsight.groups
{
	import com.easyinsight.administration.feed.UserLinkEvent;
	
	import flash.events.MouseEvent;
	
	import mx.containers.HBox;
	import mx.controls.Button;

	public class GroupUserIcons extends HBox
	{
		private var userStub:GroupUser;
		private var button:Button;
		
		[Bindable]
        [Embed(source="../../../../assets/navigate_cross.png")]
        public var deleteIcon:Class;
		
		public function GroupUserIcons()
		{
			super();
			setStyle("horizontalAlign", "center");
			this.percentWidth = 100;			
		}
		
		override protected function createChildren():void {
			if (button == null) {
				button = new Button();
				button.toolTip = "Delete";
				button.setStyle("icon", deleteIcon);
				button.addEventListener(MouseEvent.CLICK, deleteUser);
			}
			addChild(button);
		}
		
		private function deleteUser(event:MouseEvent):void {
			parent.dispatchEvent(new UserLinkEvent(UserLinkEvent.DELETE_USER_LINK, userStub));
		}
		
		override public function set data(object:Object):void {
			this.userStub = object as GroupUser;
		}
		
		override public function get data():Object {
			return this.userStub;
		}
		
	}
}