package com.easyinsight.analysis.list
{
import com.easyinsight.analysis.*;
import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.easyinsight.analysis.WSListDefinition")]
	public class ListDefinition extends AnalysisDefinition
	{
		public var columns:ArrayCollection;
		public var listDefinitionID:int;
		public var showLineNumbers:Boolean;
		public var listLimitsMetadata:ListLimitsMetadata;
        public var summaryTotal:Boolean;
        public var textColor:uint = 0x000000;
        public var headerTextColor:uint = 0x000000;
        public var rowColor1:uint = 0xF7F7F7;
        public var rowColor2:uint = 0xFFFFFF;
        public var headerColor1:uint = 0xFFFFFF;
        public var headerColor2:uint = 0xEFEFEF;
        public var summaryRowBackgroundColor:uint = 0x6699ff;
        public var summaryRowTextColor:uint = 0x000000;
        public var rolloverIcon:Boolean;
        public var horizontalGridLines:Boolean = false;
        public var verticalGridLines:Boolean = false;
        public var borderStyle:String = "none";
        public var multiLineHeaders:Boolean = false;

		public function ListDefinition()
		{
		}

        override public function getFont():String {
            if (customFontFamily != null && customFontFamily != "" && useCustomFontFamily) {
                return customFontFamily;
            }
            if (fontName == "Lucida Grande" || fontName == "Open Sans") {
                return fontName;
            } else {
                return "Lucida Grande";
            }
        }

        override public function supportsEmbeddedFonts():Boolean {
            return true;
        }
		
		override public function getFields():ArrayCollection {
			return columns;
		}

        override public function populate(fields:ArrayCollection):void {
            columns = new ArrayCollection();
            for each (var field:AnalysisItem in fields) {
                if (field != null) {
                    columns.addItem(field);
                }
            }            
        }

        override public function createDefaultLimits():void {
            if (this.listLimitsMetadata == null) {
                if (columns != null && columns.length > 0) {
                    var limitsMetadata:ListLimitsMetadata = new ListLimitsMetadata();
                    limitsMetadata.number = 1000;
                    limitsMetadata.top = true;
                    this.listLimitsMetadata = limitsMetadata;
                }
            }
        }


        override public function get type():int {
            return AnalysisDefinition.LIST;
        }

        override public function fromSave(savedDef:AnalysisDefinition):void {
            super.fromSave(savedDef);
            this.listDefinitionID = ListDefinition(savedDef).listDefinitionID;
        }
    }
}