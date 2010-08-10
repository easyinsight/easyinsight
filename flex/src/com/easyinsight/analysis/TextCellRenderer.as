package com.easyinsight.analysis
{
	import com.easyinsight.analysis.conditions.ConditionRenderer;



import com.easyinsight.pseudocontext.PseudoContextWindow;
import com.easyinsight.pseudocontext.StandardContextWindow;

import flash.events.Event;
import flash.events.MouseEvent;

import mx.controls.Text;
import mx.events.FlexEvent;
    import mx.formatters.Formatter;
import mx.managers.PopUpManager;


public class TextCellRenderer extends Text
	{
		private var _data:Object;
		private var _analysisItem:AnalysisText;
		private var _renderer:ConditionRenderer;

		public function TextCellRenderer() {
			super();
            addEventListener(MouseEvent.CLICK, onClick);
		}


        private function onClick(event:MouseEvent):void {
            if (event.shiftKey) {
                var window:PseudoContextWindow = new PseudoContextWindow(_analysisItem, passThrough, this);
                window.data = this.data;
                PopUpManager.addPopUp(window, this);
                window.x = event.stageX + 5;
                window.y = event.stageY + 5;
            }
        }

        private function passThrough(event:Event):void {
            dispatchEvent(event);
        }

        public function get analysisItem():AnalysisText {
            return _analysisItem;
        }

        public function set analysisItem(val:AnalysisText):void {
            _analysisItem = val;
        }

        public function get renderer():ConditionRenderer {
            return _renderer;
        }

        public function set renderer(val:ConditionRenderer):void {
            _renderer = val;
        }

		override public function set data(value:Object):void {
			_data = value;
            var text:String;
			if (value != null) {
                var field:String = analysisItem.qualifiedName();
                var formatter:Formatter = analysisItem.getFormatter();
                if (value[field] is Value) {
                    var objVal:Value = value[field];
                    if (objVal == null) {
                        text = "";
                    } else {
                        text = formatter.format(objVal.getValue());
                    }
                } else {
                    if (value[field] != null) {
                        text = formatter.format(value[field]);
                    } else {
                        text = "";
                    }

                }
			} else {
				text = "";
			}
            if (analysisItem.html) {
                this.htmlText = text;
            } else {
                this.text = text;
            }
            new StandardContextWindow(analysisItem, passThrough, this, value);
            invalidateProperties();
            dispatchEvent(new FlexEvent(FlexEvent.DATA_CHANGE));
		}

        override public function get data():Object {
            return _data;
        }
	}
}