/**
 * Created with IntelliJ IDEA.
 * User: jamesboe
 * Date: 1/17/14
 * Time: 10:33 AM
 * To change this template use File | Settings | File Templates.
 */
package com.easyinsight.analysis.summary {
import com.easyinsight.analysis.AnalysisDefinition;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.DrillThrough;
import com.easyinsight.analysis.DrillThroughEvent;
import com.easyinsight.analysis.DrillThroughExecutor;
import com.easyinsight.analysis.Link;
import com.easyinsight.analysis.ReportWindowEvent;
import com.easyinsight.analysis.TextReportFieldExtension;
import com.easyinsight.analysis.TextValueExtension;
import com.easyinsight.analysis.TreeRow;
import com.easyinsight.analysis.URLLink;
import com.easyinsight.analysis.Value;
import com.easyinsight.pseudocontext.StandardContextWindow;
import com.easyinsight.report.ReportNavigationEvent;
import com.easyinsight.solutions.InsightDescriptor;

import flash.events.Event;

import flash.events.MouseEvent;
import flash.net.URLRequest;
import flash.net.navigateToURL;

import mx.collections.ArrayCollection;

import mx.controls.Alert;

import mx.controls.listClasses.IListItemRenderer;
import mx.core.UITextField;
import mx.core.UITextFormat;
import mx.formatters.Formatter;

public class NewSummaryCellRenderer extends UITextField implements IListItemRenderer {
    private var _data:Object;
    private var _analysisItem:AnalysisItem;
    private var _selectionEnabled:Boolean;
    private var _report:AnalysisDefinition;

    private var hyperlinked:Boolean;

    public function NewSummaryCellRenderer() {
        super();
        this.percentWidth = 100;
    }

    public function set report(value:AnalysisDefinition):void {
        _report = value;
    }

    private function onClick(event:MouseEvent):void {
        if (defaultLink != null) {
            if (defaultLink is URLLink) {
                var urlLink:URLLink = defaultLink as URLLink;
                var url:String = data[urlLink.label + "_link"];
                try {
                    navigateToURL(new URLRequest(url), "_blank");
                } catch (e:Error) {
                    Alert.show(e.message);
                }
            } else if (defaultLink is DrillThrough) {
                var drillThrough:DrillThrough = defaultLink as DrillThrough;
                var values:ArrayCollection = null;
                var executor:DrillThroughExecutor = new DrillThroughExecutor(drillThrough, data, analysisItem, _report, null, values);
                executor.addEventListener(DrillThroughEvent.DRILL_THROUGH, onDrill);
                executor.send();
            }
        }
    }

    private function onDrill(event:DrillThroughEvent):void {
        if (event.drillThrough.miniWindow) {
            dispatchEvent(new ReportWindowEvent(event.drillThroughResponse.descriptor.id, 0, 0, event.drillThroughResponse.filters, InsightDescriptor(event.drillThroughResponse.descriptor).dataFeedID,
                    InsightDescriptor(event.drillThroughResponse.descriptor).reportType));
        } else {
            dispatchEvent(new ReportNavigationEvent(ReportNavigationEvent.TO_REPORT, event.drillThroughResponse.descriptor, event.drillThroughResponse.filters));
        }
    }

    private function onRollOver(event:MouseEvent):void {
        if (hyperlinked) {
            if (utf != null) {
                setTextFormat(hyperlinkedUTF);
                invalidateProperties();
            }
        }
    }

    private var hyperlinkedUTF:UITextFormat;
    private var utf:UITextFormat;

    private function onRollOut(event:MouseEvent):void {
        if (hyperlinked) {
            if (utf != null) {

                setTextFormat(utf);
                invalidateProperties();
            }
        }
    }

    private function passThrough(event:Event):void {
        dispatchEvent(event);
    }

    public function get analysisItem():AnalysisItem {
        return _analysisItem;
    }

    private var defaultLink:Link;

    public function set analysisItem(val:AnalysisItem):void {
        _analysisItem = val;
        if (_analysisItem != null) {
            toolTip = _analysisItem.tooltip;
            if (_analysisItem.links != null) {
                for each (var link:Link in _analysisItem.links) {
                    if (link.defaultLink) {
                        defaultLink = link;
                        break;
                    }
                }
            }
        }
    }

    public function validateProperties():void {
        validateNow();
    }

    public function validateSize(recursive:Boolean = false):void {
        validateNow();
    }

    public function validateDisplayList():void {
        validateNow();
    }

    override public function validateNow():void {

        if (data && parent) {
            if (_changed) {
                _changed = false;
                setText(_valText);
                setTextFormat(_format);
            }
        }
        super.validateNow();

    }

    public function set data(value:Object):void {
        _data = value;
        var treeRow:NewSummaryRow = value as NewSummaryRow;
        var color:uint = treeRow.textColor;
        var bold:Object = null;
        var backgroundColor:uint = 0xFFFFFF;
        var text:String;
        if (value != null) {
            var field:String = analysisItem.qualifiedName();
            var formatter:Formatter = analysisItem.getFormatter();
            if (treeRow.values[field] is Value) {
                var objVal:Value = treeRow.values[field];
                if (objVal == null) {
                    text = "";
                } else {
                    text = formatter.format(objVal.getValue());
                }
                if (objVal.valueExtension != null) {
                    var ext:TextValueExtension = objVal.valueExtension as TextValueExtension;
                    color = ext.color;
                    if (ext.bold) {
                        bold = true;
                    }
                    backgroundColor = ext.backgroundColor;
                }
                if (defaultLink != null && objVal != null && objVal.type() != Value.EMPTY) {
                    hyperlinked = true;
                }
            } else {
                hyperlinked = false;
                if (treeRow.values[field] != null) {
                    text = formatter.format(treeRow.values[field]);
                } else {
                    text = "";
                }

            }
        } else {
            text = "";
        }

        _valText = text;

        var rext:TextReportFieldExtension = analysisItem.reportFieldExtension as TextReportFieldExtension;
        var align:String = "left";
        if (rext != null && rext.align != null && rext.align != "Default") {
            align = rext.align.toLowerCase();
        }
        if (_report.getFont() == "Open Sans" && !bold) {
            styleName = "myFontStyle";
        } else if (_report.getFont() == "Open Sans" && bold) {
            styleName = "boldStyle";
        }
        if (hyperlinked && !hasLinks) {
            hasLinks = true;
            addEventListener(MouseEvent.ROLL_OVER, onRollOver);
            addEventListener(MouseEvent.ROLL_OUT, onRollOut);
            addEventListener(MouseEvent.CLICK, onClick);
        } else if (!hyperlinked && hasLinks) {
            hasLinks = false;
            removeEventListener(MouseEvent.ROLL_OVER, onRollOver);
            removeEventListener(MouseEvent.ROLL_OUT, onRollOut);
            removeEventListener(MouseEvent.CLICK, onClick);
        }
        utf = new UITextFormat(this.systemManager, _report.getFont(), _report.fontSize, color, bold, null, false);
        utf.align = align;
        if (hyperlinked) {
            hyperlinkedUTF = new UITextFormat(this.systemManager, _report.getFont(), _report.fontSize, color, bold, null, true);
            hyperlinkedUTF.align = align;
        }
        _format = utf;
        _changed = true;
        if (backgroundColor != 0xFFFFFF) {
            this.backgroundColor = backgroundColor;
            this.background = true;
        }
        new StandardContextWindow(analysisItem, passThrough, this, value, _report);
        invalidateProperties();
        invalidateSize();
    }

    private var _changed:Boolean;
    private var _valText:String;
    private var _format:UITextFormat;

    private var hasLinks:Boolean;

    protected function setText(text:String):void {
        this.text = text;
    }

    public function get data():Object {
        return _data;
    }
}
}
