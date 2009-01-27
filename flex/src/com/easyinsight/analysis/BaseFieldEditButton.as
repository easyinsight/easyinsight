package com.easyinsight.analysis
{
	import flash.events.MouseEvent;
	import flash.geom.Point;
	
	import mx.collections.ArrayCollection;
	import mx.containers.HBox;
	import mx.controls.Button;
	import mx.managers.PopUpManager;

    import com.easyinsight.administration.feed.DeleteAnalysisItemEvent;

	public class BaseFieldEditButton extends HBox
	{
		private var analysisItemWrapper:AnalysisItemWrapper;
		private var _displayName:String;
		private var _analysisItems:ArrayCollection;
		private var button:Button;
		private var deleteButton:Button;

		[Bindable]
		[Embed(source="../../../../assets/pencil.png")]
        public var editIcon:Class;

        [Bindable]
		[Embed(source="../../../../assets/navigate_cross.png")]
        public var deleteIcon:Class;
        
		public function BaseFieldEditButton()
		{
			super();	
			setStyle("horizontalAlign", "center");
			this.percentWidth = 100;		
		}
		
		override protected function createChildren():void {
			if (button == null) {
				button = new Button();
				button.setStyle("icon", editIcon);
				button.toolTip = "Edit...";
				button.addEventListener(MouseEvent.CLICK, editItem);
			}
            addChild(button);
            if (deleteButton == null) {
                deleteButton = new Button();
                deleteButton.setStyle("icon", deleteIcon);
                deleteButton.toolTip = "Delete Field";
                deleteButton.addEventListener(MouseEvent.CLICK, deleteItem);
            }
            addChild(deleteButton);
		}
		
		[Bindable]
		public function get displayName():String {
			return _displayName;
		}
		
		public function set displayName(displayName:String):void {
			this._displayName = displayName;
		}

        private function deleteItem(event:MouseEvent):void {
            dispatchEvent(new DeleteAnalysisItemEvent(analysisItemWrapper));
        }
		
		private function editItem(event:MouseEvent):void {
			var analysisItemEditor:AnalysisItemEditWindow = new AnalysisItemEditWindow();
			analysisItemEditor.editorClass = AnalysisItemEditor; 			
			analysisItemEditor.analysisItem = analysisItemWrapper.analysisItem;
			analysisItemEditor.analysisItems = this._analysisItems;
			analysisItemEditor.addEventListener(AnalysisItemEditEvent.ANALYSIS_ITEM_EDIT, analysisItemEdited);
			PopUpManager.addPopUp(analysisItemEditor, this.parent, true);
			var point:Point = new Point();
			point.x = 0;
			point.y = 0;
			point = this.localToGlobal(point);
			analysisItemEditor.x = point.x + 25;
			analysisItemEditor.y = point.y + 25;
		}
		
		private function analysisItemEdited(event:AnalysisItemEditEvent):void {
			this.analysisItemWrapper.analysisItem = event.analysisItem;
			this.analysisItemWrapper.displayName = event.analysisItem.displayName;
			this.displayName = event.analysisItem.displayName;			 
		}
		
		[Bindable]
		override public function set data(value:Object):void {
			this.analysisItemWrapper = value as AnalysisItemWrapper;
			this.displayName = analysisItemWrapper.displayName;			
		}
		
		override public function get data():Object {
			return this.analysisItemWrapper;
		}
	}
}